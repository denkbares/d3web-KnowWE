/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.uitest;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Implementation for KnowWE default template.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 24.03.17
 */
public class DefaultTemplate implements WikiTemplate {


	private static DefaultTemplate instance = null;

	public static DefaultTemplate getInstance() {
		if (instance == null) {
			instance = new DefaultTemplate();
		}
		return instance;
	}

	private DefaultTemplate() {

	}

	@Override
	public void pressSidebarButton(WebDriver driver) {
		String idSidebarButton = "favorites-toggle-button";
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id(idSidebarButton)));
		driver.findElement(By.id(idSidebarButton)).click();
		try {
			Thread.sleep(500); // Wait for animation
		}
		catch (InterruptedException ignore) {
		}
	}

	@Override
	public WebElement getSidebar(WebDriver driver) {
		return driver.findElement(By.id("favorites"));
	}

	@Override
	public int getHeaderBottom(WebDriver driver) {
		int headerHeight = driver.findElement(By.id("header")).getSize().getHeight();
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		Long scrollY = (Long) executor.executeScript("return window.scrollY;");
		headerHeight = (int) Math.max(0, headerHeight - scrollY);
		return headerHeight;
	}

	@Override
	public int getFooterTop(WebDriver driver) {
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		Long footerTop = (Long) executor.executeScript("return Math.max(" +
				"document.body.scrollHeight, document.documentElement.scrollHeight," +
				"document.body.offsetHeight, document.documentElement.offsetHeight," +
				"document.body.clientHeight, document.documentElement.clientHeight" +
				");");
		return Math.toIntExact(footerTop);
	}

	@Override
	public boolean isPageAlignedLeft(WebDriver driver) {
		return Integer.parseInt(driver.findElement(By.id("page"))
				.getCssValue("left")
				.replaceAll("px", "")) <= 5;
	}

	@Override
	public boolean isPageAlignedLeftWithSidebar(WebDriver driver) {
		int sidebarWidth = getSidebar(driver).getSize().getWidth();
		int pageLeft = Integer.parseInt(driver.findElement(By.id("page"))
				.getCssValue("left")
				.replaceAll("px", ""));
		return Math.abs(pageLeft - sidebarWidth) <= 5;
	}

	@Override
	public boolean isPageAlignedRight(WebDriver driver) {
		int windowWidth = driver.manage().window().getSize().getWidth();
		int posXEnd = driver.findElement(By.id("pagecontent")).getLocation().getX();
		posXEnd += driver.findElement(By.id("pagecontent")).getSize().getWidth();
		UITestUtils.WebOS os = UITestUtils.getWebOS(driver);
		int tolerance = os == UITestUtils.WebOS.windows ? 35 : 10; //Scrollbar width
		return Math.abs(windowWidth - posXEnd) <= tolerance;
	}

	@Override
	public boolean isPageAlignedRightWithRightPanel(WebDriver driver) {
		int posXEnd = driver.findElement(By.id("pagecontent")).getLocation().getX();
		posXEnd += driver.findElement(By.id("pagecontent")).getSize().getWidth();

		return posXEnd < getRightPanel(driver).getLocation().getX();
	}

	@Override
	public void login(WebDriver driver, UITestUtils.UseCase use, String username, String password) {
		List<WebElement> elements = null;
		if (use == UITestUtils.UseCase.LOGIN_PAGE) {
			String idLoginElement = "logincontent";
			elements = driver.findElements(By.id(idLoginElement));
		}
		else if (use == UITestUtils.UseCase.NORMAL_PAGE) {
			WebElement loginButton = new WebDriverWait(driver, 20).until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.action.login")));
			elements = Collections.singletonList(loginButton);
		}

		if (elements == null) {
			throw new NullPointerException("No Login Interface found.");
		}
		else if (elements.isEmpty()) {
			return; // already logged in
		}

		elements.get(0).click();
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("j_username")))
				.sendKeys(username);
		driver.findElement(By.id("j_password")).sendKeys(password);
		driver.findElement(By.name("submitlogin")).click();
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.action.logout")));
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("edit-source-button")));
	}

	@Override
	public String toString() {
		return "Default";
	}
}
