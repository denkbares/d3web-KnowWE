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
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.denkbares.strings.Strings;
import de.knowwe.uitest.UITestUtils.Browser;
import de.knowwe.uitest.UITestUtils.TestMode;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Jonas Müller
 * @created 06.10.16
 */
public abstract class KnowWEUITest {

	private final TestMode testMode;
	private final String knowWeUrl;
	private final RemoteWebDriver driver;
	private final Browser browser;
	private final Platform os;
	private final WikiTemplate template;

	public static final String RESOURCE_DIR = "src/test/resources/";

	/**
	 * In order to test locally set the following dev mode parameters
	 * -Dknowwe.testMode="true"
	 * -Dknowwe.haddock.url="your-haddock-URL"
	 * -Dknowwe.standard.url="your-standardTemplate-URL"
	 */
	public KnowWEUITest(Browser browser, Platform os, WikiTemplate template) throws IOException, InterruptedException {
		this(browser, os, template, true, null);
	}

	public KnowWEUITest(Browser browser, Platform os, WikiTemplate template, boolean login, Function<String, String> urlConstructor) throws IOException, InterruptedException {
		this.browser = browser;
		this.os = os;
		this.template = template;
		// for backwards compatibility, use knowwwe.testMode instead
		boolean devMode = Boolean.parseBoolean(System.getProperty("knowwe.devMode", "false"));
		testMode = devMode ? TestMode.local : TestMode.valueOf(System.getProperty("knowwe.testMode", "local"));
		if (urlConstructor == null) {
			urlConstructor = (String article) -> UITestUtils.getKnowWEUrl(getTemplate(), article);
		}

		knowWeUrl = urlConstructor.apply(getArticleName());

		//driver.get(UITestUtils.getKnowWEUrl(template, "Main"));;
		driver = UITestUtils.setUp(browser, os, template, getArticleName(), testMode, knowWeUrl, login, urlConstructor);
	}

	@After
	public void after() {
		driver.quit();
	}

	protected WikiTemplate getTemplate() {
		return template;
	}

	public WebDriver getDriver() {
		return driver;
	}

	public abstract String getArticleName();

	protected void changeArticleText(String newText) {
		this.changeArticleText(newText, true);
	}

	protected void changeArticleText(String newText, boolean replacePackage) {
		try {
			waitUntilPresent(By.id("edit-source-button"));
		}
		catch (Exception e) {
			someoneEditedPageWorkaround();
			waitUntilPresent(By.id("edit-source-button"));
		}
		getDriver().findElement(By.id("edit-source-button")).click();
		if (replacePackage) {
			newText = newText.replaceAll("(?i)(%%Package\\s+)",
					"$1" + template + "-" + browser + "-" + os.toString().toLowerCase() + "-");
		}
		UITestUtils.enterArticleText(newText, getDriver(), getTemplate());
	}

	private void someoneEditedPageWorkaround() {
		if (getTemplate() instanceof HaddockTemplate) {
			waitUntilPresent(By.className("error"));
			Optional oops = getDriver().findElements(By.cssSelector("h4"))
					.stream()
					.filter(webElement -> Strings.containsIgnoreCase(webElement.getText(), "Oops!"))
					.findFirst();
			if (oops.isPresent()) {
				getDriver().findElement(By.cssSelector("a.btn.btn-primary.btn-block")).click();
			}
		}
		else {
			waitUntilPresent(By.id("conflict"));
			getDriver().findElement(By.id("conflict")).findElement(By.cssSelector("a")).click();
		}
		waitUntilPresent((By.name("ok")));
		getDriver().findElement(By.name("ok")).click();
	}

	protected void checkNoErrorsExist() {
		assertEquals(0, getDriver().findElements(By.className("error")).size());
	}

	protected void checkErrorsExist() {
		Assert.assertFalse(getDriver().findElements(By.className("error")).isEmpty());
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
				.replace("%%package systemtest", "%%package systemtest" + getArticleName());
	}

}
