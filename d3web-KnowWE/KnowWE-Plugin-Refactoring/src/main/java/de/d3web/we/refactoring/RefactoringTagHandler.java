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

package de.d3web.we.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEScriptLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.xcl.XCList;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Franz Schwab
 */
public class RefactoringTagHandler extends AbstractTagHandler {

	public RefactoringTagHandler() {
		super("refactoring");
	}
	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		KnowWEArticle article = KnowWEEnvironment.getInstance()
						.getArticleManager(web).getArticle(topic);
		Section<?> articleSection = article.getSection();
		List<Section<Refactoring>> sections = new ArrayList<Section<Refactoring>>();
		articleSection.findSuccessorsOfType(new Refactoring(), sections);
		StringBuilder html = new StringBuilder();
		KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
		// oder veraltete Möglichkeit: html.append("<script type=text/javascript src=KnowWEExtension/scripts/RefactoringPlugin.js></script>\n");
		//html.append("<div id='refactoring-result'></div>");
		html.append("<div id='refactoring-panel' class='panel'><h3>Refactoring Konsole</h3><div id='refactoring-content'><fieldset><div class='left'>");
		html.append("<p>Es wurden <strong>" +
				sections.size() +
				"</strong> Refactorings gefunden. Bitte wählen Sie das gewünschte Refactoring aus.</p></div>");
		html.append("<div style='clear:both'></div><form name='refactoringform'><div class='left'><label for='article'>Refactoring</label>");
		html.append("<select name='refactoringselect'>");
		for(Section<Refactoring> s:sections) {
			html.append("<option value='");
			html.append(s.findChildOfType(RefactoringContent.class).getId());
			html.append("'>");
			Map<String,String> attMap = AbstractXMLObjectType.getAttributeMapFor(s);
			html.append(attMap.get("name"));
			html.append("</option>");
		}                
		html.append("</select></div><div><input type='button' value='» Ausführen' name='submit' class='button' onclick='selectRefactoring();'/></div></fieldset></div></form></div>");

		return html.toString();
	}
}
