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

/* Created on 25. Januar 2005, 19:44 */
package de.d3web.textParser.decisionTable;

import de.d3web.report.Report;

/**
 * Implementierung des Interface SyntaxChecker zur ï¿½berprï¿½fung
 * der Syntax einer Regel-Tabelle mit einfachen Regeln
 * @author  Andreas Klar
 */
public class SymptomAbstractionTableSyntaxChecker extends DecisionTableSyntaxChecker {
    
    
    /* (non-Javadoc)
     * @see de.d3web.textParser.decisionTable.SyntaxChecker#checkSyntax(de.d3web.textParser.decisionTable.DecisionTable)
     */
    public Report checkSyntax(DecisionTable table) {
        Report report = new Report();
        
        report.addAll(checkTableDimension(table, 3, 3));

        if (report.isEmpty()) {
        	report.addAll(checkTableSpecificProperties(table));
        }
        
        return report;
    }

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.DecisionTableSyntaxChecker#checkTableSpecificProperties(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	@Override
	protected Report checkTableSpecificProperties(DecisionTable table) {
		Report report = new Report();
		// Zeile 1: Spalte 1 u, 2 leer, ab Spalte 3 muss in jeder Spalte ein Wert vorkommen
		if (!table.get(0,0).equals(""))
		    report.error(MessageGenerator.fieldNotEmpty(0,0,table.get(0,0)));
		if (!table.get(0,1).equals(""))
		    report.error(MessageGenerator.fieldNotEmpty(0,1,table.get(0,1)));
		for (int j=2; j<table.columns(); j++) {
		    if (table.get(0,j).equals(""))
		        report.error(MessageGenerator.fieldEmpty(0,j));
		}
		for (int i=1; i<table.rows(); i++) {
		    if (!table.get(i,0).equals("")) {
		// Wenn Symptom in Spalte 1, dann mï¿½ssen alle anderen Spalten leer sein
		        for (int j=1; j<table.columns(); j++) {
		            if (!table.get(i,j).equals(""))
		                report.error(MessageGenerator.fieldNotEmpty(i,j,table.get(i,j)));
		        }
		// Fï¿½r jedes Symptom muss mindestens eine Ausprï¿½gung vorhanden sein
		        if ((i+1)>=table.rows() || (!table.get(i+1,0).equals("") && table.get(i+1,1).equals("")))
		            report.error(MessageGenerator.missingAnswerForQuestion(i,0,table.get(i,0)));
		    }
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
}
