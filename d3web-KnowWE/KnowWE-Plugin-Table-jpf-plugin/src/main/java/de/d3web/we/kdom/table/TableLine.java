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

package de.d3web.we.kdom.table;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * TableLine.
 * 
 * Represents a line of a WIKI table.
 * 
 * @author smark, Sebastian Furth
 * 
 */
public class TableLine extends DefaultAbstractKnowWEObjectType {

	public TableLine() {
		childrenTypes.add(new TableCell());
		sectionFinder = new TableLineSectionFinder();
		setCustomRenderer(new TableLineRenderer());
	}

	/**
	 * Returns for a TableLine whether it is a TableHeadLine or not. Therefore
	 * it is checked if the TableCell has only childs which are TableHead cells
	 * 
	 * @author Sebastian Furth
	 * @created 19/10/2010
	 * @param tableLine the table line which will be checked
	 * @return true if table head line, otherwise false.
	 */
	public static boolean isHeaderLine(Section<TableLine> tableLine) {

		List<Section<TableCell>> cells = new LinkedList<Section<TableCell>>();
		tableLine.findSuccessorsOfType(TableCell.class, cells);

		boolean isHeaderLine = true;
		for (Section<TableCell> cell : cells) {
			if (!TableCell.isTableHead(cell)) {
				isHeaderLine = false;
			}
		}

		return isHeaderLine;
	}

	/**
	 * Handles the table lines. Introduced to the fact, that the
	 * LineSectionFinder allows empty lines. In the table context only lines
	 * with content are important (line break after Table tag had been rendered
	 * as cell).
	 * 
	 * @author smark, Sebastian Furth
	 * @see ISectionFinder
	 */
	public class TableLineSectionFinder implements ISectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, KnowWEObjectType type) {

			String lineRegex = "\\s*(\\|{1,2}.*)+\\r?\\n?";
			Pattern linePattern = Pattern.compile(lineRegex);

			Matcher tagMatcher = linePattern.matcher(text);
			List<SectionFinderResult> resultRegex = new LinkedList<SectionFinderResult>();

			while (tagMatcher.find()) {
				resultRegex.add(new SectionFinderResult(tagMatcher.start(), tagMatcher.end()));
			}
			return resultRegex;
		}
	}



}
