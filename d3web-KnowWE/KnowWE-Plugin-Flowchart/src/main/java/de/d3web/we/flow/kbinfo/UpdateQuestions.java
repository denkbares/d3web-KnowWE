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

package de.d3web.we.flow.kbinfo;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;


/**
 * @author Florian Ziegler
 */
public class UpdateQuestions implements KnowWEAction {
	
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		// get everything important from the parameter map
		String web = parameterMap.getWeb();
		String infos = parameterMap.get("infos");
		String questionText = infos.substring(infos.indexOf("[Text]") + 6, infos.indexOf("[Type]"));
		String questionType = infos.substring(infos.indexOf("[Type]") + 6, infos.indexOf("[Pagename]"));
		String pageName = infos.substring(infos.indexOf("[Pagename]") + 10, infos.indexOf("[Answers]"));
		String answersToLine = infos.substring(infos.indexOf("[Answers]") + 9).replace("[next]", ":next:");
		String[] answers = answersToLine.split(":next:");
		
		
		// get everything to update the article
		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(pageName);
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		Section sec = article.getSection();
		KnowWEParameterMap map =  new KnowWEParameterMap(KnowWEAttributes.WEB, sec.getWeb());
		String oldText = article.getSection().getOriginalText();

		String firstPart = "";
		String lastPart = "";
		String newQuestionsSection = "";
		
		// check if the page already contains a Kopic/Questions-section
		// and if not add it and create the new Questions-section with
		// the new question and answers
		if (oldText.contains("<Questions-section>")
				&& oldText.contains("</Questions-section>")) {
			firstPart = oldText.substring(0, oldText
					.indexOf("<Questions-section>"));
			lastPart = oldText.substring(oldText
					.indexOf("</Questions-section>"));
			String questionsSection = oldText.substring(oldText
					.indexOf("<Questions-section>"), oldText
					.indexOf("</Questions-section>"));
			newQuestionsSection = this.addQuestion(questionsSection,
					questionText, questionType, answers);
			
		// if there is a Kopic Tag, insert Question-section directly after
		} else if (oldText.contains("<Kopic>") && oldText.contains("</Kopic>")) {
			firstPart = oldText.substring(0, oldText.indexOf("<Kopic>"));
			lastPart = LINE_SEPARATOR + "</Questions-section>"
					+ oldText.substring(oldText.indexOf("<Kopic>") + 7);
			newQuestionsSection = this.addQuestion("<Kopic>" + LINE_SEPARATOR
					+ "<Questions-section>", questionText, questionType,
					answers);

		// if Kopic as well as Question-section Tag are missing
		} else {
			firstPart = oldText;
			lastPart = "</Questions-section></Kopic>";
			newQuestionsSection = this.addQuestion("<Kopic>" + LINE_SEPARATOR + "<Questions-section>",
					questionText, questionType, answers);
		}
		
		
		String newText = firstPart + newQuestionsSection + lastPart;
		instance.saveArticle(sec.getWeb(), sec.getTitle(), newText, map);

		return "questionText: " + questionText + " questionType: "
		+ questionType + " pageName: " + pageName;
	}
	
	
	private String addQuestion(String questionsSection, String questionText, String questionType, String[] answers) { 
		String newQuestionsSection = questionsSection;
		StringBuffer buffy = new StringBuffer();
		
		if (!questionsSection.contains("added Questions")) {
			buffy.append(LINE_SEPARATOR + "added Questions" + LINE_SEPARATOR);
			buffy.append("- " + questionText + " [" + questionType + "]" + LINE_SEPARATOR);
			for (String s : answers) {
				buffy.append("-- " + s + LINE_SEPARATOR);
			}
			
		} else {
			buffy.append("- " + questionText + " [" + questionType + "]" + LINE_SEPARATOR);
			for (String s : answers) {
				buffy.append("-- " + s + LINE_SEPARATOR);
			}
		}
		newQuestionsSection += buffy.toString();
		return newQuestionsSection;
	}


	@Override
	public boolean isAdminAction() {
		// TODO Auto-generated method stub
		return false;
	}

}
