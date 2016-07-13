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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.denkbares.collections.MultiMap;
import com.denkbares.collections.N2MMap;
import de.d3web.testing.TestSpecification;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Manages and provides {@link CIDashboard}s
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 31.12.2013
 */
public class CIDashboardManager {

	private static final Map<ArticleManager, MultiMap<Object, CIDashboard>> dashboardsForManagers = new WeakHashMap<>();

	private static MultiMap<Object, CIDashboard> getDashboardsMap(ArticleManager manager) {
		MultiMap<Object, CIDashboard> dashboards = dashboardsForManagers.get(manager);
		if (dashboards == null) {
			dashboards = new N2MMap<>();
			dashboardsForManagers.put(manager, dashboards);
		}
		return dashboards;
	}

	public static synchronized CIDashboard generateAndRegisterDashboard(Section<CIDashboardType> section, List<TestSpecification<?>> tests) {
		CIDashboard dashboard = new CIDashboard(section, tests);
		MultiMap<Object, CIDashboard> dashboardsMap = getDashboardsMap(section.getArticleManager());
		dashboardsMap.put(dashboard.getDashboardName(), dashboard);
		dashboardsMap.put(section, dashboard);
		return dashboard;
	}

	public static synchronized CIDashboard getDashboard(ArticleManager manager, String dashboardName) {
		Set<CIDashboard> dashboards = getDashboardsMap(manager).getValues(dashboardName);
		if (!dashboards.isEmpty()) return dashboards.iterator().next();
		return null;
	}

	public static synchronized CIDashboard getDashboard(Section<CIDashboardType> section) {
		Set<CIDashboard> dashboards = getDashboardsMap(section.getArticleManager()).getValues(
				section);
		for (CIDashboard dashboard : dashboards) {
			if (dashboard.getDashboardSection() == section) return dashboard;
		}
		return null;
	}

	public static synchronized void unregisterDashboard(Section<CIDashboardType> section) {
		getDashboardsMap(section.getArticleManager()).removeValue(getDashboard(section));
	}

	public static synchronized Collection<Section<CIDashboardType>> getDashboardSections(ArticleManager manager, String dashboardName) {
		Collection<Section<CIDashboardType>> dashboardSections = new ArrayList<>();
		for (CIDashboard ciDashboard : getDashboardsMap(manager).getValues(dashboardName)) {
			dashboardSections.add(ciDashboard.getDashboardSection());
		}
		return dashboardSections;
	}
}
