package com.denkbares.knowwe.changeannotations;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageAnnotatorTest {

	private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant T2 = Instant.parse("2026-02-01T00:00:00Z");
	private static final Instant T3 = Instant.parse("2026-03-01T00:00:00Z");

	@Test
	void rejectsEmptyHistory() {
		assertThrows(IllegalArgumentException.class,
				() -> PageAnnotator.annotate("Main", List.of()));
	}

	@Test
	void singleVersionAttributesEverythingToV1() {
		VersionEntry v1 = entry(1, "alice", T1, null, "alpha\nbeta\ngamma\n");
		PageAnnotation result = PageAnnotator.annotate("Main", List.of(v1));

		assertEquals(1, result.currentVersion());
		assertEquals(3, result.lines().size());
		for (LineBlame blame : result.lines()) {
			assertEquals(1, blame.introducedInVersion());
			assertEquals("alice", blame.author());
		}
	}

	@Test
	void insertedLineGetsNewBlame() {
		VersionEntry v1 = entry(1, "alice", T1, null, "alpha\ngamma\n");
		VersionEntry v2 = entry(2, "bob", T2, "add beta", "alpha\nbeta\ngamma\n");

		PageAnnotation result = PageAnnotator.annotate("Main", List.of(v1, v2));

		assertEquals(2, result.currentVersion());
		assertEquals(3, result.lines().size());
		assertBlame(result.lines().get(0), 1, 1, "alice");
		assertBlame(result.lines().get(1), 2, 2, "bob");
		assertBlame(result.lines().get(2), 3, 1, "alice");
	}

	@Test
	void deletedLineDropsFromAnnotation() {
		VersionEntry v1 = entry(1, "alice", T1, null, "alpha\nbeta\ngamma\n");
		VersionEntry v2 = entry(2, "bob", T2, "remove beta", "alpha\ngamma\n");

		PageAnnotation result = PageAnnotator.annotate("Main", List.of(v1, v2));

		assertEquals(2, result.lines().size());
		assertBlame(result.lines().get(0), 1, 1, "alice");
		assertBlame(result.lines().get(1), 2, 1, "alice");
	}

	@Test
	void changedLineGetsNewBlame() {
		VersionEntry v1 = entry(1, "alice", T1, null, "alpha\nbeta\ngamma\n");
		VersionEntry v2 = entry(2, "bob", T2, "rename beta", "alpha\nBETA\ngamma\n");

		PageAnnotation result = PageAnnotator.annotate("Main", List.of(v1, v2));

		assertBlame(result.lines().get(0), 1, 1, "alice");
		assertBlame(result.lines().get(1), 2, 2, "bob");
		assertBlame(result.lines().get(2), 3, 1, "alice");
	}

	@Test
	void whitespaceOnlyChangePreservesBlame() {
		VersionEntry v1 = entry(1, "alice", T1, null, "alpha\nbeta\n");
		// v2 only changes indentation/whitespace — must keep alice's blame on both lines.
		VersionEntry v2 = entry(2, "bob", T2, "reformat", "alpha   \n  beta\n");

		PageAnnotation result = PageAnnotator.annotate("Main", List.of(v1, v2));

		assertBlame(result.lines().get(0), 1, 1, "alice");
		assertBlame(result.lines().get(1), 2, 1, "alice");
	}

	@Test
	void revertGetsLatestBlame() {
		// alice writes "alpha"; bob changes to "ALPHA"; alice reverts back to "alpha".
		// Like git blame without -w: the revert is treated as a fresh introduction (v3 / alice).
		VersionEntry v1 = entry(1, "alice", T1, null, "alpha\n");
		VersionEntry v2 = entry(2, "bob", T2, "shout", "ALPHA\n");
		VersionEntry v3 = entry(3, "alice", T3, "revert", "alpha\n");

		PageAnnotation result = PageAnnotator.annotate("Main", List.of(v1, v2, v3));

		assertEquals(3, result.currentVersion());
		assertBlame(result.lines().get(0), 1, 3, "alice");
	}

	@Test
	void unsortedHistoryIsHandled() {
		VersionEntry v1 = entry(1, "alice", T1, null, "alpha\n");
		VersionEntry v2 = entry(2, "bob", T2, null, "alpha\nbeta\n");

		PageAnnotation result = PageAnnotator.annotate("Main", List.of(v2, v1));

		assertEquals(2, result.currentVersion());
		assertBlame(result.lines().get(0), 1, 1, "alice");
		assertBlame(result.lines().get(1), 2, 2, "bob");
	}

	@Test
	void emptyVersionYieldsNoLines() {
		VersionEntry v1 = entry(1, "alice", T1, null, "");
		PageAnnotation result = PageAnnotator.annotate("Main", List.of(v1));
		assertEquals(0, result.lines().size());
	}

	@Test
	void changeNoteIsCarriedForBlame() {
		VersionEntry v1 = entry(1, "alice", T1, "initial", "alpha\n");
		VersionEntry v2 = entry(2, "bob", T2, "fix typo", "alpha\nbeta\n");

		PageAnnotation result = PageAnnotator.annotate("Main", List.of(v1, v2));

		assertEquals("initial", result.lines().get(0).changeNote());
		assertEquals("fix typo", result.lines().get(1).changeNote());
	}

	private static VersionEntry entry(int version, String author, Instant date,
									  String changeNote, String text) {
		return new VersionEntry(version, author, date, changeNote, text);
	}

	private static void assertBlame(LineBlame blame, int lineNumber, int introducedInVersion, String author) {
		assertEquals(lineNumber, blame.lineNumber(), "lineNumber");
		assertEquals(introducedInVersion, blame.introducedInVersion(), "introducedInVersion");
		assertEquals(author, blame.author(), "author");
	}
}
