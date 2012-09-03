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

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.io.progress.AjaxProgressListenerImpl;
import de.d3web.core.io.progress.ProgressListener;
import de.d3web.core.io.progress.ProgressListenerManager;
import de.d3web.testing.BuildResult;
import de.d3web.testing.TestExecutor;
import de.d3web.testing.TestObjectProvider;
import de.d3web.testing.TestObjectProviderManager;
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
	 * Therefore the TestExecutor is used. Adds the resultset to dashboard.
	 */
	public void executeBuild() {

		// retrieve build number
		BuildResult previousBuild = dashboard.getLatestBuild();
		int buildNumber = (previousBuild == null) ? 1 : previousBuild.getBuildNumber() + 1;

		List<TestObjectProvider> providers = new ArrayList<TestObjectProvider>();
		providers.add(DefaultWikiTestObjectProvider.getInstance());
		List<TestObjectProvider> pluggedProviders = TestObjectProviderManager.findTestObjectProviders();
		providers.addAll(pluggedProviders);

		ProgressListener listener = new AjaxProgressListenerImpl();
		ProgressListenerManager.getInstance().setProgressListener(dashboard.getDashboardName(),listener);
			
		// create and run TestExecutor
		TestExecutor executor = new TestExecutor(providers,
				this.config.getTests(), listener, buildNumber);
		
		CIUtilities.registerBuildExecutor(dashboard.getDashboardName(), executor);
		executor.run();

		BuildResult build = executor.getBuildResult();

		// add resulting build to dashboard
		if (build != null && !Thread.interrupted()) {
			dashboard.addBuild(build);
		}
		ProgressListenerManager.getInstance().removeProgressListener(dashboard.getDashboardName());
		CIUtilities.deregisterAndTerminateBuildExecutor(dashboard.getDashboardName());
	}

}
