/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.testcases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import de.d3web.core.utilities.Pair;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;

/**
 * 
 * @author Volker Belli & Markus Friedrich (denkbares GmbH)
 * @created 20.02.2012
 */
public class TableModel {

	private final HashMap<Pair<Integer, Integer>, String> cells = new HashMap<Pair<Integer, Integer>, String>();
	private final HashMap<Integer, Integer> columnWidths = new HashMap<Integer, Integer>();
	private int rowCount = 0;
	private int columnCount = 0;
	private String name;

	public void addCell(int row, int column, String value, int width) {
		rowCount = Math.max(rowCount, row);
		columnCount = Math.max(columnCount, column);
		cells.put(new Pair<Integer, Integer>(row, column), value);
		Integer actualWidth = columnWidths.get(column);
		if (actualWidth == null || actualWidth < width) {
			columnWidths.put(column, width);
		}
	}

	public int getWidth(int column) {
		Integer width = columnWidths.get(column);
		if (width == null) {
			return 0;
		}
		return width;
	}

	public String getCell(int row, int column) {
		String cell = cells.get(new Pair<Integer, Integer>(row, column));
		if (cell == null) {
			return "";
		}
		return Strings.maskJSPWikiMarkup(cell);
	}

	/**
	 * <pre>
	 * {
	 * 			widths: [x, y, ...],
	 * 			cells: [
	 * 			        [...],
	 * 			        [...],
	 * 			       ],
	 * 		}
	 * </pre>
	 * 
	 * @created 20.02.2012
	 * @return
	 */
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("widths: [");
		for (int i = 0; i <= columnCount; i++) {
			sb.append(getWidth(i));
			if (i != columnCount) {
				sb.append(",");
			}
		}
		sb.append("],");
		sb.append("cells: [");
		for (int i = 0; i <= columnCount; i++) {
			sb.append("[");
			for (int j = 0; j <= rowCount; j++) {
				sb.append(getCell(i, j));
				if (j != columnCount) {
					sb.append(",");
				}
			}
			sb.append("]");
			if (i != columnCount) {
				sb.append(",");
			}
		}
		sb.append("],");
		return sb.toString();
	}

	public String toHtml(Section<?> section, UserContext user) {
		StringBuilder string = new StringBuilder();
		string.append(Strings.maskHTML("<div style='overflow:auto'>"));
		string.append(Strings.maskHTML("<table class='wikitable' border='1'>"));
		// headline
		Set<Integer> collapsedColumns = getCollapsedColumns(section, user);
		string.append(Strings.maskHTML("<tr>"));
		for (int i = 0; i <= columnCount; i++) {
			String cell = getCell(0, i);
			appendCell("th", cell, i, collapsedColumns, string);
		}
		string.append(Strings.maskHTML("</tr>\n"));
		for (int i = 1; i <= rowCount; i++) {
			string.append(Strings.maskHTML(i % 2 == 1 ? "<tr>" : "<tr class='odd'>"));
			for (int j = 0; j <= columnCount; j++) {
				String cell = getCell(i, j);
				appendCell("td", cell, j, collapsedColumns, string);
			}
			string.append(Strings.maskHTML("</tr>\n"));
		}
		string.append(Strings.maskHTML("</table>"));
		string.append(Strings.maskHTML("</div>"));

		return string.toString();
	}

	private void appendCell(String type, String cell, int column, Set<Integer> collapsedColumns, StringBuilder string) {
		ArrayList<String> attributes = new ArrayList<String>();
		if (column > 0) {
			attributes.add("column");
			attributes.add(String.valueOf(column));
			if (collapsedColumns.contains(column)) {
				attributes.add("class");
				attributes.add("collapsedcolumn");
				attributes.add("title");
				if (type.equals("th")) {
					attributes.add("Expand " + cell);
				}
				else {
					attributes.add(cell);
				}
			}
			else {
				if (type.equals("th")) {
					attributes.add("title");
					attributes.add("Collapse");
				}
			}
		}
		String[] attrArray = attributes.toArray(new String[attributes.size()]);
		String cellDiv = Strings.getHtmlElement("div", cell);
		string.append(Strings.getHtmlElement(type, cellDiv, attrArray));
	}

	private Set<Integer> getCollapsedColumns(Section<?> section, UserContext user) {
		String key = "columnstatus_" + section.getID() + "_" + name;
		Set<Integer> collapsed = new HashSet<Integer>();
		String cookie = KnowWEUtils.getCookie(key, user);
		if (cookie != null) {
			String[] columns = cookie.split("#");
			for (String colString : columns) {
				try {
					int col = Integer.parseInt(colString);
					collapsed.add(col);
				}
				catch (NumberFormatException e) {
					// just skip...
				}

			}
		}
		return collapsed;
	}

	public TableModel copy() {
		TableModel copy = new TableModel();
		copy.columnCount = columnCount;
		copy.rowCount = rowCount;
		for (Entry<Pair<Integer, Integer>, String> e : cells.entrySet()) {
			copy.cells.put(e.getKey(), e.getValue());
		}
		for (Entry<Integer, Integer> e : columnWidths.entrySet()) {
			copy.columnWidths.put(e.getKey(), e.getValue());
		}
		return copy;
	}

	public void setName(String name) {
		this.name = name;
	}
}
