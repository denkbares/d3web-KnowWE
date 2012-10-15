/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.testing.BuildResult;
import de.d3web.we.ci4ke.build.CIBuildCache;
import de.d3web.we.ci4ke.build.CIPersistence;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * This class represents a dashboard data structure, managing the build results,
 * their persistence and provides an appropriate renderer.
 * 
 * @author volker_belli
 * @created 19.05.2012
 */
public class CIDashboard {

	private final String web;
	private final String dashboardArticle;
	private final String dashboardName;
	private final CIRenderer renderer;
	private final CIPersistence persistence;
	private final CIBuildCache buildCache = new CIBuildCache();

	private CIDashboard(String web, String dashboardArticle, String dashboardName) {
		this.web = web;
		this.dashboardArticle = dashboardArticle;
		this.dashboardName = dashboardName;
		this.renderer = new CIRenderer(this);
		this.persistence = new CIPersistence(this);
	}

	public String getWeb() {
		return web;
	}

	public String getDashboardArticle() {
		return dashboardArticle;
	}

	public String getDashboardName() {
		return dashboardName;
	}

	public CIRenderer getRenderer() {
		return renderer;
	}

	public int getLatestBuildNumber() {
		int buildNumber = 0;
		BuildResult latestBuild = getLatestBuild();
		if (latestBuild != null) {
			return latestBuild.getBuildNumber();
		}
		return buildNumber;
	}

	/**
	 * Returns the latest build stored in this dashboard or null if there is no
	 * build stored.
	 * 
	 * @created 19.05.2012
	 * @return the latest build
	 */
	public BuildResult getLatestBuild() {
		return getBuildIfPossible(-1, true);
	}

	/**
	 * Returns the build of the specified buildNumber stored in this dashboard
	 * or null if this build does not exist.
	 * 
	 * @created 19.05.2012
	 * @param buildNumber the build to be returned, you can use -1 to get the
	 *        latest build
	 * @return the specified build
	 */
	public BuildResult getBuild(int buildNumber) {
		return getBuildIfPossible(buildNumber, true);
	}

	/**
	 * Returns an array of builds that have the specified index in the list of
	 * versions available in this dashboard. The first build to be accessed
	 * starts at fromIndex, the latest build is before toIndex (exclusively). If
	 * the specified indexes exceed the list of history, the array contains less
	 * than "toIndex - fromIndex" values. If there is no build between the
	 * indexes, an empty array is returned.
	 * 
	 * @created 19.05.2012
	 * @param buildNumber the newest build to be returned, you can use -1 to
	 *        start with the latest build
	 * @param numberOfBuilds the amount of builds you want to get (the next
	 *        older ones)
	 * @return the specified builds
	 */
	public List<BuildResult> getBuilds(int fromIndex, int numberOfBuilds) {
		fromIndex = cap(fromIndex);
		List<BuildResult> results = new ArrayList<BuildResult>(numberOfBuilds);
		int index = fromIndex;
		while (results.size() < numberOfBuilds && index > 0) {
			BuildResult build = getBuildIfPossible(index, false);
			if (build != null && build.getBuildNumber() <= fromIndex) {
				results.add(build);
			}
			index--;
		}
		return results;
	}

	/**
	 * If the buildNumber is to high or low, the highest available buildNumber
	 * is returned;
	 * 
	 * @created 18.09.2012
	 * @return the highest available build number if the given is higher that
	 *         that
	 */
	private int cap(int buildIndex) {
		int latestBuildNumber = getLatestBuildNumber();
		if (latestBuildNumber < buildIndex) return latestBuildNumber;
		if (buildIndex < 1) return latestBuildNumber;
		return buildIndex;
	}

	/**
	 * Adds a new build to the dashboard and makes the underlying persistence to
	 * store that build. The given build will always be considered as the latest
	 * build
	 * 
	 * @created 19.05.2012
	 * @param build the build to be added
	 */
	public synchronized void addNewBuild(BuildResult build) {
		if (build == null) throw new IllegalArgumentException("build is null!");

		// also store here for quick access and elements not covered by the
		// persistence
		int buildNumber = getLatestBuildNumber() + 1;
		build.setBuildNumber(buildNumber);
		// the latest build is either from persistence and has the version of
		// the attachment as the build number or it is already set from previous
		// calls to this method

		buildCache.setLatestBuild(build);
		buildCache.addBuild(build);

		// attach to wiki if possible
		try {
			persistence.write(build);
		}
		catch (IOException e) {
			// we cannot store the build as attachment
			// so log this and continue as usual
			Logger.getLogger(getClass().getName()).log(Level.SEVERE,
					"Cannot attached build information due to internal error", e);
		}
	}

	private BuildResult getBuildIfPossible(final int buildVersion, boolean logging) {
		BuildResult build = null;
		build = buildCache.getBuild(buildVersion);
		if (build != null) return build;
		try {
			build = persistence.read(buildVersion);
		}
		catch (Exception e) {
			if (logging) {
				Logger.getLogger(getClass().getName()).warning(
						"Unable to access build " + buildVersion + ". Error: " + e.getMessage());
			}
		}
		if (build != null) {
			if (buildVersion < 1) buildCache.setLatestBuild(build);
			buildCache.addBuild(build);
		}
		return build;
	}

	private static final Map<String, CIDashboard> dashboards = new HashMap<String, CIDashboard>();

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
	public static boolean hasDashboard(String web, String dashboardArticleTitle, String dashboardName) {
		ArticleManager articleManager = Environment.getInstance().getArticleManager(web);
		Article article = articleManager.getArticle(dashboardArticleTitle);
		if (article == null) {
			return false;
		}
		List<Section<CIDashboardType>> sections =
				Sections.findSuccessorsOfType(article.getRootSection(), CIDashboardType.class);
		for (Section<CIDashboardType> section : sections) {
			String name = CIDashboardType.getDashboardName(section);
			if (name != null && name.equalsIgnoreCase(dashboardName)) {
				return true;
			}
		}
		return false;
	}

}
