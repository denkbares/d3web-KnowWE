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
import java.util.Scanner;

import com.denkbares.utils.Pair;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
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

	private int minColumnFromLast;
	private int maxColumnFromLast;

	private int minRowFromLast;
	private int maxRowFromLast;

	public TableIndexConstraint(int minColumn, int maxColumn, int minRow, int maxRow) {
		this.minColumn = minColumn;
		this.maxColumn = maxColumn;
		this.minRow = minRow;
		this.maxRow = maxRow;
	}

	public TableIndexConstraint() {
		// using setters
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

	/**
	 * Specify your accepted rows counting from last row backwards
	 *
	 * @param minRow starting range, offset counted from last
	 * @param maxRow ending range, offset counted from last
	 */
	public void setRowConstraintsFromLast(int minRow, int maxRow) {
		this.minRowFromLast = minRow;
		this.maxRowFromLast = maxRow;
	}

	/**
	 * Specify your accepted columns counting from last column backwards
	 *
	 * @param minColumn starting range, offset counted from last
	 * @param maxColumn ending range, offset counted from last
	 */
	public void setColumnConstraintsFromLast(int minColumn, int maxColumn) {
		this.minColumnFromLast = minColumn;
		this.maxColumnFromLast = maxColumn;
	}

	public void setRowConstraints(int index) {
		setRowConstraints(index, index);
	}

	@Override
	public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		int currentColumn = TableUtils.getColumn(father);
		int currentRow = TableUtils.getRow(father);
		Pair<Integer, Integer> tableSize = determineTableSize(father);
		if (columnRangeIsOk(currentColumn, tableSize) && rowRangeIsOk(currentRow, tableSize)) {
			return;
		}

		found.clear();
	}

	private boolean columnRangeIsOk(int currentColumn, Pair<Integer, Integer> tableSize) {
		int numberOfColumns = tableSize.getA();
		// we support counting backwards from the end
		return (currentColumn >= minColumn && currentColumn < maxColumn)
				|| (currentColumn >= numberOfColumns - minColumnFromLast
				&& currentColumn < numberOfColumns - maxColumnFromLast);
	}

	private boolean rowRangeIsOk(int currentRow, Pair<Integer, Integer> tableSize) {
		int numberOfRows = tableSize.getB();
		// we support counting backwards from the end
		return (currentRow >= minRow && currentRow < maxRow)
				|| (currentRow >= numberOfRows - minRowFromLast
				&& currentRow < numberOfRows - maxRowFromLast);
	}

	private Pair<Integer, Integer> determineTableSize(Section<?> father) {
		Section<Table> tableSection = Sections.ancestor(father, Table.class);
		assert tableSection != null;
		//noinspection unchecked
		Pair<Integer, Integer> tableSize = (Pair<Integer, Integer>) tableSection.getObject("tableSize");
		if (tableSize == null) {
			String tableText = tableSection.getText();
			int columns = 0;
			int rows = 0;
			boolean first = true;
			Scanner scanner = new Scanner(tableText);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (first) {
					char[] charArray = line.toCharArray();
					for (int i = 0; i < charArray.length; i++) {
						char c = charArray[i];
						if (c == '|') {
							columns++;
							// consume directly following |, which is not a new column but a bold column
							if (i + 1 < charArray.length && charArray[i + 1] == '|') {
								i++;
							}
						}
					}
					first = false;
				}
				if (line.startsWith("|")) {
					rows++;
				}
			}
			tableSize = new Pair<>(columns, rows);
			tableSection.storeObject("tableSize", tableSize);
		}
		return tableSize;
	}
}
