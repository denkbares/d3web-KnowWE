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

import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.table.XCLRelationBuilder;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.KnowledgeUtils;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.Context;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.kdom.table.TableUtils;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;

public class EntryCellContentSubtreeHandler implements ReviseSubTreeHandler {

	public static final String XCLRELATION_ID = "xclid";
	public static final String KEY_REPORT = "report_message";

	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
		if (s.getOriginalText().trim().length() > 0) {

			KnowledgeBaseManagement mgn = D3webModule
					.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(article, s);
			
			if (mgn == null) {
				return null;
			}
			
			SingleKBMIDObjectManager mgr = new SingleKBMIDObjectManager(mgn);
			XCLRelationBuilder xcl = new XCLRelationBuilder(null);

			Context c = ContextManager.getInstance().getContextForClass(s,
					SolutionColumnContext.class);
			
			if(c == null) {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
				"No solution context found"));
				return null;
			}

			Section lineSec = KnowWEObjectTypeUtils.getAncestorOfType(s,
					TableLine.class);
			Section answerSec = lineSec.findSuccessor(AnswerCellContent.class);
			String answer = null;
			if (answerSec != null) {
				answer = answerSec.getOriginalText().trim();

				int line = TableUtils.getRow(s);
				int col = TableUtils.getColumn(s);

				Section questionCellContent = AnswerCellContent.getQuestionCellContent(
						answerSec);
				String question = "questionCellNotFound";
				if(questionCellContent != null) {
					question = questionCellContent.getOriginalText();
				}
				question = question.replaceAll("\\|", "");
				question = question.replaceAll("__", "");
				
				if(question == null) {
					KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
					"No question found"));
					return null;
				}

				List<Message> messages = KnowledgeUtils.addKnowledge(mgr,
						question.trim(), answer, ((SolutionColumnContext) c)
								.getSolution(), s.getOriginalText().trim(),
						line, col, false, s.getId(), xcl);

				for (Message message : messages) {
					if (message.getMessageText().startsWith("relID:")) {
						String relationID = message.getMessageText()
								.replaceAll("relID:", "");
						KnowWEUtils.storeSectionInfo(s.getArticle().getWeb(), s
								.getTitle(), s.getId(), XCLRELATION_ID,
								relationID);
					}else {
						KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"Error creating relation: "+message.getMessageText()));
					}
				}
			} else {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"AnswerSection not found"));
			}
			
		}
		
		return null;
	}

}
