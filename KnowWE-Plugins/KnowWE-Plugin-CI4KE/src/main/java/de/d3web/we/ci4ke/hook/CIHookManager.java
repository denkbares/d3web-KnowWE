/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.ci4ke.hook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.strings.Strings;
import de.d3web.we.ci4ke.build.CIBuildManager;
import de.d3web.we.ci4ke.build.CIDashboardPriorityOverrideEvent;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.event.UserDefaultDashboardUpdateEvent;
import de.knowwe.core.compile.CompilationFinishedEvent;
import de.knowwe.core.compile.CompilerFinishedEvent;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.event.ArticleUpdateEvent;
import de.knowwe.event.FullParseEvent;

/**
 * @author Marc-Oliver Ochlast, Albrecht Striffler
 */
public class CIHookManager implements EventListener {

	public static final String KNOWWE_CI_HOOKS_ACTIVE = "knowwe.ci.hooks.active";

	private static final Logger LOGGER = LoggerFactory.getLogger(CIHookManager.class);

	private static CIHookManager instance = null;

	private volatile String lastTriggeringUser = "";

	private final Map<String, Set<String>> userToDefaultDashboardNames = new ConcurrentHashMap<>();

	/**
	 * Fore each monitored articles a list of hooks are stored.
	 */
	private final MultiMap<String, CIHook> hooks = new DefaultMultiMap<>();

	public static CIHookManager getInstance() {
		if (instance == null) {
			instance = new CIHookManager();
		}
		return instance;
	}

	private CIHookManager() {
		// prevent instantiation
		EventManager.getInstance().registerListener(this);
	}

	public synchronized void registerHook(CIHook hook) {
		for (String monitoredArticle : hook.getMonitoredArticles()) {
			hooks.put(monitoredArticle, hook);
		}
	}

	public synchronized void unregisterHook(CIHook hook) {
		hooks.removeValue(hook);
	}

	public synchronized void triggerHooks(Article monitoredArticle) {
		triggerHooks(List.of(monitoredArticle));
	}

	/**
	 * Triggers the registered hooks for a given Article.
	 *
	 * @param monitoredArticles the article to trigger hooks for
	 */
	public synchronized void triggerHooks(Collection<Article> monitoredArticles) {
		if (!Boolean.parseBoolean(System.getProperty(KNOWWE_CI_HOOKS_ACTIVE, "true"))) return;
		Set<CIDashboard> dashboardsToTrigger = new HashSet<>();
		for (Article monitoredArticle : monitoredArticles) {
			Set<CIHook> hookSet = hooks.getValues(monitoredArticle.getTitle());
			for (final CIHook hook : hookSet) {
				int compilationId = getCurrentCompilationId(hook);
				// avoid triggering the same hook multiple times for the same compilation
				// this can happen, if the regular expression matches multiple articles
				if (hook.getLastTrigger() == compilationId) continue;
				hook.setLastTrigger(compilationId);
				dashboardsToTrigger.add(hook.getDashboard());
				handlePriorityOverride(hook);
			}
		}
		CIBuildManager.getInstance().startBuilds(dashboardsToTrigger);
		List<String> triggered = dashboardsToTrigger.stream().map(CIDashboard::getDashboardName).sorted().toList();
		if (!triggered.isEmpty()) LOGGER.info("Triggered the following dashboards: " + String.join(", ", triggered));
	}

	private void handlePriorityOverride(CIHook hook) {
		String userName = lastTriggeringUser;
		if (Strings.isNotBlank(userName)) {
			Set<String> defaultDashboardsOfTriggeringUser = userToDefaultDashboardNames.getOrDefault(userName, Set.of());
			if (defaultDashboardsOfTriggeringUser.contains(hook.getDashboard().getDashboardName())) {
				EventManager.getInstance()
						.fireEvent(new CIDashboardPriorityOverrideEvent(hook.getDashboard(), Double.MAX_VALUE));
				System.out.println("Override prio of " + hook.getDashboard().getDashboardName());
			}
		}
	}

	private int getCurrentCompilationId(CIHook hook) {
		//noinspection ConstantConditions
		return hook.getDashboard()
				.getDashboardSection()
				.getArticleManager()
				.getCompilerManager()
				.getCompilationId();
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		ArrayList<Class<? extends Event>> events = new ArrayList<>(4);
		events.add(CompilerFinishedEvent.class);
		events.add(ArticleUpdateEvent.class);
		events.add(UserDefaultDashboardUpdateEvent.class);
		events.add(CompilationFinishedEvent.class);
		events.add(FullParseEvent.class);
		return events;
	}

	@Override
	public void notify(Event event) {
		if (event instanceof CompilerFinishedEvent<?> compilerFinishedEvent) {
			de.knowwe.core.compile.Compiler compiler = compilerFinishedEvent.getCompiler();
			if (compiler instanceof PackageCompiler) {
				Article article = ((PackageCompiler) compiler).getCompileSection().getArticle();
				articlesToTrigger.put(article.getTitle().toLowerCase(), article);
			}
		}
		if (event instanceof CompilationFinishedEvent) {
			synchronized (articlesToTrigger) {
				CIHookManager.getInstance().triggerHooks(articlesToTrigger.values());
				articlesToTrigger.clear();
			}
		}
		else if (event instanceof ArticleUpdateEvent updateEvent) {
			setLastTriggeringUser(updateEvent.getUsername());
		}
		else if (event instanceof FullParseEvent fullParseEvent) {
			setLastTriggeringUser(fullParseEvent.getUserName());
		}
		else if (event instanceof UserDefaultDashboardUpdateEvent dashboardUpdateEvent) {
			userToDefaultDashboardNames.put(dashboardUpdateEvent.getUserName(), dashboardUpdateEvent.getDashboardNames());
		}
	}

	private void setLastTriggeringUser(String username) {
		if ("system".equalsIgnoreCase(username)) return;
		if (!userToDefaultDashboardNames.containsKey(username)) return;
		lastTriggeringUser = username;
	}

	private final Map<String, Article> articlesToTrigger = Collections.synchronizedMap(new HashMap<>());
}
