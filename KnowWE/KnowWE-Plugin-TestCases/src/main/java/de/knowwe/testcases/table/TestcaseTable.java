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

import de.knowwe.core.kdom.InvalidKDOMSchemaModificationOperation;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableLine;

/**
 * @author Florian Ziegler
 */
public class TestcaseTable extends Table {

	public static final String TESTCASE_INFOSTORE_KEY = "testcasetable";
	public static final String TESTCASE_KEY = "TESTCASE";

	public TestcaseTable() {

		try {
			replaceChildType(new HeaderLine(), TableLine.class);
		}
		catch (InvalidKDOMSchemaModificationOperation e) {
			e.printStackTrace();
		}
		addChildType(new TestcaseTableLine());

		addSubtreeHandler(new TestcaseTableSubtreeHandler());
	}

	@Override
	public boolean isSortable() {
		return false;
	}

	/**
	 * 
	 * @created 22.01.2011
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Section<? extends HeaderCell> findHeaderCell(Section<?> s) {
		Section<TableLine> line = Sections.findAncestorOfType(s, TableLine.class);
		int i = 0;
		for (Section<?> section : line.getChildren()) {

			if (s.equalsOrIsSuccessorOf(section)) {
				break;
			}

			i++;
		}

		Section<Table> table = Sections.findAncestorOfType(line, Table.class);
		Section<TableLine> hLine = Sections.findSuccessor(table, TableLine.class);

		if (i >= hLine.getChildren().size()) {
			return null;
		}

		Section<? extends HeaderCell> hCell = (Section<? extends HeaderCell>) hLine.getChildren().get(
				i);
		return hCell;
	}
}
