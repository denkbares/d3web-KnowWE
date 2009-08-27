/*
 * Created on 21.06.2005
 */
package de.d3web.textParser.decisionTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.Score;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondAnd;
import de.d3web.kernel.domainModel.ruleCondition.CondEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondMofN;
import de.d3web.kernel.domainModel.ruleCondition.CondNot;
import de.d3web.kernel.domainModel.ruleCondition.CondNum;
import de.d3web.kernel.domainModel.ruleCondition.CondNumEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreater;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreaterEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumIn;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLess;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLessEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondOr;
import de.d3web.kernel.domainModel.ruleCondition.CondUnknown;
import de.d3web.kernel.psMethods.MethodKind;
import de.d3web.kernel.psMethods.heuristic.PSMethodHeuristic;
import de.d3web.report.Report;
import de.d3web.textParser.Utils.ScoreFinder;

/**
 * @author Andreas Klar
 */
public abstract class DecisionTableRuleGenerator extends KnowledgeGenerator {

	/** List of all diagnoses that have already been erased and can be used to create new rules */
	private static List<Diagnosis> allowedDiagnoses = new ArrayList<Diagnosis>();
	/** List of all diagnoses that are used in knowledgeSlices and can not be used for update */ 
	private List<Diagnosis> forbiddenDiagnoses = new ArrayList<Diagnosis>();
	private List<Diagnosis> watchedDiagnoses = new ArrayList<Diagnosis>();
	public static List<RuleComplex> existingRules = new ArrayList<RuleComplex>();
	protected boolean update;
	
	public DecisionTableRuleGenerator(KnowledgeBaseManagement kbm, boolean update) {
		super(kbm);
		this.update = update;
	}

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.RuleGenerator#generateRules(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	@Override
	public abstract Report generateKnowledge(DecisionTable table);

	
	public RuleComplex createRule(AbstractCondition theCondition,
									Diagnosis theDiagnosis,
									Score theScore) {
		if (!watchedDiagnoses.contains(theDiagnosis)) {
			watchedDiagnoses.add(theDiagnosis);
			List<RuleComplex> rules = getBackwardRulesForDiagnosis(theDiagnosis);
			if (rules.size()==0)
				allowedDiagnoses.add(theDiagnosis);
			else
				existingRules.addAll(rules);
		}
		
		if (!update || allowedDiagnoses.contains(theDiagnosis)) {
			String newRuleID = kbm.findNewIDFor(new RuleComplex());
			return RuleFactory.createHeuristicPSRule(newRuleID, theDiagnosis, theScore, theCondition);
		}
		else {
			if (!forbiddenDiagnoses.contains(theDiagnosis)) {
				forbiddenDiagnoses.add(theDiagnosis);
				report.warning(MessageGenerator.diagnosisHasRules(theDiagnosis.getText()));
			}
			return null;
		}
	}
	
	public static void reset() {
		allowedDiagnoses = new ArrayList<Diagnosis>();
		existingRules = new ArrayList<RuleComplex>();
	}
	
