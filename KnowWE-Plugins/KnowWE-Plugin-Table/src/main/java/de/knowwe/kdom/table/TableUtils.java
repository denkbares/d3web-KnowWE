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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

import static de.knowwe.core.kdom.parsing.Sections.$;

public class TableUtils {

	public static final String COLUMN_INDEX_KEY = "columnIndexKey";
	public static final String COLUMN_HEADER_KEY = "columnHeaderKey";
	public static final String ROW_NUMBERING_KEY = "rowNumbering";

	/**
	 * Checks whether the given Section is from the header of the table
	 *
	 * @param tableSection a Section of the table
	 * @return if the given section is from the header of the table
	 * @created 29.07.2012
	 */
	public static boolean isHeaderRow(Section<?> tableSection) {
		Section<?> tableLine = getTableLine(tableSection);
		Section<TableLine> headerLine = getHeaderRow(tableLine);
		return headerLine == tableLine;
	}

	/**
	 * For the table of the given table section, the first row, aka the header row, is returned.
	 *
	 * @param tableSection any section of the table
	 * @return the header/first row of the table
	 */
	@Nullable
	public static Section<TableLine> getHeaderRow(Section<?> tableSection) {
		Section<Table> table = Sections.ancestor(tableSection, Table.class);
		return Sections.successor(table, TableLine.class);
	}

	public static Section<TableLine> getTableLine(Section<?> tableSection) {
		return Sections.cast(tableSection.get() instanceof TableLine
				? tableSection
				: Sections.ancestor(tableSection, TableLine.class), TableLine.class);
	}

	public static Section<TableCellContent> getCell(Section<?> section, int x, int y) {
		Section<Table> table = Sections.ancestor(section, Table.class);
		List<Section<TableLine>> lines = Sections.successors(table, TableLine.class);
		if(lines.size() > y) {
			Section<TableLine> lineY = lines.get(y);
			List<Section<TableCellContent>> cells = Sections.successors(lineY, TableCellContent.class);
			if(cells.size() > x) {
				return cells.get(x);
			}
		}
		return null;
	}

	/**
	 * Returns the column of the table in which the current cell occurs.
	 *
	 * @param columnSection current section
	 */
	public static int getColumn(Section<?> columnSection) {
		Section<?> tableCell = getTableCell(columnSection);
		Integer index = (Integer) tableCell.getObject(COLUMN_INDEX_KEY);
		if (index == null) {
			Section<?> tableLine = getTableLine(tableCell);
			List<Section<TableCell>> tableCells = Sections.successors(tableLine, TableCell.class);
			//noinspection SuspiciousMethodCalls
			index = tableCells.indexOf(tableCell);
			tableCell.storeObject(COLUMN_INDEX_KEY, index);
		}
		return index;
	}

	public static Section<?> getTableCell(Section<?> columnSection) {
		return columnSection.get() instanceof TableCell
				? columnSection
				: Sections.ancestor(columnSection, TableCell.class);
	}

	public static int getColumns(Section<?> tableSection) {
		Section<?> tableLine = getTableLine(tableSection);
		List<Section<TableCell>> tableCells = Sections.successors(tableLine, TableCell.class);
		return tableCells.size();
	}

	public static int getRows(Section<?> tableSection) {
		Section<Table> table = Sections.ancestor(tableSection, Table.class);
		List<Section<TableLine>> rows = Sections.successors(table, TableLine.class);
		return rows.size();
	}

	/**
	 * Returns the row of the given section inside the table
	 *
	 * @param rowSection the section inside the table you want the row of
	 * @return the row of the section you are checking
	 */
	public static int getRow(Section<?> rowSection) {
		Section<TableLine> tableLine = getTableLine(rowSection);
		Section<Table> table = Sections.ancestor(tableLine, Table.class);
		if (table == null) return -1;
		//noinspection unchecked
		Map<String, Integer> rowNumbering = (Map<String, Integer>) table.getObject(ROW_NUMBERING_KEY);
		if (rowNumbering == null || !rowNumbering.containsKey(tableLine.getID())) {
			List<Section<TableLine>> lines = Sections.children(table, TableLine.class);
			if (lines.size() > 10) {
				rowNumbering = new HashMap<>(lines.size());
				for (int i = 0; i < lines.size(); i++) {
					Section<TableLine> line = lines.get(i);
					rowNumbering.put(line.getID(), i);
				}
				table.storeObject(ROW_NUMBERING_KEY, rowNumbering);
			} else {
				return lines.indexOf(tableLine);
			}
		}
		return rowNumbering.get(tableLine.getID());
	}

	/**
	 * Checks the width attribute of the table tag and returns a HTML string
	 * containing the width as CSS style information.
	 */
	public static String getWidth(String input) {
		String pattern = "[+]?[0-9]+\\.?[0-9]+(%|px|em|mm|cm|pt|pc|in)";
		String digit = "[+]?[0-9]+\\.?[0-9]+";

		if (input == null) return "";

		if (input.matches(digit)) {
			return "style='width:" + input + "px'";
		}
		if (input.matches(pattern)) {
			return "style='width:" + input + "'";
		}
		else {
			return "";
		}
	}

	/**
	 * returns whether the current Section is or is in a table and is sortable
	 */
	public static boolean sortOption(Section<?> sec) {
		boolean sortable = false;
		Section<Table> tableType = Sections.ancestor(sec, Table.class);
		if (sec.get() instanceof Table) {
			Table table = (Table) sec.get();
			sortable = table.isSortable();
		}
		else if (tableType != null) {
			Table table = tableType.get();
			sortable = table.isSortable();
		}
		return sortable;
	}

	/**
	 * returns whether the current Section gets a sort button
	 *
	 * @created 31.07.2010
	 */
	public static boolean sortTest(Section<?> s) {

		boolean sortable = sortOption(s);
		boolean isHeaderLine = false;

		Section<TableLine> tableLine = Sections.ancestor(s, TableLine.class);
		if (tableLine != null) {
			isHeaderLine = TableLine.isHeaderLine(tableLine);
		}

		return (sortable && isHeaderLine);
	}

	/**
	 * Returns the TableCellContent section of the header for any section of a table.
	 */
	public static Section<TableCellContent> getColumnHeader(Section<?> columnSection) {
		return getColumnHeader(columnSection, TableCellContent.class);
	}

	/**
	 * Returns the section with the given type from the header for any section of a table.
	 */
	public static <T extends Type> Section<T> getColumnHeader(Section<?> columnSection, Class<T> headerType) {
		Section<?> tableCell = getTableCell(columnSection);
		@SuppressWarnings("unchecked")
		Sections<TableCell> headerCell = (Sections<TableCell>) tableCell.getObject(COLUMN_HEADER_KEY);
		if (headerCell == null) {
			headerCell = $(columnSection)
					.ancestor(Table.class)
					.successor(TableLine.class)
					.first()
					.successor(TableCell.class)
					.nth(getColumn(columnSection));
			tableCell.storeObject(COLUMN_HEADER_KEY, headerCell);
		}
		return headerCell.successor(headerType).getFirst();
	}

	/**
	 * Returns the text of the row description as String. The row description is
	 * the text in the cell in the first column and the same row as the
	 * specified cell.
	 *
	 * @return row description as String
	 * @created 20/10/2010
	 */
	public static String getRowDescription(Section<?> cell) {
		Section<TableCellContent> header = getRowHeader(cell);
		return header != null ? header.getText() : null;
	}

	public static Section<TableCellContent> getRowHeader(Section<?> cell) {
		Section<TableLine> line = Sections.ancestor(cell, TableLine.class);
		return Sections.successor(line, TableCellContent.class);
	}
}
