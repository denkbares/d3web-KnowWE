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

package de.d3web.we.kdom.sectionFinder;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.SplitUtility;

/**
 * Simple SectionFinder that identifies the first (or last if flagged)
 * occurrence of a specific String that is _unquoted_ in the text, i.e., there
 * is not an odd number of quotes '"' before it {@link SplitUtility}
 * 
 * 
 * @author Jochen
 * 
 */

public class StringSectionFinderUnquoted extends AbstractSingleResultFinder {

	private final String string;
	private boolean last = false;

	public StringSectionFinderUnquoted(String s) {
		this.string = s;
	}

	public StringSectionFinderUnquoted(String s, boolean last) {
		this.string = s;
		this.last = last;
	}

	@Override
	public SectionFinderResult lookForSection(String text, Section father, KnowWEObjectType type) {

		int index;

		if (last) {
			index = SplitUtility.lastIndexOfUnquoted(text, string);
		}
		else {
			index = SplitUtility.indexOfUnquoted(text, string);
		}

		if (index == -1) return null;

		return new SectionFinderResult(index, index + string.length());
	}

}
