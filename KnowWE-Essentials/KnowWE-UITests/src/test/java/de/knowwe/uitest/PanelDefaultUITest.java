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

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.fail;

/**
 * Edit class text
 *
 * @author Jonas Müller
 * @created 06.10.16
 */
public abstract class PanelDefaultUITest extends PanelUITest {
	@Override
	protected WikiTemplate getTemplate() {
		return WikiTemplate.standard;
	}

	@Override
	protected void pressSidebarButton() {
		String idSidebarButton = "favorites-toggle-button";
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.id(idSidebarButton)));
		getDriver().findElement(By.id(idSidebarButton)).click();
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
		Long footerTop = (Long) executor.executeScript("return document.body.scrollHeight;");
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
		int pageWidth = getDriver().findElement(By.id("page")).getSize().getWidth();
		int sideBarWidthVisible = getSidebar().getSize().getWidth();
		sideBarWidthVisible += Integer.parseInt(getSidebar().getCssValue("left").replaceAll("px", ""));
		int windowWidth = getDriver().manage().window().getSize().getWidth();
		int posXEnd = getDriver().findElement(By.id("pagecontent")).getLocation().getX();
		posXEnd += getDriver().findElement(By.id("pagecontent")).getSize().getWidth();
		return Math.abs(windowWidth - pageWidth - sideBarWidthVisible) <= 5
				&& Math.abs(windowWidth - posXEnd) <= 10;
	}

	@Override
	protected boolean isPageAlignedRightWithRightPanel() {
		int sideBarWidthVisible = getSidebar().getSize().getWidth();
		sideBarWidthVisible += Integer.parseInt(getSidebar().getCssValue("left").replaceAll("px", ""));
		int rightPanelWidth = getRightPanel().getSize().getWidth();
		int pageWidth = Integer.parseInt(getDriver().findElement(By.id("page")).getCssValue("width").replaceAll("px", ""));
		int windowWidth = getDriver().manage().window().getSize().getWidth();

		int posXEnd = getDriver().findElement(By.id("pagecontent")).getLocation().getX();
		posXEnd += getDriver().findElement(By.id("pagecontent")).getSize().getWidth();

		return Math.abs(windowWidth - sideBarWidthVisible - rightPanelWidth - pageWidth) <= 10
				&& posXEnd < getRightPanel().getLocation().getX();
	}
}
