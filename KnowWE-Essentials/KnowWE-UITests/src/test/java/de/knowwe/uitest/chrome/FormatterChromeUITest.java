/*
 * Copyright (C) 2016 denkbares GmbH, Germany
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
package de.knowwe.uitest.chrome;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import de.knowwe.uitest.FormatterUITest;
import de.knowwe.uitest.UITestUtils;

public abstract class FormatterChromeUITest extends FormatterUITest {

	private static RemoteWebDriver driver;

	@Override
	protected WebDriver getDriver() {
		return driver;
	}

	@BeforeClass
	public static void setUp() throws Exception {
		driver = UITestUtils.setUp(DesiredCapabilities.chrome(), FormatterChromeUITest.class.getSimpleName());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (!UITestUtils.getDevMode()) driver.quit();
	}

}
