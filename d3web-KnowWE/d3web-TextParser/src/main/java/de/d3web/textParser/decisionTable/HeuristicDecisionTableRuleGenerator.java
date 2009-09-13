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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.report.Report;

/**
 * Implementierung des Interface RuleMaker zur Generierung von Regeln,
 * die aus einer Regel-Tabelle mit komplexen Regeln gelesen werden kï¿½nnen
 * @author  Andreas Klar
 */
public class HeuristicDecisionTableRuleGenerator extends DecisionTableRuleGenerator {
   
    public HeuristicDecisionTableRuleGenerator(KnowledgeBaseManagement kbm, boolean update) {
        super(kbm, update);
    }
    
    /* (non-Javadoc)
     * @see de.d3web.textParser.decisionTable.RuleGenerator#generateRules(de.d3web.textParser.decisionTable.DecisionTable)
     */
    @Override
	public Report generateKnowledge(DecisionTable table) {
		report = new Report();
    	int ruleCount = 0;
		
        int firstDataRow = 4;
        int firstDataColumn = 2;
        
        // each column represents one rule
        for (int j=firstDataColumn; j<table.columns(); j++) {
            String diagnosisText = table.get(0, j);
        	String columnScore = table.get(1,j);
            String columnOperator = table.get(2,j);
            // Hier werden Bedingungen gespeichert, die aus mehreren Klauseln bestehen
            // (also mit u... oder o... verknï¿½pft sind)
            Map<String, List<AbstractCondition>> conditionParts = new Hashtable<String, List<AbstractCondition>>();
            // Hier werden Bedingungen gespeichert, die spï¿½ter mit der
            // Spalten-Verknï¿½pfung zusammengefï¿½gt werden
            List<AbstractCondition> conditionList = new ArrayList<AbstractCondition>(1);

            // Einzelne Klauseln aus einer Spalte einlesen
            for (int i=firstDataRow; i<table.rows(); i++) {
                createConditions(table, i, j, conditionParts, conditionList);
            }
            // combine all conditions with row association and store them in "conditionList"
            conditionList.addAll(combineConditionParts(conditionParts));
            if (!conditionList.isEmpty()) {
            	RuleComplex newRule = generateRule(conditionList, columnOperator, diagnosisText, columnScore); 
            	if (newRule!=null) 
            		ruleCount++;
            }
        }
        
        report.note(MessageGenerator.addedRules(
        		table.getSheetName() + ":" + table.getTableNumber(),
        		ruleCount));

    	return report;
    }
}
