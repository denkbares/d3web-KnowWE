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
 * @author Andreas
 *
 */
public class ReplacementTableSyntaxChecker implements SyntaxChecker {

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.SyntaxChecker#checkSyntax(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	public Report checkSyntax(DecisionTable table) {
		Report report = new Report();
        
		report.addAll(checkTableSpecificProperties(table));

		return report;
	}

	private Report checkTableSpecificProperties(DecisionTable table) {
		Report report = new Report();
        
		for (int i=0; i<table.rows(); i++) {
			if (!table.isEmptyRow(i)) {
				if (table.isEmptyCell(i,0))
					report.error(MessageGenerator.missingObjectName(i,0));
				if (table.isEmptyCell(i,1))
					report.error(MessageGenerator.missingReplaceName(i,1));
				for (int j=3; j<table.columns(); j++) {
					if (!table.isEmptyCell(i,j))
						report.error(MessageGenerator.fieldNotEmpty(i,j,table.get(0,0)));
				}
			}
		}
		
		return report;
	}
}
