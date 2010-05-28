/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SolutionStateViewHandler extends AbstractTagHandler {
	
	private static Map<String, Map<String, Integer>> selected = new HashMap<String, Map<String, Integer>>();
	
	private static Map<String, List<String>> articleNameList = new HashMap<String, List<String>>();
	
	public SolutionStateViewHandler() {
		super("solutionStates");
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.Solutions.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);
		
		return "<div id='sstate-panel' class='panel' name='solutionstate'><h3>" + rb.getString("KnowWE.Solutions.name") + "</h3><div>" 
			    + createDropdownList(user, web, rb)
			    + "<img src='KnowWEExtension/images/refresh.png' id='sstate-update' class='pointer' title='" + rb.getString("KnowWE.Solutions.update") + "' />\n"
			    + "<img src='KnowWEExtension/images/cross_blue.png' id='sstate-clear' class='pointer' title='" + rb.getString("KnowWE.Solutions.clear") + "' />\n"
			    + "<img src='KnowWEExtension/images/application_view_list_big.png' id='sstate-findings' class='pointer' title='" + rb.getString("KnowWE.Solutions.findings") + "' />\n"    
			    + "</p>"
			    + "<div id='sstate-result'></div>"
			    + "</div></div>";
	}
	

	public static void setSelected(String user, String web, int selectedOption) {
		Map<String, Integer> swMap = selected.get(user);
		if (swMap == null) {
			swMap = new HashMap<String, Integer>();
			selected.put(user, swMap);
		}
		swMap.put(web, selectedOption);
	}
	
	/**
	 * Returns the article names in the dropdown list of the SolutionPanel.
	 * The first two options in the rendered dropdown list are not in the
	 * List returned by this method (since they are relative to the rendered
	 * page).
	 */
	public static List<String> getArticleNames(String web) {
		return articleNameList.get(web);
	}
	
	private String createDropdownList(KnowWEUserContext user, String web, ResourceBundle rb) {
		StringBuilder b = new StringBuilder();
		int selected;
		if (SolutionStateViewHandler.selected.get(user.getUsername()) != null 
				&& SolutionStateViewHandler.selected.get(user.getUsername()).get(web) != null) {
			selected = SolutionStateViewHandler.selected.get(user.getUsername()).get(web);
		} else {
			selected = 0;
		}
		b.append("<select id='sdropdownbox' style='width:100px'>");
		b.append("<option" + (selected == 0 ? " selected" : "") + ">" 
				+ rb.getString("KnowWE.Solutions.all") + "</option>");
		b.append("<option" + (selected == 1 ? " selected" : "") + ">" 
				+ rb.getString("KnowWE.Solutions.this") + "</option>");
		
		List<String> nameList = new ArrayList<String>();
		for (KnowWEArticle art:KnowWEEnvironment.getInstance().getArticleManager(web).getArticles()) {
			Session session = D3webUtils.getSession(art.getTitle(), user, art.getWeb());
			if (session != null) {
				Collection<Solution> solutions = session.getBlackboard().getValuedSolutions();
				if (!solutions.isEmpty()) {
					nameList.add(art.getTitle());
					b.append("<option" + (selected == nameList.size() + 1 ? " selected" : "") + ">" 
							+ art.getTitle() + "</option>");
				}
			}
		}
		articleNameList.put(web, nameList);
		b.append("</select>  ");
		return b.toString();
	}
}
