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

package de.d3web.we.refactoring.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.xcl.XCList;

/**
 * @author Franz Schwab
 */
@Deprecated
public class GetXCLAction extends DeprecatedAbstractKnowWEAction {

	KnowWEParameterMap parameters;
	String id;
	String topic;
	String web;
	KnowWEArticleManager manager;
	KnowWEArticle article;
	Section<?> section;

	@Override
	public String perform(KnowWEParameterMap parameters) {
		this.parameters = parameters;
		initAttributes();
		return perform();
	}

	private void initAttributes() {
		id = parameters.get("formdata");
		topic = parameters.getTopic();
		web = parameters.getWeb();
		manager = KnowWEEnvironment.getInstance().getArticleManager(web);
		article = manager.getArticle(topic);
		section = article.getSection();
	}

	public String perform() {
		KnowWEArticle article = KnowWEEnvironment.getInstance().getArticleManager(web).getArticle(topic);
		Section<?> articleSection = article.getSection();
		List<Section<XCList>> xclists = new ArrayList<Section<XCList>>();
		articleSection.findSuccessorsOfType(XCList.class, xclists);
		StringBuilder html = new StringBuilder();
		KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
		html.append("<fieldset><div class='left'>"
				+ "<p>Es wurden <strong>x</strong> Refactorings gefunden. Bitte wählen Sie das gewünschte Refactoring aus.</p></div>"
				+ "<div style='clear:both'></div><form name='refactoringform'><div class='left'><label for='article'>Refactoring</label>"
				+ "<select name='refactoringselect'>");
		for (Section<?> xclist : xclists) {
			ArrayList<Section<SolutionID>> solutions = new ArrayList<Section<SolutionID>>();
			xclist.findSuccessorsOfType(SolutionID.class, solutions);
			html.append("<option value='" + xclist.getId() + "'>" + solutions.get(0).getOriginalText() + "</option>");
		}
		html.append("</select></div><div>"
				+ "<input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/></div></fieldset>");
		return html.toString();
	}
}