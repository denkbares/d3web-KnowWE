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
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.core.utilities.Pair;
import de.d3web.we.ci4ke.handling.CIConfig;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.d3web.we.ci4ke.handling.CIHook;
import de.d3web.we.ci4ke.testing.CITest;
import de.d3web.we.ci4ke.testing.CITestResult;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class CIBuilder {

	public static final String ACTUAL_BUILD_STATUS = "actualBuildStatus";
	public static final String BUILD_RESULT = "result";

	private final CIConfig config;

	/**
	 * This constructor searches only the given dashboardArticle for dashboard
	 * with the given ID
	 * 
	 * @param dashboardArticleTitle
	 * @param dashboardName
	 */
	public CIBuilder(String dashboardArticleTitle, String dashboardName) {
		KnowWEArticle dashboardArticle = KnowWEEnvironment.getInstance().getArticleManager(
				KnowWEEnvironment.DEFAULT_WEB).getArticle(dashboardArticleTitle);
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
		this(hook.getDashboardArticleTitle(), hook.getDashboardName());
	}

	/**
	 * This is the main method of a ci builder, which executes a new Build. -
	 * get the names of all tests - get the classes of all tests - instantiate()
	 * and init() all test-classes - invoke all tests with the help of a
	 * threaded executor service - collect all results and construct the build
	 * resultset - write the resultset to file (by PersistenceHandler)
	 */
	public void executeBuild() {

		// Map<String, Class<? extends CITest>> testClasses =
		// CIUtilities.parseTestClasses(this.config.getTests().keySet());
		Map<String, Class<? extends CITest>> testClasses = CIUtilities.getAllCITestClasses();

		// Map<String, Future<CITestResult>> futureResults =
		// new HashMap<String, Future<CITestResult>>();
		List<Pair<String, Future<CITestResult>>> futureResults =
				new ArrayList<Pair<String, Future<CITestResult>>>();

		// Just for now, be build only in a single, parallel thread.
		// Multithreaded building with more than one build thread has
		// to be thought about.
		ExecutorService executor = Executors.newSingleThreadExecutor();

		for (Pair<String, List<String>> testAndItsParameters : this.config.getTests()) {

			String testName = testAndItsParameters.getA();
			List<String> parameters = testAndItsParameters.getB();

			Class<? extends CITest> clazz = testClasses.get(testName);
			if (clazz != null) {
				CITest test = null;
				try {
					test = clazz.newInstance();
				}
				catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (test != null) {
					// init the test with the CIConfig
					test.init(config);

					// set the test paramterers
					test.setParameters(parameters);

					// Submit this test for threaded execution!
					// The result will arrive in the future...
					Future<CITestResult> res = executor.submit(test);

					// store this
					futureResults.add(new Pair<String, Future<CITestResult>>(testName, res));
				}
			}
			else {
				// TODO here is some feedback inside the wiki necessary
				Logger.getLogger(CIBuilder.class.getName()).log(Level.WARNING,
						"CITest class not found: '" + testName + "'");
			}
		}

		// Now collect the results
		CIBuildResultset resultset = new CIBuildResultset();
		// resultset.setArticleVersion(monitoredArticleVersion);

		for (Pair<String, Future<CITestResult>> runningTest : futureResults) {

			String testname = runningTest.getA();
			Future<CITestResult> futureResult = runningTest.getB();

			try {
				resultset.addTestResult(testname,
							futureResult.get());
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// write the resultset to XML
		CIBuildPersistenceHandler persi = CIBuildPersistenceHandler.getHandler(
				this.config.getDashboardName(),
				this.config.getDashboardArticleTitle());

		persi.write(resultset);
	}
}
