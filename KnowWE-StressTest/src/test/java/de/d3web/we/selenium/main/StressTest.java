/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import junit.framework.TestSuite;
import de.d3web.we.selenium.tests.KBChangeStressTest;
import de.d3web.we.selenium.tests.TimeLineStressTest;

/**
 * This test is called every night by the build on the Hudson server. It
 * simulates many user actions changing the Wiki, which runs on a separate VM so
 * that a crash will do no harm. So far, there is a StressTest for a
 * KnowledgeBase (CarDiagnosis), also with the Markup and one for the Hermes-
 * Plugin (TimeLineEntries).
 * 
 * @author Max Diez
 * @created 21.07.2010
 */
public class StressTest extends TestSuite {

	/**
	 * Containing all the specific StressTests.
	 * 
	 * @created 21.07.2010
	 * @return A TestSuite with all the StressTests.
	 * @throws Exception
	 */
	public static TestSuite suite() throws Exception {
		TestSuite mainSuite = new StressTest();
		mainSuite.addTestSuite(KBChangeStressTest.class);
		mainSuite.addTestSuite(TimeLineStressTest.class);
		return mainSuite;
	}
}
