package de.knowwe.rdf2go;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Streams;

import static org.junit.Assert.*;

/**
 * Testing correct unquoting
 *
 * @author Felix Herrmann (Olyro GmbH)
 * @created 2/18/19
 */
public class Rdf2GoCoreTest {
	@Test
	public void checkTurtleLiteralUnquote() throws IOException {
		Map<String, String> tests = new HashMap<>();
		try (InputStream in = Rdf2GoCoreTest.class.getResourceAsStream("TurtleLiteralUnquoteTest.txt")) {
			String data = Streams.readStream(in);

			for (String test : data.split("\\s*---\\s*")) {
				String[] testLines = test.split("\\s*--\\s*");
				tests.put(Strings.trim(testLines[0]), Strings.trim(testLines[1]));
			}

			for (Map.Entry<String, String> entry : tests.entrySet()) {
				assertEquals(entry.getValue(), Rdf2GoCore.unquoteTurtleLiteral(entry.getKey()));
			}
		}
	}
}