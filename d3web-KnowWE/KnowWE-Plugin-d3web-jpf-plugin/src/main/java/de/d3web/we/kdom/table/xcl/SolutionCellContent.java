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

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.knofficeparser2.SingleKBMIDObjectManager;
import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableCellContentRenderer;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author jochen
 * 
 *         A cell containing a solution. These cells are in the headerLine (@see
 *         SolutionTableHeaderLine)
 * 
 */
public class SolutionCellContent extends TableCellContent {

	private static SolutionCellContent instance;

	public static SolutionCellContent getInstance() {
		if (instance == null) instance = new SolutionCellContent();
		return instance;
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return QuestionCellContentRenderer.getInstance();
	}

	@Override
	public void init() {
		this.addSubtreeHandler(new SolutionCellHandler());
	}

}

class QuestionCellContentRenderer extends TableCellContentRenderer {

	/**
	 * Wraps the content of the cell (sectionText) with the HTML-Code needed for
	 * the table
	 */
	@Override
	protected String wrappContent(String sectionText, Section sec,
			KnowWEUserContext user) {

		String title = "";
		Object o = KnowWEUtils.getStoredObject(sec, QuestionCellHandler.KEY_REPORT);
		if (o != null && o instanceof Message) {
			title = ((Message) o).getMessageText();
		}

		String sectionID = sec.getID();
		StringBuilder html = new StringBuilder();
		html.append("<td title='" + title + "' style='background-color:#96BBD9;'>   ");
		generateContent(sectionText, sec, user, sectionID, html);
		html.append("</td>");
		return KnowWEEnvironment.maskHTML(html.toString());
	}

	private static QuestionCellContentRenderer instance = null;

	public static QuestionCellContentRenderer getInstance() {
		if (instance == null) instance = new QuestionCellContentRenderer();
		return instance;
	}

}

class SolutionCellHandler extends D3webSubtreeHandler {

	public static final String KEY_REPORT = "report_message";

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
		KnowledgeBaseManagement mgn = getKBM(article);

		if (mgn == null) {
			return null;
		}

		SingleKBMIDObjectManager mgr = new SingleKBMIDObjectManager(mgn);

		String name = s.getOriginalText();
		name = name.replaceAll("__", "").trim();

		Solution d = mgr.findSolution(name);

		if (d == null) {
			Solution newD = mgr.createSolution(name, mgr.getKnowledgeBase()
					.getRootSolution());
			if (newD != null) {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"Created solution : " + name));
			}
			else {
				KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
						"Failed creating solution : " + name));
			}
		}
		else {
			KnowWEUtils.storeSectionInfo(s, KEY_REPORT, new Message(
					"Solution already defined: " + name));
		}

		return null;

	}

}