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

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.MultiSectionFinder;
import de.d3web.we.kdom.Section;

/**
 * This SectionFinder is created with an array of Strings. It looks for unquoted
 * occurrences of these Strings and creates Sections from it.
 * 
 * @author Jochen
 * 
 */
public class OneOfStringEnumUnquotedFinder extends SectionFinder {

	private final MultiSectionFinder msf;

	public OneOfStringEnumUnquotedFinder(String[] values) {
		msf = new MultiSectionFinder();
		for (int i = 0; i < values.length; i++) {
			msf.addSectionFinder(new UnquotedExpressionFinder(values[i]));
		}
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
		return msf.lookForSections(text, father, type);
	}
}
