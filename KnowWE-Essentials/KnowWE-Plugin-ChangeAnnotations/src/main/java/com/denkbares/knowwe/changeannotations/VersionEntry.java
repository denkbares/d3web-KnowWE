package com.denkbares.knowwe.changeannotations;

import java.time.Instant;

import org.jetbrains.annotations.Nullable;

/**
 * Pure-data input for the page annotator: one entry per page version. Decouples the annotator
 * from JSPWiki's {@code PageManager} so the algorithm can be unit-tested with synthetic
 * histories.
 *
 * @param version    1-based JSPWiki page version
 * @param author     principal name as recorded by JSPWiki for this version
 * @param date       save timestamp of this version
 * @param changeNote optional commit-style change note from this save
 * @param text       full text of the page at this version
 */
public record VersionEntry(
		int version,
		String author,
		Instant date,
		@Nullable String changeNote,
		String text
) {
	public VersionEntry {
		if (version < 1) {
			throw new IllegalArgumentException("version must be >= 1: " + version);
		}
		if (author == null) throw new NullPointerException("author");
		if (date == null) throw new NullPointerException("date");
		if (text == null) throw new NullPointerException("text");
	}
}
