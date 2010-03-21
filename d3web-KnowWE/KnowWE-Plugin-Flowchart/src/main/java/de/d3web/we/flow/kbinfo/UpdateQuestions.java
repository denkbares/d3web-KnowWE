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

import java.io.IOException;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;


/**
 * @author Florian Ziegler
 */
public class UpdateQuestions extends AbstractAction {
	
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	
	private String addQuestion(String questionsSection, String questionText, String questionType, String[] answers) { 
		String newQuestionsSection = questionsSection;
		StringBuffer buffy = new StringBuffer();
		
		if (!questionsSection.contains("added Questions")) {
			buffy.append(LINE_SEPARATOR + "added Questions" + LINE_SEPARATOR);
			buffy.append("- " + revertSpecialCharacterEscape(questionText) + " [" + questionType + "]" + LINE_SEPARATOR);
			if (!questionType.equals("num")) {
				for (String s : answers) {
					buffy.append("-- " + revertSpecialCharacterEscape(s) + LINE_SEPARATOR);
				}
			}
			
		} else {
			buffy.append("- " + revertSpecialCharacterEscape(questionText) + " [" + questionType + "]" + LINE_SEPARATOR);
			if (!questionType.equals("num")) {
				for (String s : answers) {
					buffy.append("-- " + revertSpecialCharacterEscape(s) + LINE_SEPARATOR);
				}
			}
		}
		newQuestionsSection += buffy.toString();
		return newQuestionsSection;
	}

	/**
	 * some special characters cause bugs, so the have to be
	 * escaped in javascript. this method turns them back into
	 * normal special characters.
	 * @param text
	 * @return
	 */
	public static String revertSpecialCharacterEscape(String text) {
		text = text.replace("[FLOWCHART_ST]", "<");
		text = text.replace("[FLOWCHART_AND]", "&");
		text = text.replace("[FLOWCHART_PLUS]", "+");
		text = text.replace("[FLOWCHART_CAP]", "^");
		text = text.replace("[FLOWCHART_BACKSLASH]", "\\");
		text = text.replace("[FLOWCHART_AG]", "`");
		text = text.replace("[FLOWCHART_HASH]", "#");
		text = text.replace("[FLOWCHART_oe]", "ö");
		text = text.replace("[FLOWCHART_OE]", "Ö");
		text = text.replace("[FLOWCHART_ae]", "ä");
		text = text.replace("[FLOWCHART_AE]", "Ä");
		text = text.replace("[FLOWCHART_ue]", "ü");
		text = text.replace("[FLOWCHART_UE]", "Ü");
		text = text.replace("[FLOWCHART_SS]", "ß");
		System.out.println(text);
		return text;
	}
	
	@Override
	public boolean isAdminAction() {
		return false;
	}


	@Override
	public void execute(ActionContext context) throws IOException {
		// get everything important from the parameter map
		String web = context.getParameter(KnowWEAttributes.WEB);
		String questionText = context.getParameter("text");
		String questionType = context.getParameter("type");
		String pageName = context.getParameter("pageName");
		String answersToLine = context.getParameter("answers");
		String[] answers = answersToLine.split("::");
		
		System.out.println(questionText);
		System.out.println(questionType);
		System.out.println(pageName);
		System.out.println(answersToLine);
		
		
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

		context.getWriter().write("questionText: " + questionText + " questionType: "
		+ questionType + " pageName: " + pageName);
		
	}

}
