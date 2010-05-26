/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.ci4ke.build;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.ci4ke.handling.CIConfig;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.d3web.we.ci4ke.handling.CITest;
import de.d3web.we.ci4ke.handling.CITestResult;
import de.d3web.we.ci4ke.handling.CIHookManager.CIHook;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class CIBuilder {

	public static final String ACTUAL_BUILD_STATUS 	= "actualBuildStatus";
	public static final String BUILD_RESULT 		= "result";

	public static enum CIBuildTriggers {
		onDemand,
		onSave,
		onNight
	}	
	
	private CIConfig config;
	
	/**
	 * This constructor searches only the given dashboardArticle for
	 * dashboard with the given ID
	 * @param dashboardArticleTitle
	 * @param dashboardID
	 */
	public CIBuilder(String dashboardArticleTitle, String dashboardID) {
		Section<CIDashboardType> sec = CIUtilities.
			findCIDashboardSection(dashboardArticleTitle, dashboardID);
		if(sec==null) throw new IllegalArgumentException("No dashboard "+
			"with the given ID found on this article!!");
		this.config = (CIConfig) KnowWEUtils.getStoredObject(sec, 
			CIConfig.CICONFIG_STORE_KEY);
	}
	
	/**
	 * This constructor searches all articles for the given dashboardID
	 * @param dashboardID
	 */
	public CIBuilder(String dashboardID) {
		Section<CIDashboardType> sec = CIUtilities.
			findCIDashboardSection(dashboardID);
		if(sec==null)
			throw new IllegalArgumentException("No dashboard with the given ID found!");
		this.config = (CIConfig) KnowWEUtils.getStoredObject(sec, 
				CIConfig.CICONFIG_STORE_KEY);
		
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
				">>> constructed a new CIBuilder >>>");
	}
	
	/**
	 * Convenience Constructor
	 * @param hook
	 */
	public CIBuilder(CIHook hook) {
		this(hook.getDashboardArticleTitle(), hook.getDashboardID());
	}	
	
	/**
	 * This is the main method of a ci builder, which executes a new Build.
	 * - get the names of all tests
	 * - get the classes of all tests
	 * - instantiate() and init() all test-classes
	 * - invoke all tests with the help of a threaded executor service
	 * - collect all results and construct the build resultset
	 * - write the resultset to file (by PersistenceHandler)
	 */
	public void executeBuild() {
		
		Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
				">>> starting to execute a new build! >>>");		
		
		Map<String, Class<? extends CITest>> testClasses = 
			CIUtilities.parseTestClasses(this.config.getTestNames());
	
		Map<String, Future<CITestResult>> futureResults =
			new HashMap<String, Future<CITestResult>>();
		
		//Just for now, be build only in a single, parallel thread.
		//Multithreaded building with more than one build thread has 
		//to be thought about.
		ExecutorService executor = Executors.newSingleThreadExecutor();
				
		for(Map.Entry<String, Class<? extends CITest>> entry : 
			testClasses.entrySet()) {
			
			String testname = entry.getKey();
			Class<? extends CITest> clazz = entry.getValue();
			
			CITest test = null;
			try {
				test = clazz.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(test != null) {
				//init the test with the CIConfig
				test.init(config);
				
				//Submit this test for threaded execution!
				//The result will arrive in the future...
				Future<CITestResult> res = executor.submit(test);
				
				//store this
				futureResults.put(testname, res);
			}
		}
		
		int monitoredArticleVersion = KnowWEEnvironment.getInstance().
			getWikiConnector().getVersion(this.config.getMonitoredArticleTitle());

		Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
		">>> CIBuilder: testclasses parsed! >>>");
		
		//Now collect the results
		CIBuildResultset resultset = new CIBuildResultset();
		resultset.setArticleVersion(monitoredArticleVersion);
		
		for(Map.Entry<String, Future<CITestResult>> entry :
			futureResults.entrySet()) {
			
			String testname = entry.getKey();
			Future<CITestResult> futureResult = entry.getValue();
			
			try {
//				if(futureResult.isDone()) {
					Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
						">> CI >> CIBuilder: Trying to get CITestResult of test " +testname+ "!! >>>");
					
					resultset.addTestResult(testname, 
							futureResult.get());
					
					Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
							">> CI >> CIBuilder: Got Result of test " +testname+ "!! >>>");				
//				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//write the resultset to XML
		CIBuildPersistenceHandler persi = new 
			CIBuildPersistenceHandler(this.config.getDashboardID());
		persi.write(resultset, this.config.getMonitoredArticleTitle());
		
	}
	
	
	
	/**
	 * <build executed="NOW()">
	 * 		<test .../>
	 * 		<test .../>
	 * </build>
	 * @throws IOException 
	 */
//	public void createNewBuild() throws IOException{
//		
//		TestResultType overallResult = TestResultType.SUCCESSFUL;
//		Element build = new Element("build");
//		
//		Date now = new Date();
//		build.setAttribute("executed",DATE_FORMAT.format(now));
//		
//		build.setAttribute("nr", String.valueOf(nextBuildNumber));
//		nextBuildNumber++;
//		
//		String testname;
//		CITestResult result;
//		for(Class<? extends CITest> testClass : config.getTestsToExecute()){
//			try {
//				testname = testClass.getSimpleName();
//				CITest t = testClass.newInstance();
//				t.init(config);
//				result = t.call();
//				
//				//the "worst" result of the executed tests determines 
//				//the result of the build
//				if(overallResult.compareTo(result.getResultType())<0)
//					overallResult = result.getResultType();
//				
//				Element test = new Element("test");
//				test.setAttribute("name", testname);
//				test.setAttribute("result", result.getResultType().toString());
//				
//				if(result.getTestResultMessage().length()>0)
//					test.setAttribute("message",result.getTestResultMessage());
//				
//				build.addContent(test);
////				ret.append("<p>"+testClass.getSimpleName() + ": " +
////						testClass.newInstance().execute(config)+"</p>");
//			} catch (InstantiationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}			
//		}
//		//the "worst" result of the executed tests determines the result of the build
//		this.currentBuildStatus = overallResult;
//		xmlDocument.getRootElement().setAttribute(ACTUAL_BUILD_STATUS, 
//				overallResult.toString());
//		//...also, save the result of the current build as attribute
//		//in the <build .../> Tag
//		build.setAttribute("result", overallResult.toString());
//		//add the build-element to the JDOM Tree
//		xmlDocument.getRootElement().addContent(build);
//		//and print it to file
//		XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() );
//		out.output(xmlDocument, new FileWriter(xmlFile));
//	}
	
//	public List<?> selectNodes(String xPathQuery){
//		List<?> ret = null;
//		try {
//			ret = XPath.selectNodes(xmlDocument, xPathQuery);
//		} catch (JDOMException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return ret;
//	}
}
