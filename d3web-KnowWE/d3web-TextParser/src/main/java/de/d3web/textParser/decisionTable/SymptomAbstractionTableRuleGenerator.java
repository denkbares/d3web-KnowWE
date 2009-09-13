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

/* Created on 26. Januar 2005, 13:08 */
package de.d3web.textParser.decisionTable;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.report.Report;


/**
 * Implementierung des Interface RuleGenerator zur Generierung von Regeln,
 * die aus einer Regel-Tabelle mit komplexen Regeln gelesen werden kï¿½nnen
 * @author  Andreas Klar
 */
public class SymptomAbstractionTableRuleGenerator extends DecisionTableRuleGenerator {
    
	public SymptomAbstractionTableRuleGenerator(KnowledgeBaseManagement kbm, boolean update) {
    	super(kbm, update);
    }
    
	@Override
	public Report generateKnowledge(DecisionTable table) {
		report = new Report();
		int ruleCount = 0;
		
        int firstDataRow = 2;
        int firstDataColumn = 2;
    
        for (int i=firstDataRow; i<table.rows(); i++) {
            for (int j=firstDataColumn; j<table.columns(); j++) {
            	String valueAnswerText = table.get(i,j);
            	if (!valueAnswerText.equals("")) {
                	String questionText = table.getQuestionText(i);
                	String answerText = table.get(i,1);
                	String valueQuestionText = table.get(0,j);
                	
                	AbstractCondition theCondition = createCondition(questionText, answerText);
                	
                	Question valueQuestion = kbm.findQuestion(valueQuestionText);
                	AnswerChoice valueAnswer = null;
                	if (valueQuestion instanceof QuestionChoice)
                		valueAnswer = kbm.findAnswerChoice((QuestionChoice)valueQuestion, valueAnswerText);
                	
                	if (theCondition!=null && valueQuestion!=null && valueAnswer!=null) {
                		String newRuleID = kbm.findNewIDFor(new RuleComplex());
            			RuleComplex newRule = RuleFactory.createSetValueRule(newRuleID,
            					valueQuestion, new Object[]{valueAnswer}, theCondition);
                		if (newRule!=null)
                			ruleCount++;
                	}
                }
            }
        }

        report.note(MessageGenerator.addedRules(
				table.getSheetName()+":"+table.getTableNumber(),
				ruleCount));

    	return report;
    }
	
	
}

