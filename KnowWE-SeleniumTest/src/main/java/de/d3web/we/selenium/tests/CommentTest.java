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

public class CommentTest extends KnowWETestCase{
	
	
	public void testNewCommentEntry() throws Exception{
		open(rb.getString("KnowWE.SeleniumTest.url") + "Wiki.jsp?page=Selenium-Test");
		loadAndWait("//img[@title='Comments on this Test?']");
		assertTrue(selenium.getTitle().contains("KnowWE: Selenium-Comment"));
		verifyTrue(selenium.isTextPresent("<< back"));
		type("text", "Das ist ein Selenium-Test");
		doSelActionAndWait("//div[@onclick='saveForumBox()']", "click");
		
		loadAndWait("link=<< back");
		verifyEquals("KnowWE: Selenium-Test", selenium.getTitle());
		
		loadAndWait("//img[@title='Comments on this Test?']");
		assertEquals("KnowWE: Selenium-Comment", selenium.getTitle());
		assertTrue("Neuer Beitrag wurde nicht/nicht richtig gespeichert",
				selenium.isTextPresent("Das ist ein Selenium-Test"));
		}
}
