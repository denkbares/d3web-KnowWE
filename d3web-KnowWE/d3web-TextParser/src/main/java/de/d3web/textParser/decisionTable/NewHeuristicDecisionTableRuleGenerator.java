package de.d3web.textParser.decisionTable;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondNot;
import de.d3web.report.Report;

public class NewHeuristicDecisionTableRuleGenerator extends DecisionTableRuleGenerator {

	private NewDecisionTableParserManagement man;
	 ResourceBundle rb = ResourceBundle
		.getBundle("properties.DecisionTableMessages");
	
	public NewHeuristicDecisionTableRuleGenerator(KnowledgeBaseManagement kbm, boolean update) {
		super(kbm, update);
		// TODO Auto-generated constructor stub
	}
	
	public void setParserManagement(NewDecisionTableParserManagement man) {
		this.man = man;
	}

	@Override
	public Report generateKnowledge(DecisionTable table) {
		report = new Report();
		
		
    	int ruleCount = 0;
		
        int firstDataRow = man.getDataStartLine();
        int firstDataColumn = 1;
        
        // each column represents one rule
        for (int j=firstDataColumn; j<table.columns(); j++) {
            String diagnosisText = table.get(0, j);
        	String columnScore = getColumnScore(table,j);
            String columnOperator = getColumnOperator(table,j);
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
	
	private String getColumnScore(DecisionTable table,int j) {
		int scoreLine = man.getScoreLine();
		if(scoreLine == -1) return rb.getString("default.value.score");
		
		String cellData =  table.get(scoreLine, j);
		if(cellData.equals("")) {
			return rb.getString("default.value.score");
		}
		return cellData;
	}
	
	private String getColumnOperator(DecisionTable table, int j) {
		int conLine = man.getConjunctorLine();
		if(conLine == -1) return rb.getString("default.value.operator");
		
		return table.get(conLine,j);
	}

	@Override
	protected void createConditions(DecisionTable table, int i, int j, Map<String, List<AbstractCondition>> condParts, List<AbstractCondition> condList) {
		String answer = table.get(i,j);
		if (!answer.equals("")) {
			boolean negated = false;
			if(!NewHeuristicDecisionTableValueChecker.cutNegation(answer).equals(answer)) {
				answer = NewHeuristicDecisionTableValueChecker.cutNegation(answer);
				negated = true;          
			}
			
		    String questionText = table.getQuestionText(i);
		    
		    AbstractCondition nextCondition = createCondition(questionText, answer);
			
		    if (nextCondition==null)
		    	return;
		    
		    if(negated) {
		    	condList.add(new CondNot(nextCondition));
		    }else {
		    	condList.add(nextCondition);
		    }
		}
	}

}