	/**
	 * Gets all rules which contain this diagnosis in the action part
	 * @param theDiagnosis the diagnosis
	 * @return a list of all rules
	 */
	private List<RuleComplex> getBackwardRulesForDiagnosis(Diagnosis theDiagnosis) {
		List knowledgeSlices = theDiagnosis.getKnowledge(PSMethodHeuristic.class, MethodKind.BACKWARD);
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
	
	/**
	 * Removes all rules from the knowledgebase
	 * @param rules rules to be removed
	 */
	private int removeAllRules(List<RuleComplex> rules) {
		int removedCount = 0;
		for (Iterator<RuleComplex> it = rules.iterator(); it.hasNext(); )
			if (kbm.getKnowledgeBase().remove(it.next()))
				removedCount++;
		return removedCount;
	}
	
	/**
	 * Creates an AbstractCondition with the given parameters
	 * @param questionText 
	 * @param answerText
	 * @return
	 */
	protected AbstractCondition createCondition(String questionText, String answerText) {
		return createCondition(questionText, answerText, kbm);
	}
	
	public static AbstractCondition createCondition(String questionText, String answerText, KnowledgeBaseManagement kbm) {
		if (questionText==null || answerText == null) return null;
		AbstractCondition theCondition = null;
		Question theQuestion = kbm.findQuestion(questionText);
		
		// check if unknown-condition (for all types of questions)
    	if (answerText.toLowerCase().equals("unbekannt") ||
    		answerText.toLowerCase().equals("unknown")) {
    		theCondition = new CondUnknown(theQuestion);
    	}
    	// QuestionChoice (YN, OC, MC)
    	else if (theQuestion instanceof QuestionChoice) {
    		theCondition = createCondEqual((QuestionChoice)theQuestion, answerText,kbm);
    	}
    	// QuestionNum
    	else if (theQuestion instanceof QuestionNum) {
    		theCondition = createCondNum((QuestionNum)theQuestion, answerText,kbm);
    	}
    	
    	return theCondition;
	}
	
	private static CondEqual createCondEqual(QuestionChoice theQuestion, String answerText, KnowledgeBaseManagement kbm) {
		if(answerText.startsWith("=")) {
			answerText = answerText.substring(1);
		}
		AnswerChoice theAnswer = kbm.findAnswerChoice(theQuestion, answerText);
		// Fragetext in Tabelle stimmt nicht mit Fragetext ï¿½berein
		if (theAnswer==null && theQuestion instanceof QuestionYN) {
			if (answerText.equalsIgnoreCase("yes") || answerText.equalsIgnoreCase("ja")) {
       			theAnswer = kbm.findAnswerChoice(theQuestion, "Yes");
				if (theAnswer==null) theAnswer = kbm.findAnswerChoice(theQuestion, "ja");
			}
			else if (answerText.equalsIgnoreCase("no") || answerText.equalsIgnoreCase("nein")) {
	            theAnswer = kbm.findAnswerChoice(theQuestion, "No");
	            if (theAnswer==null) theAnswer = kbm.findAnswerChoice(theQuestion, "nein");
			}
//		    Alternativer Ansatz: betrachte erste Antwort als YES-Antwort, zweite als NO-Antwort   	
//			if (table.getQuestionRowIndex(rowIx)==rowIx-1) // erste Antwort entspricht YES
//				theAnswer = kbm.findAnswerChoice((QuestionChoice)theQuestion, "YES");
//			else if (table.getQuestionRowIndex(rowIx)==rowIx-2) // zweite Antwort entspricht NO
//				theAnswer = kbm.findAnswerChoice((QuestionChoice)theQuestion, "NO");
		}
		if (theAnswer==null)
			return null;
		else 
			return new CondEqual(theQuestion, theAnswer);
	}
	
	private static CondNum createCondNum(QuestionNum theQuestion, String answerText, KnowledgeBaseManagement kbm) {
		CondNum theCondition = null;
		answerText = answerText.trim();
		if (answerText.startsWith("\"") && answerText.endsWith("\""))
			answerText = answerText.substring(1, answerText.length()-1).trim();
		answerText = answerText.replaceAll("[,]", ".");
		if (answerText.startsWith(">=")) {
			answerText = answerText.substring(2).trim();
			theCondition = new CondNumGreaterEqual(theQuestion, new Double(answerText));
		}
		else if (answerText.startsWith("<=")) {
			answerText = answerText.substring(2).trim();
			theCondition = new CondNumLessEqual(theQuestion, new Double(answerText));
		}
		else if (answerText.startsWith(">")) {
			answerText = answerText.substring(1).trim();
			theCondition = new CondNumGreater(theQuestion, new Double(answerText));
		}
		else if (answerText.startsWith("<")) {
			answerText = answerText.substring(1).trim();
			theCondition = new CondNumLess(theQuestion,	new Double(answerText));
		}
		else if (answerText.startsWith("[") || answerText.startsWith("in [")) {
			if(answerText.startsWith("in [")) {
				answerText = answerText.substring(3);
			}
			answerText = answerText.substring(1, answerText.length()-1).trim();
			theCondition = new CondNumIn(theQuestion,
											new Double(answerText.split(" ")[0]),
											new Double(answerText.split(" ")[1]));
		}
		else if (answerText.startsWith("=")) {
			answerText = answerText.substring(1).trim();
			theCondition = new CondNumEqual(theQuestion, new Double(answerText));
		}
		else
			theCondition = new CondNumEqual(theQuestion, new Double(answerText));
			
		return theCondition;
	}

	protected List<AbstractCondition> combineConditionParts(
								Map<String, List<AbstractCondition>> conditionParts) {
		List<AbstractCondition> newConditions = new ArrayList<AbstractCondition>(1);
		for (Iterator<String> it = conditionParts.keySet().iterator(); it.hasNext(); ) {
	    	String key = it.next();
	    	List values = conditionParts.get(key);
	    	
	    	AbstractCondition newCondition = null;
	    	// "and" association in field
	    	if (key.startsWith(DecisionTableConfigReader.LOGICAL_OPERATOR_ROW_AND))
	    		newCondition = new CondAnd(values);
	    	// "or" association in field
	    	else if (key.startsWith(DecisionTableConfigReader.LOGICAL_OPERATOR_ROW_OR))
	    		newCondition = new CondOr(values);
	
	    	if (newCondition!=null)
	    		newConditions.add(newCondition);
	    }
		return newConditions;
	}

	protected void createConditions(DecisionTable table, int i, int j, Map<String, List<AbstractCondition>> condParts, List<AbstractCondition> condList) {
		String value = table.get(i,j);
		if (!value.equals("")) {
		    String questionText = table.getQuestionText(i);
		    String answerText = table.get(i,1);
		    
		    AbstractCondition nextCondition = createCondition(questionText, answerText);
			
		    if (nextCondition==null)
		    	return;
		    
			String[] parts = value.split(" ");
		    // negation
		    if (parts[0].equals(DecisionTableConfigReader.SIGN_NEGATIVE)) {
		        nextCondition = new CondNot(nextCondition);
		    }
		    // store conditions with row operator in Hashtable "condParts" ...
		    if (parts.length>1) {
		    	if (condParts.containsKey(parts[1])) {
		    		(condParts.get(parts[1])).add(nextCondition);
		    	}
		    	else {
		    		List<AbstractCondition> el = new ArrayList<AbstractCondition>(1);
		    		el.add(nextCondition);
		    		condParts.put(parts[1], el);
		    	}
		    }
		    // ... and others in Vector "condList"
		    else
		        condList.add(nextCondition);
		}
	}

	/**
	 * Creates a single rule and adds it to the knowledgebase
	 * @param conditionList list of all conditions
	 * @param columnOperator logical operator used to concatenate the condition listed
	 * in <CODE>conditionList<CODE>
	 * @param diagnosisText name of the diagnosis
	 * @param columnScore string containing the score of the rule
	 * @return the created rule
	 */
	protected RuleComplex generateRule(List<AbstractCondition> conditionList,
								String columnOperator,
								String diagnosisText,
								String columnScore) {
		Diagnosis theDiagnosis = kbm.findDiagnosis(diagnosisText);
		Score theScore = ScoreFinder.getScore(columnScore);
		AbstractCondition theCondition = null;
		
		// "and" operator in column
		if (columnOperator.equalsIgnoreCase(DecisionTableConfigReader.LOGICAL_OPERATOR_COLUMN_AND)) {
			theCondition = new CondAnd(conditionList);
		}
		// "or" operator in column
		else if (columnOperator.equalsIgnoreCase(DecisionTableConfigReader.LOGICAL_OPERATOR_COLUMN_OR)) {
			theCondition = new CondOr(conditionList);
		}
		// "minmax" operator in column
		else if (columnOperator.toLowerCase().indexOf(
							DecisionTableConfigReader.LOGICAL_OPERATOR_COLUMN_MINMAX)!=-1) {
			String[] split = columnOperator.split(DecisionTableConfigReader.LOGICAL_OPERATOR_COLUMN_MINMAX);
			int min = Integer.parseInt(split[0]);
			int max = Integer.parseInt(split[1]);
			theCondition = new CondMofN(conditionList, min, max);
		}
		// no logical operator
		else if (columnOperator.equals("") && conditionList.size()==1) {
				theCondition = conditionList.get(0);
		}
		
		if (theDiagnosis!=null && theScore!=null && theCondition!=null) {
			return createRule(theCondition, theDiagnosis, theScore);
		}
		return null;
	}
}
