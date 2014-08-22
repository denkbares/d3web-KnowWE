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

import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class TableUtils {

	/**
	 * Checks whether the given Section is from the header of the table
	 * 
	 * @created 29.07.2012
	 * @param tableSection a Section of the table
	 * @return if the given section is from the header of the table
	 */
	public static boolean isHeaderRow(Section<?> tableSection) {
		Section<?> tableLine = getTableLine(tableSection);
		Section<Table> table = Sections.ancestor(tableLine, Table.class);
		Section<TableLine> headerLine = Sections.successor(table, TableLine.class);
		return headerLine == tableLine;
	}

	public static Section<?> getTableLine(Section<?> tableSection) {
		Section<?> tableLine = tableSection.get() instanceof TableLine
				? tableSection
				: Sections.ancestor(tableSection, TableLine.class);
		return tableLine;
	}

	/**
	 * Returns the column of the table in which the current cell occurs.
	 * 
	 * @param columnSection current section
	 * @return
	 */
	public static int getColumn(Section<?> columnSection) {
		Section<?> tableCell = getTableCell(columnSection);
		Section<?> tableLine = getTableLine(tableCell);
		List<Section<TableCell>> tableCells = Sections.successors(tableLine,
				TableCell.class);
		return tableCells.indexOf(tableCell);
	}

	public static Section<?> getTableCell(Section<?> columnSection) {
		Section<?> tableCell = columnSection.get() instanceof TableCell
				? columnSection
				: Sections.ancestor(columnSection, TableCell.class);
		return tableCell;
	}

	public static int getColumns(Section<?> tableSection) {
		Section<?> tableLine = getTableLine(tableSection);
		List<Section<TableCell>> tableCells = Sections.successors(tableLine,
				TableCell.class);
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
		Section<?> tableLine = getTableLine(rowSection);
		Section<Table> table = Sections.ancestor(tableLine, Table.class);
		List<Section<TableLine>> lines = Sections.successors(table, TableLine.class);
		return lines.indexOf(tableLine);
	}

	/**
	 * Checks the width attribute of the table tag and returns a HTML string
	 * containing the width as CSS style information.
	 * 
	 * @param input
	 * @return
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
	 * 
	 * @created 31.07.2010
	 * @param s
	 * @return
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
	 * @param s
	 * @return
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

	public static Type getTableType(Section<?> s) {
		Section<? extends Table> table = Sections.ancestor(s, Table.class);
		return table != null ? table.get() : null;
	}

	/**
	 * Returns the text as String of the column heading. The column heading is
	 * the cell in the first row and the same column as the specified cell.
	 * 
	 * @author Sebastian Furth
	 * @created 20/10/2010
	 * @param columnSection
	 * @return column heading is String
	 */
	public static Section<TableCellContent> getColumnHeader(Section<?> columnSection) {
		int column = getColumn(columnSection);
		if (column == -1) return null;
		Section<Table> table = Sections.ancestor(columnSection, Table.class);
		Section<TableLine> line = Sections.successor(table, TableLine.class);
		List<Section<TableCellContent>> cells = Sections.successors(line,
				TableCellContent.class);
		if (cells.size() <= column) return null;
		return cells.get(column);
	}

	/**
	 * Returns the text of the row description as String. The row description is
	 * the text in the cell in the first column and the same row as the
	 * specified cell.
	 * 
	 * @author Sebastian Furth
	 * @created 20/10/2010
	 * @param cell
	 * @return row description as String
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
