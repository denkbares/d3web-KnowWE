package tests;

import org.junit.Test;

import de.d3web.we.kdom.rules.RuleFormatAction;

import static org.junit.Assert.assertEquals;

/**
 * Created by Maximilian Brell on 21.03.16.
 */
public class RuleFormatterTest {

	@Test
	public void testSomething() {
		String test = "";

		String expected = test;

		assertEquals("Comparing failed.", expected, test);
	}

	@Test
	public void testNothingSpecial() {
		String test = "%%Rule\n" +
				"IF \"Check: Idle speed system.\" = ok\n" +
				"THEN Damaged idle speed system = N7\n" +
				"\n" +
				"IF \"Check: Idle speed system.\" = not ok\n" +
				"THEN Damaged idle speed system = P7\n" +
				"\n" +
				"IF NOT (Engine start = engine barely starts)\n" +
				"THEN Damaged idle speed system = N5\n" +
				"\n" +
				"IF Engine start = engine barely starts\n" +
				"THEN Damaged idle speed system = P5\n" +
				"\n" +
				"IF NOT (Driving = unsteady idle speed) \n" +
				"THEN Damaged idle speed system = N1\n" +
				"\n" +
				"IF Driving = unsteady idle speed\n" +
				"THEN Damaged idle speed system = P1\n" +
				"\n" +
				"IF NOT (Driving = low idle speed) \n" +
				"THEN Damaged idle speed system = N4\n" +
				"\n" +
				"IF Driving = low idle speed\n" +
				"THEN Damaged idle speed system = P4\n" +
				"\n" +
				"@package: demo\n" +
				"%";

		String expected = "%%Rule\n" +
				"IF \"Check: Idle speed system.\" = ok\n" +
				"THEN Damaged idle speed system = N7\n" +
				"\n" +
				"IF \"Check: Idle speed system.\" = not ok\n" +
				"THEN Damaged idle speed system = P7\n" +
				"\n" +
				"IF NOT (Engine start = engine barely starts)\n" +
				"THEN Damaged idle speed system = N5\n" +
				"\n" +
				"IF Engine start = engine barely starts\n" +
				"THEN Damaged idle speed system = P5\n" +
				"\n" +
				"IF NOT (Driving = unsteady idle speed)\n" +
				"THEN Damaged idle speed system = N1\n" +
				"\n" +
				"IF Driving = unsteady idle speed\n" +
				"THEN Damaged idle speed system = P1\n" +
				"\n" +
				"IF NOT (Driving = low idle speed)\n" +
				"THEN Damaged idle speed system = N4\n" +
				"\n" +
				"IF Driving = low idle speed\n" +
				"THEN Damaged idle speed system = P4\n" +
				"\n" +
				"@package: demo\n" +
				"%\n";

		assertEquals("Comparing failed.", expected, format(test));
	}

	@Test
	public void testIndention() {
		String test = "%%Rule\n" +
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

		String expected = "%%Rule\n" +
				"\n" +
				"IF Driving = insufficient power on full load\n" +
				"\tAND Mileage evaluation = slightly increased\n" +
				"THEN Leaking air intake system = P5\n" +
				"\n" +
				"IF NOT (Driving = insufficient power on sorted partial load\n" +
				"\tOR Driving = unsteady idle speed\n" +
				"\tOR Driving = insufficient power on full load) AND something = \"Hallo OR , ich stehe in der Zeile\"\n" +
				"THEN Leaking air intake system = N3\n" +
				"\n" +
				"IF Driving = insufficient power on full load\n" +
				"THEN Leaking air intake system = P5\n" +
				"\n" +
				"%\n";

		assertEquals("Comparing failed.", expected, format(test));
	}

	public String format(String testRule) {
		RuleFormatAction.RuleFormatter formatter = new RuleFormatAction.RuleFormatter(testRule);
		return formatter.format();
	}
}
