/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.flow.kbinfo;

import java.io.IOException;

import de.knowwe.core.KnowWEArticleManager;
import de.knowwe.core.KnowWEAttributes;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;

/**
 * @author Florian Ziegler
 */
public class AddSubFlowchart extends AbstractAction {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Override
	public boolean isAdminAction() {
		return false;
	}

	private String createID(String text) {

		if (text.contains("<flowchart")) {
			String[] flowcharts = text.split("<flowchart fcid=\"");
			String tempid = flowcharts[flowcharts.length - 1].substring(2,
					flowcharts[flowcharts.length - 1].indexOf("\""));
			int number = Integer.valueOf(tempid) + 1;

			String leadingZeros = "";
			for (char c : tempid.toCharArray()) {
				if (c == '0') {
					leadingZeros += "0";
				}
				else {
					break;
				}
			}

			return "sh" + leadingZeros + number;

		}
		else {
			return "sh001";
		}
	}

	private String[] getSurrounding(String text) {
		String before = "";
		String after = "";
		String[] surrounding = new String[2];

		if (text.contains("%%Question") && text.contains("%%Solution")) {
			int first = text.indexOf("%%Question");
			if (text.indexOf("%%Solution") < first) {
				first = text.indexOf("%%Solution");
			}
			before = text.substring(0, first);
			after = text.substring(first);
		}
		else if (text.contains("%%Question")) {
			before = text.substring(0, text.indexOf("%%Question"));
			after = text.substring(text.indexOf("%%Question"));
		}
		else if (text.contains("%%Solution")) {
			before = text.substring(0, text.indexOf("%%Solution"));
			after = text.substring(text.indexOf("%%Solution"));
		}
		else {
			before = text;
		}
		surrounding[0] = before;
		surrounding[1] = after;

		return surrounding;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		// Logging.getInstance().addHandlerToLogger(Logging.getInstance().getLogger(),
		// "AddSub.txt");
		// get everything important from the parameter map
		String web = context.getParameter(KnowWEAttributes.WEB);

		String pageName = context.getParameter("pageName");
		String name = context.getParameter("name");
		String nodesToLine = context.getParameter("nodes");
		nodesToLine = UpdateQuestions.revertSpecialCharacterEscape(nodesToLine);
		String[] exits = nodesToLine.split("::");

		// Logging.getInstance().info(nodesToLine);
		// Logging.getInstance().info("" + nodesToLine.length());

		// get everything to update the article
		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(pageName);
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		Section<KnowWEArticle> sec = article.getSection();
		String oldText = article.getSection().getOriginalText();

		String id = createID(oldText);
		int numberOfNodes = exits.length;

		String flowchart = "";

		String preview = "\t<preview mimetype=\"text/html\">"
				+ LINE_SEPARATOR
				+ "\t\t<![CDATA["
				+ LINE_SEPARATOR
				+ "<DIV class=\"Flowchart\" style=\" width: 751px; height: 501px;\">"
				+ "<DIV id=\"#node_0\" class=\"Node\" style=\"left: 340px; top: 45px; width: 72px; height: 20px;\">"
				+ "<DIV class=\"start\" style=\"width: 58px; height: 20px;\">"
				+ "<DIV class=\"decorator\" style=\"width: 25px; height: 25px;\">"
				+ "</DIV>"
				+ "<DIV class=\"title\" style=\"width: 60px; height: 16px;\">Start</DIV>"
				+ "</DIV></DIV>";

		// the html representation of the nodes

		flowchart += "%%DiaFlux" + LINE_SEPARATOR + "<flowchart fcid=\"" + id + "\" name=\"" + name
				+ "\" icon=\"sanduhr.gif\" width=\"750\" height=\"500\" idCounter=\""
				+ numberOfNodes + "\">" + LINE_SEPARATOR + LINE_SEPARATOR;

		String startNode = "\t<!-- nodes of the flowchart -->" + LINE_SEPARATOR
				+ "\t<node id=\"#node_0\">" + LINE_SEPARATOR
				+ "\t\t<position left=\"320\" top=\"45\"></position>"
				+ LINE_SEPARATOR + "\t\t<start>Start</start>" + LINE_SEPARATOR
				+ "\t</node>" + LINE_SEPARATOR + LINE_SEPARATOR + LINE_SEPARATOR;

		flowchart += startNode;

		int currentNode = 0;
		int x = 450 / (exits.length + 2);
		int y = 400;

		flowchart += "\t<!-- rules of the flowchart -->" + LINE_SEPARATOR;

		for (String s : exits) {
			currentNode++;

			// to get the id
			String tempid = "#node_" + currentNode;
			int left = (x + 1) * currentNode;
			int top = y;

			// html of the exit node
			String exit = "\t<node id=\"" + tempid + "\">" + LINE_SEPARATOR
					+ "\t\t<position left=\"" + left + "\" top=\"" + top
					+ "\"></position>" + LINE_SEPARATOR + "\t\t<exit>" + s + "</exit>"
					+ LINE_SEPARATOR + "\t</node>" + LINE_SEPARATOR + LINE_SEPARATOR;

			flowchart += exit;

			preview += "<DIV id=\"" + tempid + "\" class=\"Node\" style=\"left: " + left
					+ "px; top: " + top + "px; width: 74px; height: 20px;\">"
					+ "<DIV class=\"exit\" style=\"width: 60px; height: 20px;\">"
					+ "<DIV class=\"decorator\" style=\"width: 25px; height: 25px;\"> </DIV>"
					+ "<DIV class=\"title\" style=\"width: 60px; height: 16px;\">" + s + "</DIV>"
					+ "</DIV></DIV>";

		}

		// the flowchart div part

		preview += "</DIV>" + LINE_SEPARATOR + "\t\t]]>" + LINE_SEPARATOR +
				"\t</preview></flowchart>" + LINE_SEPARATOR + "%" + LINE_SEPARATOR;

		flowchart += preview;

		String text = getSurrounding(oldText)[0] + flowchart
				+ getSurrounding(oldText)[1];

		context.getParameters().put(KnowWEAttributes.WEB, sec.getWeb());
		instance.getWikiConnector().writeArticleToWikiEnginePersistence(sec.getTitle(),
				text, context);

		String flowID = name;

		StringBuffer buffer = new StringBuffer();
		buffer.append("<kbinfo>");
		// TODO hotfix
		String test = pageName + ".." + pageName + "_KB/" + flowID;
		GetInfoObjects.appendInfoObject(web, test, buffer);
		buffer.append("</kbinfo>");
		context.setContentType("text/xml; charset=UTF-8");
		context.getWriter().write(buffer.toString());

	}

}
