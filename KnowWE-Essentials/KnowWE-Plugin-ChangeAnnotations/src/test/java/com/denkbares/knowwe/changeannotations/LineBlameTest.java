package com.denkbares.knowwe.changeannotations;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineBlameTest {

	@Test
	void rejectsLineNumberBelowOne() {
		assertThrows(IllegalArgumentException.class,
				() -> new LineBlame(0, 1, "author", Instant.EPOCH, null));
	}

	@Test
	void rejectsVersionBelowOne() {
		assertThrows(IllegalArgumentException.class,
				() -> new LineBlame(1, 0, "author", Instant.EPOCH, null));
	}

	@Test
	void rejectsNullAuthor() {
		assertThrows(NullPointerException.class,
				() -> new LineBlame(1, 1, null, Instant.EPOCH, null));
	}

	@Test
	void rejectsNullDate() {
		assertThrows(NullPointerException.class,
				() -> new LineBlame(1, 1, "author", null, null));
	}

	@Test
	void allowsNullChangeNote() {
		LineBlame blame = new LineBlame(1, 3, "author", Instant.EPOCH, null);
		assertNull(blame.changeNote());
		assertEquals(3, blame.introducedInVersion());
	}
}
