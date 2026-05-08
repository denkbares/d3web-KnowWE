package com.denkbares.knowwe.changeannotations;

import java.time.Instant;

import org.jetbrains.annotations.Nullable;

/**
 * Blame information for a single line of a wiki page: which version introduced the line in
 * its current form, who authored that version, when, and (optionally) the change note from
 * that save.
 *
 * @param lineNumber           1-based position in the current page text
 * @param introducedInVersion  1-based JSPWiki page version
 * @param author               principal name as recorded by JSPWiki
 * @param date                 timestamp of the introducing version
 * @param changeNote           optional commit-style change note from the introducing version
 */
public record LineBlame(
		int lineNumber,
		int introducedInVersion,
		String author,
		Instant date,
		@Nullable String changeNote
) {
	public LineBlame {
		if (lineNumber < 1) {
			throw new IllegalArgumentException("lineNumber must be >= 1: " + lineNumber);
		}
		if (introducedInVersion < 1) {
			throw new IllegalArgumentException("introducedInVersion must be >= 1: " + introducedInVersion);
		}
		if (author == null) throw new NullPointerException("author");
		if (date == null) throw new NullPointerException("date");
	}
}
