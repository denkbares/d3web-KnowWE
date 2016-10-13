package de.knowwe.uitest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;

/**
 * Testing Formatter button.
 * <p>
 * Created by Adrian Müller on 13.10.16.
 */
public abstract class FormatterChromeUITest extends KnowWEUITest {

	private static RemoteWebDriver driver;
	private static boolean devMode = false;

	@Override
	protected abstract WikiTemplate getTemplate();

	@Override
	protected WebDriver getDriver() {
		return driver;
	}

	@Override
	protected boolean isDevMode() {
		return devMode;
	}

	@Override
	public String getTestName() {
		return "UITest-Formatter-Chrome";
	}

	@BeforeClass
	public static void setUp() throws Exception {
		driver = UITestUtils.setUp(devMode, DesiredCapabilities.chrome(), FormatterChromeUITest.class.getSimpleName());
	}

	@Test
	public void testSparqlFormatButton() throws InterruptedException {
		String unformatted = "%%Sparql\n" +
				"\n" +
				"  SELECT ?Substance ?CAS ?EC ?Name\n" +
				"  \n" +
				"  WHERE {\n" +
				"  \n" +
				"    ?Substance rdf:type lns:Substance .\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"    OPTIONAL { \n" +
				"?Substance <lns:hasFirstGroup+Name> ?Name \n" +
				"}\n" +
				"    \n" +
				"    ?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"    \n" +
				"    }\n" +
				"        ORDER BY ?Name ?CAS ?EC\n" +
				"%\n";

		String actual = testFormatButton(unformatted);
		String expectedSparql = "%%Sparql\n" +
				"\n" +
				"SELECT ?Substance ?CAS ?EC ?Name\n" +
				"\n" +
				"WHERE {\n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, actual);
	}

	@Test
	public void testTurtleFormatButton() throws InterruptedException {
		String unformatted = "%%turtle\n" +
				"si:SimpsonsConcept\n" +
				"\t?a\n" +
				"\t\trdfs:Class .\n" +
				"\n" +
				"si:LivingBeing rdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Lebewesen\"@de, \"Living being\" .\n" +
				"\n" +
				"si:Human\n" +
				"\trdfs:subClassOf si:LivingBeing ;\n" +
				"\t?a rdfs:Class ;\n" +
				"\t\n" +
				"\t\n" +
				"\t\n" +
				"\trdfs:label \"Mensch\"@de, \"Human\"@en .\n" +
				"\n" +
				"%";
		String actual = testFormatButton(unformatted);
		String expectedSparql = "%%turtle\n" +
				"si:SimpsonsConcept\n" +
				"\t?a\n" +
				"\t\trdfs:Class .\n" +
				"\n" +
				"si:LivingBeing rdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Lebewesen\"@de, \"Living being\" .\n" +
				"\n" +
				"si:Human\n" +
				"\trdfs:subClassOf si:LivingBeing ;\n" +
				"\t?a rdfs:Class ;\n" +
				"\t\n" +
				"\trdfs:label \"Mensch\"@de, \"Human\"@en .\n" +
				"\n" +
				"%\n";
		assertEquals("Compared Turtle are not equal", expectedSparql, actual);
	}

	@Test
	public void testRuleFormatButton() throws InterruptedException {
		String unformatted = "%%Rule\n" +
				"\n" +
				"IF Driving = insufficient power on full load \n" +
				"   AND Mileage evaluation = slightly increased\n" +
				"THEN Leaking air intake system = P5\n" +
				"\n" +
				"IF NOT (Driving = insufficient power on sorted partial load OR \n" +
				"     Driving = unsteady idle speed OR \n" +
				"     Driving = insufficient power on full load) AND something = \"Hallo OR , ich stehe in der Zeile\"\n" +
				"THEN Leaking air intake system  = N3\n" +
				"\n" +
				"IF Driving = insufficient power on full load\n" +
				"THEN Leaking air intake system = P5\n" +
				"\n" +
				"%";
		String actual = testFormatButton(unformatted);
		String expectedSparql = "%%Rule\n" +
				"\n" +
				"IF Driving = insufficient power on full load\n" +
				"\tAND Mileage evaluation = slightly increased\n" +
				"THEN Leaking air intake system = P5\n" +
				"\n" +
				"IF NOT (Driving = insufficient power on sorted partial load\n" +
				"\t\tOR Driving = unsteady idle speed\n" +
				"\t\tOR Driving = insufficient power on full load)\n" +
				"\tAND something = \"Hallo OR , ich stehe in der Zeile\"\n" +
				"THEN Leaking air intake system = N3\n" +
				"\n" +
				"IF Driving = insufficient power on full load\n" +
				"THEN Leaking air intake system = P5\n" +
				"\n" +
				"%\n";
		assertEquals("Compared Rule are not equal", expectedSparql, actual);
	}

	private String testFormatButton(String unformatted) {
		getDriver().findElement(By.id("edit-mode-button")).click();
		getDriver().findElement(By.className("markupText")).click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.presenceOfElementLocated(By.className("defaultEditTool")));
		getDriver().findElement(By.className("defaultEditTool")).clear();
		getDriver().findElement(By.className("defaultEditTool")).sendKeys(unformatted);
		saveAndReopen();
		getDriver().findElement(By.cssSelector(".action.format")).click();
		saveAndReopen();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.presenceOfElementLocated(By.className("defaultEditTool")));
		return getDriver().findElement(By.className("defaultEditTool")).getAttribute("value");
	}

	private void saveAndReopen() {
		getDriver().findElement(By.cssSelector(".action.save")).click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.presenceOfElementLocated(By.id("edit-mode-button")));
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOfElementLocated(By.id("edit-mode-button")));
		getDriver().findElement(By.id("edit-mode-button")).click();
		getDriver().findElement(By.className("markupText")).click();
	}
}
