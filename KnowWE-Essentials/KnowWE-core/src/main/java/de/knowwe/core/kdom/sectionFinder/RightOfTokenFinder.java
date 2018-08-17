/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
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
 * A section finder that takes the right side of a specified token (literal string or regular expression).
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 17.08.2018
 */
public class RightOfTokenFinder implements SectionFinder {

	private static final QuoteSet QUOTES = new QuoteSet(Strings.QUOTE_DOUBLE);
	private final Pattern token;

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a literal string (NOT a regular expression). If the token is not found at all, no match
	 * is created.
	 *
	 * @param literalToken the token string
	 */
	public RightOfTokenFinder(String literalToken) {
		this.token = Pattern.compile(Pattern.quote(literalToken));
	}

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a regular expression. If the token is not found at all, no match is created.
	 *
	 * @param token the token's regular expression
	 */
	public RightOfTokenFinder(Pattern token) {
		this.token = token;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

		List<SectionFinderResult> result = new ArrayList<>();
		List<StringFragment> findings = Strings.splitUnquoted(text, token, true, QUOTES);

		if (findings.size() > 1) {
			int start = findings.get(1).getStartTrimmed();
			int end = findings.get(1).getEndTrimmed();
			if (start < end) {
				SectionFinderResult s = new SectionFinderResult(start, end);
				result.add(s);
			}
		}

		return result;
	}
}
