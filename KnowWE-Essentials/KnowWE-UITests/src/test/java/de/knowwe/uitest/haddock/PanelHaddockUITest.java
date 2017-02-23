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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import de.knowwe.uitest.PanelUITest;
import de.knowwe.uitest.UITestConfig;
import de.knowwe.uitest.UITestUtils;
import de.knowwe.uitest.WikiTemplate;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Tests correct behavior of left and right panel for haddock template
 *
 * @author Jonas Müller
 * @created 06.10.16
 */
@RunWith(Parameterized.class)
public class PanelHaddockUITest extends PanelUITest {

	private final String browser;
	private final Platform os;
	private final WebDriver driver;

	private final static WikiTemplate TEMPLATE = WikiTemplate.haddock;
	private static final HashMap<UITestConfig, WebDriver> drivers = new HashMap<>();

	public PanelHaddockUITest(String browser, Platform os) throws IOException, InterruptedException {
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

	@Test
	public void testSidebarOnMediumWindow() throws InterruptedException {
		getDriver().manage().window().setSize(MEDIUM_SIZE);
		Thread.sleep(500);

		assertFalse(isSidebarVisible());
		assertTrue(isPageAlignedLeft());
		assertTrue(isPageAlignedRight());

		pressSidebarButton();
		assertTrue(isSidebarVisible());
		assertTrue(isPageAlignedLeftWithSidebar());
		assertTrue(isPageAlignedRight());

		pressSidebarButton();
		assertFalse(isSidebarVisible());
		assertTrue(isPageAlignedLeft());
		assertTrue(isPageAlignedRight());

		getDriver().manage().window().setSize(STANDARD_SIZE);
		Thread.sleep(500);

		assertTrue(isSidebarVisible());
		assertTrue(isPageAlignedLeftWithSidebar());
		assertTrue(isPageAlignedRight());
	}

	@Test
	public void testSideBarOnNarrowWindow() throws InterruptedException {
		getDriver().manage().window().setSize(NARROW_SIZE);
		Thread.sleep(500);

		assertFalse(isSidebarVisible());
		assertTrue(isPageAlignedLeft());
		assertTrue(isPageAlignedRight());

		pressSidebarButton();
		assertTrue(isSidebarVisible());
		int tolerance = UITestUtils.getWebOS(getDriver()) == UITestUtils.WebOS.windows ? 30 : 0; //Scrollbars
		assertEquals("Sidebar should take complete window width on narrow window", getSidebar().getSize()
				.getWidth(), getDriver().manage().window().getSize().getWidth(), tolerance);

		pressSidebarButton();
		assertFalse(isSidebarVisible());
		assertTrue(isPageAlignedLeft());
		assertTrue(isPageAlignedRight());

		getDriver().manage().window().setSize(STANDARD_SIZE);
		Thread.sleep(500);

		assertTrue(isSidebarVisible());
		assertTrue(isPageAlignedLeftWithSidebar());
		assertTrue(isPageAlignedRight());
	}

	@Test
	public void testRightPanelOnNarrowWindow() throws InterruptedException {
		if (!isRightPanelVisible()) pressRightPanelButton();

		getDriver().manage().window().setSize(NARROW_SIZE);
		Thread.sleep(500);

		assertTrue(isRightPanelVisible());
		int rightPanelWidth = Integer.parseInt(getRightPanel().getCssValue("width").replaceAll("px", ""));
		int windowWidth = getDriver().manage().window().getSize().getWidth();
		int tolerance = UITestUtils.getWebOS(getDriver()) == UITestUtils.WebOS.windows ? 30 : 0; //Scrollbars
		assertEquals("Right Panel should have full width", rightPanelWidth, windowWidth, tolerance);
		assertTrue("Right Panel should be fixed", getRightPanel().getCssValue("position").equals("fixed"));
		assertTrue("Right Panel should be on bottom of the page", Integer.parseInt(getRightPanel().getCssValue("bottom")
				.replaceAll("px", "")) == 0);

		for (int i = 0; i < 10; i++) {
			addWatchDummy();
			Thread.sleep(100);
		}
		scrollToBottom();

		int watchesBottom = getRightPanel().getLocation().getY() + getRightPanel().getSize().getHeight();
		// One pixel tolerance due to rounding differences
		assertEquals("Right Panel should end exactly above footer", watchesBottom, getFooterTop(), 1);

		scrollToTop();
		getDriver().manage().window().setSize(STANDARD_SIZE);
		Thread.sleep(500);

	}

	@Test
	public void testRightPanelCollapseWatchesOnNarrowWindow() throws InterruptedException {
		getDriver().manage().window().setSize(NARROW_SIZE);
		Thread.sleep(500);

		if (!isRightPanelVisible()) pressRightPanelButton();
		assertTrue(isRightPanelVisible());
		WebElement watches = getRightPanel().findElement(By.id("watches"));
		WebElement watchesTitle = watches.findElement(By.className("title"));
		watchesTitle.click();
		Thread.sleep(500);
		WebElement watchesContent = watches.findElement(By.className("right-panel-content"));
		assertEquals("Watches shoud be collapsed", watchesContent.getCssValue("display"), "none");
		watches.findElement(By.className("title")).click();
		Thread.sleep(500);
		assertThat(watchesContent.getCssValue("display"), is(not("none")));
		pressRightPanelButton();
		getDriver().manage().window().setSize(STANDARD_SIZE);
		Thread.sleep(500);

	}

	@Test
	public void testSidebarAndRightPanelOnMediumWindow() throws InterruptedException {
		getDriver().manage().window().setSize(MEDIUM_SIZE);
		Thread.sleep(500);

		assertFalse(isSidebarVisible());
		assertFalse(isRightPanelVisible());
		assertTrue(isPageAlignedLeft());
		assertTrue(isPageAlignedRight());

		pressSidebarButton();
		assertTrue(isSidebarVisible());
		assertFalse(isRightPanelVisible());
		assertTrue(isPageAlignedLeftWithSidebar());
		assertTrue(isPageAlignedRight());

		pressRightPanelButton();
		assertTrue(isSidebarVisible());
		assertTrue(isRightPanelVisible());
		assertTrue(isPageAlignedLeftWithSidebar());
		assertTrue(isPageAlignedRightWithRightPanel());

		pressSidebarButton();
		assertFalse(isSidebarVisible());
		assertTrue(isRightPanelVisible());
		assertTrue(isPageAlignedLeft());
		assertTrue(isPageAlignedRightWithRightPanel());

		pressSidebarButton();
		assertTrue(isSidebarVisible());
		assertTrue(isRightPanelVisible());
		assertTrue(isPageAlignedLeftWithSidebar());
		assertTrue(isPageAlignedRightWithRightPanel());

		pressRightPanelButton();
		assertTrue(isSidebarVisible());
		assertFalse(isRightPanelVisible());
		assertTrue(isPageAlignedLeftWithSidebar());
		assertTrue(isPageAlignedRight());

		getDriver().manage().window().setSize(STANDARD_SIZE);
		Thread.sleep(500);
	}

	@Test
	public void testSidebarAndRightPanelOnSmallWindow() throws InterruptedException {
		getDriver().manage().window().setSize(NARROW_SIZE);
		Thread.sleep(500);

		assertFalse(isSidebarVisible());
		assertFalse(isRightPanelVisible());
		assertTrue(isPageAlignedLeft());
		assertTrue(isPageAlignedRight());

		pressRightPanelButton();
		assertFalse(isSidebarVisible());
		assertTrue(isRightPanelVisible());

		pressSidebarButton();
		assertTrue(isSidebarVisible());
		assertFalse(isRightPanelVisible());

		pressSidebarButton();
		assertFalse(isSidebarVisible());
		assertTrue(isRightPanelVisible());

		pressRightPanelButton();
		assertFalse(isSidebarVisible());
		assertFalse(isRightPanelVisible());
		assertTrue(isPageAlignedLeft());
		assertTrue(isPageAlignedRight());

		getDriver().manage().window().setSize(STANDARD_SIZE);
		Thread.sleep(500);
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
	protected void pressSidebarButton() throws InterruptedException {
		find(By.id("menu")).click();
		Thread.sleep(500); // Wait for Animation
	}

	@Override
	protected WebElement getSidebar() {
		return getDriver().findElement(By.cssSelector(".sidebar"));
	}

	@Override
	protected int getHeaderBottom() {
		int headerHeight = getDriver().findElement(By.className("header")).getSize().getHeight();
		JavascriptExecutor executor = (JavascriptExecutor) getDriver();
		Long scrollY = (Long) executor.executeScript("return window.scrollY;");
		headerHeight = (int) Math.max(0, headerHeight - scrollY);
		// -1 because header and sticky row are overlapping
		headerHeight += getDriver().findElement(By.cssSelector("div.row.sticky")).getSize().getHeight() - 1;
		return headerHeight;
	}

	@Override
	protected int getFooterTop() {
		return getDriver().findElement(By.className("footer")).getLocation().getY();
	}

	@Override
	protected boolean isPageAlignedLeft() {
		return Integer.parseInt(getDriver().findElement(By.className("page"))
				.getCssValue("margin-left")
				.replaceAll("px", "")) == 0;
	}

	@Override
	protected boolean isPageAlignedLeftWithSidebar() {
		int sidebarWidth = Integer.parseInt(getSidebar().getCssValue("width").replaceAll("px", ""));
		int pageMarginLeft = Integer.parseInt(getDriver().findElement(By.className("page"))
				.getCssValue("margin-left")
				.replaceAll("px", ""));
		return pageMarginLeft >= sidebarWidth;
	}

	@Override
	protected boolean isPageAlignedRight() {
		return Integer.parseInt(getDriver().findElement(By.className("page-content"))
				.getCssValue("margin-right")
				.replaceAll("px", "")) == 0;
	}

	@Override
	protected boolean isPageAlignedRightWithRightPanel() {
		int rightPanelWidth = Integer.parseInt(getRightPanel().getCssValue("width").replaceAll("px", ""));
		int pageContentMarginRight = Integer.parseInt(getDriver().findElement(By.className("page-content"))
				.getCssValue("margin-right")
				.replaceAll("px", ""));
		return pageContentMarginRight >= rightPanelWidth;
	}

	@Override
	public String getTestName() {
		return "UI-Test-" + super.getTestName() + "-" + TEMPLATE + "-" + browser + "-" + os;
	}

}
