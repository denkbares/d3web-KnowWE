/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.sectionFinder;

public class LineSectionFinderNonBlankTimmed extends SplittingSectionFinder {

	private static final LineSectionFinderNonBlankTimmed instance = new LineSectionFinderNonBlankTimmed();

	protected LineSectionFinderNonBlankTimmed() {
		super("[\\s\\h\\v]*\\n[\\s\\h\\v]*");
	}

	public static LineSectionFinderNonBlankTimmed getInstance() {
		return instance;
	}
}
