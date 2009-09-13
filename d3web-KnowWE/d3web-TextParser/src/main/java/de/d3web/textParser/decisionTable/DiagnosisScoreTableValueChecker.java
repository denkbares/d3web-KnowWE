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
