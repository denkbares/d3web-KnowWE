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
public class ChromePanelDefaultUITest extends ChromePanelUITest {
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
		JavascriptExecutor executor = getDriver();
		Long scrollY = (Long) executor.executeScript("return window.scrollY;");
		headerHeight = (int) Math.max(0, headerHeight -  scrollY);
		return headerHeight;
	}

	@Override
	protected int getFooterTop() {
		JavascriptExecutor executor = getDriver();
		Long footerTop = (Long) executor.executeScript("return document.body.scrollHeight;");
		return Math.toIntExact(footerTop);
	}

	@Override
	protected boolean pageAlignedLeft() {
		//TODO
		return false;
	}

	@Override
	protected boolean pageAlignedLeftWithSidebar() {
		//TODO
		return false;
	}

	@Override
	protected boolean pageAlignedRight() {
		//TODO
		return false;
	}

	@Override
	protected boolean pageAlignedRightWithRightPanel() {
		//TODO
		return false;
	}
}
