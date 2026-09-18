package com.denkbares.knowwe.changeannotations;

import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins that the whitespace folding behind the blame comparison answers exactly what the regular expression it
 * replaces answered, for every character a line can contain.
 */
public class NormalizeForBlameComparisonTest {

	/**
	 * What the folding used to be, kept here as the thing the implementation has to keep agreeing with.
	 */
	private static String byRegex(String line) {
		return line.replaceAll("\\s+", " ").trim();
	}

	private static void assertSameAsRegex(String line) {
		assertEquals(byRegex(line), PageAnnotator.normalizeForBlameComparison(line),
				"normalizing " + describe(line));
	}

	private static String describe(String line) {
		StringBuilder description = new StringBuilder();
		for (int i = 0; i < line.length(); i++) {
			description.append(String.format("\\u%04x", (int) line.charAt(i)));
		}
		return description.toString();
	}

	@Test
	public void everyCharacterIsFoldedAsTheRegularExpressionFoldsIt() {
		for (int character = 0; character <= Character.MAX_VALUE; character++) {
			char c = (char) character;
			assertSameAsRegex(String.valueOf(c));
			assertSameAsRegex("a" + c + "b");
			assertSameAsRegex(c + "a");
			assertSameAsRegex("a" + c);
			assertSameAsRegex("" + c + c);
			assertSameAsRegex("a" + c + c + "b");
			assertSameAsRegex(" " + c + " a");
		}
	}

	@Test
	public void runsOfMixedWhitespaceAreFoldedAsTheRegularExpressionFoldsThem() {
		String[] whitespace = { " ", "\t", "\n", "\u000B", "\f", "\r" };
		for (String first : whitespace) {
			for (String second : whitespace) {
				for (String third : whitespace) {
					assertSameAsRegex(first + "a" + second + third + "b" + first);
					assertSameAsRegex(first + second + third);
				}
			}
		}
	}

	@Test
	public void randomLinesAreFoldedAsTheRegularExpressionFoldsThem() {
		Random random = new Random(20260918);
		char[] alphabet = {
				'a', 'b', ' ', ' ', '\t', '\n', '\r', '\f', '\u000B', '\u0000', '\u001C', '\u00A0', '\u2028'
		};
		for (int line = 0; line < 20000; line++) {
			StringBuilder text = new StringBuilder();
			int length = random.nextInt(12);
			for (int i = 0; i < length; i++) {
				text.append(alphabet[random.nextInt(alphabet.length)]);
			}
			assertSameAsRegex(text.toString());
		}
	}

	@Test
	public void theEmptyLineStaysEmpty() {
		assertSameAsRegex("");
	}
}
