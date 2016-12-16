package de.knowwe.uitest.chrome;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import de.knowwe.uitest.DiaFluxUITest;
import de.knowwe.uitest.UITestUtils;

/**
 * DiaFluxSystemTest for Chrome.
 *<p>
 * You will need a ST-BMI-Chrome wiki page in order to carry out this test
 *<p>
 * Created by Albrecht Striffler (denkbares GmbH) on 25.04.2015.
 */
public abstract class DiaFluxChromeUITest extends DiaFluxUITest {

	private static RemoteWebDriver driver;

	@BeforeClass
	public static void setUp() throws Exception {
		driver = UITestUtils.setUp(DesiredCapabilities.chrome(), DiaFluxChromeUITest.class.getSimpleName());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// if we quit, we don't see the status of the test at the end
		//noinspection ConstantConditions
		if (!UITestUtils.getDevMode()) driver.quit();
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
