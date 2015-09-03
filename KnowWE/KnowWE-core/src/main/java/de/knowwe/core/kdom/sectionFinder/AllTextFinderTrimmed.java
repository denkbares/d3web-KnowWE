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

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * @author Jochen
 * 
 *         This SectionFinder takes all the text given, but performing a trim()
 *         operation cutting off whitespace characters
 * 
 */
public class AllTextFinderTrimmed implements SectionFinder {

	private static AllTextFinderTrimmed instance;
	private final boolean trimBlankLinesOnly;

	public static AllTextFinderTrimmed getInstance() {
		if (instance == null) instance = new AllTextFinderTrimmed();
		return instance;
	}

	public AllTextFinderTrimmed() {
		this(false);
	}

	public AllTextFinderTrimmed(boolean trimBlankLinesOnly) {
		this.trimBlankLinesOnly = trimBlankLinesOnly;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

		String trimmed = trimBlankLinesOnly ? Strings.trimBlankLines(text) : Strings.trim(text);
		if (trimmed.length() == 0) return result;
		int leadingSpaces = text.indexOf(trimmed);
		int followingSpaces = text.length()
				- (trimmed.length() + leadingSpaces);

		result.add(new SectionFinderResult(leadingSpaces, text.length()
				- followingSpaces));
		return result;
	}

}
