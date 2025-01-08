/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * A section finder that uses a regex to split the section. Each SectionFinderResult contains the text up to the next
 * match (including or excluding the matched split symbol at the sections end, depending if includeSplitSymbol is set to
 * true or false).
 *
 * @author Reinhard Hatko Created on: 10.12.2009
 */
public class SplittingSectionFinder implements SectionFinder {

	private final boolean includeSplitSymbol;
	private final boolean includeEmptySections;
	private final boolean trimIfNoSplit;
	private final Pattern pattern;

	/**
	 * Creates a new splitting sectionizer that splits the parent text at the specified split symbol, that is assumed to
	 * be a regular expression pattern string. The split symbol is not included in the matched regions and therefore
	 * left unmatched. Empty sections between two split symbols are skipped (but not blank ones, if the split symbol
	 * does not consume the whitespaces!).
	 *
	 * @param splitRegex the regular expression to match the split symbols
	 */
	public SplittingSectionFinder(String splitRegex) {
		this(false, splitRegex);
	}

	/**
	 * Creates a new splitting sectionizer that splits the parent text at the specified split symbol, that is assumed to
	 * be a regular expression pattern string. Empty sections between two split symbols are skipped (but not blank ones,
	 * if the split symbol does not consume the whitespaces!).
	 *
	 * @param includeSplitSymbol true, if the split symbol should be appended at the end of the resulting sections
	 * @param splitRegex         the regular expression to match the split symbols
	 */
	public SplittingSectionFinder(boolean includeSplitSymbol, String splitRegex) {
		this(includeSplitSymbol, false, splitRegex);
	}

	/**
	 * Creates a new splitting sectionizer that splits the parent text at the specified split symbol, that is assumed to
	 * be a regular expression pattern string.
	 *
	 * @param includeSplitSymbol   true, if the split symbol should be appended at the end of the resulting sections
	 * @param includeEmptySections true, if empty sections between two split symbols should be created
	 * @param splitRegex           the regular expression to match the split symbols
	 */
	public SplittingSectionFinder(boolean includeSplitSymbol, boolean includeEmptySections, String splitRegex) {
		this(includeSplitSymbol, includeEmptySections, false, splitRegex);
	}

	public SplittingSectionFinder(boolean includeSplitSymbol, boolean includeEmptySections, boolean trimIfNoSplit, String splitRegex) {
		this.includeSplitSymbol = includeSplitSymbol;
		this.includeEmptySections = includeEmptySections;
		this.trimIfNoSplit = trimIfNoSplit;
		this.pattern = Pattern.compile(splitRegex);
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

		Matcher tagMatcher = pattern.matcher(text);
		List<SectionFinderResult> resultRegex = new ArrayList<>();
		int nextStart = 0;
		boolean noMatch = true;
		while (tagMatcher.find()) {
			addSection(resultRegex, nextStart, includeSplitSymbol ? tagMatcher.end() : tagMatcher.start());
			noMatch = false;
			nextStart = tagMatcher.end();
		}
		// append section result after last split character / substring
		// also adds the total text, if there is no match (!)
		if (nextStart < text.length()) {
			int end = text.length();
			if (noMatch && trimIfNoSplit) {
				while (nextStart < end && Strings.isWhitespace(text.charAt(nextStart))) nextStart++;
				while (end >= nextStart && Strings.isWhitespace(text.charAt(end - 1))) end--;
			}
			addSection(resultRegex, nextStart, end);
		}
		return resultRegex;
	}

	private void addSection(List<SectionFinderResult> result, int start, int end) {
		if (includeEmptySections || start < end) {
			result.add(new SectionFinderResult(start, end));
		}
	}
}
