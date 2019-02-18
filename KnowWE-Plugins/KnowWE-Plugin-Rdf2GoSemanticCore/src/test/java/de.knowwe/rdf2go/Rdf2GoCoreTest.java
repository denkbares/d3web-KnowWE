package de.knowwe.rdf2go;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Your Name (optionally your company)
 * @created 2/18/19
 */
public class Rdf2GoCoreTest {
	@Test
	public void checkTurtleLiteralUnquote() throws IOException {
		Map<String, String> tests = new HashMap<>();
		try (InputStream in = Rdf2GoCoreTest.class.getResourceAsStream("TurtleLiteralUnquoteTest.txt")) {
			final String data = new Scanner(in).useDelimiter("\\A").next().trim();

			for (String test : data.split("\\s*---\\s*")) {
				String[] testLines = test.split("\\s*--\\s*");
				tests.put(testLines[0], testLines[1]);
			}
		}

		for (Map.Entry<String, String> entry : tests.entrySet()) {
			assertEquals(Rdf2GoCore.unquoteTurtleLiteral(entry.getKey()), entry.getValue());
		}
	}
}