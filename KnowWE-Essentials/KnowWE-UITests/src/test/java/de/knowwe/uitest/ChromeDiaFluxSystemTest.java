package de.knowwe.uitest;

import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * DiaFluxSystemTest for Chrome.
 *
 * Created by Albrecht Striffler (denkbares GmbH) on 25.04.2015.
 */
public abstract class ChromeDiaFluxSystemTest extends DiaFluxSystemTest {

	private static RemoteWebDriver driver;

	@BeforeClass
	public static void setUp() throws Exception {
		driver = UITestUtils.setUp(devMode, DesiredCapabilities.chrome(), ChromeDiaFluxSystemTest.class.getSimpleName());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// if we quit, we don't see the status of the test at the end
		//noinspection ConstantConditions
		if (!devMode) driver.quit();
	}

	@Override
	protected WebDriver getDriver() {
		return driver;
	}

	@Override
	public String getTestName() {
		return "ST-BMI-Chrome";
	}
}
