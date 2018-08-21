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
	private final boolean multiple;

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a literal string (NOT a regular expression). If the token is not found at all, no match
	 * is created. If the token is found multiple times, only the first match is created.
	 *
	 * @param literalToken the token string
	 */
	public RightOfTokenFinder(String literalToken) {
		this(literalToken, false);
	}

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a literal string (NOT a regular expression). If the token is not found at all, no match
	 * is created.
	 *
	 * @param literalToken the token string
	 * @param multiple     if the tokenizer should allow multiple matches or only the first one (and ignore text after
	 *                     the second occurrence of the token)
	 */
	public RightOfTokenFinder(String literalToken, boolean multiple) {
		this(Pattern.compile(Pattern.quote(literalToken)), multiple);
	}

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a regular expression. If the token is not found at all, no match is created. If the token
	 * is found multiple times, only the first match is created.
	 *
	 * @param token the token's regular expression
	 */
	public RightOfTokenFinder(Pattern token) {
		this(token, false);
	}

	/**
	 * Creates a new section finder that takes all the (trimmed) text, on the left side of a specified (unquoted) token.
	 * The token is defined as a regular expression. If the token is not found at all, no match is created.
	 *
	 * @param token    the token's regular expression
	 * @param multiple if the tokenizer should allow multiple matches or only the first one (and ignore text after the
	 *                 second occurrence of the token)
	 */
	public RightOfTokenFinder(Pattern token, boolean multiple) {
		this.token = token;
		this.multiple = multiple;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

		List<SectionFinderResult> result = new ArrayList<>();
		List<StringFragment> findings = Strings.splitUnquoted(text, token, true, QUOTES);

		int maxIndex = findings.size() - 1;
		if (maxIndex > 1 && !multiple) maxIndex = 1;
		for (int i = 1; i <= maxIndex; i++) {
			int start = findings.get(i).getStartTrimmed();
			int end = findings.get(i).getEndTrimmed();
			if (start < end) {
				SectionFinderResult s = new SectionFinderResult(start, end);
				result.add(s);
			}
		}

		return result;
	}
}
