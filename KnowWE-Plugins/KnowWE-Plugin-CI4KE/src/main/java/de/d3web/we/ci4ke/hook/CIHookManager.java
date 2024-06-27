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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import de.d3web.we.ci4ke.build.CIBuildManager;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.knowwe.core.kdom.Article;

/**
 * @author Marc-Oliver Ochlast, Albrecht Striffler
 */
public class CIHookManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(CIHookManager.class);

	/**
	 * Fore each monitored articles a list of hooks are stored.
	 */
	private static final MultiMap<String, CIHook> hooks = new DefaultMultiMap<>();
	public static final String KNOWWE_CI_HOOKS_ACTIVE = "knowwe.ci.hooks.active";

	public static synchronized void registerHook(CIHook hook) {
		for (String monitoredArticle : hook.getMonitoredArticles()) {
			hooks.put(monitoredArticle, hook);
		}
	}

	public static synchronized void unregisterHook(CIHook hook) {
		hooks.removeValue(hook);
	}

	public static synchronized void triggerHooks(Article monitoredArticle) {
		triggerHooks(List.of(monitoredArticle));
	}

	/**
	 * Triggers the registered hooks for a given Article.
	 *
	 * @param monitoredArticles the article to trigger hooks for
	 */
	public static synchronized void triggerHooks(Collection<Article> monitoredArticles) {
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
			}
		}
		CIBuildManager.getInstance().startBuilds(dashboardsToTrigger);
		List<String> triggered = dashboardsToTrigger.stream().map(CIDashboard::getDashboardName).sorted().toList();
		if (!triggered.isEmpty()) LOGGER.info("Triggered the following dashboards: " + String.join(", ", triggered));
	}

	private static int getCurrentCompilationId(CIHook hook) {
		//noinspection ConstantConditions
		return hook.getDashboard()
				.getDashboardSection()
				.getArticleManager()
				.getCompilerManager()
				.getCompilationId();
	}
}
