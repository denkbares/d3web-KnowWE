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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.denkbares.test.RetryRule;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Tests correct behavior of left and right panel
 *
 * @author Jonas Müller
 * @created 06.10.16
 */
@RunWith(Parameterized.class)
public class PanelUITest extends KnowWEUITest {

	private final Dimension STANDARD_SIZE = new Dimension(1024, 768);
	private final Dimension MEDIUM_SIZE = new Dimension(768, 768);
	private final Dimension NARROW_SIZE = new Dimension(400, 768);

	public PanelUITest(UITestUtils.Browser browser, Platform os, WikiTemplate template) throws IOException, InterruptedException {
		super(browser, os, template);
	}

	@Parameterized.Parameters(name="{index}: UITest-Panel-{2}-{0}-{1})")
	public static Collection<Object[]> parameters() {
		return UITestUtils.getTestParametersChromeAndFireFox();
	}

	@Override
	public String getArticleName() {
		return "Panel";
	}

	@Rule
	public RetryRule retry = new RetryRule(2);

	@After
	public void restoreDefault() throws InterruptedException {
		getDriver().manage().window().setSize(STANDARD_SIZE);
		scrollToTop();
		clearWatches();
		Thread.sleep(500);
		if (!isSidebarVisible()) pressSidebarButton();
		if (isRightPanelVisible()) pressRightPanelButton();
	}

	@Test
	public void testSidebarToggleButton() throws InterruptedException {
		pressSidebarButton();
		assertFalse("Sidebar toggle button does not work correctly", isSidebarVisible());
		assertTrue(isPageAlignedLeft());
		assertTrue(isPageAlignedRight());

		pressSidebarButton();
		assertTrue("Sidebar toggle button does not work correctly", isSidebarVisible());
		assertTrue(isPageAlignedLeftWithSidebar());
		assertTrue(isPageAlignedRight());
	}

	@Test
	public void testRightPanelToggleButton() throws InterruptedException {

		pressRightPanelButton();
		assertTrue("RightPanel toggle button does not work correctly", isRightPanelVisible());
		assertTrue(isPageAlignedRightWithRightPanel());
		assertTrue(isPageAlignedLeftWithSidebar());

		pressRightPanelButton();
		assertFalse("RightPanel toggle button does not work correctly", isRightPanelVisible());
		assertTrue(isPageAlignedRight());
		assertTrue(isPageAlignedLeftWithSidebar());
	}

	@Test
	public void testRightPanelCollapseWatches() throws InterruptedException {
		pressRightPanelButton();
		WebElement watches = getRightPanel().findElement(By.id("watches"));
		watches.findElement(By.className("title")).click();
		Thread.sleep(500);
		WebElement watchesContent = watches.findElement(By.className("right-panel-content"));
		assertEquals("Watches shoud be collapsed", watchesContent.getCssValue("display"), "none");
		watches.findElement(By.className("title")).click();
		Thread.sleep(500);
		assertThat(watchesContent.getCssValue("display"), is(not("none")));
		pressRightPanelButton();
	}

	@Test
	public void testSidebarAndRightPanel() throws InterruptedException {
		assertTrue("Sidebar should be visible at the beginning", isSidebarVisible());
		assertFalse("Right Panel should not be visible at the beginning", isRightPanelVisible());

		pressRightPanelButton();
		assertTrue("Sidebar should be visible after showing right panel", isSidebarVisible());
		assertTrue("Right panel should be visible after pressing its button", isRightPanelVisible());
		assertTrue("Page content should be aligned correctly with sidebar visible", isPageAlignedLeftWithSidebar());
		assertTrue("Page content should be aligned correctly with right panel visible", isPageAlignedRightWithRightPanel());

		pressSidebarButton();
		assertFalse("Sidebar should be hidden after pressing its button", isSidebarVisible());
		assertTrue("Right panel should be visible after hiding sidebar", isRightPanelVisible());
		assertTrue("Page content should be aligned correctly without sidebar visible", isPageAlignedLeft());
		assertTrue("Page content should be aligned correctly with right panel visible", isPageAlignedRightWithRightPanel());

		pressSidebarButton();
		assertTrue("Sidebar should be visible after pressing its button", isSidebarVisible());
		assertTrue("Right panel should be visible after showing sidebar", isRightPanelVisible());
		assertTrue("Page content should be aligned correctly after showing sidebar", isPageAlignedLeftWithSidebar());
		assertTrue("Page content should be aligned correctly with right panel visible after showing sidebar", isPageAlignedRightWithRightPanel());

		pressRightPanelButton();
		assertTrue("Sidebar should be visible after hiding right panel", isSidebarVisible());
		assertFalse("Right panel should not be visible after pressing its button", isRightPanelVisible());
		assertTrue("Page content should be aligned correctly with sidebar after hiding right panel", isPageAlignedLeftWithSidebar());
		assertTrue("Page content should be aligned correctly without right panel after hiding right panel", isPageAlignedRight());
	}

