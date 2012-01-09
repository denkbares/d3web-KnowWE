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
import de.d3web.we.basic.D3webModule;
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
	public void execute(UserActionContext context) throws IOException {
		// get everything important from the parameter map
		String web = context.getParameter(KnowWEAttributes.WEB);
		String solutionText = context.getParameter("text");
		String pageName = context.getParameter("pageName");

		// revert the escaped special characters
		solutionText = UpdateQuestions.revertSpecialCharacterEscape(solutionText);

		// get everything to update the article
		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(pageName);
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		Section<KnowWEArticle> sec = article.getSection();
		context.getParameters().put(KnowWEAttributes.WEB, sec.getWeb());
		String oldText = article.getSection().getOriginalText();

		// get everything for the new solution
		String[] surroundings = UpdateQuestions.getRightInsertPosition(oldText, "Solution");
		String firstPart = surroundings[0];
		String lastPart = surroundings[1];
		String currentSolutionsSection = UpdateQuestions.getCurrentSectionContent(oldText,
				"Solution");
		String newSolutionsSection = this.addSolution(currentSolutionsSection, solutionText);

		// save the new article
		String newText = firstPart + newSolutionsSection + lastPart;
		instance.getWikiConnector().writeArticleToWikiEnginePersistence(sec.getTitle(),
				newText, context);

		// remove leading and ending quotes
		solutionText = UpdateQuestions.removeLeadingAndClosingQuotes(solutionText);

		// get the right id for the nodemodel
		KnowledgeBase kb = D3webModule.getKnowledgeRepresentationHandler(article.getWeb()).getKB(
				article.getTitle());

		List<Solution> diagnoses = kb.getManager().getSolutions();

		String diagnosisID = null;
		for (Solution diagnosis : diagnoses) {
			if (diagnosis.getName().equals(
					UpdateQuestions.revertSpecialCharacterEscape(solutionText))) {
				diagnosisID = diagnosis.getName();
				break;
			}

		}

		if (diagnosisID != null) {

			StringBuilder bob = new StringBuilder();
			bob.append("<kbinfo>");
			// TODO hotfix
			String test = pageName + ".." + pageName + "_KB/" + diagnosisID;

			GetInfoObjects.appendInfoObject(web, test, bob);
			bob.append("</kbinfo>");
			context.setContentType("text/xml; charset=UTF-8");
			context.getWriter().write(bob.toString());

		}
		else {
			context.setContentType("text/plain; charset=UTF-8");
			context.getWriter().write("Could not find id for: " + solutionText);
		}

	}

}
