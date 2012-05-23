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

package de.d3web.we.ci4ke.build;

import java.util.Map;

import cc.denkbares.testing.BuildResultSet;
import cc.denkbares.testing.Test;
import cc.denkbares.testing.TestExecutor;
import de.d3web.we.ci4ke.handling.CIConfig;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.d3web.we.ci4ke.handling.CIHook;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

public class CIBuilder {

	public static final String ACTUAL_BUILD_STATUS = "actualBuildStatus";
	public static final String BUILD_RESULT = "result";

	private final CIConfig config;
	private final Dashboard dashboard;

	/**
	 * This constructor searches only the given dashboardArticle for dashboard
	 * with the given ID
	 * 
	 * @param dashboardArticleTitle
	 * @param dashboardName
	 */
	public CIBuilder(String web, String dashboardArticleTitle, String dashboardName) {
		this.dashboard = Dashboard.getDashboard(web, dashboardArticleTitle, dashboardName);
		Article dashboardArticle = Environment.getInstance().getArticleManager(
				Environment.DEFAULT_WEB).getArticle(dashboardArticleTitle);
		Section<CIDashboardType> sec = CIUtilities.
				findCIDashboardSection(dashboardArticleTitle, dashboardName);
		if (sec == null) {
			throw new IllegalArgumentException("No dashboard " +
					"with the given Name found on this article!!");
		}
		this.config = (CIConfig) KnowWEUtils.getStoredObject(dashboardArticle, sec,
				CIConfig.CICONFIG_STORE_KEY);

	}

	/**
	 * Convenience Constructor
	 * 
	 * @param hook
	 */
	public CIBuilder(CIHook hook) {
		this(hook.getWeb(), hook.getDashboardArticleTitle(), hook.getDashboardName());
	}

	/**
	 * This is the main method of a ci builder, which executes a new Build. -
	 * get the names of all tests - get the classes of all tests - instantiate()
	 * and init() all test-classes - invoke all tests with the help of a
	 * threaded executor service - collect all results and construct the build
	 * resultset - write the resultset to file (by PersistenceHandler)
	 */
	public void executeBuild() {

		BuildResultSet previousBuild = dashboard.getLatestBuild();
		int buildNumber = (previousBuild == null) ? 1 : previousBuild.getBuildNumber() + 1;

		// Map<String, Class<? extends CITest>> testClasses =
		// CIUtilities.parseTestClasses(this.config.getTests().keySet());
		Map<String, Class<? extends Test>> testClasses = CIUtilities.getAllCITestClasses();

		// Map<String, Future<CITestResult>> futureResults =
		// new HashMap<String, Future<CITestResult>>();
		TestExecutor executor = new TestExecutor(
				DefaultWikiTestObjectProvider.getInstance(), this.config.getTests());

		BuildResultSet build = executor.runtTests(buildNumber);

		dashboard.addBuild(build);
	}
}
