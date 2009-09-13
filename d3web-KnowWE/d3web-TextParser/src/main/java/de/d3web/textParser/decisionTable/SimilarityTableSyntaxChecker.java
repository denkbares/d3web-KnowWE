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

/**
 * 
 */
package de.d3web.textParser.decisionTable;

import de.d3web.report.Report;

/**
 * Creation date: (31.10.2005)
 * @author Andreas
 *
 */
public class SimilarityTableSyntaxChecker extends DecisionTableSyntaxChecker {

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.DecisionTableSyntaxChecker#checkTableSpecificProperties(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	@Override
	protected Report checkTableSpecificProperties(DecisionTable table) {
		Report report = new Report();
		// question comparator must be present for each question
		for (int i=0; i<table.rows(); i++) {
			if (!table.isEmptyCell(i,0) && table.isEmptyCell(0,1))
				report.error(MessageGenerator.missingQuestionComparator(i,1));
		}
		return report;
	}

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.DecisionTableSyntaxChecker#checkConfigSettings(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	@Override
	protected Report checkConfigSettings(DecisionTable table) {
		return new Report();
	}

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.SyntaxChecker#checkSyntax(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	public Report checkSyntax(DecisionTable table) {
		Report report = new Report();
		
		report.addAll(checkTableDimension(table, 2, 2));
        
        if (report.isEmpty()) {
        	report.addAll(checkTableSpecificProperties(table));
        }
        
        return report;
	}

}
