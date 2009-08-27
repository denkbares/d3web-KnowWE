/* Created on 25. Januar 2005, 21:07 */
package de.d3web.textParser.decisionTable;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Report;

/**
 * Implementierung des Interface ValueChecker zur ï¿½berprï¿½fung
 * der Werte einer Regel-Tabelle mit einfachen Regeln
 * @author  Andreas Klar
 */
public class DiagnosisScoreTableValueChecker extends DecisionTableValueChecker {
    
    /**
     * Erstellt eine neue Instanz von Value-Checker fï¿½r einfache Regel-Tabellen,
     * welche das ï¿½bergebene KnowledgeBaseManagement und den Config-Reader 
     * zur Werte-ï¿½berprï¿½fung benutzt.
     * @param cReader Config-Reader, der zur ï¿½berprï¿½fung der Werte benutzt werden soll,
     * welche in der Config-Datei festgelegt sind
     * @param kbm KnowledgeBaseManagement, dessen Wissensbasis zur ï¿½berprï¿½fung der Werte
     * benutzt werden soll
     */
    public DiagnosisScoreTableValueChecker(DecisionTableConfigReader cReader, KnowledgeBaseManagement kbm) {
        super(cReader, kbm);
    }

    /* (non-Javadoc)
     * @see de.d3web.textParser.decisionTable.ValueChecker#checkValues(de.d3web.textParser.decisionTable.DecisionTable)
     */
    @Override
	public Report checkValues(DecisionTable table) {
        Report report = new Report();
       
        report.addAll(checkScores(table, 2, table.rows(), 2, table.columns(),null));
        report.addAll(checkQuestions(table, 1, table.rows()));
        report.addAll(checkAnswers(table, 2, table.rows()));
        report.addAll(checkDiagnoses(table, 2, table.columns()));
        
        return report;
    }
}
