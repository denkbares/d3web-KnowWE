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

import java.util.Collection;
import java.util.List;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.table.XCLRelationBuilder;
import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.kernel.verbalizer.ConditionVerbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.KnowledgeUtils;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.Context;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableCellContentRenderer;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.kdom.table.TableUtils;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.inference.PSMethodXCL;

/**
 * @author jochen
 * 
 *         cell content type for xclrelation-content cells
 * 
 */
public class EntryCellContent extends TableCellContent {

	/**
	 * <p>
	 * Returns the renderer for the TableCellContent.
	 * </p>
	 * 
	 * @return {@link KnowWEDomRenderer}
	 * @see TableCellContentRenderer
	 */
	@Override
	public KnowWEDomRenderer getRenderer() {
		return CoveringTableEntryRenderer.getInstance();
	}

	@Override
	public void init() {
		this.addSubtreeHandler(new EntryCellContentSubtreeHandler());
	}

}

class CoveringTableEntryRenderer extends TableCellContentRenderer {

	/**
	 * Wraps the content of the cell (sectionText) with the HTML-Code needed for
	 * the table
	 */
	@Override
	protected String wrappContent(String sectionText, Section sec,
			KnowWEUserContext user) {

		String title = "relation_not_found";

		String kbrelId = (String) KnowWEUtils.getStoredObject(sec.getArticle()
				.getWeb(), sec.getTitle(), sec.getID(),
				EntryCellContentSubtreeHandler.XCLRELATION_ID);
		if (kbrelId != null) {
			// Get the KnowledgeSlices from KB and find the XCLRelation to be
			// rendered
			KnowledgeBaseManagement mgn = D3webModule
					.getKnowledgeRepresentationHandler(sec.getWeb()).getKBM(sec.getTitle());

			Collection<KnowledgeSlice> models = mgn.getKnowledgeBase()
					.getAllKnowledgeSlicesFor(PSMethodXCL.class);
			for (KnowledgeSlice knowledgeSlice : models) {
				if (knowledgeSlice instanceof XCLModel) {

					// Check if model contains the relation
					if (((XCLModel) knowledgeSlice).findRelation(kbrelId) != null) {

						XCLRelation rel = ((XCLModel) knowledgeSlice)
								.findRelation(kbrelId);

						title = ((XCLModel) knowledgeSlice).getSolution().getName()+": "+ new ConditionVerbalizer().verbalize( rel.getConditionedFinding(),RenderingFormat.PLAIN_TEXT,null);
						break;
						// // eval the Relation to find the right Rendering
						// try {
						// boolean b =
						// ((XCLModel)knowledgeSlice).findRelation(kbrelId
						// ).eval(c);
						// // Highlight Relation
						// return this.renderRelationChildren(sec, user, true,
						// b);
						// } catch (Exception e) {
						// // Call the XCLRelationMarkerHighlightingRenderer
						// // without any additional info
						// return this.renderRelationChildren(sec, user, false,
						// false);
						// }
					}
				}
			}

		}else {
			Object o = KnowWEUtils.getStoredObject(sec, EntryCellContentSubtreeHandler.KEY_REPORT);
			if(o != null && o instanceof Message) {
				title = ((Message)o).getMessageText();
			}
		}

		String sectionID = sec.getID();
		StringBuilder html = new StringBuilder();
		if (sectionText.trim().length() > 0) {
			if(kbrelId != null) {
			html
					.append("<td style='color: green; background-color:#DDDDDD;text-align:center;'>   ");
			html.append("<span id='" + sec.getID()
					+ "' class = 'XCLRelationInTable'>");
			html.append("<span id=''>");
			html.append("<span title='" + title + "'>");

			generateContent(sectionText, sec, user, sectionID, html);
			html.append("</span>");
			html.append("</span>");
			html.append("</span>");
			html.append("</td>");
			}else {
				html.append("<td title='" + title + "' style='background-color:red;text-align:center;'>   ");
				generateContent(sectionText, sec, user, sectionID, html);
				html.append("</td>");
			}

		} else {
			html.append("<td style='background-color:#BBBBBB;'>   ");
			html.append("</td>");
		}
		return KnowWEEnvironment.maskHTML(html.toString());
	}

	private static CoveringTableEntryRenderer instance = null;

	public static CoveringTableEntryRenderer getInstance() {
		if (instance == null) {
			instance = new CoveringTableEntryRenderer();
		}
		return instance;
	}

}

class EntryCellContentSubtreeHandler extends D3webSubtreeHandler {

	public static final String XCLRELATION_ID = "xclid";
	public static final String KEY_REPORT = "report_message";

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
		if (s.getOriginalText().trim().length() > 0) {

			KnowledgeBaseManagement mgn = getKBM(article);
			
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
						line, col, false, s.getID(), xcl);

				for (Message message : messages) {
					if (message.getMessageText().startsWith("relID:")) {
						String relationID = message.getMessageText()
								.replaceAll("relID:", "");
						KnowWEUtils.storeSectionInfo(s.getArticle().getWeb(), s
								.getTitle(), s.getID(), XCLRELATION_ID,
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
