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

package de.knowwe.kdom.table;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.LineBreak;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.utils.Patterns;

/**
 * TableCell class.
 * 
 * This class represents a cell of a WIKI table. Each cell has a start and a
 * content area.
 * 
 * @author smark, Sebastian Furth
 * @see AbstractType
 * @see TableCellStart
 * @see TableCell
 */
public class TableCell extends AbstractType {

	public TableCell() {
		setSectionFinder(new TableCellSectionFinder());
		LineBreak lineBreak = new LineBreak();
		lineBreak.setRenderer(NothingRenderer.getInstance());
		this.addChildType(lineBreak);
		this.addChildType(new TableHeadStart());
		this.addChildType(new TableCellStart());
		this.addChildType(new TableCellContent());
	}

	/**
	 * Returns for a TableCell whether it is a TableHead or not. Therefore it is
	 * checked if the TableCell has a child of type TableHeadStart
	 * 
	 * @author Sebastian Furth
	 * @created 19/10/2010
	 * @param tableCell the table cell which will be checked
	 * @return true if table head, otherwise false.
	 */
	public static boolean isTableHead(Section<? extends TableCell> tableCell) {
		return Sections.child(tableCell, TableHeadStart.class) != null;
	}

	/**
	 * TableCellSetioner.
	 * 
	 * This class parses the <code>TableLine</code> into <code>TableCell</code>
	 * s. Looking for the TableCell delimiter character is not enough due the
	 * appearance of special markup e.g. links. The parser takes therefore this
	 * special markup into account and handles it.
	 * 
	 * @author smark, Sebastian Furth
	 * @see SectionFinder
	 */
	public static class TableCellSectionFinder implements SectionFinder {

		public static final String CELLSTART = "\\|{1,2}\\s*";
		public static final String JSPLINK = "(\\[[^]]+\\|?[^]]*\\])";
		public static final String REGEX = CELLSTART + "(" + JSPLINK + "|"
				+ Patterns.QUOTED + "|[^|]*)*";
		private final Pattern pattern = Pattern.compile(REGEX);

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			Matcher tagMatcher = pattern.matcher(text);
			List<SectionFinderResult> resultRegex = new LinkedList<>();

			while (tagMatcher.find()) {
				resultRegex.add(new SectionFinderResult(tagMatcher.start(), tagMatcher.end()));
			}
			return resultRegex;
		}
	}
}
