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

