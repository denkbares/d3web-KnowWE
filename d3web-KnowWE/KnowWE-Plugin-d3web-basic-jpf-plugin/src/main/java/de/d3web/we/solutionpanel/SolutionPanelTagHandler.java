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

package de.d3web.we.solutionpanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.session.Session;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Taghandler for integrating the solution panel into a wiki page. Displays the
 * solution states (e.g., established, excluded...) when the knowledge base is
 * used in a wiki page, e.g. via the quick interview.
 * 
 * @author Martina Freiberg
 * @created 13.08.2010
 */
public class SolutionPanelTagHandler extends AbstractTagHandler {

	private static Map<String, Map<String, Integer>> selected = new HashMap<String, Map<String, Integer>>();

	private static Map<String, Map<String, List<String>>> articleNamesMap = new HashMap<String, Map<String, List<String>>>();

	/**
	 * Create the TagHandler --> "solutionPanel" defines the "name" of the tag,
	 * so the tag is inserted with like [KnowWEPlugin solutionPanel]
	 */
	public SolutionPanelTagHandler() {
		super("solutionPanel");
		// KnowWERessourceLoader.getInstance().add("solPane.css",
		// KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("solutionPanel.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {

		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);

		return "<div id='sstate-panel' class='panel' name='solutionstate'><h3>"
				+ rb.getString("KnowWE.Solutions.name")
				+ "</h3><div>"
				+ createDropdownList(user, web, rb)
				+ "<img src='KnowWEExtension/images/refresh.gif' id='sstate-update' class='pointer' title='"
				+ rb.getString("KnowWE.Solutions.update")
				+ "' />\n"
				+ "<img src='KnowWEExtension/images/progress_stop.gif' id='sstate-clear' class='pointer' title='"
				+ rb.getString("KnowWE.Solutions.clear")
				+ "' />\n"
				+ "<div id='solutionPanelResults'></div>"
				+ "</div>"
				+ "</div>";

	}

	public static void setSelected(String web, String user, int selectedOption) {
		getSelectedMap(web).put(user, selectedOption);
	}

	private static Map<String, Integer> getSelectedMap(String web) {
		Map<String, Integer> swMap = selected.get(web);
		if (swMap == null) {
			swMap = new HashMap<String, Integer>();
			selected.put(web, swMap);
		}
		return swMap;
	}

	/**
	 * Returns the article names in the dropdown list of the SolutionPanel. The
	 * first two options in the rendered dropdown list are not in the List
	 * returned by this method (since they are relative to the rendered page).
	 */
	public static List<String> getArticleNames(String web, String user) {
		Map<String, List<String>> articleNamesForWeb = getArticleNamesMap(web);
		List<String> articleNames = articleNamesForWeb.get(user);
		if (articleNames == null) {
			articleNames = new ArrayList<String>();
			articleNamesForWeb.put(user, articleNames);
		}
		return articleNames;
	}

	private static Map<String, List<String>> getArticleNamesMap(String web) {
		Map<String, List<String>> articleNamesForWeb = articleNamesMap.get(web);
		if (articleNamesForWeb == null) {
			articleNamesForWeb = new HashMap<String, List<String>>();
			articleNamesMap.put(web, articleNamesForWeb);
		}
		return articleNamesForWeb;
	}

	private String createDropdownList(KnowWEUserContext user, String web, ResourceBundle rb) {
		StringBuilder b = new StringBuilder();
		int index;
		if (getSelectedMap(web).get(user.getUserName()) != null) {
			index = getSelectedMap(web).get(user.getUserName());
		}
		else {
			index = 0;
		}
		b.append("<select id='sdropdownbox' style='width:100px'>");
		b.append("<option" + (index == 0 ? " selected" : "") + ">"
				+ rb.getString("KnowWE.Solutions.all") + "</option>");
		b.append("<option" + (index == 1 ? " selected" : "") + ">"
				+ rb.getString("KnowWE.Solutions.this") + "</option>");

		List<String> nameList = new ArrayList<String>();
		for (KnowWEArticle art : KnowWEEnvironment.getInstance().getArticleManager(web).getArticles()) {
			Session session = D3webUtils.getSession(art.getTitle(), user, art.getWeb());
			if (session != null) {
				if (!session.getBlackboard().getSolutions(State.ESTABLISHED).isEmpty()
						|| !session.getBlackboard().getSolutions(State.SUGGESTED).isEmpty()
						|| !session.getBlackboard().getSolutions(State.EXCLUDED).isEmpty()) {
					nameList.add(art.getTitle());
					b.append("<option" + (index == nameList.size() + 1 ? " selected" : "") + ">"
							+ art.getTitle() + "</option>");
				}
			}
		}
		getArticleNamesMap(web).put(user.getUserName(), nameList);
		b.append("</select>  ");
		return b.toString();
	}
}
