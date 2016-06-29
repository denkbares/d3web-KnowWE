/*
 * Copyright (C) 2013 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Finds Sections starting with the opening and ending with the closing String (given in the constructor). If there are
 * nested sections with the same opening and closing, they are ignored.
 * <p/>
 * <b>Example:</b>
 * <p/>
 * <p/>
 * In the Text:<br> <br> "Calculation 5 - (2 * (4 / 2)) = 1"<br> <br> a new {@link NestedBracketsFinder}("(", ")") will
 * find the Section<br> <br> "(2 * (4 / 2))"
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 24.04.2013
 */
public class NestedBracketsFinder implements SectionFinder {

	private final Pattern pattern;

	/**
	 * There are some restrictions when using this constructor: <b>You are not allowed to create new capture-groups in
	 * the given regex. Only us (?: ... ), which does create a group but not captured group.</b>
	 */
	public NestedBracketsFinder(String opening, Pattern optionalOpeningSuffix, String closing) {
		String optionalOpeningSuffixRegex = optionalOpeningSuffix == null ?
				"" : "(" + optionalOpeningSuffix.pattern() + ")?";
		String regex = "(" + Pattern.quote(opening) + ")" + optionalOpeningSuffixRegex + "|("
				+ Pattern.quote(closing) + "(?:\\r?\\n)?)";
		this.pattern = Pattern.compile(regex);
	}

	public NestedBracketsFinder(String opening, String optionalOpeningSuffix, String closing) {
		this(opening,
				optionalOpeningSuffix == null ?
						null :
						Pattern.compile(Pattern.quote(optionalOpeningSuffix)),
				closing);
	}

	public NestedBracketsFinder(String opening, String closing) {
		this(opening, (Pattern) null, closing);
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		Matcher matcher = pattern.matcher(text);
		int depth = 0;
		int start = -1;
		List<SectionFinderResult> results = new ArrayList<>();
		while (matcher.find()) {
			int groupCount = matcher.groupCount();
			boolean foundNesting = matcher.group(1) != null;
			boolean foundOpening = foundNesting
					&& (groupCount == 3 ? matcher.group(2) != null : true);
			boolean foundClosing = matcher.group(groupCount) != null;
			if (foundOpening) {
				if (depth == 0) {
					start = matcher.start(1);
				}
			}
			if (foundNesting && start > -1) {
				depth++;
			}
			if (foundClosing && start > -1) {
				depth--;
				if (depth == 0) {
					results.add(new SectionFinderResult(start, matcher.end(groupCount)));
					start = -1;
				}
			}

		}
		return results;
	}
}
