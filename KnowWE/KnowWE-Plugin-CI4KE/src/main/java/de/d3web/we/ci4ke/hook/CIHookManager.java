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

import java.util.Set;

import de.d3web.collections.DefaultMultiMap;
import de.d3web.collections.MultiMap;
import de.d3web.we.ci4ke.build.CIBuildManager;
import de.knowwe.core.kdom.Article;

/**
 * @author Marc-Oliver Ochlast, Albrecht Striffler
 */
public class CIHookManager {

	/**
	 * Fore each monitored articles a list of hooks are stored.
	 */
	private static final MultiMap<String, CIHook> hooks = new DefaultMultiMap<>();

	public static synchronized void registerHook(CIHook hook) {
		for (String monitoredArticle : hook.getMonitoredArticles()) {
			hooks.put(monitoredArticle, hook);
		}
	}

	public static synchronized void unregisterHook(CIHook hook) {
		for (String monitoredArticle : hook.getMonitoredArticles()) {
			hooks.removeKey(monitoredArticle);
		}
	}

	/**
	 * Triggers the registered hooks for a given Article.
	 *
	 * @param monitoredArticle the article to trigger hooks for
	 */
	public static synchronized void triggerHooks(Article monitoredArticle) {
		Set<CIHook> hookSet = hooks.getValues(monitoredArticle.getTitle());
		for (final CIHook hook : hookSet) {
			CIBuildManager.getInstance().startBuild(hook.getDashboard());
		}
	}
}
