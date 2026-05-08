package com.denkbares.knowwe.textdiff;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextDiffTest {

	@Test
	void equalInputsAllCommon() {
		TextDiff diff = new TextDiff("a\nb\nc", "a\nb\nc");
		assertEquals(3, diff.lines().size());
		for (TextDiff.Line l : diff.lines()) {
			assertEquals(TextDiff.Line.Status.COMMON, l.status());
			assertTrue(l.oldLineNumber() > 0);
			assertTrue(l.newLineNumber() > 0);
		}
	}

	@Test
	void emptyOldAllAdded() {
		TextDiff diff = new TextDiff("", "a\nb");
		assertEquals(TextDiff.Line.Status.ADDED, diff.lines().get(diff.lines().size() - 1).status());
		assertTrue(diff.lines()
				.stream()
				.anyMatch(l -> l.text().equals("a") && l.status() == TextDiff.Line.Status.ADDED));
	}

	@Test
	void emptyNewAllRemoved() {
		TextDiff diff = new TextDiff("a\nb", "");
		assertTrue(diff.lines()
				.stream()
				.anyMatch(l -> l.text().equals("a") && l.status() == TextDiff.Line.Status.REMOVED));
	}

	@Test
	void nullOldTreatedAsAdded() {
		TextDiff diff = new TextDiff(null, "x\ny");
		assertEquals(2, diff.lines().size());
		for (TextDiff.Line l : diff.lines()) {
			assertEquals(TextDiff.Line.Status.ADDED, l.status());
			assertEquals(-1, l.oldLineNumber());
			assertTrue(l.newLineNumber() > 0);
		}
	}

	@Test
	void nullNewTreatedAsRemoved() {
		TextDiff diff = new TextDiff("x\ny", null);
		assertEquals(2, diff.lines().size());
		for (TextDiff.Line l : diff.lines()) {
			assertEquals(TextDiff.Line.Status.REMOVED, l.status());
			assertTrue(l.oldLineNumber() > 0);
			assertEquals(-1, l.newLineNumber());
		}
	}

	@Test
	void bothNullEmptyModel() {
		assertTrue(new TextDiff(null, null).lines().isEmpty());
	}

	@Test
	void normalizesCrlfAndCr() {
		TextDiff a = new TextDiff("a\r\nb\r\nc", "a\nb\nc");
		assertTrue(a.lines().stream().allMatch(l -> l.status() == TextDiff.Line.Status.COMMON));

		TextDiff b = new TextDiff("a\rb\rc", "a\nb\nc");
		assertTrue(b.lines().stream().allMatch(l -> l.status() == TextDiff.Line.Status.COMMON));
	}

	@Test
	void singleMidLineChangeLineNumbers() {
		TextDiff diff = new TextDiff("a\nB\nc", "a\nb\nc");
		List<TextDiff.Line> ls = diff.lines();
		// expected: COMMON a (1,1); REMOVED B (2,-1); ADDED b (-1,2); COMMON c (3,3)
		assertEquals(4, ls.size());
		assertEquals(TextDiff.Line.Status.COMMON, ls.get(0).status());
		assertEquals(1, ls.get(0).oldLineNumber());
		assertEquals(1, ls.get(0).newLineNumber());

		assertEquals(TextDiff.Line.Status.REMOVED, ls.get(1).status());
		assertEquals("B", ls.get(1).text());
		assertEquals(2, ls.get(1).oldLineNumber());
		assertEquals(-1, ls.get(1).newLineNumber());

		assertEquals(TextDiff.Line.Status.ADDED, ls.get(2).status());
		assertEquals("b", ls.get(2).text());
		assertEquals(-1, ls.get(2).oldLineNumber());
		assertEquals(2, ls.get(2).newLineNumber());

		assertEquals(TextDiff.Line.Status.COMMON, ls.get(3).status());
		assertEquals(3, ls.get(3).oldLineNumber());
		assertEquals(3, ls.get(3).newLineNumber());
	}

	@Test
	void adjacentAddAndRemove() {
		TextDiff diff = new TextDiff("a\nb\nc", "a\nx\ny\nc");
		long added = diff.lines().stream().filter(l -> l.status() == TextDiff.Line.Status.ADDED).count();
		long removed = diff.lines().stream().filter(l -> l.status() == TextDiff.Line.Status.REMOVED).count();
		assertTrue(added >= 1);
		assertTrue(removed >= 1);
	}

	@Test
	void trailingNewlineProducesTrailingEmptyLine() {
		TextDiff diff = new TextDiff("a\n", "a\n");
		assertEquals(2, diff.lines().size());
		assertEquals("", diff.lines().get(1).text());
	}

	@Test
	void largeInputSanity() {
		StringBuilder a = new StringBuilder();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < 5000; i++) {
			a.append("line ").append(i).append('\n');
			b.append("line ").append(i).append('\n');
		}
		b.append("extra\n");
		TextDiff diff = new TextDiff(a.toString(), b.toString());
		assertTrue(diff.lines().size() > 5000);
	}

	@Test
	void iterableYieldsSameLines() {
		TextDiff diff = new TextDiff("a\nb", "a\nc");
		List<TextDiff.Line> viaIterator = new java.util.ArrayList<>();
		for (TextDiff.Line line : diff) {
			viaIterator.add(line);
		}
		assertEquals(diff.lines(), viaIterator);
	}
}
