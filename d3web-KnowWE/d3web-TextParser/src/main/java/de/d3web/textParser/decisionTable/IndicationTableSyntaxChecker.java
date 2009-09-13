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

public class IndicationTableSyntaxChecker extends DecisionTableSyntaxChecker {

	public IndicationTableSyntaxChecker() {
		super();
	}

	public Report checkSyntax(DecisionTable table) {
		Report report = new Report();
		
		report.addAll(checkTableDimension(table, 2, 2));
		report.addAll(checkTableSpecificProperties(table));
		
		return report;
	}

	@Override
	protected Report checkTableSpecificProperties(DecisionTable table) {
		Report report = new Report();
		// cell A1 has to be empty
		if (!table.isEmptyCell(0,0))
			report.error(MessageGenerator.fieldNotEmpty(0,0,table.get(0,0)));
		// cell A2 has to be empty
		if (!table.isEmptyCell(1,0))
			report.error(MessageGenerator.fieldNotEmpty(1,0,table.get(1,0)));
		// line 1 and 2 must contain a value in each column except column 0
		for (int j=1; j<table.columns(); j++) {
			if (table.isEmptyCell(0,j))
				report.error(MessageGenerator.missingDiagnosis(0,j));
			if (table.isEmptyCell(1,j))
				report.error(MessageGenerator.missingDiagnosisState(1,j));
		}
		// each line must contain a value in column A if there's data in this line
		// (except line 1 and 2)
		for (int i=2; i<table.rows(); i++) {
			if (table.isEmptyCell(i,0) && !table.isEmptyRow(i))
				report.error(MessageGenerator.fieldEmpty(i,0));
		}
		
		return report;
	}

	@Override
	protected Report checkConfigSettings(DecisionTable table) {
		return new Report();
	}
}
