package com.denkbares.knowwe.changeannotations;

import java.util.List;

/**
 * Result of annotating a wiki page: one {@link LineBlame} per line of the current page text.
 * The {@code lines} list is index-aligned with the (1-based) line numbers of the page text
 * — entry {@code i} carries blame for line {@code i + 1}.
 *
 * @param pageName       wiki page identifier
 * @param currentVersion the version this annotation describes (typically the latest version)
 * @param lines          immutable list of per-line blame entries; ordered by line number ascending
 */
public record PageAnnotation(
		String pageName,
		int currentVersion,
		List<LineBlame> lines
) {
	public PageAnnotation {
		if (pageName == null) throw new NullPointerException("pageName");
		if (currentVersion < 1) {
			throw new IllegalArgumentException("currentVersion must be >= 1: " + currentVersion);
		}
		if (lines == null) throw new NullPointerException("lines");
		lines = List.copyOf(lines);
	}
}
