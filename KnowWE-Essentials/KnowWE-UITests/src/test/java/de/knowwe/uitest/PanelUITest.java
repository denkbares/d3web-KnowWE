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

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests correct behavior of left and right panel
 *
 * @author Jonas Müller
 * @created 06.10.16
 */
public abstract class PanelUITest extends KnowWEUITest {

	protected final Dimension STANDARD_SIZE = new Dimension(1024, 768);
	protected final Dimension MEDIUM_SIZE = new Dimension(768, 768);
	protected final Dimension NARROW_SIZE = new Dimension(400, 768);

	@After
	public void restoreDefault() throws InterruptedException {
		getDriver().manage().window().setSize(STANDARD_SIZE);
		scrollToTop();
		clearWatches();
		if (!isSidebarVisible()) pressSidebarButton();
		if (isRightPanelVisible()) pressRightPanelButton();
	}

	@Test
	public void testSidebarToggleButton() throws InterruptedException {
		boolean sideBarWasVisible = isSidebarVisible();

		pressSidebarButton();
		assertEquals("Sidebar toggle button does not work correctly", !sideBarWasVisible, isSidebarVisible());
		if (isSidebarVisible()) assertTrue(pageAlignedLeftWithSidebar()); else assertTrue(pageAlignedLeft());

		pressSidebarButton();
		assertEquals("Sidebar toggle button does not work correctly", sideBarWasVisible, isSidebarVisible());
		if (isSidebarVisible()) assertTrue(pageAlignedLeftWithSidebar()); else assertTrue(pageAlignedLeft());
	}

	@Test
	public void testRightPanelToggleButton() throws InterruptedException {
		boolean rightPanelWasVisible = isRightPanelVisible();

		pressRightPanelButton();
		assertEquals("RightPanel toggle button does not work correctly", !rightPanelWasVisible, isRightPanelVisible());
		if (isRightPanelVisible()) assertTrue(pageAlignedRightWithRightPanel()); else assertTrue(pageAlignedRight());

		pressRightPanelButton();
		assertEquals("RightPanel toggle button does not work correctly", rightPanelWasVisible, isRightPanelVisible());
		if (isRightPanelVisible()) assertTrue(pageAlignedRightWithRightPanel()); else assertTrue(pageAlignedRight());
	}

	@Test
	public void testRightPanelCollapseWatches() throws InterruptedException {
		if (!isRightPanelVisible()) pressRightPanelButton();
		WebElement watches = getRightPanel().findElement(By.id("watches"));
		watches.findElement(By.className("title")).click();
		Thread.sleep(1000);
		WebElement watchesContent = watches.findElement(By.className("right-panel-content"));
		assertEquals("Watches shoud be collapsed", watchesContent.getCssValue("display"), "none");
		watches.findElement(By.className("title")).click();
		Thread.sleep(1000);
		assertThat(watchesContent.getCssValue("display"), is(not("none")));
		pressRightPanelButton();
	}

	@Test
	public void testSidebarAndRightPanel() throws InterruptedException {
		if (!isSidebarVisible()) pressSidebarButton();
		if (!isRightPanelVisible()) pressRightPanelButton();
		Thread.sleep(500);
		assertTrue(isSidebarVisible());
		assertTrue(isRightPanelVisible());

		Thread.sleep(500);
		pressSidebarButton();
		pressRightPanelButton();
		Thread.sleep(500);
		assertFalse(isSidebarVisible());
		assertFalse(isRightPanelVisible());

		Thread.sleep(500);
		pressRightPanelButton();
		pressSidebarButton();
		Thread.sleep(500);
		assertTrue(isSidebarVisible());
		assertTrue(isRightPanelVisible());

		Thread.sleep(500);
		pressSidebarButton();
		Thread.sleep(500);
		assertFalse(isSidebarVisible());
		assertTrue(isRightPanelVisible());

		Thread.sleep(500);
		pressSidebarButton();
		pressRightPanelButton();
		Thread.sleep(500);
		assertTrue(isSidebarVisible());
		assertFalse(isRightPanelVisible());
	}

