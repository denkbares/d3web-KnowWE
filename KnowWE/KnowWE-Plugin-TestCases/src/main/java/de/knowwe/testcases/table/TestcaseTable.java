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

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableCell;
import de.knowwe.kdom.table.TableLine;

/**
 * @author Florian Ziegler
 */
public class TestcaseTable extends Table {

	public static final String TESTCASE_INFOSTORE_KEY = "testcasetablekey";
	private static final String INDEX_KEY = "indexkey";
	private static final String HEADERCELL_KEY = "headercellkey";

	public TestcaseTable() {

		replaceChildType(new HeaderLine(), TableLine.class);

		addChildType(new TestcaseTableLine());
		addCompileScript(new TestcaseTableSubtreeHandler());
		setSectionFinder(new AllTextFinderTrimmed());
	}

	@Override
	public boolean isSortable() {
		return false;
	}

	public static int getColumnIndex(Section<?> section) {
		Section<TableCell> tCell;
		if (section.get() instanceof TableCell) {
			tCell = Sections.cast(section, TableCell.class);
		}
		else {
			tCell = Sections.ancestor(section, TableCell.class);
		}
		if (tCell == null) {
			throw new IllegalArgumentException("Specified section is not part of this table");
		}
		Object index = tCell.getSectionStore().getObject(INDEX_KEY);
		if (index == null) {
			int i = 0;
			for (Section<?> child : tCell.getParent().getChildren()) {
				if (!(child.get() instanceof TableCell)) continue;
				child.getSectionStore().storeObject(INDEX_KEY, i);
				if (child == tCell) index = i;
				i++;
			}
		}
		return (Integer) index;
	}

	public static Section<? extends HeaderCell> findHeaderCell(Section<?> s) {

		Section<Table> table = Sections.ancestor(s, Table.class);
		Section<TableLine> hLine = Sections.successor(table, TableLine.class);

		int index = getColumnIndex(s);

		String key = HEADERCELL_KEY + index;
		Object hCell = hLine.getSectionStore().getObject(key);

		if (hCell == null) {
			int i = 0;
			for (Section<?> child : hLine.getChildren()) {
				if (!(child.get() instanceof TableCell)) continue;
				if (i == index) {
					hCell = child;
					hLine.getSectionStore().storeObject(key, hCell);
				}
				i++;
			}
		}
		return Sections.cast((Section<?>) hCell, HeaderCell.class);
	}
}
