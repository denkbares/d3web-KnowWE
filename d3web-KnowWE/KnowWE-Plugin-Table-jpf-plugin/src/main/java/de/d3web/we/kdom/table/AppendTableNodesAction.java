/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.table;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

/**
 * used to either add a row or a column to a table
 * 
 * @author Florian Ziegler (basic), Sebastian Furth (stable)
 * @created 20.06.2010
 */
public class AppendTableNodesAction extends AbstractAction {


	@Override
	public void execute(ActionContext context) throws IOException {
		KnowWEParameterMap map = context.getKnowWEParameterMap();
		String web = map.getWeb();
		String topic = map.getTopic();
		String type = context.getParameter("type");
		String id = context.getParameter("table");

		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = mgr.getArticle(topic);

		Section<KnowWEArticle> root = article.getSection();

		// We need to do the following because there can be more than one table!
		List<Section<Table>> tables = new LinkedList<Section<Table>>();
		root.findSuccessorsOfType(Table.class, tables);
		Section<Table> table = null;
		for (Section<Table> temp : tables) {
			if (temp.getID().equals(id)) {
				table = temp;
			}
		}
		Map<String, String> nodesMap;

		if (type.equals("row")) {
			nodesMap = appendRow(table);
		}
		else {
			nodesMap = appendCol(table);
		}
		mgr.replaceKDOMNodes(map, topic, nodesMap);
	}

	/**
	 * adds a row by first calculating the number of cells and then adding a new
	 * line.
	 * 
	 * @author Florian Ziegler, Sebastian Furth
	 * @created 23.06.2010, 19.10.2010
	 * @param table, the table which shall get a new row
	 * @return Map with KDOMid - value pairs for replacing
	 */
	private Map<String, String> appendRow(Section<Table> table) {

		Map<String, String> nodesMap = new HashMap<String, String>();
		List<Section<TableLine>> lines = new LinkedList<Section<TableLine>>();

		table.findSuccessorsOfType(TableLine.class, lines);
		// we use the previous line to determine which cells should be TH
		Section<TableLine> line = lines != null ? lines.get(lines.size() - 1) : null;
		StringBuilder newLine = new StringBuilder("");
		
		// count cells
		if (line != null) {
			newLine.append("\n");
			List<Section<TableCell>> cells = new LinkedList<Section<TableCell>>();
			line.findSuccessorsOfType(TableCell.class, cells);
			for (Section<TableCell> cell : cells) {
				if (TableCell.isTableHead(cell)) {
					newLine.append(" || - ");
				} else {
					newLine.append(" | - ");
				}
			}
		} else {
			Logger.getLogger(this.getClass().getCanonicalName()).warning("Unable to append row, because there is no other row (can't calculate number of cells");
		}

		
		nodesMap.put(table.getID(), table.getOriginalText() + newLine.toString());
		return nodesMap;
	}

	/**
	 * adds a column by adding a new cell to each TableLine
	 * 
	 * @author Florian Ziegler, Sebastian Furth
	 * @created 23.06.2010, 17.10.2010
	 * @param table, the table which shall get a new column
	 * @return Map with KDOMid - value pairs for replacing
	 */
	private Map<String, String> appendCol(Section<Table> table) {

		Map<String, String> nodesMap = new HashMap<String, String>();

		List<Section<TableLine>> lines = new LinkedList<Section<TableLine>>();
		table.findSuccessorsOfType(TableLine.class, lines);

		String originalText = "";

		for (Section<TableLine> line : lines) {
			if (TableLine.isHeaderLine(line)) {
				originalText = line.getOriginalText().trim() + " || - \n";
			}
			else {
				originalText = line.getOriginalText().trim() + " | - \n";
			}
			nodesMap.put(line.getID(), originalText);
		}

		return nodesMap;
	}

}
