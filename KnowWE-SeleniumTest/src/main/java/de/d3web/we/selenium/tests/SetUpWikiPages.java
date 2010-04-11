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

package de.d3web.we.selenium.tests;

/**
 * This class creates all the pages being needed for the
 * Selenium-Test of KnowWE especially to test the car diagnosis.
 * @author Max Diez
 *
 */
public class SetUpWikiPages extends KnowWETestCase{
	
	public void testCreateWikiPages() throws Exception {
		open("Wiki.jsp?page=Main");
		assertEquals("KnowWE: Main", selenium.getTitle());
		
		//Add Solutionstates if necessary 
		if (!selenium.isElementPresent("sstate-panel")) {
			open("/KnowWE/Wiki.jsp?page=LeftMenuFooter");
			loadAndWait("//div[@id='actionsTop']/ul/li[1]/a");
			doSelActionAndWait("editorarea", "type",
					selenium.getValue("editorarea")
					+ "\n\n[{KnowWEPlugin solutionstates}]");
			loadAndWait("ok");
			open("/KnowWE/Wiki.jsp?page=Main");
		}
		
		assertTrue("Solutionstates wasn't integrated", selenium.isElementPresent("sstate-panel"));		
		
		//If Wikipages already exist -> done
		if (selenium.isElementPresent("link=Selenium-Test")) {
			return;
		}
		
		loadAndWait("//div[@id='actionsTop']/ul/li[1]/a");
		doSelActionAndWait("editorarea", "type", selenium.getValue("editorarea") 
				+ "\n\n[Selenium-Test]");
		loadAndWait("ok");
		
		loadAndWait("link=Selenium-Test");
		doSelActionAndWait("editorarea", "type",  rb.getString("KnowWE.SeleniumTest.MainPage"));
		loadAndWait("ok");
		
		loadAndWait("link=Car-Diagnosis-Test");
		doSelActionAndWait("editorarea", "type", 
				rb.getString("KnowWE.SeleniumTest.Car-Diagnosis-Test-Page"));
		loadAndWait("ok");
		
		loadAndWait("link=Selenium-Test");
		loadAndWait("link=Car-Diagnosis-Test");
		assertTrue("Die 'Car Diagnosis' Seite wurde nicht richtig erstellt",
				selenium.isTextPresent("Car Diagnosis"));
		
		//Solutions-Pages, KB Page
		loadAndWait("link=Damaged idle speed system");
		doSelActionAndWait("editorarea", "type", 
				rb.getString("KnowWE.SeleniumTest.Damaged"));
		loadAndWait("ok");
		
		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Leaking air intake system");
		doSelActionAndWait("editorarea", "type", 
				rb.getString("KnowWE.SeleniumTest.Leaking"));
		loadAndWait("ok");
		
		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Clogged air filter");
		doSelActionAndWait("editorarea", "type", 
				rb.getString("KnowWE.SeleniumTest.Clogged"));
		loadAndWait("ok");
		
		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Bad ignition timing");
		doSelActionAndWait("editorarea", "type", 
				rb.getString("KnowWE.SeleniumTest.Bad"));
		loadAndWait("ok");
		
		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Empty battery");
		doSelActionAndWait("editorarea", "type", 
				rb.getString("KnowWE.SeleniumTest.Empty"));
		loadAndWait("ok");
		
		open(rb.getString("KnowWE.SeleniumTest.url") + "/Wiki.jsp?page=Selenium-Test");
		loadAndWait("link=Quick-Edit-Test");
		doSelActionAndWait("editorarea", "type",
				rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test"));
		loadAndWait("ok");
		
		loadAndWait("link=Car-Diagnosis-Test");
		loadAndWait("link=Car Diagnosis Compiled KB");
		doSelActionAndWait("editorarea", "type", 
				rb.getString("KnowWE.SeleniumTest.KB"));
		loadAndWait("ok");
		
		open(rb.getString("KnowWE.SeleniumTest.url") + "/Edit.jsp?page=CD-compiled-KB");
		loadAndWait("ok");
		loadAndWait("link=Car-Diagnosis-Test");		
	}
}
