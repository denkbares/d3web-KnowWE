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
package de.d3web.we.selenium.tests;

import com.thoughtworks.selenium.SeleniumException;

import de.d3web.we.selenium.main.StressTestCase;

/**
 * This StressTest simulates many changes in TimeEvents (Hermes-Plugin) with
 * QuickEdit to check the stability.
 * 
 * @author Max Diez
 * @created 12.10.2010
 */
public class TimeLineStressTest extends StressTestCase {

	final String URL = rb.getString("KnowWE.SeleniumTest.TimeLineEntries");
	final String TIMELINEID = "TimeLineEntries/RootType/TimeEventType";
	final String QEA = rb.getString("KnowWE.SeleniumTest.Quick-Edit-Test.EditorArea");

	/**
	 * Every TimeEvent on the wiki page TimeLineEntries gets the content of the
	 * one before. This will be repeated until the specified duration is
	 * expired. Afterwards the page's/server's accessibility is checked.
	 * 
	 * @created 13.10.2010
	 */
	public void testChangeAllEntries() {
		open(bundle.getString("KnowWE.SeleniumStressTest.TimeLine.page"));
		resetWikiPageContent(URL);
		
		long startTime = System.currentTimeMillis();
		String newContent = "<<Lamischer Krieg (2)\n2111v\nBla Blub\n>>";
		String oldContent;
		while (System.currentTimeMillis() - startTime < testDuration) {
			try {
				for (int i = 2; i < 304; i++) {
					// Open QE
					// If another session opened Quick-Edit-Mode -> close it
					if (selenium.isElementPresent(TIMELINEID + i + CANCEL)) {
						doSelActionAndWait(TIMELINEID + i + CANCEL, "click");
					}
					doSelActionAndWait(TIMELINEID + i + QEB, "click");
					// Kind of guaranteeing the completion of opening the QuickEdit
					waitForElement(TIMELINEID + i + CANCEL);
					
					// Add and save new content
					oldContent = selenium.getValue(TIMELINEID + i + QEA);
					doSelActionAndWait(TIMELINEID + i + QEA, "type", newContent);
					doSelActionAndWait(TIMELINEID + i + ACCEPT, "click");
					waitForElement(TIMELINEID + i);
					verifyTrue(selenium.getText(TIMELINEID + i).contains(newContent));
					newContent = oldContent;
				}
			}
			catch (SeleniumException se) {
				se.printStackTrace();
				break;
			}
			pageCheck(URL);
		}
	}

}
