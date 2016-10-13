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

package de.knowwe.uitest;

import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Edit class text
 *
 * @author Jonas Müller
 * @created 06.10.16
 */
public abstract class ChromePanelUITest extends PanelUITest {

	private static RemoteWebDriver driver;

	private static boolean devMode = true;

	@BeforeClass
	public static void setUp() throws Exception {
		driver = UITestUtils.setUp(devMode, DesiredCapabilities.chrome(), ChromePanelUITest.class.getSimpleName());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (!devMode) driver.quit();
	}

	@Override
	protected boolean isDevMode() {
		return devMode;
	}

	@Override
	protected RemoteWebDriver getDriver() {
		return driver;
	}

	@Override
	public String getTestName() {
		return "UITest-Panel-Chrome";
	}
}

