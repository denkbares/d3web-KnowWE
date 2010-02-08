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

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.table.TableCell;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableContent;
import de.d3web.we.kdom.table.TableHeaderLine;
import de.d3web.we.kdom.table.TableUtils;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

/**
 * @author jochen
 * 
 * Type for the coveringTable-solutionHeaderLine. The first Line contains solutions.
 *
 */
public class SolutionTableHeaderLine extends TableHeaderLine {

	@Override
	protected void init() {
		this.addReviseSubtreeHandler(new SolutionHeaderLineSubtreeHandler());

	}
	
	class SolutionHeaderLineSubtreeHandler implements ReviseSubTreeHandler {

		/* creates the solution contexts
		 * allowing the EntryCells (@see EntryCellContent) to
		 * easily access their column context. (@see SolutionColumnContext)
		 * 
		 */
		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {


			List<Section<TableCell>> cells = new ArrayList<Section<TableCell>>();
			s.findSuccessorsOfType(TableCell.class, cells);

			Section<TableContent> tableContent = KnowWEObjectTypeUtils.getAncestorOfType(s,
					TableContent.class);

			for (Section<TableCell> section : cells) {
				Section<TableCellContent> findChildOfType = section.findSuccessor(TableCellContent.class);
				if(findChildOfType == null) continue;
				findChildOfType.setType(SolutionCellContent.getInstance());
				String solution = null;
				if (findChildOfType != null) {
					solution = findChildOfType.getOriginalText();
					solution = solution.replaceAll("__", "").trim();
					if (solution.trim().length() == 0)
						solution = null;
				}
				int col = TableUtils.getColumn(section.findSuccessor(TableCellContent.class));
				if (solution != null) {
					ContextManager.getInstance().attachContextForClass(
							tableContent,
							new SolutionColumnContext(tableContent, col, solution));
				}
			}
			return null;
		}

	}



}
