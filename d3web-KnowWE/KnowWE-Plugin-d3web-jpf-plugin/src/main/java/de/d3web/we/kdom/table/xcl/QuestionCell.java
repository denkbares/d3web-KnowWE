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

package de.d3web.we.kdom.table.xcl;

import java.util.Collection;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.util.D3webQuestionFactory;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.KnowledgeUtils;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.Context;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.table.Table;
import de.d3web.we.kdom.table.TableCellContentRenderer;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author jochen
 * 
 *         A type for cells of the first column, that contain a question
 * 
 */
public class QuestionCell extends CoveringTableHeaderColumnCellContent {

	private static QuestionCell instance;

	public static QuestionCell getInstance() {
		if (instance == null) instance = new QuestionCell();
		return instance;
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return QuestionCellRenderer.getInstance();
	}

	@Override
	public void init() {
		this.addSubtreeHandler(new QuestionCellHandler());
	}

}

class QuestionCellRenderer extends TableCellContentRenderer {

	/**
	 * Wraps the content of the cell (sectionText) with the HTML-Code needed for
	 * the table
	 */
	@Override
	protected String wrappContent(String sectionText, Section sec,
			KnowWEUserContext user) {

		Context c = ContextManager.getInstance().getContextForClass(sec,
				SolutionColumnContext.class);

		String title = "";

		Object o = KnowWEUtils.getStoredObject(sec,
				QuestionCellHandler.KEY_REPORT);
		if (o != null && o instanceof Message) {
			title = ((Message) o).getMessageText();
		}

		String sectionID = sec.getID();
		StringBuilder html = new StringBuilder();
		html.append("<td title='" + title
				+ "' style='background-color:#EEEEEE;'>   ");
		generateContent(sectionText, sec, user, sectionID, html);
		if (c != null) html.append("col: " + ((SolutionColumnContext) c).getSolution());
		html.append("</td>");
		return KnowWEEnvironment.maskHTML(html.toString());
	}

	private static QuestionCellRenderer instance = null;

	public static QuestionCellRenderer getInstance() {
		if (instance == null) {
			instance = new QuestionCellRenderer();
		}
		return instance;
	}

}

class QuestionCellHandler extends D3webSubtreeHandler {

	public static final String KEY_REPORT = "report_message";

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
		KnowledgeBaseManagement mgn = getKBM(article);

		if (mgn == null) {
			return null;
		}
		SingleKBMIDObjectManager mgr = new SingleKBMIDObjectManager(mgn);
		Section<Table> tableContentSection = s.findAncestorOfType(Table.class);
		Section questionnaireSection = (Section) KnowWEUtils.getStoredObject(s
				.getArticle().getWeb(), s.getTitle(), tableContentSection
				.getID(),
				CoveringTableHeaderColumnCellContent.QUESTIONNAIRE_CELL);

		QContainer parent = null;

		if (questionnaireSection != null) {
			parent = mgr.findQContainer(QuestionnaireCellContent.trimQContainerDeclarationSyntax(questionnaireSection.getOriginalText().trim()));
		}

		String text = s.getOriginalText();

		String type = KnowledgeUtils.getQuestionTypeFromDeclaration(text);
		String name = KnowledgeUtils.getQuestionNameFromDeclaration(text);

		Question q = mgr.findQuestion(name);

		if (q == null) {
			// create question
			Question q2 = null;
			if (parent == null) {
				q2 = D3webQuestionFactory.createQuestion(name, null, type, mgr);
			}
			else {
				q2 = D3webQuestionFactory.createQuestion(mgr, parent, name, null, type);
			}

			if (q2 != null) {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"Created question " + type + " : " + name));
			}
			else {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"Failed creating question " + type + " : " + name));
			}

		}
		else {
			// TODO: CHECK Type match!
			KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
					"Question already defined: " + name));

		}

		return null;

	}
}