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

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by Veronika Sehne (denkbares GmbH) on 28.01.15.
 *
 * Test the Test Protocol for DiaFlux (System Test - Manual DiaFlux BMI)
 */
public class DiaFluxSystemTest {

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
	public void testDiaFlux() throws Exception {
		driver.get("http://www.d3web.de/Wiki.jsp?page=ST-BMI");
		String input = "%%package systemtest\n" +
				"\n" +
				"%%QuickInterview %\n" +
				"\n" +
				"%%ShowSolutions \n" +
				"%\n" +
				"\n" +
				"%%KnowledgeBase\n" +
				"Manual system test\n" +
				"%\n" +
				"\n" +
				"%%Question\n" +
				"Data\n" +
				"- Age [num] (0 99) {years}\n" +
				"- Age classification [oc] <abstract>\n" +
				"-- Pediatrics\n" +
				"-- Adult\n" +
				"- Height [num] (0 3) {m} \n" +
				"- Weight [num] (1 300) {kg}\n" +
				"- bmi [num] <abstract>\n" +
				"- Weight classification [oc]\n" +
				"-- Normal weight\n" +
				"-- Overweight\n" +
				"-- Severe overweight\n" +
				"- Continue selected therapy [yn]\n" +
				"Therapies\n" +
				"- Therapy [oc] <abstract>\n" +
				"-- Mild therapy\n" +
				"-- Rigorous therapy\n" +
				"%\n" +
				"\n" +
				"%%Solution\n" +
				"Illegal arguments\n" +
				"%\n";

		// first step: Insert Terminology and Administration
		driver.findElement(By.cssSelector("a.action.login")).click();
		driver.findElement(By.id("j_username")).sendKeys("test");
		driver.findElement(By.id("j_password")).sendKeys("8bGNmPjn");
		driver.findElement(By.name("submitlogin")).click();
		driver.findElement(By.id("edit-source-button")).click();
		WebElement editorarea = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("editorarea")));
		driver.findElement(By.id("editorarea")).clear();
		editorarea.sendKeys(input);
		//driver.findElement(By.id("editorarea")).sendKeys(input);
		driver.findElement(By.name("ok")).click();
		// hier noch ueberpruefen, dass keine Fehlermeldung aufgetreten ist
		//assertEquals("The annotation @master is deprecated.", driver.findElement(By.id("content_b4874b07")).getText());

		// second step: Insert Terminology and Administration

		driver.findElement(By.id("edit-source-button")).click();
		driver.findElement(By.id("editorarea")).clear();
		driver.findElement(By.id("editorarea")).sendKeys(input + "\n%%DiaFlux \n" +
				"%\n" +
				"\n" +
				"%%DiaFlux \n" +
				"%\n" +
				"\n" +
				"%%DiaFlux \n" +
				"%\n" +
				"\n" +
				"%%DiaFlux \n" +
				"%");
		driver.findElement(By.name("ok")).click();

		// third step: Create DiaFlux panel with Start and Exit nodes
		String winHandleBefore = driver.getWindowHandle();

		// first DiaFlux panel
		driver.findElement(By.cssSelector("span.information > a")).click();
		for(String winHandle : driver.getWindowHandles()){
			driver.switchTo().window(winHandle);
		}
		WebElement start = driver.findElement(By.id("start_prototype"));
		WebElement flowchart = driver.findElement(By.className("Flowchart"));
		WebElement exit = driver.findElement(By.id("exit_prototype"));
		driver.findElement(By.id("properties.autostart")).click();
		driver.findElement(By.id("properties.editName")).clear();
		driver.findElement(By.id("properties.editName")).sendKeys("BMI-Main");
		(new Actions(driver)).dragAndDrop(start, flowchart).perform();
		(new Actions(driver)).dragAndDrop(exit, flowchart).perform();
		driver.findElement(By.id("saveClose")).click();
		driver.switchTo().window(winHandleBefore);

		// second DiaFlux panel
		driver.findElement(By.linkText("Click here to create one.")).click();
		for(String winHandle : driver.getWindowHandles()){
			driver.switchTo().window(winHandle);
		}
		driver.findElement(By.id("properties.editName")).clear();
		driver.findElement(By.id("properties.editName")).sendKeys("BMI-Anamnesis");
		start = driver.findElement(By.id("start_prototype"));
		flowchart = driver.findElement(By.className("Flowchart"));
		exit = driver.findElement(By.id("exit_prototype"));
		(new Actions(driver)).dragAndDrop(start, flowchart).perform();
		(new Actions(driver)).dragAndDrop(exit, flowchart).perform();
		// add Start and Exit to Flowchart
		// designate Start and Exit
		// move Start and Exit
//		driver.findElement(By.className("exitPane")).click();
//		(new Actions(driver)).doubleClick(driver.findElement(By.id("#node_2_highlight")));
//		driver.findElement(By.xpath("//div[@id='flow_871e1cb3']/div/div[3]/div[2]/div[2]/input")).click();
//		driver.findElement(By.className("value")).clear();
//		driver.findElement(By.className("value")).sendKeys("Illegal Arguments");
		(new Actions(driver)).dragAndDrop(exit, flowchart).perform();
		(new Actions(driver)).dragAndDrop(exit, flowchart).perform();
		Actions builder = (new Actions(driver));
		builder.moveToElement(driver.findElement(By.id("#node_2")), 400, 400);
		builder.click();
		builder.build().perform();
		driver.findElement(By.id("saveClose")).click();
		driver.switchTo().window(winHandleBefore);

//		// third DiaFlux panel
//		driver.findElement(By.linkText("Click here to create one.")).click();
//		for(String winHandle : driver.getWindowHandles()){
//			driver.switchTo().window(winHandle);
//		}
//		// add Start and Exit to Flowchart
		// designate Start and Exit
		// move Start and Exit
//		driver.switchTo().window(winHandleBefore);

//		// fourth DiaFlux panel
//		driver.findElement(By.linkText("Click here to create one.")).click();
//		for(String winHandle : driver.getWindowHandles()){
//			driver.switchTo().window(winHandle);
//		}
//		// add Start and Exit to Flowchart
		// designate Start and Exit
		// move Start and Exit
//		driver.switchTo().window(winHandleBefore);
	}


	private void awaitRerender(By by) {
		try {
			new WebDriverWait(driver, 10).until(ExpectedConditions.stalenessOf(driver.findElement(by)));
		} catch (TimeoutException ignore) {}
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(by));
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
	}
}
