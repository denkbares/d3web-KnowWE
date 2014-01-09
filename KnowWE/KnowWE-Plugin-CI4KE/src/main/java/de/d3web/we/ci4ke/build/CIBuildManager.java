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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.testing.BuildResult;
import de.d3web.testing.TestExecutor;
import de.d3web.testing.TestObjectProvider;
import de.d3web.testing.TestObjectProviderManager;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.d3web.we.ci4ke.hook.CIEventForwarder;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.progress.AjaxProgressListener;
import de.knowwe.core.utils.progress.DefaultAjaxProgressListener;
import de.knowwe.core.utils.progress.ProgressListenerManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIBuildManager {

	public static final String ACTUAL_BUILD_STATUS = "actualBuildStatus";
	public static final String BUILD_RESULT = "result";

	private static Map<CIDashboard, TestExecutor> runningBuilds = Collections.synchronizedMap(new HashMap<CIDashboard, TestExecutor>());

	/**
	 * Runs a build for the given dashboard, but does not . Waits until the
	 * build is registered in the manager, but does not wait until the build is
	 * done.
	 */
	public static void startBuild(final CIDashboard dashboard) {
		Section<CIDashboardType> sec = dashboard.getDashboardSection();

		Logger.getLogger(CIEventForwarder.class.getName()).log(
				Level.INFO,
				"Executing new CI build for dashboard '" + dashboard
						+ "'");

		List<TestObjectProvider> providers = new ArrayList<TestObjectProvider>();
		providers.add(DefaultWikiTestObjectProvider.getInstance());
		List<TestObjectProvider> pluggedProviders = TestObjectProviderManager.getTestObjectProviders();
		providers.addAll(pluggedProviders);

		AjaxProgressListener listener = new DefaultAjaxProgressListener(null);
		ProgressListenerManager.getInstance().setProgressListener(
				Integer.toString(dashboard.hashCode()), listener);

		// create and run TestExecutor
		final TestExecutor executor = new TestExecutor(providers,
				dashboard.getTestSpecifications(), listener);

		// if there already is a running build, we terminate it
		terminate(dashboard);
		runningBuilds.put(dashboard, executor);

		new Thread() {

			@Override
			public void run() {
				try {
					executor.run();

					BuildResult build = executor.getBuildResult();

					// add resulting build to dashboard
					if (build != null && !Thread.interrupted()) {
						// set verbose persistence flag, will be considered by
						// persistence
						build.setVerbosePersistence(lookUpVerboseFlag(dashboard));
						dashboard.addNewBuild(build);
					}
				}
				finally {
					try {
						ProgressListenerManager.getInstance().removeProgressListener(
								Integer.toString(dashboard.hashCode()));
					}
					finally {
						runningBuilds.remove(dashboard);
					}
				}

			};
		}.start();
	}

	private static boolean lookUpVerboseFlag(CIDashboard dashboard) {
		Section<CIDashboardType> ciDashboardSection = dashboard.getDashboardSection();
		String flagString = DefaultMarkupType.getAnnotation(ciDashboardSection,
				CIDashboardType.VERBOSE_PERSISTENCE_KEY);
		if (flagString != null && flagString.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

	/**
	 * Terminates the build of the given dashboard (if there is one).
	 * 
	 * @created 31.12.2013
	 * @param dashboardName the name of the dashboard to terminate
	 */
	public static void terminate(CIDashboard dashboard) {
		TestExecutor executor = runningBuilds.get(dashboard);
		if (executor != null) {
			if (executor.isRunning()) {
				executor.terminate();
				executor.awaitTermination(5, TimeUnit.SECONDS);
			}
		}
	}

	/**
	 * Blocks/waits until all running tests of the given dashboard are done,
	 * tests are not aborted by calling this method.
	 * 
	 * @created 17.12.2013
	 */
	public static void awaitTermination(CIDashboard dashboard) {
		TestExecutor executor = runningBuilds.get(dashboard);
		if (executor != null) {
			executor.awaitTermination();
		}
	}

	/**
	 * Blocks/waits until all running tests of all registered dashboards are
	 * done. Tests are not aborted by calling this method.
	 * 
	 * @created 17.12.2013
	 */
	public static void awaitTermination() {
		TestExecutor executor = getNextExecutor();
		while (executor != null) {
			executor.awaitTermination();
			executor = getNextExecutor();
		}
	}

	private static TestExecutor getNextExecutor() {
		TestExecutor executor = null;
		synchronized (runningBuilds) {
			if (!runningBuilds.isEmpty()) {
				executor = runningBuilds.values().iterator().next();
			}
		}
		return executor;
	}

	/**
	 * Looks up whether there is currently a build process running for this
	 * dashboard
	 * 
	 * @created 16.08.2012
	 * @param dashboardName
	 * @return
	 */
	public static boolean isRunning(CIDashboard dashboard) {
		return runningBuilds.get(dashboard) != null;
	}
}
