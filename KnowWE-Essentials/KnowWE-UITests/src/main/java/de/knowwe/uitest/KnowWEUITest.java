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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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
	private final WebDriver driver;
	protected final Browser browser;
	protected final Platform os;
	private final WikiTemplate template;

	private static final String RESOURCE_DIR = "src/test/resources/";
	private static boolean acceptNextAlert = true;

	/**
	 * In order to test locally set the following dev mode parameters
	 * -Dknowwe.testMode="true"
	 * -Dknowwe.haddock.url="your-haddock-URL"
	 * -Dknowwe.standard.url="your-standardTemplate-URL"
	 */
	public KnowWEUITest(final Browser browser, final Platform os, final WikiTemplate template) throws IOException {
		this(browser, os, template, true, null);
	}

	public KnowWEUITest(final Browser browser, final Platform os, final WikiTemplate template, final boolean login, Function<String, String> urlConstructor) throws IOException {
		this.browser = browser;
		this.os = os;
		this.template = template;
		// for backwards compatibility, use knowwwe.testMode instead
		final boolean devMode = Boolean.parseBoolean(System.getProperty("knowwe.devMode", "false"));
		this.testMode = devMode ? TestMode.local : TestMode.valueOf(System.getProperty("knowwe.testMode", "local"));
		if (urlConstructor == null) {
			urlConstructor = (String article) -> UITestUtils.getKnowWEUrl(getTemplate(), article);
		}

		this.knowWeUrl = urlConstructor.apply(getArticleName());

		//driver.get(UITestUtils.getKnowWEUrl(template, "Main"));;
		this.driver = UITestUtils.setUp(browser, os, template, getArticleName(), this.testMode, this.knowWeUrl, login, urlConstructor);
	}

	/**
	 * This test watcher catches a failed ui test and saves a screen capture of the current page. For more details there
	 * is also saved a xml file of the page's html content. The files will be saved to the /tmp folder by default.
	 */
	@Rule
	public final TestRule watchman = new TestWatcher() {

		@Override
		protected void failed(final Throwable e, final Description description) {
			String className = description.getClassName();
			String methodName = description.getMethodName();
			String prefix = className.substring(className.lastIndexOf(".") + 1);
			String suffix = methodName.substring(0, methodName.lastIndexOf("["));
			String debugFolder = System.getProperty("ui.debug.folder");
			UITestUtils.generateDebugFiles(driver, debugFolder, prefix + "-" + suffix);
		}

		@Override
		protected void finished(final Description description) {
			KnowWEUITest.this.driver.quit();
		}
	};

	protected WikiTemplate getTemplate() {
		return this.template;
	}

	public WebDriver getDriver() {
		return this.driver;
	}

	public abstract String getArticleName();

	protected void changeArticleText(final String newText) {
		this.changeArticleText(newText, true);
	}

	protected void changeArticleText(String newText, final boolean replacePackage) {
		try {
			waitUntilPresent(By.id("edit-source-button"));
		}
		catch (final Exception e) {
			someoneEditedPageWorkaround();
			waitUntilPresent(By.id("edit-source-button"));
		}
		getDriver().findElement(By.id("edit-source-button")).click();
		if (replacePackage) {
			newText = newText.replaceAll("(?i)(%%Package\\s+)",
					"$1" + this.template + "-" + this.browser + "-" + this.os.toString().toLowerCase() + "-");
		}
		UITestUtils.enterArticleText(newText, getDriver(), getTemplate());
	}

	private void someoneEditedPageWorkaround() {
		if (getTemplate() instanceof HaddockTemplate) {
			waitUntilPresent(By.className("error"));
			final Optional oops = getDriver().findElements(By.cssSelector("h4"))
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

	protected WebElement find(final By selector) {
		return getDriver().findElement(selector);
	}

	protected List<WebElement> findAll(final By selector) {
		return getDriver().findElements(selector);
	}

	protected WebElement waitUntilPresent(final By selector) {
		return await().until(ExpectedConditions.presenceOfElementLocated(selector));
	}

	protected WebElement waitUntilPresent(final By selector, final int timeOutInSeconds) {
		return await(timeOutInSeconds).until(ExpectedConditions.presenceOfElementLocated(selector));
	}

	protected WebElement waitUntilClickable(final By selector) {
		return await().until(ExpectedConditions.elementToBeClickable(selector));
	}

	@NotNull
	protected WebDriverWait await() {
		return await(10);
	}

	@NotNull
	protected WebDriverWait await(final int timeOutInSeconds) {
		return new WebDriverWait(getDriver(), timeOutInSeconds);
	}

	protected WebElement waitUntilVisible(final By selector) {
		return await().until(ExpectedConditions.visibilityOfElementLocated(selector));
	}

	protected void hoverElement(final WebElement element) {
		new Actions(getDriver()).moveToElement(element).pause(1000).build().perform();
	}

	protected void hoverAndClickElement(final WebElement element) {
		new Actions(getDriver()).moveToElement(element).pause(1000).click().build().perform();
	}

	protected void scrollToElement(final WebElement element) {
		final JavascriptExecutor js = (JavascriptExecutor) getDriver();
		final String scrollElementIntoMiddle = ""
				+ "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
				+ "var elementTop = arguments[0].getBoundingClientRect().top;"
				+ "window.scrollBy(0, elementTop-(viewPortHeight/2));";
		js.executeScript(scrollElementIntoMiddle, element);
	}

	protected String readFile(final String fileName) throws IOException {
		return Strings.readFile(RESOURCE_DIR + fileName)
				.replace("%%package systemtest", "%%package systemtest" + getArticleName());
	}

	protected String readResource(final String resourceName) throws IOException {
		final URL resource = getClass().getResource(resourceName);
		return IOUtils.toString(resource, StandardCharsets.UTF_8);
	}

	protected void setText(final By by, final String text, final WebElement parent) {
		setText(parent.findElement(by), text);
	}

	protected void setText(final By by, final String text) {
		setText(find(by), text);
	}

	protected void setText(final WebElement element, final String text) {
		element.click();
		element.clear();
		element.sendKeys(text);
	}

	protected boolean isAlertPresent() {
		try {
			getDriver().switchTo().alert();
			return true;
		}
		catch (final NoAlertPresentException e) {
			return false;
		}
	}

	protected boolean isElementPresent(final By by) {
		try {
			getDriver().findElement(by);
			return true;
		}
		catch (final NoSuchElementException e) {
			return false;
		}
	}

	protected boolean isElementPresent(final By by, final WebElement parent) {
		try {
			parent.findElement(by);
			return true;
		}
		catch (final NoSuchElementException e) {
			return false;
		}
	}

	protected String confirmAlertAndGetMsg() {
		try {
			final Alert alert = getDriver().switchTo().alert();
			final String alertText = alert.getText();
			if (acceptNextAlert) {
				alert.accept();
			}
			else {
				alert.dismiss();
			}
			return alertText;
		}
		finally {
			acceptNextAlert = true;
		}
	}
}
