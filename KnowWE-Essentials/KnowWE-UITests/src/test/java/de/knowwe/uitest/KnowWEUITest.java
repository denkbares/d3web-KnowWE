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

import org.junit.Before;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Jonas Müller
 * @created 06.10.16
 */
public abstract class KnowWEUITest {

	public static final String RESOURCE_DIR = "src/test/resources/";

	protected abstract WikiTemplate getTemplate();

	protected abstract WebDriver getDriver();

	/**
	 * If you set devMode to true, you can test locally, which will be much faster
	 * Don't commit this as true, because Jenkins build WILL fail!
	 * <p>
	 * To test locally, you also need to download the ChromeDriver from
	 * https://sites.google.com/a/chromium.org/chromegetDriver()/downloads
	 * and start it on your machine. Also, you need a locally running KnowWE with a page "ST-BMI".
	 * State of the page does not matter, it will be cleared for each new test.
	 */
	protected abstract boolean isDevMode();


	public abstract String getTestName();

	@Before
	public void load() throws Exception {
		if (isDevMode()) {
			getDriver().get("http://localhost:8080/KnowWE/Wiki.jsp?page=" + getTestName());
		} else {
			getDriver().get("https://knowwe-nightly.denkbares.com/Wiki.jsp?page=" + getTestName());
			logIn();
		}
	}

	protected void logIn() {
		UITestUtils.logIn(getDriver(), "UiTest", "fyyWWyVeHzzHfkUMZxUQ?3nDBPbTT6", UITestUtils.UseCase.NORMAL_PAGE);
	}

	protected void changeArticleText(String newText) {
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.id("edit-source-button")));
		getDriver().findElement(By.id("edit-source-button")).click();
		String areaSelector = getTemplate() == WikiTemplate.haddock ?  ".editor.form-control" : "#editorarea";
		List<WebElement> editorAreas = new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(areaSelector)));
		if (getDriver() instanceof JavascriptExecutor) {
			// hacky but fast/instant!
			((JavascriptExecutor) getDriver()).executeScript("var areas = document.querySelectorAll('" + areaSelector + "');" +
					"for (var i=0; i<areas.length; i++) { areas[i].value = arguments[0] };", newText);
		} else {
			// sets the keys one by one, pretty slow...
			editorAreas.forEach(webElement -> webElement.clear());
			editorAreas.forEach(webElement -> webElement.sendKeys(newText));
		}
		getDriver().findElement(By.name("ok")).click();
	}

	protected void checkNoErrorsExist() {
		assertEquals(0, getDriver().findElements(By.className("error")).size());
	}

	protected void checkErrorsExist() {
		assertFalse(getDriver().findElements(By.className("error")).isEmpty());
	}

}
