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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class TableUtils {

	/**
	 * Returns the column of the table in which the current cell occurs.
	 * 
	 * @param s current section
	 * @return
	 */
	public static int getColumn(Section<?> s) {
		Section<TableLine> tableLine = Sections.findAncestorOfType(s, TableLine.class);
		List<Section<TableCellContent>> cells = Sections.findSuccessorsOfType(tableLine,
				TableCellContent.class);
		return cells.indexOf(s);
	}

	/**
	 * Returns the row of the table in which the current cell occurs.
	 * 
	 * @param s current section
	 * @return
	 */
	public static int getRow(Section<? extends TableCellContent> s) {
		Section<Table> table = Sections.findAncestorOfType(s, Table.class);

		List<Section<TableLine>> rows = new ArrayList<Section<TableLine>>();
		Sections.findSuccessorsOfType(table, TableLine.class, rows);

		int col = getColumn(s);
		for (Section<TableLine> row : rows) {
			List<Section<TableCellContent>> cells = Sections.findSuccessorsOfType(row,
					TableCellContent.class);
			if (cells.size() > col && cells.get(col).equals(s)) {
				return rows.indexOf(row);
			}
		}
		return -1;
	}

	/**
	 * The row number of the given table line.
	 * 
	 * @created 16.03.2011
	 * @param tableLine
	 * @return
	 */
	public static int getRowOfLine(Section<?> tableLine) {
		return Sections.findAncestorOfType(tableLine, Table.class).getChildren().indexOf(
				tableLine);
	}

	/**
	 * Checks if the current cell is editable. Returns<code>TRUE</code> if so,
	 * otherwise <code>FALSE</code>.
	 * 
	 * @param section current section
	 * @param rows value of the row table attribute
	 * @param cols value of the column table attribute
	 * @return
	 */
	public static boolean isEditable(Section<? extends TableCellContent> section, String rows, String cols) {
		if (rows == null && cols == null) return true;

		boolean isRowEditable = true, isColEditable = true;
		if (rows != null) {
			List<String> rowsIndex = Arrays.asList(splitAttribute(rows));
			String cellRow = String.valueOf(getRow(section));
			isRowEditable = !rowsIndex.contains(cellRow);
		}

		if (cols != null) {
			List<String> colsIndex = Arrays.asList(splitAttribute(cols));
			String cellCol = String.valueOf(getColumn(section));
			isColEditable = !colsIndex.contains(cellCol);
		}
		return (isColEditable && isRowEditable);
	}

	/**
	 * Quotes some special chars.
	 * 
	 * @param content
	 * @return
	 */
	public static String quote(String content) {
		if (!(content.contains("\""))) return content.trim();

		content = content.replace("\"", "&quot;");
		return content.trim();
	}

	/**
	 * Split an given attribute into tokens.
	 * 
	 * @param attribute
	 * @return
	 */
	public static String[] splitAttribute(String attribute) {
		Pattern p = Pattern.compile("[,|;|:]");
		return p.split(attribute);
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
		Section<Table> tableType = Sections.findAncestorOfType(sec, Table.class);
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

		Section<TableLine> tableLine = Sections.findAncestorOfType(s, TableLine.class);
		if (tableLine != null) {
			isHeaderLine = TableLine.isHeaderLine(tableLine);
		}

		return (sortable && isHeaderLine);
	}

	public static Type getTableType(Section<?> s) {
		Section<? extends Table> table = Sections.findAncestorOfType(s, Table.class);
		return table != null ? table.get() : null;
	}

	/**
	 * Returns the text as String of the column heading. The column heading is
	 * the cell in the first row and the same column as the specified cell.
	 * 
	 * @author Sebastian Furth
	 * @created 20/10/2010
	 * @param cell
	 * @return column heading is String
	 */
	public static String getColumnHeadingForCellContent(Section<? extends TableCellContent> cell) {
		Section<Table> table = Sections.findAncestorOfType(cell, Table.class);
		Section<TableLine> line = Sections.findSuccessor(table, TableLine.class);
		List<Section<TableCellContent>> cells = new LinkedList<Section<TableCellContent>>();
		Sections.findSuccessorsOfType(line, TableCellContent.class, cells);

		int i = getColumn(cell);
		Section<TableCellContent> headerCell = cells.get(i);
		return headerCell != null ? headerCell.getText() : null;
	}

	/**
	 * Returns the text of the column heading as String. The column heading is
	 * the text in the cell in the first row and the same column as the
	 * specified cell.
	 * 
	 * <b> Convenience method which delegates to
	 * getColumnHeadingForCellContent(..) </b>
	 * 
	 * @author Sebastian Furth
	 * @created 20/10/2010
	 * @param cell
	 * @return column heading as String
	 */
	public static String getColumnHeading(Section<? extends TableCell> cell) {
		return getColumnHeadingForCellContent(Sections.findAncestorOfType(cell,
				TableCellContent.class));
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
	public static String getRowDescriptionForCellContent(Section<? extends TableCellContent> cell) {
		Section<TableLine> line = Sections.findAncestorOfType(cell, TableLine.class);
		Section<TableCellContent> descriptionCell = Sections.findSuccessor(line,
				TableCellContent.class);
		return descriptionCell != null ? descriptionCell.getText() : null;
	}

	/**
	 * Returns the text of the row description as String. The row description is
	 * the text in the cell in the first column and the same row as the
	 * specified cell.
	 * 
	 * <b> Convenience method which delegates to
	 * getRowDescritionForCellContent(..) </b>
	 * 
	 * @author Sebastian Furth
	 * @created 20/10/2010
	 * @param cell
	 * @return row description as String
	 */
	public static String getRowDescription(Section<? extends TableCell> cell) {
		return getRowDescriptionForCellContent(Sections.findAncestorOfType(cell,
				TableCellContent.class));
	}
}
