/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;

import static de.knowwe.uitest.UITestUtils.UseCase.LOGIN_PAGE;

/**
 * Utils methods for selenium UI tests.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.07.15
 */
public class UITestUtils {

	private static final String TMP_DEBUG_FOLDER = "/tmp/UI Debug Files/";

	public enum TestMode {
		local, saucelabs
	}

	public enum WebOS {
		windows, macOS, linux, other
	}

	public enum Browser {
		firefox, chrome
	}

	/**
	 * Loads the given article and waits for it to be loaded. If an alert pops up, it will be accepted.
	 *
	 * @param url         the url of the running wiki instance
	 * @param articleName name of the article to be loaded
	 * @param driver      the web driver
	 */
	public static void goToArticle(final String url, final String articleName, final WebDriver driver) {
		driver.get(url + "/Wiki.jsp?page=" + articleName);
		try {
			driver.switchTo().alert().accept();
		}
		catch (final NoAlertPresentException ignore) {
		}
	}

	public static void recompileCurrentArticle(final WebDriver driver) {
		String currentUrl = driver.getCurrentUrl();
		if (!currentUrl.contains("&parse=full")) {
			currentUrl += "&parse=full";
		}
		driver.get(currentUrl);
	}

	public enum UseCase {
		LOGIN_PAGE, NORMAL_PAGE
	}

	public static void logIn(final WebDriver driver, final String username, final String password, final UseCase use, final WikiTemplate template) {
		template.login(driver, use, username, password);
	}

	private static boolean isLoggedIn(final WebDriver driver, final WikiTemplate template) {
		final String logoutSelector = template == HaddockTemplate.getInstance() ? "a.btn.btn-default.btn-block.logout" : "a.action.logout";
		return !driver.findElements(By.cssSelector(logoutSelector)).isEmpty();
	}

	public static void awaitStatusChange(final WebDriver driver, final String status) {
		new WebDriverWait(driver, 10).until(ExpectedConditions.not(ExpectedConditions.attributeToBe(By.cssSelector("#knowWEInfoStatus"), "value", status)));
	}

	public static String getCurrentStatus(final WebDriver driver) {
		return driver.findElement(By.cssSelector("#knowWEInfoStatus")).getAttribute("value");
	}

	public static String getKnowWEUrl(final WikiTemplate template, final String articleName) {
		final String defaultUrl = template instanceof HaddockTemplate ? "https://knowwe-nightly-haddock.denkbares.com" : "https://knowwe-nightly.denkbares.com";
		final String knowweUrl = System.getProperty(template instanceof HaddockTemplate ? "knowwe.haddock.url" : "knowwe.standard.url", defaultUrl);
		return knowweUrl + "/Wiki.jsp?page=" + articleName;
	}

	public static LinkedList<Object[]> getTestParametersChromeAndFireFox() {
		final LinkedList<Object[]> params = new LinkedList<>();
		params.addAll(getTestParametersFireFox());
		params.addAll(getTestParametersChrome());
		return params;
	}

	public static LinkedList<Object[]> getTestParametersFireFox() {
		final LinkedList<Object[]> params = new LinkedList<>();
		params.add(new Object[] { Browser.firefox, Platform.WINDOWS, HaddockTemplate.getInstance() });
		params.add(new Object[] { Browser.firefox, Platform.WINDOWS, DefaultTemplate.getInstance() });
//		params.add(new Object[] { Browser.firefox, Platform.MAC, HaddockTemplate.getInstance() });
//		params.add(new Object[] { Browser.firefox, Platform.MAC, DefaultTemplate.getInstance() });
//		params.add(new Object[] { Browser.firefox, Platform.LINUX, HaddockTemplate.getInstance() });
//		params.add(new Object[] { Browser.firefox, Platform.LINUX, DefaultTemplate.getInstance() });
		return params;
	}

