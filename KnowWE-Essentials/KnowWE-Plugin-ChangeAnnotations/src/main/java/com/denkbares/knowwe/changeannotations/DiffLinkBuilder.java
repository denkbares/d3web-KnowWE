package com.denkbares.knowwe.changeannotations;

import org.jetbrains.annotations.Nullable;

/**
 * Strategy for turning a version number into a URL that opens the diff between that version
 * and its predecessor. Supplied by the wiki-side caller (e.g. the JSPWiki connector points
 * this at {@code Diff.jsp?page=...&r1=...&r2=...}). The renderer only emits a link when the
 * builder returns a non-null URL.
 */
@FunctionalInterface
public interface DiffLinkBuilder {

	/**
	 * Returns the URL for the diff between {@code version} and its predecessor, or
	 * {@code null} if no link should be rendered (typically for {@code version == 1}).
	 */
	@Nullable
	String diffLink(int version);

	/** Builder that never produces a link — useful for tests and headless rendering. */
	DiffLinkBuilder NONE = version -> null;
}
