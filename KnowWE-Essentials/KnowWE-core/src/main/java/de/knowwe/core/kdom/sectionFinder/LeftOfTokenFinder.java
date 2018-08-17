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
 * A section finder that takes the left side of a specified token (literal string or regular expression).
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 17.08.2018
 */
public class LeftOfTokenFinder implements SectionFinder {

	private static final QuoteSet QUOTES = new QuoteSet(Strings.QUOTE_DOUBLE);
	private final Pattern token;
	private final boolean allOnMissingToken;

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a literal string (NOT a regular expression). If the token is not found at all, the whole
	 * parent section is consumed.
	 *
	 * @param literalToken the token string
	 */
	public LeftOfTokenFinder(String literalToken) {
		this(literalToken, true);
	}

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a literal string (NOT a regular expression). If the token is not found at all, the flag
	 * allOnMissingToken defines if the whole parent section is consumed (if true), or no match is created at all (if
	 * false).
	 *
	 * @param literalToken      the token string
	 * @param allOnMissingToken if a missing token should create a full-match
	 */
	public LeftOfTokenFinder(String literalToken, boolean allOnMissingToken) {
		this(Pattern.compile(Pattern.quote(literalToken)), allOnMissingToken);
	}

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a regular expression. If the token is not found at all, the whole parent section is
	 * consumed.
	 *
	 * @param token the token's regular expression
	 */
	public LeftOfTokenFinder(Pattern token) {
		this(token, true);
	}

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a regular expression. If the token is not found at all, the flag allOnMissingToken
	 * defines if the whole parent section is consumed (if true), or no match is created at all (if false).
	 *
	 * @param token             the token's regular expression
	 * @param allOnMissingToken if a missing token should create a full-match
	 */
	public LeftOfTokenFinder(Pattern token, boolean allOnMissingToken) {
		this.token = token;
		this.allOnMissingToken = allOnMissingToken;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

		List<SectionFinderResult> result = new ArrayList<>();
		List<StringFragment> findings = Strings.splitUnquoted(text, token, true, QUOTES);

		if (allOnMissingToken || findings.size() > 1) {
			int start = findings.get(0).getStartTrimmed();
			int end = findings.get(0).getEndTrimmed();
			if (start < end) {
				SectionFinderResult s = new SectionFinderResult(start, end);
				result.add(s);
			}
		}

		return result;
	}
}
