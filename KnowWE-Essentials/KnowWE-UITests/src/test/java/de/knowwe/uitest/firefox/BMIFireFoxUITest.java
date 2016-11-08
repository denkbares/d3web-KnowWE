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

package de.knowwe.uitest.firefox;

import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import de.knowwe.uitest.UITestUtils;

import static junit.framework.TestCase.assertEquals;

/**
 * First test for SauceLabs web service....
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 21.01.15
 */
public class BMIFireFoxUITest {

	private WebDriver driver;

	@Before
	public void setUp() throws Exception {
		// Choose the browser, version, and platform to test
		DesiredCapabilities capabilities = DesiredCapabilities.firefox();
		capabilities.setCapability("name", this.getClass().getSimpleName());
		capabilities.setCapability("platform", Platform.WINDOWS);
		// Create the connection to Sauce Labs to run the tests
		this.driver = new RemoteWebDriver(
				new URL("http://d3web:8c7e5a48-56dd-4cde-baf0-b17f83803044@ondemand.saucelabs.com:80/wd/hub"), capabilities);
	}

	@Test
	public void testBmi() throws Exception {
		driver.get("https://knowwe-nightly.denkbares.com/Wiki.jsp?page=Body-Mass-Index");
		By reset = By.className("reset");

		String currentStatus = UITestUtils.getCurrentStatus(driver);
		driver.findElement(reset).click();
		UITestUtils.awaitStatusChange(driver, currentStatus);

		currentStatus = UITestUtils.getCurrentStatus(driver);
		driver.findElements(By.className("numinput")).get(0).sendKeys("2" + Keys.ENTER);
		UITestUtils.awaitStatusChange(driver, currentStatus);

		currentStatus = UITestUtils.getCurrentStatus(driver);
		List<WebElement> numinput = driver.findElements(By.className("numinput"));
		numinput.get(1).sendKeys("100" + Keys.ENTER);
		UITestUtils.awaitStatusChange(driver, currentStatus);

		assertEquals("25", driver.findElements(By.className("numinput")).get(2).getAttribute("value"));
		assertEquals("Normal weight", driver.findElement(By.className("SOLUTION-ESTABLISHED")).getText());
		assertEquals("bmi = 25", driver.findElement(By.className("ABSTRACTION")).getText());
	}



	@After
	public void tearDown() throws Exception {
		driver.quit();
	}

}
