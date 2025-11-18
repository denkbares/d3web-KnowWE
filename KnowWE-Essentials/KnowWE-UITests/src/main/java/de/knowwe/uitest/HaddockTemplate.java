/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.uitest;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Implementation for Haddock template.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 24.03.17
 */
public class HaddockTemplate implements WikiTemplate {

	private static HaddockTemplate instance = null;

	public static HaddockTemplate getInstance() {
		if (instance == null) {
			instance = new HaddockTemplate();
		}
		return instance;
	}

	private HaddockTemplate() {

	}

	@Override
	public void pressSidebarButton(WebDriver driver) {
		driver.findElement(By.id("menu")).click();
		try {
			Thread.sleep(500); // Wait for Animation
		}
		catch (InterruptedException ignore) {
		}
	}

	@Override
	public WebElement getSidebar(WebDriver driver) {
		return driver.findElement(By.cssSelector(".sidebar"));
	}

	@Override
	public int getHeaderBottom(WebDriver driver) {
		int headerHeight = driver.findElement(By.className("header")).getSize().getHeight();
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		Long scrollY = (Long) executor.executeScript("return window.scrollY;");
		headerHeight = (int) Math.max(0, headerHeight - scrollY);
		// -1 because header and sticky row are overlapping
		headerHeight += driver.findElement(By.cssSelector("div.row.sticky")).getSize().getHeight() - 1;
		return headerHeight;
	}

	@Override
	public int getFooterTop(WebDriver driver) {
		return driver.findElement(By.className("footer")).getLocation().getY();
	}

	@Override
	public boolean isPageAlignedLeft(WebDriver driver) {
		return Integer.parseInt(driver.findElement(By.className("page"))
				.getCssValue("margin-left")
				.replaceAll("px", "")) == 0;
	}

	@Override
	public boolean isPageAlignedLeftWithSidebar(WebDriver driver) {
		int sidebarWidth = Integer.parseInt(getSidebar(driver).getCssValue("width").replaceAll("px", ""));
		int pageMarginLeft = Integer.parseInt(driver.findElement(By.className("page"))
				.getCssValue("margin-left")
				.replaceAll("px", ""));
		return pageMarginLeft >= sidebarWidth;
	}

	@Override
	public boolean isPageAlignedRight(WebDriver driver) {
		return Integer.parseInt(driver.findElement(By.className("page-content"))
				.getCssValue("margin-right")
				.replaceAll("px", "")) == 0;
	}

	@Override
	public boolean isPageAlignedRightWithRightPanel(WebDriver driver) {
		int rightPanelWidth = Integer.parseInt(getRightPanel(driver).getCssValue("width").replaceAll("px", ""));
		int pageContentMarginRight = Integer.parseInt(driver.findElement(By.className("page-content"))
				.getCssValue("margin-right")
				.replaceAll("px", ""));
		return pageContentMarginRight >= rightPanelWidth;
	}

	@Override
	public void login(WebDriver driver, UITestUtils.UseCase use, String username, String password) {
		List<WebElement> elements = null;
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		if (use == UITestUtils.UseCase.LOGIN_PAGE) {
			elements = driver.findElements(By.id("section-login"));
		}
		else if (use == UITestUtils.UseCase.NORMAL_PAGE) {
			WebElement userbox = wait.until(ExpectedConditions.elementToBeClickable(By.className("userbox")));
			new Actions(driver).moveToElement(userbox).build().perform();
			try {
				Thread.sleep(1000); //Animation
			}
			catch (InterruptedException ignore) {
			}
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn.btn-primary.btn-block.login")));
			elements = driver.findElements(By.cssSelector("a.btn.btn-primary.btn-block.login"));
		}

		if (elements == null) {
			throw new NullPointerException("No Login Interface found.");
		}
		else if (elements.isEmpty()) {
			return; // already logged in
		}

		elements.get(0).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("j_username"))).sendKeys(username);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("j_password"))).sendKeys(password);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("submitlogin"))).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.btn.btn-default.btn-block.logout")));
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("edit-source-button")));
	}

	@Override
	public String toString() {
		return "Haddock";
	}
}
