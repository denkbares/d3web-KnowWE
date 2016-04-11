/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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

import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.constraint.SectionFinderConstraint;

/**
 * A SectionFinderConstraint that allows to restrict types to certain ranges of
 * cell indices.
 * 
 * @author Reinhard Hatko
 * @created 11.06.2013
 */
public class TableIndexConstraint implements SectionFinderConstraint {

	private int minColumn;
	private int maxColumn;

	private int minRow;
	private int maxRow;

	/**
	 * @param minColumn
	 * @param maxColumn
	 * @param minRow
	 * @param maxRow
	 */
	public TableIndexConstraint(int minColumn, int maxColumn, int minRow, int maxRow) {
		this.minColumn = minColumn;
		this.maxColumn = maxColumn;
		this.minRow = minRow;
		this.maxRow = maxRow;
	}

	public TableIndexConstraint() {
		this(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
	}

	public void setColumnConstraints(int minColumn, int maxColumn) {
		this.minColumn = minColumn;
		this.maxColumn = maxColumn;
	}

	public void setColumnConstraints(int index) {
		setColumnConstraints(index, index);
	}

	public void setRowConstraints(int minRow, int maxRow) {
		this.minRow = minRow;
		this.maxRow = maxRow;
	}

	public void setRowConstraints(int index) {
		setRowConstraints(index, index);
	}

	@Override
	public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		int column = TableUtils.getColumn(father);
		int row = TableUtils.getRow(father);
		if (column >= minColumn && column < maxColumn && row >= minRow && row < maxRow) return;
		found.clear();
	}

}
