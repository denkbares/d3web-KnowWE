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

package de.d3web.textParser.decisionTable;

import de.d3web.report.Report;

public class NewHeuristicDecisionTableSyntaxChecker extends DecisionTableSyntaxChecker {

	private NewDecisionTableParserManagement man;
	
	
	public void setParserManagement(NewDecisionTableParserManagement man) {
		this.man = man;
	}
	@Override
	protected Report checkConfigSettings(DecisionTable table) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Report checkTableSpecificProperties(DecisionTable table) {
		Report report = new Report();
		// Zeile 0: Spalte 0 leer;
		if (!table.isEmptyCell(0,0)) {
		    report.error(MessageGenerator.fieldNotEmpty(0,0,table.get(0,0)));
		}
		int rows = table.getTableData().length;
		int cols = table.getTableData()[0].length;
		for(int i = man.getDataStartLine(); i < rows; i++ ) {
			if(table.isEmptyCell(i, 0)) {
				report.error(MessageGenerator.missingObjectName(i, 0));
			}
		}
		for(int j = 1; j < cols; j++) {
			if(table.isEmptyCell(0,j)) {
				report.error(MessageGenerator.missingDiagnosis(0, j));
			}
		}
		return report;
	}


	

	public Report checkSyntax(DecisionTable table) {
		 Report report = new Report();
		 

	        report.addAll(checkTableDimension(table, 2, 2));
	        
	        if (report.isEmpty()) {
	            report.addAll(checkTableSpecificProperties(table));
	        }
	        return report;
	}

}
