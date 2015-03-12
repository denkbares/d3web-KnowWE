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

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.d3web.strings.Strings;

/**
 * Created by Veronika Sehne, Albrecht Striffler (denkbares GmbH) on 28.01.15.
 * <p/>
 * Test the Test Protocol for DiaFlux (System Test - Manual DiaFlux BMI)
 */
public class DiaFluxSystemTest {

	public static final String RESOURCE_DIR = "src/test/resources/";
	private static WebDriver driver;

	/*
	 *  If you set devMode to true, you can test locally, which will be much faster
	 *  Don't commit this as true, because Jenkins build WILL fail!
	 *
	 *  To test locally, you also need to download the ChromeDriver from
	 *  https://sites.google.com/a/chromium.org/chromedriver/downloads
	 *  and start it on your machine. Also, you need a locally running KnowWE with a page "ST-BMI".
	 *  State of the page does not matter, it will be cleared for each new test.
	 */
	private static boolean devMode = false;

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
			logIn();
		}
	}

	private void logIn() {
		List<WebElement> elements = driver.findElements(By.cssSelector("a.action.login"));
		if (elements.isEmpty()) return; // already logged in
		elements.get(0).click();
		driver.findElement(By.id("j_username")).sendKeys("test");
		driver.findElement(By.id("j_password")).sendKeys("8bGNmPjn");
		driver.findElement(By.name("submitlogin")).click();
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.action.logout")));
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("edit-source-button")));
	}

	@Test
	public void addTerminology() throws IOException {
		changeArticleText(readFile("Step1.txt"));

		// hier noch überpruefen, dass keine Fehlermeldung aufgetreten ist
		//assertEquals("The annotation @master is deprecated.", driver.findElement(By.id("content_b4874b07")).getText());
	}

	@Test
	public void addFlowchartStumps() throws Exception {
		changeArticleText(readFile("Step2.txt"));

		String article = driver.getWindowHandle();

		// first DiaFlux panel
		createNextFlow();
		switchToEditor(article);

		driver.findElement(By.id("properties.autostart")).click();

		setFlowName("BMI-Main");
		addStartNode(null, -300, 300);
		addExitNode(null, 100, 300);

		saveAndSwitchBack(article);

		// second DiaFlux panel
		createNextFlow();
		switchToEditor(article);

		setFlowName("BMI-Anamnesis");

		addStartNode(null, -300, 300);
		addExitNode("Illegal arguments", -100, 400);
		addExitNode("Weight ok", -250, 500);
		addExitNode("Weight problem", 0, 500);

		saveAndSwitchBack(article);

		// third DiaFlux panel
		createNextFlow();
		switchToEditor(article);

		setFlowName("BMI-SelectTherapy");
		addStartNode("Mild therapy", -300, 300);
		addStartNode("Rigorous therapy", -300, 400);
		addExitNode("Done", -100, 350);

		saveAndSwitchBack(article);

		// third DiaFlux panel
		createNextFlow();
		switchToEditor(article);

		setFlowName("BMI-SelectMode");
		addStartNode(null, -300, 300);
		addExitNode("Pediatrics", -100, 450);
		addExitNode("Adult", 0, 450);

		saveAndSwitchBack(article);

	}

	//@Test
	public void implementBMIMain() throws Exception {
		changeArticleText(readFile("Step3.txt"));

		String articleHandle = driver.getWindowHandle();

		clickTool("type_DiaFlux", 1, "visual editor");

		switchToEditor(articleHandle);

		addActionNode("BMI-SelectMode", -300, 60);

		connect(2, 4);
		connect(4, 3, "Pediatrics");

		addActionNode("BMI-Anamnesis", -100, 150); // 7

		connect(4, 7, "Adult");
		connect(7, 3, "Illegal arguments");
		connect(7, 3, "Weight ok");

		addActionNode("bmi", -150, 220); // 11

		connect(7, 11, "Weight problem");

		addActionNode("BMI-SelectTherapy", -500, 220); // 13
		addActionNode("BMI-SelectTherapy", "Rigorous therapy", -500, 320); // 14

		connect(11, 13, "Formula", "gradient(bmi[-7d, 0s]) >= 0 & gradient(bmi[-7d, 0s]) < 5");
		connect(11, 14, "Formula", "gradient(bmi[-7d, 0s]) >= 5");

		addActionNode("Continue selected therapy", "ask", -150, 280); // 17

		connect(11, 17, "Formula", "gradient(bmi[-7d, 0s]) < 0");

		addSnapshotNode(null, -160, 340); // 19

		connect(13, 19, "Done");
		connect(14, 19, "Done");
		connect(17, 19);

		connect(19, 7);

		saveAndSwitchBack(articleHandle);
	}

	private void connect(int sourceId, int targetId) {
		connect(sourceId, targetId, null, null);
	}

	private void connect(int sourceId, int targetId, String option) {
		connect(sourceId, targetId, option, null);
	}

	private void connect(int sourceId, int targetId, String option, String optionText) {
		driver.findElement(By.id("#node_" + sourceId)).click();
		WebElement arrowTool = driver.findElement(By.className("ArrowTool"));
		WebElement targetNode = driver.findElement(By.id("#node_" + targetId));
		(new Actions(driver)).dragAndDrop(arrowTool, targetNode).perform();
		if (option != null) {
			WebElement select = driver.findElement(By.cssSelector(".selectedRule select"));
			select.click();
			if (option.equalsIgnoreCase("formula")) {
				select.findElement(By.xpath("//option[@value='" + 13 + "']")).click();
				driver.findElement(By.cssSelector(".selectedRule textarea")).sendKeys(optionText + Keys.ENTER);
			} else {
				select.findElement(By.xpath("//option[text()='" + option + "']")).click();
			}
		}
	}

	private void clickTool(String markupClass, int nth, String tooltipContains) {
		WebElement markup = driver.findElements(By.className(markupClass)).get(nth - 1);
		WebElement toolMenu = markup.findElement(By.className("headerMenu"));
		WebElement editTool = markup.findElements(By.cssSelector(".markupMenu a.markupMenuItem"))
				.stream()
				.filter(element -> Strings.containsIgnoreCase(element.getAttribute("title"), tooltipContains))
				.findFirst().get();
		new Actions(driver).moveToElement(toolMenu).moveToElement(editTool).click(editTool).perform();
	}

	private void changeArticleText(String newText) {
		driver.findElement(By.id("edit-source-button")).click();
		WebElement editorArea = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("editorarea")));
		if (driver instanceof JavascriptExecutor) {
			// hacky but fast/instant!
			((JavascriptExecutor) driver).executeScript("document.getElementById('editorarea').value = arguments[0]", newText);
		}
		else {
			// sets the keys one by one, pretty slow...
			editorArea.clear();
			editorArea.sendKeys(newText);
		}
		driver.findElement(By.name("ok")).click();
	}

	private void createNextFlow() {
		driver.findElement(By.linkText("Click here to create one.")).click();
	}

	private void saveAndSwitchBack(String winHandleBefore) {
		driver.findElement(By.id("saveClose")).click();
		driver.switchTo().window(winHandleBefore);
		awaitRerender(By.id("pagecontent"));
	}

	private void addActionNode(String text, int xOffset, int yOffset) throws InterruptedException {
		addActionNode(text, null, xOffset, yOffset);
	}

	private void addActionNode(String text, String dropdown, int xOffset, int yOffset) throws InterruptedException {
		addNode(By.id("decision_prototype"), By.cssSelector(".NodeEditor .ObjectSelect *"), text, dropdown, xOffset, yOffset);
	}

	private void addStartNode(String text, int xOffset, int yOffset) throws InterruptedException {
		addNode(By.id("start_prototype"), By.cssSelector(".NodeEditor .startPane input"), text, null, xOffset, yOffset);
	}

	private void addSnapshotNode(String text, int xOffset, int yOffset) throws InterruptedException {
		addNode(By.id("snapshot_prototype"), By.cssSelector(".NodeEditor .snapshotPane input"), text, null, xOffset, yOffset);
	}

	private void addExitNode(String text, int xOffset, int yOffset) throws InterruptedException {
		addNode(By.id("exit_prototype"), By.cssSelector(".NodeEditor .exitPane input"), text, null, xOffset, yOffset);
	}

	private void addNode(By prototypeSelector, By textSelector, String text, String dropdown, int xOffset, int yOffset) throws InterruptedException {
		WebElement start = driver.findElement(prototypeSelector);
		new Actions(driver).dragAndDropBy(start, xOffset, yOffset).perform();
		Thread.sleep(300);
		if (text != null) {
			List<WebElement> nodes = driver.findElements(By.cssSelector(".Flowchart > .Node"));
			WebElement newNode = nodes.get(nodes.size() - 1);
			new Actions(driver).doubleClick(newNode).perform();
			driver.findElement(textSelector).click();
			driver.findElement(textSelector).clear();
			driver.findElement(textSelector).sendKeys(text);
			Thread.sleep(100);
			driver.findElement(textSelector).sendKeys(Keys.ENTER);
			if (dropdown != null) {
				WebElement select = driver.findElement(By.cssSelector(".ActionEditor select"));
				select.click();
				select.findElement(By.xpath("//option[text()='" + dropdown + "']")).click();
			}

			List<WebElement> okButtons = driver.findElements(By.cssSelector(".NodeEditor .ok"));
			if (okButtons.size() == 1) okButtons.get(0).click();
		}

	}

	private void setFlowName(String flowName) {
		driver.findElement(By.id("properties.editName")).clear();
		driver.findElement(By.id("properties.editName")).sendKeys(flowName);
	}

	private void switchToEditor(String articleHandle) {
		Set<String> windowHandles = new HashSet<>(driver.getWindowHandles());
		windowHandles.remove(articleHandle);
		driver.switchTo().window(windowHandles.iterator().next());
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("start_prototype")));
	}

	private void awaitRerender(By by) {
		try {
			new WebDriverWait(driver, 10).until(ExpectedConditions.stalenessOf(driver.findElement(by)));
		}
		catch (TimeoutException ignore) {
		}
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(by));
	}

	private String readFile(String fileName) throws IOException {
		return Strings.readFile(RESOURCE_DIR + fileName);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// if we quit, we don't see the status of the test at the end
		if (!devMode) driver.quit();
	}
}
