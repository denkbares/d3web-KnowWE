/*
 * Copyright (C) 2017 denkbares GmbH, Germany
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

package de.knowwe.uitest.haddock;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;

import de.knowwe.uitest.FormatterUITest;
import de.knowwe.uitest.UITestConfig;
import de.knowwe.uitest.UITestUtils;
import de.knowwe.uitest.WikiTemplate;
import de.knowwe.uitest.standard.FormatterStandardUITest;

/**
 * Edit class text
 *
 * @author Jonas Müller
 * @created 17.02.17
 */
@RunWith(Parameterized.class)
public class FormatterHaddockUITest extends FormatterUITest {

	private final String browser;
	private final Platform os;
	private final WebDriver driver;

	private final static WikiTemplate TEMPLATE = WikiTemplate.haddock;
	private static final HashMap<UITestConfig, WebDriver> drivers = new HashMap<>();

	public FormatterHaddockUITest(String browser, Platform os) throws IOException, InterruptedException {
		super();

		this.browser = browser;
		this.os = os;

		UITestConfig config = new UITestConfig(browser, os);
		if (drivers.get(config) != null) {
			driver = drivers.get(config);
		} else {
			for (WebDriver d : drivers.values()) {
				d.quit();
			}
			driver = UITestUtils.setUp(browser, BMIHaddockUITest.class.getSimpleName(), os, TEMPLATE, getTestName(), devMode);
			drivers.put(config, driver);
		}
	}

	@Parameterized.Parameters
	public static LinkedList<Object[]> parameters() {
		return UITestUtils.getTestParameters();
	}

	@Override
	protected WikiTemplate getTemplate() {
		return TEMPLATE;
	}

	@Override
	protected WebDriver getDriver() {
		return driver;
	}

	@Override
	public String getTestName() {
		return "UI-Test-" + super.getTestName() + "-" + TEMPLATE + "-" + browser + "-" + os;
	}
}