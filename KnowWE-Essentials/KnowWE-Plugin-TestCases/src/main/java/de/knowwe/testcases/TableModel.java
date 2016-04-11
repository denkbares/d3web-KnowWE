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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import de.d3web.strings.Strings;
import de.d3web.utils.Pair;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Volker Belli & Markus Friedrich (denkbares GmbH)
 * @created 20.02.2012
 */
public class TableModel {

	private final Map<Pair<Integer, Integer>, String> cells = new HashMap<Pair<Integer, Integer>, String>();
	private final Map<Integer, Integer> columnWidths = new HashMap<Integer, Integer>();
	private int rowCount = 0;
	private int columnCount = 0;
	private String name;
	private int firstFinding = 0;
	private int lastFinding = 0;
	private int currentRow = 0;
	private int currentColumn = 0;

	private final UserContext user;

	public TableModel(UserContext user) {
		this.user = user;
	}

	public int getCurrentColumn() {
		return currentColumn;
	}

	public int getCurrentRow() {
		return currentRow;
	}

	public void addCell(RenderResult result, int width) {
		addCell(result.toStringRaw(), width);
	}

	public void addCell(String value, int width) {
		addCell(currentRow, currentColumn++, value, width);
	}

	public void addCell(int row, int column, RenderResult resultValue, int width) {
		addCell(row, column, resultValue.toStringRaw(), width);
	}

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
		return KnowWEUtils.maskJSPWikiMarkup(cell);
	}

	/**
	 * <pre>
	 * {
	 * 			widths: [x, y, ...],
	 * 			cells: [
	 * 			        [...],
	 * 			        [...],
	 * 			       ],
	 *        }
	 * </pre>
	 *
	 * @return
	 * @created 20.02.2012
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
		RenderResult string = new RenderResult(user);
		string.appendHtml("<div style='overflow:auto'>");
		string.appendHtml("<table class='wikitable' border='1' pagination=")
				.append(Strings.quoteSingle(section.getID()))
				.appendHtml(">");
		// headline
		Set<Integer> collapsedColumns = getCollapsedColumns(section, user);
		string.appendHtml("<tr>");
		for (int i = 0; i <= columnCount; i++) {
			String cell = getCell(0, i);
			appendCell("th", cell, i, collapsedColumns, string);
		}
		string.appendHtml("</tr>\n");
		for (int i = 1; i <= rowCount; i++) {
			string.appendHtml(i % 2 == 1 ? "<tr>" : "<tr class='odd'>");
			for (int j = 0; j <= columnCount; j++) {
				String cell = getCell(i, j);
				appendCell("td", cell, j, collapsedColumns, string);
			}
			string.appendHtml("</tr>\n");
		}
		string.appendHtml("</table>");
		string.appendHtml("</div>");

		return string.toStringRaw();
	}

	private void appendCell(String type, String cell, int column, Set<Integer> collapsedColumns, RenderResult string) {
		List<String> attributes = new ArrayList<String>();
		if (column > 0) {
			attributes.add("column");
			attributes.add(String.valueOf(column));
			if (collapsedColumns.contains(column)) {
				attributes.add("class");
				attributes.add("collapsedcolumn");
			}
			if (type.equals("th")) {
				if (column >= firstFinding && column <= lastFinding) {
					attributes.add("type");
					attributes.add("finding");
				}
			}
		}
		String[] attrArray = attributes.toArray(new String[attributes.size()]);
		RenderResult cellDiv = new RenderResult(string);
		cellDiv.appendHtmlElement("div", cell);
		string.appendHtmlElement(type, cellDiv.toStringRaw(), attrArray);
	}

	private Set<Integer> getCollapsedColumns(Section<?> section, UserContext user) {
		String key = "columnstatus_" + section.getID() + "_" + name;
		Set<Integer> collapsed = new HashSet<Integer>();
		String cookie = getEncodedCookie(key, user);
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

	private String getEncodedCookie(String name, UserContext context) {
		HttpServletRequest request = context.getRequest();
		if (request == null) return null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (Strings.decodeURL(cookie.getName()).equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public TableModel copy() {
		TableModel copy = new TableModel(user);
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

	public UserContext getUserContext() {
		return this.user;
	}

	public void setLastFinding(int column) {
		this.lastFinding = column;
	}

	public void setFirstFinding(int i) {
		this.firstFinding = i;

	}

	public void skipColumn() {
		this.currentColumn++;
	}

	public void nextRow() {
		this.currentRow++;
		this.currentColumn = 0;
	}
}
