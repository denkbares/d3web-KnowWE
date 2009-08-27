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
