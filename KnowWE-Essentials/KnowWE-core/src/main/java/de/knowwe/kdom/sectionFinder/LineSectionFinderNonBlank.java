package de.knowwe.kdom.sectionFinder;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * Returns lines that are not blank, but the line is not trimmed, and more importantly, contains the line break at the
 * end of the line.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 2019-05-13
 */
public class LineSectionFinderNonBlank extends RegexSectionFinder {

	private static final LineSectionFinderNonBlank instance = new LineSectionFinderNonBlank();

	private LineSectionFinderNonBlank() {
		super("^[^\r\n]+\r?\n", Pattern.MULTILINE);
	}

	public static LineSectionFinderNonBlank getInstance() {
		return instance;
	}
}