	@Test
	public void testScrollingHalfPage() throws InterruptedException {
		pressRightPanelButton();

		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		Long halfDocumentHeight = ((Long) jse.executeScript("return document.body.scrollHeight;")) / 2;
		scrollTo(0 , Math.toIntExact(halfDocumentHeight));

		assertEquals("Sidebar is supposed to be fixed after scrolling half page", getSidebar().getCssValue("position"), "fixed");
		assertEquals("RightPanel is supposed to be fixed after scrolling half page", getSidebar().getCssValue("position"), "fixed");


		int topSidebar = Integer.parseInt(getSidebar().getCssValue("top").replace("px", ""));
		int topRightPanel = Integer.parseInt(getRightPanel().getCssValue("top").replace("px", ""));

		assertEquals("Sidebar should not overlay with top bar", topSidebar, getHeaderBottom(), 1);
		assertEquals("RightPanel should not overlax with top bar", topRightPanel, getHeaderBottom(), 1);

		scrollToTop();

		int sidebarPosY = getSidebar().getLocation().getY();
		int rightPanelPosY = getRightPanel().getLocation().getY();
		assertEquals("Sidebar should not overlay with header after scrolling to top", Math.abs(sidebarPosY - getHeaderBottom()), 0, 1);
		assertEquals("RightPanel should not overlay with header after scrolling to top", Math.abs(rightPanelPosY - getHeaderBottom()), 0, 1);
	}

	@Test
	public void testScrollingFullPage() throws InterruptedException {
		pressRightPanelButton();

		for (int i = 0; i < 10; i++) {
			addWatchDummy();
			Thread.sleep(100);
		}

		getDriver().manage().window().setSize(new Dimension(1024, 250));

		scrollToBottom();

		int sidebarBottom = getSidebar().getLocation().getY() + getSidebar().getSize().getHeight();
		int rightPanelBottom = getRightPanel().getLocation().getY() + getRightPanel().getSize().getHeight();

		assertTrue("Sidebar's bottom should be visible on bottom of page", sidebarBottom <= getFooterTop());
		assertTrue("RightPanel's bottom should be visible on bottom of page", rightPanelBottom <= getFooterTop());

		getDriver().manage().window().setSize(new Dimension(1024, 768));

		scrollToTop();
		Thread.sleep(100);

		int sidebarPosY = getSidebar().getLocation().getY();
		int rightPanelPosY = getRightPanel().getLocation().getY();
		assertTrue("Sidebar should not overlay with header after scrolling to top", (sidebarPosY + 5) >= getHeaderBottom());
		assertTrue("RightPanel should not overlay with header after scrolling to top", rightPanelPosY >= getHeaderBottom());

	}

