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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.auth.authorize.Role;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.refactoring.dialog.RefactoringScript;
import de.d3web.we.refactoring.dialog.RefactoringScriptJavaConcrete;
import de.d3web.we.refactoring.script.DeleteComments;
import de.d3web.we.refactoring.script.EstablishedSolutionsFindingsTraceToXCL;
import de.d3web.we.refactoring.script.MergeXCLs;
import de.d3web.we.refactoring.script.QuestionTreeToQuestionsSection;
import de.d3web.we.refactoring.script.QuestionsSectionToQuestionTree;
import de.d3web.we.refactoring.script.Rename;
import de.d3web.we.refactoring.script.XCLToRules;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Franz Schwab
 */
public class RefactoringTagHandler extends AbstractTagHandler {
	public static final SortedMap<String, Class<? extends RefactoringScript>> SCRIPTS = createScriptMap();
    /** If true, check to see that user is authenticated. The value is {@value}. */
    public static final boolean CHECK_AUTHENTICATION = false;
	
	private static SortedMap<String,Class<? extends RefactoringScript>> createScriptMap(){
		SortedMap<String, Class<? extends RefactoringScript>> SCRIPTS = new TreeMap<String, Class<? extends RefactoringScript>>();
		SCRIPTS.put("DeleteComments",							DeleteComments.class);
		SCRIPTS.put("EstablishedSolutionsFindingsTraceToXCL",	EstablishedSolutionsFindingsTraceToXCL.class);
		SCRIPTS.put("MergeXCLs",								MergeXCLs.class);
		SCRIPTS.put("QuestionsSectionToQuestionTree",			QuestionsSectionToQuestionTree.class);
		SCRIPTS.put("QuestionTreeToQuestionsSection",			QuestionTreeToQuestionsSection.class);
		SCRIPTS.put("Rename",									Rename.class);
		SCRIPTS.put("XCLToRules",								XCLToRules.class);
		SCRIPTS.put("RefactoringScriptJavaConcrete",			RefactoringScriptJavaConcrete.class);
		return Collections.unmodifiableSortedMap(SCRIPTS);
	}

	public RefactoringTagHandler() {
		super("refactoring");
	}
	
	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		StringBuffer html = new StringBuffer();
		html.append("<div id='refactoring-panel' class='panel'><h3>Refactoring Konsole</h3><div id='refactoring-content'>");
		if (CHECK_AUTHENTICATION) {
			WikiEngine we = WikiEngine.getInstance(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), null);
			WikiContext context = we.createContext(user.getHttpRequest(), WikiContext.VIEW);
			if (!we.getAuthorizationManager().isUserInRole(context.getWikiSession(), Role.AUTHENTICATED)) {
				html.append("<p>Script execution not permitted for unauthenticated users.</p>");
			}
		}
		KnowWEArticle article = KnowWEEnvironment.getInstance().getArticleManager(web).getArticle(topic);
		Section<?> articleSection = article.getSection();
		List<Section<Refactoring>> refactorings = new ArrayList<Section<Refactoring>>();
		articleSection.findSuccessorsOfType(Refactoring.class, refactorings);
		KnowWERessourceLoader.getInstance().add("RefactoringPlugin.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
		html.append("<fieldset><div class='left'>" + "<p>");
		html.append("Es wurden <strong>" + refactorings.size()
				+ "</strong> Refactorings auf dieser Seite und <strong>" + SCRIPTS.size()  
				+ "</strong> Built-in Refactorings gefunden. Bitte wählen Sie das gewünschte Refactoring aus.</p></div>"
				+ "<div style='clear:both'></div><form name='refactoringForm'><div class='left'><label for='article'>Refactoring</label>"
//				+ "<select name='multipleTest' multiple class='refactoring' size='2'><option value='1'>1</option><option value='2'>2</option><option value='3'>3</option></select>"
//				+ "<input type='checkbox' name='checkboxTest' class='refactoring' value='printer'>Printer"
//				+ "<input type='checkbox' name='checkboxTest' class='refactoring' value='foto'>Foto"
//				+ "<input type='radio' name='radioTest' class='refactoring' value='radioprinter'>radioPrinter<input type='radio' name='radioTest' class='refactoring' value='radiofoto'>radioFoto"
//				+ "<input type='text' name='textTest' class='refactoring'>"
//				+ "<textarea name='testTextarea' class='refactoring'></textarea>"
				+ "<input type='hidden' name='startNewRefactoringSession' class='refactoring'>"
				+ "<select name='selectRefactoring' class='refactoring' size='30'>");
		
		html.append("<optgroup label='Refactorings dieser Seite' id ='custom'>");
		for (Section<Refactoring> refactoring : refactorings) {
			Map<String, String> attributes = AbstractXMLObjectType.getAttributeMapFor(refactoring);
			html.append("<option value='" + refactoring.findChildOfType(RefactoringContent.class).getId() + "' label='"
					+ attributes.get("name") + "'>" + attributes.get("name")
					+ "</option>");
		}
		html.append("</optgroup>");
		
		html.append("<optgroup label='Built-in Refactorings' id ='built-in'>");
		for(String scriptName: SCRIPTS.keySet()) {
			html.append("<option value='" + scriptName + "' label='" + scriptName + "'>" + scriptName
			+ "</option>");
		}
		html.append("</optgroup>");
		
		html.append("</select></div><div><input type='button' value='Ausführen' name='submit' class='button' onclick='refactoring();'/>"
				+ "</div></fieldset></div></form></div>");
		return html.toString();
	}
}