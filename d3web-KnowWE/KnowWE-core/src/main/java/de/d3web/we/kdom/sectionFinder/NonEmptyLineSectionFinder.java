package de.d3web.we.kdom.sectionFinder;

import java.util.regex.Pattern;

public class NonEmptyLineSectionFinder extends RegexSectionFinder {

	public NonEmptyLineSectionFinder() {
		super(".+\\r?\\n", Pattern.MULTILINE, 0);
	}

}
