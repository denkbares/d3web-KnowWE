package de.d3web.wisec.writers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class ConverterUtils {
	/**
	 * Removes "0" after comma, when available.
	 */
	public static String toShoppedString(String doubleValue) {
		if (doubleValue.endsWith(".0")) {
			doubleValue = doubleValue.substring(0, doubleValue.length()-2);
		}
		return doubleValue;
	}

	public static Writer createWriter(String filename) throws IOException {
		return createWriter(filename, "++generated++");
	}

	public static Writer createWriter(String filename, String headerText) throws IOException {
		Writer w = new FileWriter(new File(filename));
		w.append(headerText+ "\n");
		return w;
	}
}
