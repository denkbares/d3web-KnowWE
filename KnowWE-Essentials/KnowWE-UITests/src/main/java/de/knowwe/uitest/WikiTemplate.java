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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Interface representing a WikiTemplate with some template specific methods.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 24.03.17
 */
public interface WikiTemplate {

	default WebElement getRightPanel(WebDriver driver) {
		return driver.findElement(By.id("rightPanel"));
	}

	void pressSidebarButton(WebDriver driver);

	WebElement getSidebar(WebDriver driver);

	int getHeaderBottom(WebDriver driver);

	int getFooterTop(WebDriver driver);

	boolean isPageAlignedLeft(WebDriver driver);

	boolean isPageAlignedLeftWithSidebar(WebDriver driver);

	boolean isPageAlignedRight(WebDriver driver);

	boolean isPageAlignedRightWithRightPanel(WebDriver driver);

	void login(WebDriver driver, UITestUtils.UseCase use, String username, String password);
}
