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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.ci4ke.build.CIBuildManager;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.kdom.Article;

/**
 * 
 * @author Marc-Oliver Ochlast, Albrecht Striffler
 * 
 */
public class CIHookManager {

	/**
	 * Fore each monitored articles a list of hooks are stored.
	 */
	private final Map<String, Set<CIHook>> hooks;

	private static final CIHookManager instance = new CIHookManager();

	private CIHookManager() {
		hooks = new HashMap<String, Set<CIHook>>();
	}

	public static CIHookManager getInstance() {
		return instance;
	}

	public void registerHook(CIHook hook) {
		Collection<String> monitoredArticles = hook.getMonitoredArticles();
		for (String monitoredArticle : monitoredArticles) {
			Set<CIHook> set = hooks.get(monitoredArticle);
			if (set == null) {
				set = new HashSet<CIHook>();
				hooks.put(monitoredArticle, set);
			}
			set.add(hook);
		}
	}

	public void unregisterHook(CIHook hook) {
		Collection<String> monitoredArticles = hook.getMonitoredArticles();
		for (String monitoredArticle : monitoredArticles) {
			Set<CIHook> set = hooks.get(monitoredArticle);
			if (set != null) {
				set.remove(hook);
				if (set.isEmpty()) {
					hooks.remove(monitoredArticle);
				}
			}
		}
	}

	public void cleanHooksForArticle(String article) {
		Set<CIHook> hooksToRemove = new HashSet<CIHook>();
		for (Set<CIHook> hooks : this.hooks.values()) {
			for (CIHook hook : hooks) {
				if (article.equals(hook.getDashboardArticleTitle())) {
					hooksToRemove.add(hook);
				}
			}
		}
		for (CIHook hookToRemove : hooksToRemove) {
			unregisterHook(hookToRemove);
		}
	}

	/**
	 * Triggers the registered hooks for a given Article.
	 * 
	 * @param monitoredArticle the article to trigger hooks for
	 */
	public void triggerHooks(Article monitoredArticle) {
		triggerHooks(monitoredArticle.getTitle());
	}

	/**
	 * Triggers the registered hooks for a given Article
	 * 
	 * @param monitoredArticleTitle the article to trigger hooks for
	 */
	public void triggerHooks(String monitoredArticleTitle) {

		Set<CIHook> hookSet = hooks.get(monitoredArticleTitle);
		if (hookSet != null) {
			for (final CIHook hook : hookSet) {
				Logger.getLogger(CIEventForwarder.class.getName()).log(
						Level.INFO,
						"Executing new CI build for dashboard '" + hook.getDashboardName()
								+ "' in article '"
								+ hook.getDashboardArticleTitle() + "'");
				CIDashboard dashboard = CIDashboardManager.getDashboard(hook.getWeb(),
						hook.getDashboardArticleTitle(), hook.getDashboardName());
				CIBuildManager.startBuild(dashboard);
			}
		}
	}

}
