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

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.table.TableCellContentRenderer;
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