	private void pressRightPanelButton() throws InterruptedException {
		String idRightPanel = "rightPanel-toggle-button";
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.id(idRightPanel)));
		getDriver().findElement(By.id(idRightPanel)).click();
		Thread.sleep(500); // Wait for Animation
	}

	private boolean isSidebarVisible() throws InterruptedException {
		int x = getSidebar().getLocation().getX();
		int width = getSidebar().getSize().getWidth();
		int paddingLeft = Integer.parseInt(getSidebar().getCssValue("padding-left").replace("px", ""));
		int paddingRight = Integer.parseInt(getSidebar().getCssValue("padding-right").replace("px", ""));
		int innerWidth = width - (paddingLeft + paddingRight);
		if (x == 0 && width >= 250) {
			return true;
		} else if (x <= 0 && innerWidth <= Math.abs(x)) {
			return false;
		} else {
			fail("Sidebar is neither completely visible nor completely invisible\nPosX = " + x + "| width = " + width);
			return false;
		}
	}

	private boolean isRightPanelVisible() {
		WebElement rightPanel;
		try {
			rightPanel = getRightPanel();
		} catch (NoSuchElementException e) {
			return false;
		}
		int width = rightPanel.getSize().getWidth();
		int windowWidth = getDriver().manage().window().getSize().getWidth();
		int xStart = rightPanel.getLocation().getX();
		int xEnd = xStart + width;

		if (width > 0 || xEnd == windowWidth) {
			return true;
		} else if (xStart >= windowWidth || width <= 0) {
			return false;
		} else {
			fail("Right Panel is not shown correctly");
			return false;
		}
	}

	private void addWatchDummy() throws InterruptedException {
		getRightPanel().findElement(By.className("addwatch")).click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfNestedElementLocatedBy(getRightPanel(), By.tagName("textarea")));
		getRightPanel().findElement(By.tagName("textarea")).sendKeys("Test");
		getRightPanel().findElement(By.tagName("textarea")).sendKeys(Keys.ENTER);
	}

	private void clearWatches() {
		List<WebElement> watchesContent = getDriver().findElements(By.className("watchlistentry"));
		for (WebElement watchListEntry : watchesContent) {
			getDriver().findElement(By.className("deletewatch")).click();
		}
	}

	private void scrollToBottom() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		Long documentHeight = ((Long) jse.executeScript("return Math.max(" +
				"document.body.scrollHeight, document.documentElement.scrollHeight," +
				"document.body.offsetHeight, document.documentElement.offsetHeight," +
				"document.body.clientHeight, document.documentElement.clientHeight" +
				");"));
		scrollTo(0, Math.toIntExact(documentHeight));
	}

	private void scrollToTop() {
		scrollTo(0, 0);
	}

	private void scrollTo(int pixelX, int pixelY) {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		jse.executeScript("scroll(" + pixelX + ", " + pixelY + ");");
	}

	/**
	 * Haddock-specific test
	 */
	@Test
	public void testSidebarOnMediumWindow() throws InterruptedException {
		if (!(getTemplate() instanceof HaddockTemplate)) return;

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
		if (!(getTemplate() instanceof HaddockTemplate)) return;

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
		if (!(getTemplate() instanceof HaddockTemplate)) return;

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
			Thread.sleep(200);
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
		if (!(getTemplate() instanceof HaddockTemplate)) return;

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
		if (!(getTemplate() instanceof HaddockTemplate)) return;

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
		if (!(getTemplate() instanceof HaddockTemplate)) return;

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

	private WebElement getRightPanel() {
		return getTemplate().getRightPanel(getDriver());
	}

	private void pressSidebarButton()  {
		getTemplate().pressSidebarButton(getDriver());
	}

	private WebElement getSidebar() {
		return getTemplate().getSidebar(getDriver());
	}

	private int getHeaderBottom() {
		return getTemplate().getHeaderBottom(getDriver());
	}

	private int getFooterTop() {
		return getDriver().findElement(By.className("footer")).getLocation().getY();
	}

	private boolean isPageAlignedLeft() {
		return getTemplate().isPageAlignedLeft(getDriver());
	}

	private boolean isPageAlignedLeftWithSidebar() {
		return getTemplate().isPageAlignedLeftWithSidebar(getDriver());
	}

	private boolean isPageAlignedRight() {
		return getTemplate().isPageAlignedRight(getDriver());
	}

	private boolean isPageAlignedRightWithRightPanel() {
		return getTemplate().isPageAlignedRightWithRightPanel(getDriver());
	}

}
