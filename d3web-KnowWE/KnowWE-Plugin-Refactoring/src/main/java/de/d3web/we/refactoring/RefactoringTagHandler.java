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
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		KnowWEArticle article = KnowWEEnvironment.getInstance().getArticleManager(web).getArticle(topic);
		Section<?> articleSection = article.getSection();
		List<Section<Refactoring>> refactorings = new ArrayList<Section<Refactoring>>();
		articleSection.findSuccessorsOfType(new Refactoring(), refactorings);
		StringBuilder html = new StringBuilder();
		KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
		html.append("<div id='refactoring-panel' class='panel'><h3>Refactoring Konsole</h3><div id='refactoring-content'>"
				+ "<fieldset><div class='left'>" + "<p>Es wurden <strong>" + refactorings.size()
				+ "</strong> Refactorings auf dieser Seite gefunden. Bitte wählen Sie das gewünschte Refactoring aus.</p></div>"
				+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Refactoring</label>"
//				+ "<select name='multipleTest' multiple class='refactoring' size='2'><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option></select>"
//				+ "<input type='checkbox' name='checkboxTest' class='refactoring' value='printer'>Printer"
//				+ "<input type='checkbox' name='checkboxTest' class='refactoring' value='foto'>Foto"
//				+ "<input type='radio' name='radioTest' class='refactoring' value='radioprinter'>radioPrinter<input type='radio' name='radioTest' class='refactoring' value='radiofoto'>radioFoto"
//				+ "<input type='text' name='textTest' class='refactoring'>"
//				+ "<textarea name='testTextarea' class='refactoring'></textarea>"
				+ "<select name='selectRefactoring' class='refactoring'>");
		for (Section<Refactoring> refactoring : refactorings) {
			Map<String, String> attributes = AbstractXMLObjectType.getAttributeMapFor(refactoring);
			html.append("<option value='" + refactoring.findChildOfType(RefactoringContent.class).getId() + "'>" + attributes.get("name")
					+ "</option>");
		}
		// TODO onlick ersetzen, d.h. den button explizit registrieren
		html.append("</select></div><div><input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/>"
				+ "</div></fieldset></div></form></div>");
		return html.toString();
	}
}