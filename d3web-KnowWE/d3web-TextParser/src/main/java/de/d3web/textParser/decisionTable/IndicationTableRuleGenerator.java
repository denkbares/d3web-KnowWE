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
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondDState;
import de.d3web.kernel.psMethods.MethodKind;
import de.d3web.kernel.psMethods.questionSetter.PSMethodQuestionSetter;
import de.d3web.report.Report;

public class IndicationTableRuleGenerator extends KnowledgeGenerator {

	public IndicationTableRuleGenerator(KnowledgeBaseManagement kbm) {
		super(kbm);
	}

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.KnowledgeGenerator#generateKnowledge(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	@Override
	public Report generateKnowledge(DecisionTable table) {
		report = new Report();
		int ruleCount = 0;
		
        for (int j=1; j<table.columns(); j++) {
        	Diagnosis theDiagnosis = kbm.findDiagnosis(table.get(0,j));
        	DiagnosisState dState = getDiagnosisState(table.get(1,j));
        	SortedMap<Double,QASet> qaSets = readQASets(table, j);
        	
        	AbstractCondition theCondition = null;
        	if (dState!=null)
        		theCondition = new CondDState(theDiagnosis, dState, null);
        	
        	if (theDiagnosis!=null && theCondition!=null && !qaSets.isEmpty()) {
        		String newRuleID = kbm.findNewIDFor(new RuleComplex());
        		ArrayList<QASet> action = new ArrayList<QASet>(qaSets.values());
            	RuleComplex newRule = RuleFactory.createIndicationRule(newRuleID,
            			action, theCondition);
            	if (newRule!=null)
            		ruleCount++;
            }
        }

        report.note(MessageGenerator.addedRules(
				table.getSheetName()+":"+table.getTableNumber(),
				ruleCount));

    	return report;
    }

    private DiagnosisState getDiagnosisState(String text) {
    	text = text.trim();
    	if (text.equalsIgnoreCase("suggested") || text.equalsIgnoreCase("s") ||
    		text.equalsIgnoreCase("verdï¿½chtig") || text.equalsIgnoreCase("v"))
    		return DiagnosisState.SUGGESTED;
    	if (text.equalsIgnoreCase("established") || text.equalsIgnoreCase("e") ||
    		text.equalsIgnoreCase("bestï¿½tigt") || text.equalsIgnoreCase("b"))
    		return DiagnosisState.ESTABLISHED;
    	if (text.equalsIgnoreCase("excluded") || text.equalsIgnoreCase("x") ||
    		text.equalsIgnoreCase("ausgeschlossen") || text.equalsIgnoreCase("a"))
    		return DiagnosisState.EXCLUDED;
    	if (text.equalsIgnoreCase("unclear") || text.equalsIgnoreCase("u") ||
    		text.equalsIgnoreCase("unklar"))
    		return DiagnosisState.UNCLEAR;
    	
    	return null;
    }
    
    private SortedMap<Double,QASet> readQASets(DecisionTable table, int column) {
    	SortedMap<Double,QASet> map = new TreeMap<Double,QASet>();
    	for (int i=2; i<table.rows(); i++) {
    		if (!table.isEmptyCell(i,column)) {
    			Double ix = null;
    			try {
    				ix = Double.parseDouble(table.get(i,column));
    			}
    			catch (NumberFormatException e) {}
    			
    			String qasetText = table.get(i,0);
    			QASet q = kbm.findQContainer(qasetText);
    			if (q==null)
    				q = kbm.findQuestion(qasetText);
    			
    			if (ix!=null && q!=null) {
    				map.put(ix, q);
    			}
    		}
    	}
    	return map;
    }
    
	/**
	 * TODO: use to detect existing rules with same condition
	 * Gets all rules which contain this diagnosis in the condition part
	 * @param theDiagnosis the diagnosis
	 * @return a list of all rules
	 */
	private List<RuleComplex> getForwardRulesForDiagnosis(Diagnosis theDiagnosis) {
		List knowledgeSlices = theDiagnosis.getKnowledge(PSMethodQuestionSetter.class, MethodKind.FORWARD);
		if (knowledgeSlices==null)
			return new ArrayList<RuleComplex>();
		else {
			List<RuleComplex> rules = new ArrayList<RuleComplex>();
			for (Iterator it = knowledgeSlices.iterator(); it.hasNext(); ) {
				Object next = it.next();
				if (next instanceof RuleComplex)
					rules.add((RuleComplex)next);
			}
			return rules;
		}
	}
}
