package de.knowwe.uitest;

import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * DiaFluxSystemTest for FireFox.
 *
 * Created by Albrecht Striffler (denkbares GmbH) on 25.04.2015.
 */
public class FireFoxDiaFluxSystemTest extends DiaFluxSystemTest {

	private static RemoteWebDriver driver;

	private static boolean devMode = false;

	@BeforeClass
	public static void setUp() throws Exception {
		// Create the connection to Sauce Labs to run the tests
		//noinspection ConstantConditions
		if (devMode) {
			driver = new FirefoxDriver();
		}
		else {
			// Choose the browser, version, and platform to test
			DesiredCapabilities capabilities = DesiredCapabilities.firefox();
			capabilities.setCapability("name", FireFoxDiaFluxSystemTest.class.getSimpleName());
			capabilities.setCapability("platform", Platform.WINDOWS);
			driver = new RemoteWebDriver(
					new URL("http://d3web:8c7e5a48-56dd-4cde-baf0-b17f83803044@ondemand.saucelabs.com:80/wd/hub"),
					capabilities);
		}
		driver.manage().window().setSize(new Dimension(1024, 768));
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// if we quit, we don't see the status of the test at the end
		//noinspection ConstantConditions
		if (!devMode) driver.quit();
	}

	@Override
	protected boolean isDevMode() {
		return devMode;
	}

	@Override
	protected WebDriver getDriver() {
		return driver;
	}
}