	public static LinkedList<Object[]> getTestParametersChrome() {
		final LinkedList<Object[]> params = new LinkedList<>();
		params.add(new Object[] { Browser.chrome, Platform.WINDOWS, HaddockTemplate.getInstance() });
		params.add(new Object[] { Browser.chrome, Platform.WINDOWS, DefaultTemplate.getInstance() });
//		params.add(new Object[] { Browser.chrome, Platform.MAC, HaddockTemplate.getInstance() });
//		params.add(new Object[] { Browser.chrome, Platform.MAC, DefaultTemplate.getInstance() });
//		params.add(new Object[] { Browser.chrome, Platform.LINUX, HaddockTemplate.getInstance() });
//		params.add(new Object[] { Browser.chrome, Platform.LINUX, DefaultTemplate.getInstance() });
		return params;
	}

	public static void awaitRerender(final WebDriver driver, final By by) {
		try {
			final List<WebElement> elements = driver.findElements(by);
			if (!elements.isEmpty()) {
				new WebDriverWait(driver, 5).until(ExpectedConditions.stalenessOf(elements.get(0)));
			}
		}
		catch (final TimeoutException ignore) {
		}
		new WebDriverWait(driver, 5).until(ExpectedConditions.presenceOfElementLocated(by));
	}

	public static RemoteWebDriver setUp(final Browser browser, final Platform os, final WikiTemplate template, final String articleName, final TestMode testMode, final String knowWEUrl, final boolean login, final Function<String, String> urlConstructor) throws IOException {

		final String testName = "UITest-" + articleName + "-" + template + "-" + browser + "-" + os.name()
				.toLowerCase();

		final DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);

		if (browser == Browser.chrome) {
			final String chromeBinary = System.getProperty("knowwe.chrome.binary");
			final ChromeOptions chromeOptions = new ChromeOptions();
			if (chromeBinary != null) {
				chromeOptions.setBinary(chromeBinary);
			}
			capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		}
		else if (browser == Browser.firefox) {
			// nothing to do here
		}
		else {
			throw new IllegalArgumentException("Browser " + browser + " not yet supported.");
		}

