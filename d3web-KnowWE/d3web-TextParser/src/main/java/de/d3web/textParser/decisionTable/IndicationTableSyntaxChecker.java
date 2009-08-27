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
