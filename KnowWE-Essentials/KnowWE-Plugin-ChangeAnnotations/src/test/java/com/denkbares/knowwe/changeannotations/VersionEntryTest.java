package com.denkbares.knowwe.changeannotations;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VersionEntryTest {

	@Test
	void rejectsVersionBelowOne() {
		assertThrows(IllegalArgumentException.class,
				() -> new VersionEntry(0, "a", Instant.EPOCH, null, ""));
	}

	@Test
	void rejectsNullText() {
		assertThrows(NullPointerException.class,
				() -> new VersionEntry(1, "a", Instant.EPOCH, null, null));
	}

	@Test
	void allowsNullChangeNote() {
		VersionEntry entry = new VersionEntry(1, "a", Instant.EPOCH, null, "text");
		assertNull(entry.changeNote());
	}
}
