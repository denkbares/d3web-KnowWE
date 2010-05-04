/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

/**
 * @author Florian Ziegler
 */
public class UpdateSolutions extends AbstractAction {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private String addSolution(String solutionsSection, String solutionText) {

		// if (!solutionsSection.contains(solutionText)) {
		// "Contains" is too weak: tried to add Solution "Bogen Vergiftung" when
		// "Bogen Vergiftung Kind"
		// was already a Solution -> didnt do anything
		// Solution must be in its own line

		Matcher matcher = Pattern.compile("^" + solutionText + "$", Pattern.MULTILINE).matcher(
				solutionsSection);

		if (!matcher.lookingAt()) return solutionsSection + solutionText + LINE_SEPARATOR;
		else return solutionsSection;
	}

	@Override
	public boolean isAdminAction() {
		return false;
	}

	@Override
	public void execute(ActionContext context) throws IOException {
		// get everything important from the parameter map
		String web = context.getParameter(KnowWEAttributes.WEB);
		String solutionText = context.getParameter("text");
		String pageName = context.getParameter("pageName");

		// revert the escaped special characters
		solutionText = UpdateQuestions.revertSpecialCharacterEscape(solutionText);

		System.out.println(solutionText);

		// get everything to update the article
		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(pageName);
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		Section<KnowWEArticle> sec = article.getSection();
		KnowWEParameterMap map = new KnowWEParameterMap(KnowWEAttributes.WEB, sec.getWeb());
		String oldText = article.getSection().getOriginalText();


		// get everything for the new solution
		String[] surroundings = UpdateQuestions.getRightInsertPosition(oldText, "Solutions");
		String firstPart = surroundings[0];
		String lastPart = surroundings[1];
		String currentSolutionsSection = UpdateQuestions.getCurrentSectionContent(oldText, "Solutions");
		String newSolutionsSection = this.addSolution(currentSolutionsSection, solutionText);
		

		// save the new article
		String newText = firstPart + newSolutionsSection + lastPart;
		instance.saveArticle(sec.getWeb(), sec.getTitle(), newText, map);
		
		
		// remove leading and ending quotes
		solutionText = UpdateQuestions.removeLeadingAndClosingQuotes(solutionText);
		
		
		// get the right id for the nodemodel
		KnowledgeBase kb = D3webModule.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(
				article, null, sec).getKnowledgeBase();
		
		List<Solution> diagnoses = kb.getDiagnoses();

		String diagnosisID = null;
		for (Solution diagnosis : diagnoses) {
			if (diagnosis.getName().equals(
					UpdateQuestions.revertSpecialCharacterEscape(solutionText))) {
				diagnosisID = diagnosis.getId();
				break;
			}

		}

		if (diagnosisID != null) {

			StringBuffer buffer = new StringBuffer();
			buffer.append("<kbinfo>");
			// TODO hotfix
			String test = pageName + ".." + pageName + "_KB/" + diagnosisID;

			GetInfoObjects.appendInfoObject(web, test, buffer);
			buffer.append("</kbinfo>");
			context.getWriter().write(buffer.toString());

		}
		else {
			context.getWriter().write("Could not find id for: " + solutionText);
		}

	}

}
