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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEScriptLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.xcl.*;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.taghandler.TagHandlerType;
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
				Section articleSection = article.getSection();
		List<Section> sections = new ArrayList<Section>();
		articleSection.findSuccessorsOfType(XCList.class, sections);
		StringBuilder html = new StringBuilder();
		KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
		// oder veraltete Möglichkeit: html.append("<script type=text/javascript src=KnowWEExtension/scripts/RefactoringPlugin.js></script>\n");
		//html.append("<div id='refactoring-result'></div>");
		html.append("<div id='refactoring'><div id='refactoring-panel' class='panel'><h3>Refactoring Konsole</h3><fieldset><div class='left'>");
		html.append("<p>Es wurden <strong>x</strong> Refactorings gefunden. Bitte wählen Sie das gewünschte Refactoring aus.</p></div>");
		html.append("<div style='clear:both'></div><form name='refactoringform'><div class='left'><label for='article'>Refactoring</label>");
		html.append("<select name='refactoringselect'>");
		for(Section s:sections) {
			html.append("<option value='");
			html.append(s.getId());
			html.append("'>");
			List<Section> ls = new ArrayList<Section>();
			s.findSuccessorsOfType(SolutionID.class, ls);
			html.append(ls.get(0).getOriginalText());
			html.append("</option>");
		}                
		html.append("</select></div><div><input type='button' value='» Ausführen' name='submit' class='button' onclick='sendRefactoringRequest();'/></div></fieldset></form></div> </div>");

		return html.toString();
	}
}
