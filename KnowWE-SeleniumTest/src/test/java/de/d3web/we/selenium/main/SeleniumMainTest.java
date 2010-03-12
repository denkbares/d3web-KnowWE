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

package de.d3web.we.selenium.main;

import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.kohsuke.junit.ParallelTestSuite;

import de.d3web.we.selenium.tests.CalendarTest;
import de.d3web.we.selenium.tests.CarDiagnosisDialogTest;
import de.d3web.we.selenium.tests.CarDiagnosisTest;
import de.d3web.we.selenium.tests.CommentTest;
import de.d3web.we.selenium.tests.QuickEditTest;

/**
 * This TestClass organizes the Selenium test of KnowWE2.
 * Therefore you need to get Selenium-grid and update it's local path
 * in StartClients.bat.
 * Because Selenium is a browser based test, some clients are started with
 * StartClients.bat and closed after the test with KillClients.bat.
 * Test:
 * First the needed pages are set up and afterwards the tests will be run 
 * in PARALLEL.
 * @author Max Diez
 *
 */
public class SeleniumMainTest extends TestSuite {
	
	/**
	 * Add to mainSuite in the right order the tests which have to be run
	 * in serial or are needed as startup for the other tests. Afterwards
	 * the independent test units are added in a ParallelTestSuite
	 * @return TestSuite mainSuite with all test components
	 * @throws Exception
	 */
	public static TestSuite suite() throws Exception {

		TestSuite mainSuite = new SeleniumMainTest();
		
		//Add here the serial startup tests
		//The needed wikipages are checked out automatically into
		//the wiki (d3web-KnowWE/KnowWE/src/misc/resources/core-pages/)
//		mainSuite.addTestSuite(SetUpWikiPages.class);

//		TestSuite parallelSuite = new ParallelTestSuite("Testsuite for all parallel units");
		
		//Add here the test which could be run in parrallel
//		parallelSuite.addTestSuite(CalendarTest.class);
//		parallelSuite.addTestSuite(CarDiagnosisTest.class);
//		parallelSuite.addTestSuite(CommentTest.class);
		
//		mainSuite.addTest(parallelSuite);
		mainSuite.addTestSuite(CarDiagnosisDialogTest.class);
		mainSuite.addTestSuite(CalendarTest.class);
		mainSuite.addTestSuite(CarDiagnosisTest.class);
		mainSuite.addTestSuite(CommentTest.class);
		mainSuite.addTestSuite(QuickEditTest.class);

		return mainSuite;		
	}


	@Override
	/**
	 * Overriding method run to guarentee that the test environment
	 * is set up (Selenium-Grid server and clients).
	 */
	public void run(TestResult result){
		//Use this code if you don't want to set up clients
		//manually and shut them down.
//		String appsPath = System.getProperty("user.dir") + 
//				ResourceBundle.getBundle("KnowWE-Selenium-Test")
//					.getString("KnowWE.SeleniumTest.path");
//	
//		try {			
//			Runtime.getRuntime().exec(appsPath + "StartClients.bat");
//			System.out.println("#######\nClients started\n#######");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		System.out.println("Tests started");
		super.run(result);
		System.out.println("Tests finished!");
//		try {
//			Thread.sleep(5000);
//			Runtime.getRuntime().exec(appsPath + "KillClients.bat");
//			System.out.println("#######\nClients killed\n#######");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
		
}