		final RemoteWebDriver driver;
		if (testMode == TestMode.local) {
			if (browser == Browser.chrome) {
				final ChromeOptions localChromeOptions = new ChromeOptions();
				final boolean headless = Boolean.getBoolean("knowwe.chrome.headless");
				localChromeOptions.setHeadless(headless);
				driver = new ChromeDriver(localChromeOptions);
			}
			else //noinspection ConstantConditions
				if (browser == Browser.firefox) {
					driver = new FirefoxDriver();
				}
				else {
					throw new IllegalArgumentException();
				}
		}
		else if (testMode == TestMode.saucelabs) {
			capabilities.setCapability("name", testName);
			capabilities.setCapability("platform", os);
			driver = new RemoteWebDriver(
					new URL("http://d3web:8c7e5a48-56dd-4cde-baf0-b17f83803044@ondemand.saucelabs.com:80/wd/hub"),
					capabilities);
		}
		else {
			throw new IllegalArgumentException("TestMode " + testMode + " not yet supported.");
		}
		driver.manage().window().setSize(new Dimension(1024, 768));
		driver.get(urlConstructor.apply("Main"));
		driver.manage().deleteAllCookies();
		driver.navigate().refresh();
		if (login && !UITestUtils.isLoggedIn(driver, template)) {
			driver.get(urlConstructor.apply("Login"));
			UITestUtils.logIn(driver, "UiTest", "fyyWWyVeHzzHfkUMZxUQ?3nDBPbTT6", LOGIN_PAGE, template);
		}
		driver.get(knowWEUrl);
//		if (!pageExists(template, driver)) {
//			createDummyPage(template, driver);
//		}
		Log.info("New web driver for test " + testName);
		return driver;
	}

	/**
	 * Generate a PNG screen capture and the current XML of the driver page.
	 *
	 * @param driver   the current driver
	 * @param folder   the folder to save the files to
	 * @param fileName the name of the debug file
	 */
	public static void generateDebugFiles(WebDriver driver, String folder, String fileName) {
		if (Strings.isBlank(folder)) folder = TMP_DEBUG_FOLDER;
		String fileNamePNG = fileName + "_screen-capture.png";
		String fileNameXML = fileName + "_page-content.xml";
		captureScreenshot(driver, folder, fileNamePNG);
		try {
			File destFile = new File(folder, fileNameXML);
			Strings.writeFile(destFile, driver.getPageSource());
			Log.info("The xml file was saved to \"" + destFile.getParentFile().getAbsolutePath() + "\"");
		}
		catch (IOException e) {
			Log.warning("Failed to create xml file", e);
		}
	}

	/**
	 * Take a screenshot of the current page and return success message.
	 *
	 * @param driver   the current driver
	 * @param folder   the folder to save the screenshot to
	 * @param fileName the name of the screenshot
	 * @return
	 */
	public static void captureScreenshot(WebDriver driver, String folder, String fileName) {
		if (Strings.isBlank(folder)) folder = TMP_DEBUG_FOLDER;
		try {
			File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			File destFile = new File(folder, fileName);
			FileUtils.copyFile(file, destFile);
			Log.info("The screenshot was saved to \"" + destFile.getParentFile().getAbsolutePath() + "\"");
		}
		catch (IOException e) {
			Log.warning("Failed to capture screenshot", e);
		}
	}

	private static boolean pageExists(final WikiTemplate template, final WebDriver driver) {
		if (template instanceof HaddockTemplate) {
			try {
				new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.createpage")));
			}
			catch (final Exception e) {
				// Element not present
			}
			return driver.findElements(By.cssSelector("a.createpage")).isEmpty();
		}
		else {
			try {
				new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.information a")));
			}
			catch (final Exception e) {
				// Element not present
			}
			return driver.findElements(By.cssSelector("div.information a"))
					.stream()
					.noneMatch(webElement -> Strings.containsIgnoreCase(webElement.getText(), "create it"));
		}
	}

	private static void createDummyPage(final WikiTemplate template, final WebDriver driver) throws IOException {
		final WebElement href;
		if (template instanceof HaddockTemplate) {
			href = driver.findElement(By.cssSelector("a.createpage"));
		}
		else {
			href = driver.findElements(By.cssSelector("div.information a"))
					.stream()
					.filter(webElement -> Strings.containsIgnoreCase(webElement.getText(), "create it"))
					.findFirst()
					.orElseThrow(() -> new WebDriverException("Create button not found"));
		}
		href.click();
		enterArticleText(Strings.readFile("src/test/resources/Dummy.txt"), driver, template);
	}

	public static void enterArticleText(final String newText, final WebDriver driver, final WikiTemplate template) {
		final String areaSelector = template == HaddockTemplate.getInstance() ? ".editor.form-control" : "#editorarea";
		final List<WebElement> editorAreas = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
				.cssSelector(areaSelector)));
		if (driver instanceof JavascriptExecutor) {
			// hacky but fast/instant!
			((JavascriptExecutor) driver).executeScript("var areas = document.querySelectorAll('" + areaSelector + "');" +
					"for (var i=0; i<areas.length; i++) { areas[i].value = arguments[0] };", newText);
		}
		else {
			// sets the keys one by one, pretty slow...
			editorAreas.forEach(WebElement::clear);
			editorAreas.forEach(webElement -> webElement.sendKeys(newText));
		}
		driver.findElement(By.name("ok")).click();
	}

	public static WebOS getWebOS(final WebDriver driver) {
		final JavascriptExecutor jse = (JavascriptExecutor) driver;
		String os = (String) jse.executeScript("return navigator.appVersion");
		os = os.toLowerCase();
		if (os.contains("win")) {
			return WebOS.windows;
		}
		if (os.contains("mac")) {
			return WebOS.macOS;
		}
		if (os.contains("nux") || os.contains("nix")) {
			return WebOS.linux;
		}
		return WebOS.other;
	}
}
