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

package de.knowwe.testcases.table;

import java.util.List;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableCell;
import de.knowwe.kdom.table.TableLine;

/**
 * @author Florian Ziegler
 */
public class TestcaseTable extends Table {

	public static final String TESTCASE_INFOSTORE_KEY = "testcasetable";
	public static final String TESTCASE_KEY = "TESTCASE";

	public TestcaseTable() {

		replaceChildType(new HeaderLine(), TableLine.class);

		addChildType(new TestcaseTableLine());
		addSubtreeHandler(new TestcaseTableSubtreeHandler());
	}

	@Override
	public boolean isSortable() {
		return false;
	}

	public static int getColumnIndex(Section<?> section) {

		Section<TableLine> line = Sections.findAncestorOfType(section, TableLine.class);
		List<Section<TableCell>> children = Sections.findChildrenOfType(line, TableCell.class);
		int index = 0;
		for (Section<?> child : children) {
			if (section.equalsOrIsSuccessorOf(child)) {
				return index;
			}
			index++;
		}
		throw new IllegalArgumentException("Specified section is not part of this table");
	}

	public static Section<? extends HeaderCell> findHeaderCell(Section<?> s) {

		Section<Table> table = Sections.findAncestorOfType(s, Table.class);
		Section<TableLine> hLine = Sections.findSuccessor(table, TableLine.class);
		List<Section<TableCell>> hCells = Sections.findChildrenOfType(hLine, TableCell.class);

		int index = getColumnIndex(s);
		if (index >= hCells.size()) {
			return null;
		}

		Section<?> hCell = hCells.get(index);
		return Sections.cast(hCell, HeaderCell.class);
	}
}
