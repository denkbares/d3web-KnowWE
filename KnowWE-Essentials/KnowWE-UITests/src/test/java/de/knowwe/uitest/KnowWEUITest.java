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
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.denkbares.strings.Strings;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Jonas Müller
 * @created 06.10.16
 */
public abstract class KnowWEUITest {

	/**
	 * In order to test locally set the following dev mode parameters
	 * -Dknowwe.devMode="true"
	 * -Dknowwe.haddock.url="your-haddock-URL"
	 * -Dknowwe.standard.url="your-standardTemplate-URL"
	 */
	protected boolean devMode;
	protected String knowWeUrl;

	public KnowWEUITest() {
		devMode = Boolean.parseBoolean(System.getProperty("knowwe.devMode", "false"));
		knowWeUrl = UITestUtils.getKnowWEUrl(getTemplate(), getTestName(), devMode);
	}

	public static final String RESOURCE_DIR = "src/test/resources/";

	protected abstract WikiTemplate getTemplate();

	protected abstract WebDriver getDriver();

	/**
	 * If you set DEV_MODE to true, you can test locally, which will be much faster
	 * Don't commit this as true, because Jenkins build WILL fail!
	 * <p>
	 * To test locally, you also need to download the ChromeDriver from
	 * https://sites.google.com/a/chromium.org/chromegetDriver()/downloads
	 * and start it on your machine.
	 * State of the page does not matter, it will be cleared for each new test.
	 * <p>
	 * In order to test locally set the following dev mode parameters
	 * -Dknowwe.devMode="true"
	 * -Dknowwe.url="your-URL"
	 */
	public abstract String getTestName();

	protected void changeArticleText(String newText) {
		try {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.id("edit-source-button")));
		} catch (Exception e) {
			someoneEditedPageWorkaround();
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.id("edit-source-button")));
		}
		getDriver().findElement(By.id("edit-source-button")).click();
		UITestUtils.enterArticleText(newText, getDriver(), getTemplate());
	}

	private void someoneEditedPageWorkaround() {
		if (getTemplate() == WikiTemplate.haddock) {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.className("error")));
			Optional oops = getDriver().findElements(By.cssSelector("h4"))
					.stream()
					.filter(webElement -> Strings.containsIgnoreCase(webElement.getText(), "Oops!"))
					.findFirst();
			if (oops.isPresent()) {
				getDriver().findElement(By.cssSelector("a.btn.btn-primary.btn-block")).click();
			}
		} else {
			new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated(By.id("conflict")));
			WebElement conflict = getDriver().findElement(By.id("conflict"));
			conflict.findElement(By.cssSelector("a")).click();
		}
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfElementLocated((By.name("ok"))));
		getDriver().findElement(By.name("ok")).click();
	}

	protected void checkNoErrorsExist() {
		assertEquals(0, getDriver().findElements(By.className("error")).size());
	}

	protected void checkErrorsExist() {
		assertFalse(getDriver().findElements(By.className("error")).isEmpty());
	}

	protected WebElement find(By selector) {
		return getDriver().findElement(selector);
	}

	protected void waitUntilPresent(By selector) {
		await().until(ExpectedConditions.presenceOfElementLocated(selector));
	}

	protected WebElement waitUntilClickable(By selector) {
		return await().until(ExpectedConditions.elementToBeClickable(selector));
	}

	@NotNull
	protected WebDriverWait await() {
		return await(10);
	}

	@NotNull
	protected WebDriverWait await(int timeOutInSeconds) {
		return new WebDriverWait(getDriver(), timeOutInSeconds);
	}

	protected WebElement waitUntilVisible(By selector) {
		return await().until(ExpectedConditions.visibilityOfElementLocated(selector));
	}

	protected void moveMouseTo(By selector) {
		new Actions(getDriver()).moveToElement(getDriver().findElement(selector));
	}

	protected String readFile(String fileName) throws IOException {
		return Strings.readFile(RESOURCE_DIR + fileName)
				.replace("%%package systemtest", "%%package systemtest" + getTestName());
	}

}
