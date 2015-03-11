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
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
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
 * Created by Veronika Sehne, Albrecht Striffler (denkbares GmbH) on 28.01.15.
 * <p/>
 * Test the Test Protocol for DiaFlux (System Test - Manual DiaFlux BMI)
 */
public class DiaFluxSystemTest {

	private static WebDriver driver;

	/*
	 *  If you set this to true, you can test locally, which will be much faster
	 *  Don't commit this as true, because Jenkins build WILL fail!
	 *
	 *  To test locally, you also need to download the ChromeDriver from
	 *  https://sites.google.com/a/chromium.org/chromedriver/downloads
	 *  and start it on your machine. Also, you need a locally running KnowWE with a page "ST-BMI".
	 *  State of the page does not matter, it will be cleared for each new test.
	 */
	private static boolean devMode = false;

	private String inputStep1;
	private String inputStep2;

	{
		inputStep1 = "%%package systemtest\n" +
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

		inputStep2 = inputStep1 + "\n%%DiaFlux \n" +
				"%\n" +
				"\n" +
				"%%DiaFlux \n" +
				"%\n" +
				"\n" +
				"%%DiaFlux \n" +
				"%\n" +
				"\n" +
				"%%DiaFlux \n" +
				"%";
	}

	@BeforeClass
	public static void setUp() throws Exception {
		// Create the connection to Sauce Labs to run the tests
		if (devMode) {
			driver = new RemoteWebDriver(new URL("http://localhost:9515"), DesiredCapabilities.chrome());
		}
		else {
			// Choose the browser, version, and platform to test
			DesiredCapabilities capabilities = DesiredCapabilities.firefox();
			capabilities.setCapability("name", DiaFluxSystemTest.class.getSimpleName());
			capabilities.setCapability("platform", Platform.WINDOWS);
			driver = new RemoteWebDriver(
					new URL("http://d3web:8c7e5a48-56dd-4cde-baf0-b17f83803044@ondemand.saucelabs.com:80/wd/hub"),
					capabilities);
		}
		driver.manage().window().setSize(new Dimension(1024, 768));
	}

	@Before
	public void load() throws Exception {
		if (devMode) {
			driver.get("http://localhost:8080/KnowWE/Wiki.jsp?page=ST-BMI");
		}
		else {
			driver.get("https://www.d3web.de/Wiki.jsp?page=ST-BMI");
			authenticate();
		}
	}

	private void authenticate() {
		driver.findElement(By.cssSelector("a.action.login")).click();
		driver.findElement(By.id("j_username")).sendKeys("test");
		driver.findElement(By.id("j_password")).sendKeys("8bGNmPjn");
		driver.findElement(By.name("submitlogin")).click();
	}

	/**
	 * Insert Terminology and Administration
	 */
	//@Test
	public void firstStep() {
		driver.findElement(By.id("edit-source-button")).click();
		WebElement editorarea = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("editorarea")));
		driver.findElement(By.id("editorarea")).clear();
		editorarea.sendKeys(inputStep1);
		driver.findElement(By.name("ok")).click();

		// hier noch überpruefen, dass keine Fehlermeldung aufgetreten ist
		//assertEquals("The annotation @master is deprecated.", driver.findElement(By.id("content_b4874b07")).getText());
	}

	/**
	 * Add and initialize FlowCharts.
	 */
	@Test
	public void secondStep() throws Exception {

		driver.findElement(By.id("edit-source-button")).click();
		driver.findElement(By.id("editorarea")).clear();
		driver.findElement(By.id("editorarea")).sendKeys(inputStep2);
		driver.findElement(By.name("ok")).click();

		// Create DiaFlux panel with Start and Exit nodes
		String winHandleBefore = driver.getWindowHandle();

		// first DiaFlux panel
		createNextFlow();
		switchToEditor(winHandleBefore);

		driver.findElement(By.id("properties.autostart")).click();

		setFlowName("BMI-Main");
		addStartNode(-300, 300);
		addExitNode(0, 300);

		saveAndSwitchBack(winHandleBefore);

		// second DiaFlux panel
		createNextFlow();
		switchToEditor(winHandleBefore);

		setFlowName("BMI-SelectTherapy");

		addStartNode(-300, 300);
		addExitNode(-100, 400);
		addExitNode(-250, 500);
		addExitNode(0, 500);


		saveAndSwitchBack(winHandleBefore);

		// third DiaFlux panel
		createNextFlow();
		switchToEditor(winHandleBefore);

		setFlowName("BMI-Anamnesis");
		addStartNode(-300, 300);
		addStartNode(-300, 400);
		addExitNode(-100, 350);

		saveAndSwitchBack(winHandleBefore);

	}

	private void createNextFlow() {
		driver.findElement(By.linkText("Click here to create one.")).click();
	}

	private void saveAndSwitchBack(String winHandleBefore) {
		driver.findElement(By.id("saveClose")).click();
		driver.switchTo().window(winHandleBefore);
		awaitRerender(By.id("pagecontent"));
	}

	private void addStartNode(int xOffset, int yOffset) throws InterruptedException {
		WebElement start = driver.findElement(By.id("start_prototype"));
		(new Actions(driver)).dragAndDropBy(start, xOffset, yOffset).perform();
		Thread.sleep(300);
	}

	private void addExitNode(int xOffset, int yOffset) throws InterruptedException {
		WebElement start = driver.findElement(By.id("exit_prototype"));
		(new Actions(driver)).dragAndDropBy(start, xOffset, yOffset).perform();
		Thread.sleep(300);
	}

	private void setFlowName(String flowName) {
		driver.findElement(By.id("properties.editName")).clear();
		driver.findElement(By.id("properties.editName")).sendKeys(flowName);
	}

	private void switchToEditor(String winHandleBefore) {
		Set<String> windowHandles = new HashSet<>(driver.getWindowHandles());
		windowHandles.remove(winHandleBefore);
		driver.switchTo().window(windowHandles.iterator().next());
	}

	private void awaitRerender(By by) {
		try {
			new WebDriverWait(driver, 10).until(ExpectedConditions.stalenessOf(driver.findElement(by)));
		}
		catch (TimeoutException ignore) {
		}
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(by));
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// if we quit, we don't see the status of the test at the end
		if (!devMode) driver.quit();
	}
}
