/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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
package de.knowwe.instantedit.table;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Section finder to find wiki table markup as a section.
 * <p>
 * <b>Note:</b><br>
 * Previous implementation using RegexSectionFinder produced an
 * StackOverflowError on large Tables!
 * 
 * @author volker_belli
 * @created 16.03.2012
 */
public class TableSectionFinder implements SectionFinder {

	private final static String TABLE_LINE_REGEXP =
			// A line starting with pipes (1 or 2),
			// optional followed by a non-pipe and other characters
			// followed by one (1) return character
			"^\\|\\|?([^\\|].*)?$\\r?\\n?\\r?";

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		ArrayList<SectionFinderResult> result = new ArrayList<>();
		Pattern TABLE_LINE = Pattern.compile(TABLE_LINE_REGEXP, Pattern.MULTILINE);
		Matcher m = TABLE_LINE.matcher(text);

		int end = 0;
		int tableStart = -1;
		int tableEnd = -1;
		while (m.find(end)) {
			int start = m.start();
			end = m.end();
			if (tableEnd == start) {
				// table continued (with no other lines in between)
				tableEnd = end;
			}
			else {
				// we had a table before, also found next table
				// but there are other lines (characters in between)
				addResultIfAvailable(result, tableStart, tableEnd);
				tableStart = start;
				tableEnd = end;
			}
			// detect if we reached the end,
			// otherwise we get an IndexOutOfBoundsException from
			// "m.find(...)"
			if (end >= text.length()) break;
		}
		addResultIfAvailable(result, tableStart, tableEnd);
		return result;
	}

	private void addResultIfAvailable(ArrayList<SectionFinderResult> result, int tableStart, int tableEnd) {
		if (tableStart != -1) {
			// if no new table has been started
			result.add(new SectionFinderResult(tableStart, tableEnd));
		}
	}

}