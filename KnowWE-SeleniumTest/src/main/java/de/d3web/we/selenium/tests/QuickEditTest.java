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


public class QuickEditTest extends KnowWETestCase{
	
	public void testQuickTableEditing() {
		
		final String divID = rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test.TableID");
		final String divLocator = "//div[@id='" + divID + "']";
		
		open(rb.getString("KnowWE.SeleniumTest.url") + "Wiki.jsp?page=Quick-Edit-Test");
		assertTrue(selenium.getTitle().contains("KnowWE: Quick-Edit-Test"));
		
		//If another session opened Quick-Edit-Mode close it
		if (selenium.isElementPresent(divID + "_cancel")) {
			doSelActionAndWait(divID + "_cancel", "click");
		}
		
		loadAndWait("//div[@id='actionsTop']/ul/li[1]/a");
		final String oldPageContent = selenium.getText("//textarea[@id='editorarea']");
		loadAndWait("//input[@name='ok']");

		
		final String oldCellValue = selenium.getText(divLocator + "//table/tbody/tr[4]/td[2]");
		
		doSelActionAndWait(divID +  "_pencil", "click");
		verifyFalse(selenium.isElementPresent(divID + "_pencil"));
		doSelActionAndWait(divID +  "_cancel", "click");
		
		//Test: Eingabe -> speichern
		doSelActionAndWait(divID +  "_pencil", "click");
		doSelActionAndWait(divLocator + "/table/tbody/tr[4]/td[2]/select", "select", "label=+");
		doSelActionAndWait(divID +  "_accept", "click");
		
		refreshAndWait();
		assertEquals("Eingabe wurde nicht/nicht richtig uebernommen", "+", selenium.getText(divLocator + "//table/tbody/tr[4]/td[2]"));
		
		//Test: Eingabe -> nicht speichern
		doSelActionAndWait(divID +  "_pencil", "click");
		doSelActionAndWait(divLocator + "//table/tbody/tr[2]/td[4]/select", "select", "label=+");
		doSelActionAndWait(divID +  "_cancel", "click");
		
		refreshAndWait();
		assertEquals("Eingabe wurde uebernommen obwohl abgebrochen wurde", "hm", selenium.getText(divLocator + "//table/tbody/tr[2]/td[4]"));
		
		//Geaenderten Wert zuruecksetzen -> Seiteninhalt sollte der gleiche wie Anfangs sein
		doSelActionAndWait(divID +  "_pencil", "click");
		doSelActionAndWait(divLocator + "//table/tbody/tr[4]/td[2]/select", "select", "label=" + oldCellValue);
		doSelActionAndWait(divID +  "_accept", "click");
		
		refreshAndWait();

		loadAndWait("//div[@id='actionsTop']/ul/li[1]/a");
		final String newPageContent = selenium.getText("//textarea[@id='editorarea']");
		loadAndWait("//input[@name='ok']");
		assertEquals("Es sind unerwuenschte Aenderungen im Inhalt aufgetreten",
				oldPageContent, newPageContent);
		
	}
	
	public void testQuickCoveringTableEditing() {
		//TODO write test
	}
}
