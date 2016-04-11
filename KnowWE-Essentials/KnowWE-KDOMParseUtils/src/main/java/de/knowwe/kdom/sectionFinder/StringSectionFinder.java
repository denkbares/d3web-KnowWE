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

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Simple SectionFinder that identifies the first (or last if flagged)
 * occurrence of a specific String in the text
 * 
 * 
 * @author Jochen
 * 
 */

public class StringSectionFinder extends AbstractSingleResultFinder {

	private final String string;
	private boolean last = false;

	public StringSectionFinder(String s) {
		this.string = s;
	}

	public StringSectionFinder(String s, boolean last) {
		this.string = s;
		this.last = last;
	}

	@Override
	public SectionFinderResult lookForSection(String text, Section<?> father, Type type) {

		int index;

		if (last) index = text.lastIndexOf(string);
		else index = text.indexOf(string);

		if (index == -1) return null;

		return new SectionFinderResult(index, index + string.length());
	}

}
