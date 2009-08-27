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
