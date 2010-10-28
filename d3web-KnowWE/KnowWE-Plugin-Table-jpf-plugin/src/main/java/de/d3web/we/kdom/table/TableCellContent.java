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

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;

public class TableCellContent extends DefaultAbstractKnowWEObjectType {

	public TableCellContent() {
		this.sectionFinder = new AllTextSectionFinder();
		this.setCustomRenderer(new TableCellContentRenderer());
	}

	public static boolean isTableHeadContent(Section<? extends TableCellContent> s) {
		Section<? extends TableCell> cell = s.findAncestorOfType(TableCell.class);
		return TableCell.isTableHead(cell);
	}

	public static int getRow(Section<? extends TableCellContent> section) {
		return TableUtils.getRow(section);
	}

	public static int getCol(Section<? extends TableCellContent> section) {
		return TableUtils.getColumn(section);
	}

}
