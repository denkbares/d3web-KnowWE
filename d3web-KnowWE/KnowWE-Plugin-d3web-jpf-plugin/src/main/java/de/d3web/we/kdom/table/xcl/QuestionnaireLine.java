/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.table.TableColumnHeaderCellContent;
import de.d3web.we.kdom.table.TableLine;

public class QuestionnaireLine extends TableLine {

	private static QuestionnaireLine instance = null;

	public static QuestionnaireLine getInstance() {
		if (instance == null) {
			instance = new QuestionnaireLine();

		}

		return instance;
	}

	private QuestionnaireLine() {
		this.addSubtreeHandler(new QuestionnaireLineHandler());
	}

	static class QuestionnaireLineHandler extends SubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			Section<TableColumnHeaderCellContent> headerCellContent = s.findSuccessor(TableColumnHeaderCellContent.class);
			headerCellContent.setType(QuestionnaireCellContent.getInstance());

			return null;
		}

	}

}
