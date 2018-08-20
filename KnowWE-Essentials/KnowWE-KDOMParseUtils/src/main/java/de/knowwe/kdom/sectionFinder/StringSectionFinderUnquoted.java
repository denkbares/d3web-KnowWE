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

package de.knowwe.kdom.sectionFinder;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Simple SectionFinder that identifies the first (or last if flagged) occurrence of a specific String that is
 * _unquoted_ in the text, i.e., there is not an odd number of quotes '"' before it {@link SplitUtility}
 *
 * @author Jochen
 */

public class StringSectionFinderUnquoted extends AbstractSingleResultFinder {

	private final String[] strings;
	private boolean last = false;

	public StringSectionFinderUnquoted(String s) {
		this.strings = new String[] { s };
	}

	public StringSectionFinderUnquoted(String s, boolean last) {
		this.strings = new String[] { s };
		this.last = last;
	}

	public StringSectionFinderUnquoted(String... tokens) {
		this.strings = tokens;
		this.last = false;
	}

	@Override
	public SectionFinderResult lookForSection(String text, Section<?> father, Type type) {
		int flags = Strings.UNQUOTED | Strings.CASE_INSENSITIVE;
		if (last) flags |= Strings.LAST_INDEX;
		int index = Strings.indexOf(text, flags, strings);
		if (index == -1) return null;

		if (strings.length == 1) {
			return new SectionFinderResult(index, index + strings[0].length());
		}

		// if we have multiple strings, we have to examine which of the tokens have been matched
		for (String string : strings) {
			if (text.regionMatches(true, index, string, 0, string.length())) {
				return new SectionFinderResult(index, index + string.length());
			}
		}
		// if we found the token, any of the strings should have matched the region
		throw new IllegalStateException();
	}
}
