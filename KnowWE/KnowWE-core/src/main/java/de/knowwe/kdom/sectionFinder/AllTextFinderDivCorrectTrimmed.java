/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
 * This SectionFinder takes all the text it gets without any whitespaces at the
 * beginning and with only one newline character at the end. This is the correct
 * Finder for text being wrapped by the HTML-div-tag (<div>), because if the
 * starting newline character is within the tag, or the ending newline character
 * is outside the tag, it will create an empty line.
 * 
 * @author Max Diez
 * @created 12.08.2010
 */
public class AllTextFinderDivCorrectTrimmed implements SectionFinder {

	public AllTextFinderDivCorrectTrimmed() {
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, Type type) {
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		String trimmed = text.trim();
		if (trimmed.length() == 0) return result;
		int leadingSpaces = text.indexOf(trimmed);
		// Find first newline character, string afterwards is cut off
		String trimmedEnd = text.substring(trimmed.length() + leadingSpaces);
		Matcher m = Pattern.compile("[\\n|\\f|x0B]").matcher(trimmedEnd);
		int posFirstNewLine = trimmed.length() + leadingSpaces;
		if (m.find()) posFirstNewLine += m.start() + 1;

		result.add(new SectionFinderResult(leadingSpaces, posFirstNewLine));
		return result;
	}

}
