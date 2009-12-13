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

import java.text.SimpleDateFormat;
import java.util.Date;

public class CalendarTest extends KnowWETestCase {
	
	
	public void testNewCalendarEntry() throws Exception {
		openWindowBlank(rb.getString("KnowWE.SeleniumTest.url")
				+ "Wiki.jsp?page=Selenium-Docu", "KnowWE: Selenium-Docu");
		verifyTrue(selenium.getTitle().contains("KnowWE: Selenium-Docu"));
		loadAndWait("//div[@id='actionsTop']/ul/li[1]/a/span");
		
		Date d = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		String today = fmt.format(d);
		selenium.type("editorarea", selenium.getValue("editorarea") + 
				"\n\n<<" + today +  " MD Das ist der aktuelle Selenium-Test>>");
		loadAndWait("ok");
		
		loadAndWait("link=<< back");
		verifyEquals("KnowWE: Selenium-Test", selenium.getTitle());
		assertTrue("Neuer Kalendereintrag wurde nicht/nicht richtig in die Termin" +
				"uebersicht uebernommen",
				selenium.isTextPresent("aktuelle Selenium-Test")
				&& selenium.isTextPresent(today.split(" ")[0])
				&& selenium.isTextPresent(today.split(" ")[1]));
	}
}
