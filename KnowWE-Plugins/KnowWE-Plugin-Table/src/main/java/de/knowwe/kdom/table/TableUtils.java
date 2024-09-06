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

import com.denkbares.strings.Strings;
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
	 * Returns the first row of the table, aka the header row.
	 *
	 * @param tableSection any section of the table
	 * @return the header/first line of the table
	 */
	@Nullable
	public static Section<TableLine> getHeaderRow(Section<?> tableSection) {
		Section<Table> table = Sections.ancestor(tableSection, Table.class);
		return Sections.successor(table, TableLine.class);
	}

	public static Section<TableLine> getTableLine(Section<?> tableSection) {
		return $(tableSection).closest(TableLine.class).getFirst();
	}

	public static Section<TableCellContent> getCell(Section<?> section, int x, int y) {
		Section<Table> table = Sections.ancestor(section, Table.class);
		List<Section<TableLine>> lines = Sections.successors(table, TableLine.class);
		if (lines.size() > y) {
			Section<TableLine> lineY = lines.get(y);
			List<Section<TableCellContent>> cells = Sections.successors(lineY, TableCellContent.class);
			if (cells.size() > x) {
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
		Integer index = tableCell.getObject(COLUMN_INDEX_KEY);
		if (index == null) {
			Section<?> tableLine = getTableLine(tableCell);
			List<Section<TableCell>> tableCells = Sections.successors(tableLine, TableCell.class);
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

	/**
	 * @deprecated use {@link #getNumberOfColumns(Section)} instead
	 */
	@Deprecated
	public static int getColumns(Section<?> tableSection) {
		return getNumberOfColumns(tableSection);
	}

	public static int getNumberOfColumns(Section<?> tableSection) {
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
		Map<String, Integer> rowNumbering = table.getObject(ROW_NUMBERING_KEY);
		if (rowNumbering == null || !rowNumbering.containsKey(tableLine.getID())) {
			List<Section<TableLine>> lines = Sections.children(table, TableLine.class);
			if (lines.size() > 10) {
				rowNumbering = new HashMap<>(lines.size());
				for (int i = 0; i < lines.size(); i++) {
					Section<TableLine> line = lines.get(i);
					rowNumbering.put(line.getID(), i);
				}
				table.storeObject(ROW_NUMBERING_KEY, rowNumbering);
			}
			else {
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
		Sections<TableCell> headerCell = tableCell.getObject(COLUMN_HEADER_KEY);
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

	/**
	 * For the current row, return the first cell with the given header. If there are multiple columns with the same
	 * header, the first one from the left is returned.
	 *
	 * @param rowSection    the section of the row or a successor
	 * @param columnHeader  the column header for which we want the cell in the current row
	 * @param cellTypeClass the type of the section we want to extract from the cell
	 * @return the section with the given type in the cell with the given header in the current row
	 */
	public static <T extends Type> Section<T> getInRow(Section<?> rowSection, String columnHeader, Class<T> cellTypeClass) {
		return getInRow(rowSection, columnHeader, cellTypeClass, 0);
	}

	/**
	 * For the current row, return the cell with the given header. Use this method if the table contains
	 * multiple columns with the same header. The given 0-based index specifies which of those columns will be used.
	 *
	 * @param rowSection    the section of the row or a successor
	 * @param columnHeader  the column header for which we want the cell in the current row
	 * @param cellTypeClass the type of the section we want to extract from the cell
	 * @return the section with the given type in the cell with the given header in the current row
	 */
	@Nullable
	public static <T extends Type> Section<T> getInRow(Section<?> rowSection, String columnHeader, Class<T> cellTypeClass, int index) {
		columnHeader = Strings.trim(columnHeader);

		Section<TableLine> headerRow = getHeaderRow(rowSection);
		if (headerRow == null) return null;
		List<Section<TableCellContent>> headerCells = Sections.successors(headerRow, TableCellContent.class);

		List<Section<TableCellContent>> currentLineCells = Sections.successors(getTableLine(rowSection), TableCellContent.class);
		int matchCount = 0;
		for (int i = 0; i < headerCells.size(); i++) {
			String headerText = Strings.trim(headerCells.get(i).getText());
			if (headerText.equalsIgnoreCase(columnHeader)) {
				if (matchCount == index) {
					if (currentLineCells.size() >= i + 1) {
						return $(currentLineCells.get(i)).successor(cellTypeClass).getFirst();
					}
					else {
						return null;
					}
				}
				matchCount++;
			}
		}
		return null;
	}

	/**
	 * For the current row, return the first cell with the given header. If there are multiple columns with the same
	 * header, the first one from the left is returned.
	 *
	 * @param rowSection    the section of the row or a successor
	 * @param columnHeaderRegex  the column header for which we want the cell in the current row
	 * @param cellTypeClass the type of the section we want to extract from the cell
	 * @return the section with the given type in the cell with the given header in the current row
	 */
	public static <T extends Type> Section<T> getInRowRegex(Section<?> rowSection, String columnHeaderRegex, Class<T> cellTypeClass) {
		return getInRowRegex(rowSection, columnHeaderRegex, cellTypeClass, 0);
	}

	/**
	 * For the current row, return the cell with the given header. Use this method if the table contains
	 * multiple columns with the same header. The given 0-based index specifies which of those columns will be used.
	 *
	 * @param rowSection    the section of the row or a successor
	 * @param columnHeaderRegex  the column header for which we want the cell in the current row
	 * @param cellTypeClass the type of the section we want to extract from the cell
	 * @return the section with the given type in the cell with the given header in the current row
	 */
	@Nullable
	public static <T extends Type> Section<T> getInRowRegex(Section<?> rowSection, String columnHeaderRegex, Class<T> cellTypeClass, int index) {
		columnHeaderRegex = Strings.trim(columnHeaderRegex.toLowerCase());

		Section<TableLine> headerRow = getHeaderRow(rowSection);
		if (headerRow == null) return null;
		List<Section<TableCellContent>> headerCells = Sections.successors(headerRow, TableCellContent.class);

		List<Section<TableCellContent>> currentLineCells = Sections.successors(getTableLine(rowSection), TableCellContent.class);
		int matchCount = 0;
		for (int i = 0; i < headerCells.size(); i++) {
			String headerText = Strings.trim(headerCells.get(i).getText().toLowerCase());
			if (headerText.matches(columnHeaderRegex)) {
				if (matchCount == index) {
					if (currentLineCells.size() >= i + 1) {
						return $(currentLineCells.get(i)).successor(cellTypeClass).getFirst();
					}
					else {
						return null;
					}
				}
				matchCount++;
			}
		}
		return null;
	}
}
