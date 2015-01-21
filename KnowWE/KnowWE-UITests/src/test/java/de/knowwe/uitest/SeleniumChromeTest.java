/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.uitest;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Veronika Sehne on 07.01.15.
 */
public class SeleniumChromeTest {
	private Selenium selenium;

	@Before
	public void setUp() throws Exception {
		selenium = new DefaultSelenium("localhost", 4444, "*googlechrome", "http://www.d3web.de/");
		selenium.start();
	}

	@Test
	public void testBmi() throws Exception {
		selenium.open("/Wiki.jsp?page=Body-Mass-Index");
		selenium.click("css=div.reset.pointer");
		selenium.click("//div[@id='group_quicki1']/div[2]");
		selenium.type("id=input_quicki2", "2");
		selenium.type("id=input_quicki3", "100");
		selenium.click("id=input_quicki4");
		assertEquals("25", selenium.getValue("id=input_quicki4"));
		assertTrue(selenium.isElementPresent("link=Normal weight"));
		selenium.click("css=div.reset.pointer");
		assertEquals("", selenium.getText("id=input_quicki4"));
	}

	@After
	public void tearDown() throws Exception {
		selenium.stop();
	}
}