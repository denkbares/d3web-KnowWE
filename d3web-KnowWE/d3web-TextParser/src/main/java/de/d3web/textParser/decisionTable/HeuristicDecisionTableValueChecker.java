/* Created on 25. Januar 2005, 21:11 */
package de.d3web.textParser.decisionTable;

import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Report;

/**
 * Implementierung des Interface ValueChecker zur ï¿½berprï¿½fung
 * der Werte einer Regel-Tabelle mit komplexen Regeln
 * @author Andreas Klar
 */
public class HeuristicDecisionTableValueChecker extends DecisionTableValueChecker {
    
    /**
     * Erstellt eine neue Instanz von Value-Checker fï¿½r einfache Regel-Tabellen,
     * welche das ï¿½bergebene KnowledgeBaseManagement und den Config-Reader zur
     * Werte-ï¿½berprï¿½fung benutzt.
     * @param configReader Config-Reader, der zur ï¿½berprï¿½fung der Werte benutzt werden soll,
     * welche in der Config-Datei festgelegt sind
     * @param kbm KnowledgeBaseManagement zur ï¿½berprï¿½fung der NamedObjects
     */
    public HeuristicDecisionTableValueChecker(DecisionTableConfigReader cReader, KnowledgeBaseManagement kbm) {
        super(cReader, kbm);
    }
    

    /* (non-Javadoc)
     * @see de.d3web.textParser.decisionTable.ValueChecker#checkValues(de.d3web.textParser.decisionTable.DecisionTable)
     */
    @Override
	public Report checkValues(DecisionTable table) {
        Report report = new Report();
        ResourceBundle rb = ResourceBundle
		.getBundle("properties.DecisionTableMessages");
        report.addAll(checkScores(table, 1, 2, 2, table.columns(),rb.getString("default.value.score")));
        
        report.addAll(checkLogicalOperatorsColumn(table, 2, table.columns(),2,rb.getString("default.value.operator")));
        report.addAll(checkLogicalOperatorsRow(table, 4, table.rows(), 2, table.columns(),null));
        report.addAll(checkSigns(table, 4, table.rows(), 2, table.columns()));
        
        report.addAll(checkQuestions(table, 3, table.rows()));
        report.addAll(checkAnswers(table, 4, table.rows()));
        report.addAll(checkDiagnoses(table, 0, table.columns()));
        
        return report;
    }
    

    
    private Report checkLogicalOperatorsRow(DecisionTable table,
    									int startRowIx, int endRowIx,
										int startColumnIx, int endColumnIx, String defaultString) {
    	Report report = new Report();
 
		
		String op1 = DecisionTableConfigReader.LOGICAL_OPERATOR_ROW_AND;
        String op2 = DecisionTableConfigReader.LOGICAL_OPERATOR_ROW_OR;
        for (int i=startRowIx; i<endRowIx; i++) {
            for (int j=startColumnIx; j<endColumnIx; j++) {
//            	bei nicht vorhandenem Operator wird default wert gesetzt
        		if(table.isEmptyCell(i,j) && defaultString != null) {
        			table.getTableData()[i][j] = defaultString;
        			report.note(MessageGenerator.usingDefault(i,j, " "+defaultString));
        		}
                if (!table.isEmptyCell(i,1) && !table.isEmptyCell(i,j)) {
                    if (table.get(i,j).length()>2 && table.get(i,j).indexOf(" ")!=-1) {
                        String op = table.get(i,j).substring(table.get(i,j).indexOf(" ")+1);
                        if (!op.startsWith(op1) && !op.startsWith(op2))
                            report.error(MessageGenerator.invalidLogicalOperator(i,j,op));
                    }
                }
            }
        }
        return report;
    }
    
    private Report checkSigns(DecisionTable table,
    							int startRowIx, int endRowIx,
								int startColumnIx, int endColumnIx) {
		Report report = new Report();
       
		String pos = DecisionTableConfigReader.SIGN_POSITIVE;
        String neg = DecisionTableConfigReader.SIGN_NEGATIVE;
        for (int i=startRowIx; i<endRowIx; i++) {
            for (int j=startColumnIx; j<endColumnIx; j++) {
                if (!table.isEmptyCell(i,1) && !table.isEmptyCell(i,j)) {
                    if ((!table.get(i,j).startsWith(pos) && !table.get(i,j).startsWith(neg))
                        || (table.get(i,j).length()>1 && (table.get(i,j).indexOf(" ")!=1))) {
                        String sign = new String();
                        if (table.get(i,j).indexOf(" ")==-1)
                            sign = table.get(i,j);
                        else
                            sign = table.get(i,j).substring(0, table.get(i,j).indexOf(" "));
                        report.error(MessageGenerator.invalidSign(i,j,sign));
                    }
                }
            }
        }
        return report;
    }
}

