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

import java.text.SimpleDateFormat;
import java.util.Date;

import de.d3web.we.selenium.main.KnowWETestCase;

public class CalendarTest extends KnowWETestCase {

	public void testNewCalendarEntry() {
		open(rb.getString("KnowWE.SeleniumTest.url") + "Wiki.jsp?page=Selenium-Test");
		loadAndWait("//img[@title='Add some appointments']");
		verifyTrue(selenium.getTitle().contains("Selenium-Docu"));
		loadAndWait(B_EDIT);

		Date d = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		String today = fmt.format(d);
		doSelActionAndWait(EA, "type", selenium.getValue(EA) +
				"\n\n<&<" + today + " MD Das ist der aktuelle Selenium-Test>&>");
		loadAndWait(B_SAVE);

		loadAndWait("link=<< back");
		verifyEquals("KnowWE: Selenium-Test", selenium.getTitle());
		assertTrue("Neuer Kalendereintrag wurde nicht/nicht richtig in die Termin" +
				"uebersicht uebernommen",
				selenium.isTextPresent("aktuelle Selenium-Test")
						&& selenium.isTextPresent(today.split(" ")[0])
						&& selenium.isTextPresent(today.split(" ")[1]));
	}

}
