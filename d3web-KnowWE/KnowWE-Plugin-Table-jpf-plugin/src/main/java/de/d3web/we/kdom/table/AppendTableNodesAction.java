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
import java.util.Map;

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
 * @author Florian Ziegler
 * @created 20.06.2010
 */
public class AppendTableNodesAction extends AbstractAction {

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * adds a row or a column to a table (as last),
	 * by either replacing the last row with 2 rows
	 * or in every line the last cell with 2 cells
	 */
	public void execute(ActionContext context) throws IOException {
		KnowWEParameterMap map = context.getKnowWEParameterMap();
		String web = map.getWeb();
		String topic = map.getTopic();
		String type = context.getParameter("type");
		String tablePath = context.getParameter("table");

		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = mgr.getArticle(topic);

		Section<KnowWEArticle> root = article.getSection();
		Section<Table> table = (Section<Table>) root.findChild(tablePath);
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
	 * adds a row by replacing the last row with with itself, \n and a new line
	 * 
	 * @created 23.06.2010
	 * @param table, the table which shall get a new row
	 * @return Map with KDOMid - value pairs for replacing
	 */
	private Map<String, String> appendRow(Section<Table> table) {
		String tableText = table.getOriginalText();
		Map<String, String> nodesMap = new HashMap<String, String>();

		String[] lines = tableText.split("\n");
		int lastLine = lines.length - 1;
		int cells = lines[0].length() - lines[0].replace("|", "").length();
		String id = table.getChildren().get(lines.length - 1).getID();

		StringBuffer buffy = new StringBuffer();
		buffy.append("\n|platzhalter");
		for (int i = 1; i < cells; i++) {
			buffy.append("| - ");
		}
		buffy.append("\n");

		nodesMap.put(id, lines[lastLine] + buffy.toString());
		return nodesMap;
	}

	/**
	 * adds a column by replacing the last cell with itself and a new cell.
	 * 
	 * @created 23.06.2010
	 * @param table, the table which shall get a new column
	 * @return Map with KDOMid - value pairs for replacing
	 */
	private Map<String, String> appendCol(Section<Table> table) {
		String tableText = table.getOriginalText();
		Map<String, String> nodesMap = new HashMap<String, String>();

		String[] lines = tableText.split("\n");
		String id = "";

		for (int i = 0; i < lines.length; i++) {
			id = table.getChildren().get(i).getID();
			lines[i] = lines[i].trim() + "| - \n";
			nodesMap.put(id, lines[i]);

		}

		return nodesMap;
	}

}
