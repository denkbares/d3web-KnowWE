/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.core.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * This SectionFinder takes all the text given, but performing a trim() operation cutting off whitespace characters.
 *
 * @author Jochen
 */
public class AllTextFinderTrimmed implements SectionFinder {

	private static AllTextFinderTrimmed instance;
	private final TrimType trimType;

	public enum TrimType {
		SPACES,
		SPACES_AND_LINE_BREAKS,
		BLANK_LINES,
		BLANK_LINES_AND_TRAILING_LINE_BREAK
	}

	public static AllTextFinderTrimmed getInstance() {
		if (instance == null) instance = new AllTextFinderTrimmed();
		return instance;
	}

	public AllTextFinderTrimmed() {
		this(TrimType.SPACES);
	}

	// for backwards compatibility
	public AllTextFinderTrimmed(boolean trimBlankLinesAndTrailingLineBreakOnly) {
		this(trimBlankLinesAndTrailingLineBreakOnly ? TrimType.BLANK_LINES_AND_TRAILING_LINE_BREAK : TrimType.SPACES);
	}

	public AllTextFinderTrimmed(TrimType type) {
		this.trimType = type;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		List<SectionFinderResult> result = new ArrayList<>();

		String trimmed;
		switch (trimType) {
			case SPACES:
				trimmed = Strings.trim(text);
				break;
			case SPACES_AND_LINE_BREAKS:
				trimmed = Strings.trim(Strings.trimBlankLinesAndTrailingLineBreak(text));
				break;
			case BLANK_LINES:
				trimmed = Strings.trimBlankLines(text);
				break;
			case BLANK_LINES_AND_TRAILING_LINE_BREAK:
				trimmed = Strings.trimBlankLinesAndTrailingLineBreak(text);
				break;
			default:
				throw new IllegalStateException();
		}

		if (trimmed.isEmpty()) return result;
		int start = text.indexOf(trimmed);
		int end = start + trimmed.length();
		result.add(new SectionFinderResult(start, end));
		return result;
	}
}
