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

package de.knowwe.uitest.standard;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.knowwe.uitest.PanelUITest;
import de.knowwe.uitest.UITestConfig;
import de.knowwe.uitest.UITestUtils;
import de.knowwe.uitest.WikiTemplate;
import de.knowwe.uitest.haddock.BMIHaddockUITest;

/**
 * Tests correct behavior of left and right panel for default template
 *
 * @author Jonas Müller
 * @created 06.10.16
 */
@RunWith(Parameterized.class)
public class PanelStandardUITest extends PanelUITest {

	private final String browser;
	private final Platform os;
	private final WebDriver driver;

	private final static WikiTemplate TEMPLATE = WikiTemplate.standard;
	private static final HashMap<UITestConfig, WebDriver> drivers = new HashMap<>();

	public PanelStandardUITest(String browser, Platform os) throws IOException, InterruptedException {
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
	protected void pressSidebarButton() throws InterruptedException {
		String idSidebarButton = "favorites-toggle-button";
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.id(idSidebarButton)));
		getDriver().findElement(By.id(idSidebarButton)).click();
		Thread.sleep(500); // Wait for animation
	}

	@Override
	protected WebElement getSidebar() {
		return getDriver().findElement(By.id("favorites"));
	}

	@Override
	protected int getHeaderBottom() {
		int headerHeight = getDriver().findElement(By.id("header")).getSize().getHeight();
		JavascriptExecutor executor = (JavascriptExecutor) getDriver();
		Long scrollY = (Long) executor.executeScript("return window.scrollY;");
		headerHeight = (int) Math.max(0, headerHeight - scrollY);
		return headerHeight;
	}

	@Override
	protected int getFooterTop() {
		JavascriptExecutor executor = (JavascriptExecutor) getDriver();
		Long footerTop = (Long) executor.executeScript("return Math.max(" +
				"document.body.scrollHeight, document.documentElement.scrollHeight," +
				"document.body.offsetHeight, document.documentElement.offsetHeight," +
				"document.body.clientHeight, document.documentElement.clientHeight" +
				");");
		return Math.toIntExact(footerTop);
	}

	@Override
	protected boolean isPageAlignedLeft() {
		return Integer.parseInt(getDriver().findElement(By.id("page"))
				.getCssValue("left")
				.replaceAll("px", "")) <= 5;
	}

	@Override
	protected boolean isPageAlignedLeftWithSidebar() {
		int sidebarWidth = getSidebar().getSize().getWidth();
		int pageLeft = Integer.parseInt(getDriver().findElement(By.id("page"))
				.getCssValue("left")
				.replaceAll("px", ""));
		return Math.abs(pageLeft - sidebarWidth) <= 5;
	}

	@Override
	protected boolean isPageAlignedRight() {
		int windowWidth = getDriver().manage().window().getSize().getWidth();
		int posXEnd = getDriver().findElement(By.id("pagecontent")).getLocation().getX();
		posXEnd += getDriver().findElement(By.id("pagecontent")).getSize().getWidth();
		UITestUtils.WebOS os = UITestUtils.getWebOS(getDriver());
		int tolerance = os == UITestUtils.WebOS.windows ? 35 : 10; //Scrollbar width
		return Math.abs(windowWidth - posXEnd) <= tolerance;
	}

	@Override
	protected boolean isPageAlignedRightWithRightPanel() {
		int posXEnd = getDriver().findElement(By.id("pagecontent")).getLocation().getX();
		posXEnd += getDriver().findElement(By.id("pagecontent")).getSize().getWidth();

		return posXEnd < getRightPanel().getLocation().getX();
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
