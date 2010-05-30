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
import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableColumnHeaderCellContent;
import de.d3web.we.kdom.table.TableLine;

/**
 * @author jochen
 * 
 * A type for a line having a question in the header column. (These lines dont have
 * xclrelation-entry (cause only the answer-lines have))
 *
 */
public class QuestionLine extends TableLine {
	
	private static QuestionLine instance; 
	
	public static QuestionLine getInstance() {
		if(instance == null) instance = new QuestionLine();
		return instance;
	}
	
	
	@Override
	public void init() {
		this.addSubtreeHandler(new QuestionLineHandler());
	}

	
	class QuestionLineHandler extends SubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			
			Section<TableColumnHeaderCellContent> headerCell = s.findSuccessor(TableColumnHeaderCellContent.class);
			headerCell.setType(QuestionCell.getInstance());
			
			List<Section<TableCellContent>> cells = new ArrayList<Section<TableCellContent>>();
			s.findSuccessorsOfType(TableCellContent.class, cells);
			for (Section<TableCellContent> section : cells) {
				section.setType(QuestionLineCell.getInstance());
			}
			
			return null;
		}
		
	}
	
}
