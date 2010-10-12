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

package de.d3web.we.selenium.tests;

import de.d3web.we.selenium.main.KnowWETestCase;

/**
 * This class creates all the pages being needed for the Selenium-Test of KnowWE
 * especially to test the car diagnosis.
 * 
 * @author Max Diez
 * 
 */
public class SetUpWikiPages extends KnowWETestCase {

	public void testCreateWikiPages() throws Exception {
		open("Wiki.jsp?page=Main");
		assertEquals("KnowWE: Main", selenium.getTitle());

		// Add SolutionPanel if necessary
		if (!selenium.isElementPresent("sstate-panel")) {
			open("/KnowWE/Wiki.jsp?page=LeftMenuFooter");
			loadAndWait(B_EDIT);
			doSelActionAndWait(EA, "type",
					selenium.getValue(EA)
							+ "\n\n[{KnowWEPlugin solutionpanel}]");
			loadAndWait(B_SAVE);
			open("/KnowWE/Wiki.jsp?page=Main");
		}

		assertTrue("SolutionPanel wasn't integrated", selenium.isElementPresent("sstate-panel"));

		// If Wikipages already exist -> done
		if (selenium.isElementPresent("link=Selenium-Test")) {
			return;
		}

		loadAndWait(B_EDIT);
		doSelActionAndWait(EA, "type", selenium.getValue(EA)
				+ "\n\n[Selenium-Test]");
		loadAndWait(B_SAVE);


		// Selenium Main Page
		loadAndWait("link=Selenium-Test");
		doSelActionAndWait(EA, "type", rb.getString("KnowWE.SeleniumTest.MainPage"));
		loadAndWait(B_SAVE);

		// Car-Diagnosis-Test Page
		loadAndWait("link=Car-Diagnosis-Test");
		doSelActionAndWait(EA, "type",
				rb.getString("KnowWE.SeleniumTest.Car-Diagnosis-Test-Page"));
		loadAndWait(B_SAVE);

		loadAndWait("link=Selenium-Test");
		loadAndWait("link=Car-Diagnosis-Test");
		assertTrue("Die 'Car Diagnosis' Seite wurde nicht richtig erstellt",
				selenium.isTextPresent("Car Diagnosis"));

		// Solutions-Pages, KB Page
		loadAndWait("link=Damaged idle speed system");
		doSelActionAndWait(EA, "type",
				rb.getString("KnowWE.SeleniumTest.Damaged"));
		loadAndWait(B_SAVE);

		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Leaking air intake system");
		doSelActionAndWait(EA, "type",
				rb.getString("KnowWE.SeleniumTest.Leaking"));
		loadAndWait(B_SAVE);

		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Clogged air filter");
		doSelActionAndWait(EA, "type",
				rb.getString("KnowWE.SeleniumTest.Clogged"));
		loadAndWait(B_SAVE);

		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Bad ignition timing");
		doSelActionAndWait(EA, "type",
				rb.getString("KnowWE.SeleniumTest.Bad"));
		loadAndWait(B_SAVE);

		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Empty battery");
		doSelActionAndWait(EA, "type",
				rb.getString("KnowWE.SeleniumTest.Empty"));
		loadAndWait(B_SAVE);

		open(rb.getString("KnowWE.SeleniumTest.url") + "/Wiki.jsp?page=Selenium-Test");
		loadAndWait("link=Quick-Edit-Test");
		doSelActionAndWait(EA, "type",
				rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test"));
		loadAndWait(B_SAVE);

		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Car Diagnosis Compiled KB");
		doSelActionAndWait(EA, "type",
				rb.getString("KnowWE.SeleniumTest.KB"));
		loadAndWait(B_SAVE);

		open(rb.getString("KnowWE.SeleniumTest.url") + "/Edit.jsp?page=CD-compiled-KB");
		loadAndWait(B_SAVE);
		loadAndWait("link=Car-Diagnosis-Test");

		// Markup (Car-Diagnosis) Page
		open("Edit.jsp?page=Markup(Car-Diagnosis)");
		doSelActionAndWait(EA, "type", rb.getString("KnowWE.SeleniumTest.Markup"));
		loadAndWait(B_SAVE);

		// Hermes-Test Page
		open("Edit.jsp?page=TimeLineEntries");
		doSelActionAndWait(EA, "type", rb.getString("KnowWE.SeleniumTest.TimeLineEntries"));
		loadAndWait(B_SAVE);
		
		// Renaming-Tool-Test Page
		open("Edit.jsp?page=Renaming-Tool-Test");
		doSelActionAndWait(EA, "type", rb.getString("KnowWE.SeleniumTest.Renaming"));
		loadAndWait(B_SAVE);		
		
	}
}
