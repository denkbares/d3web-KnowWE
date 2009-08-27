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
