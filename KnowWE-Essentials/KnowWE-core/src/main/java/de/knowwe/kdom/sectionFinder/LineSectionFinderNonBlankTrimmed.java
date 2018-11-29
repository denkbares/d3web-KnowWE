/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.sectionFinder;

/**
 * Section finder that detects non-empty (or blank) lines and creates a trimmed section for each of these lines. Even if
 * multiple subsequent lines have content, each of those lines is sectionized into an individual section.
 */
public class LineSectionFinderNonBlankTrimmed extends SplittingSectionFinder {

	private static final LineSectionFinderNonBlankTrimmed instance = new LineSectionFinderNonBlankTrimmed();

	private LineSectionFinderNonBlankTrimmed() {
		super("[\\s\\h\\v]*\\n[\\s\\h\\v]*|\\A[\\s\\h\\v]+|[\\s\\h\\v]+\\z");
	}

	/**
	 * Returns the singleton instance for this section finder.
	 */
	public static LineSectionFinderNonBlankTrimmed getInstance() {
		return instance;
	}
}
