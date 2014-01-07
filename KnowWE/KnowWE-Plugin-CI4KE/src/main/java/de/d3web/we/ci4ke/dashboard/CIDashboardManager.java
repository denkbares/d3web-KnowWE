/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.d3web.we.ci4ke.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Manages and provides {@link CIDashboard}s
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 31.12.2013
 */
public class CIDashboardManager {

	public static final Map<String, CIDashboard> dashboards = new HashMap<String, CIDashboard>();

	/**
	 * Get the {@link CIDashboard} instance responsible for a specific
	 * dashboardName-dashboardArticle-combination. If no handler exists for this
	 * combination, a new handler is created.
	 */
	public static synchronized CIDashboard getDashboard(String web, String dashboardArticleTitle, String dashboardName) {
		String key = web + "/" + dashboardArticleTitle + "/" + dashboardName;
		CIDashboard dashboard = dashboards.get(key);
		if (dashboard == null) {
			dashboard = new CIDashboard(web, dashboardArticleTitle, dashboardName);
			dashboards.put(key, dashboard);
		}
		return dashboard;
	}

	/**
	 * Checks if there is a {@link CIDashboard} instance responsible for a
	 * specific dashboardName-dashboardArticle-combination. If no dashboard
	 * exists for this combination, false is returned.
	 * 
	 * @param dashboardArticleTitle the article where the dashboard is located
	 * @param dashboardName the name of the dashboard
	 */
	public static Section<CIDashboardType> hasDashboard(String web, String dashboardArticleTitle, String dashboardName) {
		ArticleManager articleManager = Environment.getInstance().getArticleManager(web);
		Article article = articleManager.getArticle(dashboardArticleTitle);
		if (article == null) {
			return null;
		}
		List<Section<CIDashboardType>> sections =
				Sections.findSuccessorsOfType(article.getRootSection(), CIDashboardType.class);
		for (Section<CIDashboardType> section : sections) {
			String name = CIDashboardType.getDashboardName(section);
			if (name != null && name.equalsIgnoreCase(dashboardName)) {
				return section;
			}
		}
		return null;
	}

	public static CIDashboard getDashboard(Section<CIDashboardType> section) {
		String dashboardName = DefaultMarkupType.getAnnotation(section, "name");
		return getDashboard(Environment.DEFAULT_WEB, section.getTitle(), dashboardName);
	}

}
