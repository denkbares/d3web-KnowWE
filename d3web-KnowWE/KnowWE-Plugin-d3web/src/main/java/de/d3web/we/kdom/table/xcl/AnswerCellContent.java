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

package de.d3web.we.kdom.table.xcl;

import java.util.ArrayList;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.core.terminology.Question;
import de.d3web.core.terminology.QuestionChoice;
import de.d3web.core.terminology.QuestionNum;
import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.KnowledgeUtils;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.table.TableCellContentRenderer;
import de.d3web.we.kdom.table.TableUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class AnswerCellContent extends CoveringTableHeaderColumnCellContent {

	private static AnswerCellContent instance;

	public static AnswerCellContent getInstance() {
		if (instance == null)
			instance = new AnswerCellContent();
		return instance;
	}

	public static Section getQuestionCellContent(Section s) {
		if (s.getObjectType() instanceof AnswerCellContent) {
			return (Section) KnowWEUtils.getStoredObject(s.getArticle()
					.getWeb(), s.getTitle(), s.getId(), QUESTION_CELL);
		}

		return null;
	}
	
	@Override
	public void init() {
		this.addReviseSubtreeHandler(new AnswerCellHandler());
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return AnswerCellRenderer.getInstance();
	}

}

class AnswerCellRenderer extends TableCellContentRenderer {

	/**
	 * Wraps the content of the cell (sectionText) with the HTML-Code needed for
	 * the table
	 */
	@Override
	protected String wrappContent(String sectionText, Section sec,
			KnowWEUserContext user) {


		String title = "";
		
		Object o = KnowWEUtils.getStoredObject(sec, QuestionCellHandler.KEY_REPORT);
		if(o != null && o instanceof Message) {
			title = ((Message)o).getMessageText();
		}
	
		String sectionID = sec.getId();
		StringBuilder html = new StringBuilder();
		html.append("<td title='"+title+"' style='background-color:#888888;'>   ");
		//html.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		html.append("  ");
		generateContent(sectionText, sec, user, sectionID, html);
			
		html.append("</td>");
		return KnowWEEnvironment.maskHTML(html.toString());
	}

	private static AnswerCellRenderer instance = null;

	public static AnswerCellRenderer getInstance() {
		if (instance == null) {
			instance = new AnswerCellRenderer();
		}
		return instance;
	}

}

class AnswerCellHandler implements ReviseSubTreeHandler {

	public static final String KEY_REPORT = "report_message";

	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
		
		KnowledgeBaseManagement mgn = D3webModule
			.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(article, s);
		
		if (mgn == null) {
			return null;
		}
		
		Section questionCell = AnswerCellContent.getQuestionCellContent(s);

		String qName = KnowledgeUtils
				.getQuestionNameFromDeclaration(questionCell.getOriginalText());


		SingleKBMIDObjectManager mgr = new SingleKBMIDObjectManager(mgn);

		int line = TableUtils.getRow(s);

		Question q = mgr.findQuestion(qName);

		if (q == null) {

			KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
					"Question not present : " + qName));

		} else {

			String answerText = s.getOriginalText().trim();
			if (q instanceof QuestionChoice) {
				AnswerChoice ac = mgr.findAnswerChoice(((QuestionChoice) q),
						answerText);
				if (ac == null) {
					mgn.addChoiceAnswer(((QuestionChoice) q), answerText);
					KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
							"Creating answer: " + answerText));
				} else {
					KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
							"Answer already present: " + answerText));
				}
			} else if (q instanceof QuestionNum) {
				List<Message> errors = new ArrayList<Message>();
				TerminalCondition cond = KnowledgeUtils.tryBuildCondNum(answerText, line, 1, s.getId(),
						errors, (QuestionNum) q);
				if(cond != null) {
					KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
							"Numerical Condition OK: " + answerText));
				}else {
					KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
							"Numerical Condition wrong: " + errors.get(0).getMessageText()));
				}

			} else {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"Cannot handler question type for: " + qName));
			}

		}
		
		return null;

	}

}