	@Test
	public void testScrollingHalfPage() throws InterruptedException {
		if (!isSidebarVisible()) pressSidebarButton();
		if (!isRightPanelVisible()) pressRightPanelButton();

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
		assertTrue("Sidebar should not overlay with header after scrolling to top", sidebarPosY >= getHeaderBottom());
		assertTrue("RightPanel should not overlay with header after scrolling to top", rightPanelPosY >= getHeaderBottom());
	}

	@Test
	public void testScrollingFullPage() throws InterruptedException {
		if (!isSidebarVisible()) pressSidebarButton();
		if (!isRightPanelVisible()) pressRightPanelButton();
		Thread.sleep(1000);

		for (int i = 0; i < 18; i++) {
			addWatchDummy();
		}

		getDriver().manage().window().setSize(new Dimension(1024, 250));

		scrollToBottom();

		int sidebarBottom = getSidebar().getLocation().getY() + getSidebar().getSize().getHeight();
		int rightPanelBottom = getRightPanel().getLocation().getY() + getRightPanel().getSize().getHeight();

		assertTrue("Sidebar's bottom should be visible on bottom of page", sidebarBottom <= getFooterTop());
		assertTrue("RightPanel's bottom should be visible on bottom of page", rightPanelBottom <= getFooterTop());

		getDriver().manage().window().setSize(new Dimension(1024, 768));

		scrollToTop();

		int sidebarPosY = getSidebar().getLocation().getY();
		int rightPanelPosY = getRightPanel().getLocation().getY();
		assertTrue("Sidebar should not overlay with header after scrolling to top", sidebarPosY >= getHeaderBottom());
		assertTrue("RightPanel should not overlay with header after scrolling to top", rightPanelPosY >= getHeaderBottom());

		clearWatches();

	}

	protected abstract void pressSidebarButton();

	protected void pressRightPanelButton() {
		String idRightPanel = "rightPanel-toggle-button";
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.id(idRightPanel)));
		getDriver().findElement(By.id(idRightPanel)).click();
	}

	protected boolean isSidebarVisible() throws InterruptedException {
		Thread.sleep(1000);
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

	protected boolean isRightPanelVisible() throws InterruptedException {
		Thread.sleep(1000);
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

	protected abstract WebElement getSidebar();

	protected abstract int getHeaderBottom();

	protected abstract int getFooterTop();

	protected WebElement getRightPanel() {
		return getDriver().findElement(By.id("rightPanel"));
	}

	protected abstract boolean pageAlignedLeft();

	protected abstract boolean pageAlignedLeftWithSidebar();

	protected abstract boolean pageAlignedRight();

	protected abstract boolean pageAlignedRightWithRightPanel();

	protected void addWatchDummy() {
		getRightPanel().findElement(By.className("addwatch")).click();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.tagName("textarea")));
		getRightPanel().findElement(By.tagName("textarea")).sendKeys("Weight");
		getRightPanel().findElement(By.tagName("textarea")).sendKeys(Keys.ENTER);
	}

	protected void clearWatches() {
		List<WebElement> watchesContent = getDriver().findElements(By.className("watchlistentry"));
		for (WebElement watchListEntry : watchesContent) {
			getDriver().findElement(By.className("deletewatch")).click();
		}
	}

	protected void scrollToBottom() {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		Long documentHeight = ((Long) jse.executeScript("return document.body.scrollHeight;"));
		scrollTo(0, Math.toIntExact(documentHeight));
	}

	protected void scrollToTop() {
		scrollTo(0, 0);
	}

	protected void scrollTo(int pixelX, int pixelY) {
		JavascriptExecutor jse = (JavascriptExecutor) getDriver();
		jse.executeScript("scroll(" + pixelX + ", " + pixelY + ");");
	}

}
