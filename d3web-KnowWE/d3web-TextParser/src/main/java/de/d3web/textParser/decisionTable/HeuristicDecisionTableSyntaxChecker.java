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

/* Created on 25. Januar 2005, 19:45 */
package de.d3web.textParser.decisionTable;

import de.d3web.report.Report;

/**
 * Implementierung des Interface SyntaxChecker zur ï¿½berprï¿½fung
 * der Syntax einer Regel-Tabelle mit komplexen Regeln
 * @author  Andreas Klar
 */
public class HeuristicDecisionTableSyntaxChecker extends DecisionTableSyntaxChecker {
    
    private DecisionTableConfigReader cReader;
    
    /**
     * Erstellt eine neue Instanz von Syntax-Checker fï¿½r komplexe Regel-Tabellen.
     * Mit dem ï¿½bergebenen ConfigReader wird ï¿½berprï¿½ft, ob in der ConfigDatei auch
     * alle nï¿½tigen Eintragungen fï¿½r diesen Tabellen-Typ vorhanden sind.
     * @param configReader ConfigReader, der zur ï¿½berprï¿½fung der Werte benutzt werden soll
     */
    public HeuristicDecisionTableSyntaxChecker(DecisionTableConfigReader configReader) {
        this.cReader = configReader;
    }
 
    /* (non-Javadoc)
     * @see de.d3web.textParser.decisionTable.SyntaxChecker#checkSyntax(de.d3web.textParser.decisionTable.DecisionTable)
     */
    public Report checkSyntax(DecisionTable table) {
        Report report = new Report();

        report.addAll(checkTableDimension(table, 5, 3));
        
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
		// Zeile 1: Spalte 2 leer;
		if (!table.isEmptyCell(0,1)) {
		    report.error(MessageGenerator.fieldNotEmpty(0,1,table.get(0,1)));
		}
		// Zeile 1:
		// ENTWEDER Spalte 1 leer, jedoch in allen ab Spalte 3 kommt Wert vor
		// ODER nur in Spalte 1 Wert, alle anderen jedoch leer
		boolean isEmpty = false;
		if (table.isEmptyCell(0,0))
		    isEmpty = true;
		for (int j=2; j<table.columns(); j++) {
		    if ((!table.isEmptyCell(0,j) && !isEmpty) ||
		        (table.isEmptyCell(0,j) && isEmpty)) {
		        report.error(MessageGenerator.missingRuleDiagnoses());
		        break;
		    }
  		// Wenn in einer Spalte keine Kondition vorhanden ist wird Warnung ausgegeben
		    boolean hasCond = false;
		    for (int i=4; i<table.rows() && !hasCond; i++) {
		    	if (!table.isEmptyCell(i,j))
		    		hasCond=true;
		    }
		    if (!hasCond)
		    	report.warning(MessageGenerator.missingCondition(0,j));
		}
		// Spalten 1 und 2 in Zeile 2 und 3 mï¿½ssen leer sein
		for (int i=1; i<3; i++) {
		    for (int j=0; j<2; j++) {
		        if (!table.isEmptyCell(i,j))
		            report.error(MessageGenerator.fieldNotEmpty(i,j,table.get(i,j)));
		    }
		}
		for (int i=3; i<table.rows(); i++) {
		    if (!table.isEmptyCell(i,0)) {
		// Wenn Symptom in Spalte 1, dann mï¿½ssen alle anderen Spalten leer sein
		        for (int j=1; j<table.columns(); j++) {
		            if (!table.isEmptyCell(i,j))
		                report.error(MessageGenerator.fieldNotEmpty(i,j,table.get(i,j)));
		        }
		// Fï¿½r jedes Symptom muss mindestens eine Ausprï¿½gung vorhanden sein
		        if ((i+1)>=table.rows() ||
		        	(!table.isEmptyCell(i+1,0) && table.isEmptyCell(i+1,1))) {
		            report.error(MessageGenerator.missingAnswerForQuestion(i,0,table.get(i,0)));
		        }
		    }
		}
		report.addAll(checkConfigSettings(table));
		return report;
	}

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.DecisionTableSyntaxChecker#checkConfigSettings(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	@Override
	protected Report checkConfigSettings(DecisionTable table) {
		Report report = new Report();
		// In Zeile 2 muss in jeder Spalte ab Spalte 3 eine Bewertung stehen
		for (int j=2; j<table.columns(); j++) {
		    if (table.isEmptyCell(1,j))
		        report.warning(MessageGenerator.missingScore(1,j));
		}
		// In Zeile 3 muss in jeder Spalte ab Spalte 3 eine Verknï¿½pfung stehen, es sei denn die
		// Kondition besteht nur aus einer einfachen TerminalCondition
		for (int j=2; j<table.columns(); j++) {
		    if (table.isEmptyCell(2,j)) {
		    	int condCount = 0;
		    	for (int i=4; i<table.rows() && condCount<2; i++) {
		    		if (!table.isEmptyCell(i,j))
		    			condCount++;
		    	}
		    	if (condCount>1)
		    		report.warning(MessageGenerator.missingLogicalOperatorColumn(2,j));
		    }
		}
		// ï¿½berprï¿½fung der Verknï¿½pfungen in den Werte-Zellen
		for (int i=4; i<table.rows(); i++) {
		    for (int j=2; j<table.columns(); j++) {
		        if (!table.isEmptyCell(i,1)) {
		            if (table.isEmptyCell(i,j) &&
		            	cReader.isNecessary(DecisionTableConfigReader.SIGN))
		                report.error(MessageGenerator.missingSign(i,j));
		            if (!table.isEmptyCell(i,j) && table.get(i,j).indexOf(" ")==-1 &&
		            	cReader.isNecessary(DecisionTableConfigReader.LOGICAL_OPERATOR_ROW))
		                report.error(MessageGenerator.missingLogicalOperator(i,j));
		        }
		    }
		}
		return report;
	}
    
}
