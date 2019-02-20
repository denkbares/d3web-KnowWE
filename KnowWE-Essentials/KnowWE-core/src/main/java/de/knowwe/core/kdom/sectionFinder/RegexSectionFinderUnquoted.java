/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.denkbares.strings.QuoteSet;
import com.denkbares.strings.StringFragment;
import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Section finder that returns every match of a specified regular expression, that is matched in the unquoted areas of a
 * given section text. If not returns empty finder results.
 */
public class RegexSectionFinderUnquoted implements SectionFinder {

	private static final QuoteSet DOUBLE = new QuoteSet(Strings.QUOTE_DOUBLE);
	private final Pattern pattern;

	public RegexSectionFinderUnquoted(String p) {
		this(p, 0);
	}

	public RegexSectionFinderUnquoted(String p, int patternmod) {
		this(Pattern.compile(p, patternmod));
	}

	public RegexSectionFinderUnquoted(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		ArrayList<SectionFinderResult> result = new ArrayList<>();

		// we split at the split symbol. After that each non-empty in-between area is a finder result
		int from = 0;
		for (StringFragment match : Strings.splitUnquoted(text, pattern, true, DOUBLE)) {
			int to = match.getStart();
			if (from < to) {
				result.add(new SectionFinderResult(from, to));
			}
			from = match.getEnd();
		}

		// and finally append the rest, if there is a trailing match
		if (from < text.length()) {
			result.add(new SectionFinderResult(from, text.length()));
		}

		return result;
	}
}
