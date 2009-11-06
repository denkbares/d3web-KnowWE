/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.table;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.TextLine;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

/**
 * TableLine.
 * 
 * Represents a line of a WIKI table.
 * 
 * @see TextLine
 */
public class TableLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		childrenTypes.add(new TableCell());
		sectionFinder = new TableLineSectionFinder();
		subtreeHandler.add(0, new TableLineHandler());
		setCustomRenderer(new TableLineRenderer());
	}

	/**
	 * Handles the table lines. Introduced to the fact, that the
	 * LineSectionFinder allows empty lines. In the table context only lines
	 * with content are important (line break after Table tag had been rendered
	 * as cell).
	 * 
	 * @author smark
	 * @see SectionFinder
	 */
	public class TableLineSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			String lineRegex = ".+(\\r?\\n)";
			Pattern linePattern = Pattern.compile(lineRegex, Pattern.MULTILINE);

			Matcher tagMatcher = linePattern.matcher(text);
			ArrayList<SectionFinderResult> resultRegex = new ArrayList<SectionFinderResult>();

			while (tagMatcher.find()) {
				resultRegex.add(new SectionFinderResult(tagMatcher.start(),
						tagMatcher.end()));
			}
			return resultRegex;
		}
	}

	private class TableLineHandler implements ReviseSubTreeHandler {

		@Override
		public void reviseSubtree(Section s) {
			Section colHeaderCell = s.findSuccessor(TableCell.class);
			if (colHeaderCell != null) {

				Section content = colHeaderCell
						.findChildOfType(TableCellContent.class);
				if (content != null) {
					AbstractKnowWEObjectType colHeaderType = new TableColumnHeaderCellContent();
					content.setType(colHeaderType);
				}
			}
		}
	}
}
