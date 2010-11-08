/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.abstraction.formula.Operator;
import de.d3web.abstraction.formula.Operator.Operation;
import de.d3web.abstraction.formula.QNumWrapper;
import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondKnown;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondOr;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.MMInfoSubject;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.scoring.Score;
import de.d3web.we.basic.D3webKnowledgeHandler;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelationType;

/**
 * This Class loads the KnowledgeBase which will be tested.
 *
 * Furthermore in this class the KnowledgeBase against which the loaded
 * KnowledgeBase is compared is created.
 *
 * This class is a Singleton class because this insures that the KnowledgeBase
 * is loaded only once.
 *
 * Please be careful when editing anything in here because the order of the
 * elements does matter in the tests! (especially the IDs)
 *
 * @author Sebastian Furth
 * @see KnowledgeBaseCreationTest
 *
 */
public class KBTestUtilNewMarkup {

	public static final String KBCREATION_ARTICLE_FILE = "src/test/resources/KBCreationTestNewMarkup.txt";

	private static KBTestUtilNewMarkup instance = new KBTestUtilNewMarkup();
	private KnowledgeBase createdKB;
	private KnowledgeBaseManagement createdKBM;

	/**
	 * Returns the KnowledgeBase of a specific article
	 *
	 * @created 01.09.2010
	 * @param article
	 */
	public KnowledgeBase getKnowledgeBase(KnowWEArticle article) {
		// Load KnowledgeBase
		D3webKnowledgeHandler d3Handler = D3webModule.getKnowledgeRepresentationHandler("default_web");
		return d3Handler.getKBM(article.getTitle()).getKnowledgeBase();
	}

	/**
	 * Private Constructor insures noninstantiabilty.
	 */
	private KBTestUtilNewMarkup() {

		createGoldenKnowledge();

	}

	/**
	 * Returns an instance of KBCreationTestKBStorage.
	 *
	 * @return KBCreationTestKBStorage
	 */
	public static KBTestUtilNewMarkup getInstance() {
		return instance;
	}

	/**
	 * Returns the KnowledgeBase which was created manually.
	 *
	 * @return KnowledgeBase
	 */
	public KnowledgeBase getCreatedKB() {
		return createdKB;
	}

	/**
	 * Creates the Knowledge against which the loaded KnowledgeBase will be
	 * tested.
	 */
	private void createGoldenKnowledge() {

		createdKB = new KnowledgeBase();
		createdKBM = KnowledgeBaseManagement.createInstance(createdKB);
		createSolutions();
		createQuestionnaires();
		createQuestions();
		createRules();
		createXCLModels();

	}

	/**
	 * Creates the Diagnoses for the KnowledgeBase.
	 */
	private void createSolutions() {

		Solution p0 = new Solution("P000");
		p0.setName("P000");
		createdKB.add(p0);
		createdKB.setRootSolution(p0);

		Solution p1 = new Solution("P1");
		p1.setName("Mechanical problem");
		createdKB.getRootSolution().addChild(p1);
		createdKB.add(p1);

		Solution p2 = new Solution("P2");
		p2.setName("Damaged idle speed system");
		p1.addChild(p2);
		createdKB.add(p2);

		Solution p3 = new Solution("P3");
		p3.setName("Leaking air intake system");
		p3.getInfoStore().addValue(BasicProperties.EXPLANATION, "The air intake system is leaking.");
		p1.addChild(p3);
		createdKB.add(p3);

		Solution p4 = new Solution("P4");
		p4.setName("Other problem");
		createdKB.getRootSolution().addChild(p4);
		createdKB.add(p4);

	}

	/**
	 * Creates the Questionnaires for the KnowledgeBase.
	 */
	private void createQuestionnaires() {

		QContainer qc0 = new QContainer("Q000");
		qc0.setName("Q000");
		createdKB.add(qc0);
		createdKB.setRootQASet(qc0);

		QContainer qc1 = new QContainer("QC1");
		qc1.setName("Observations");
		createdKB.getRootQASet().addChild(qc1);
		createdKB.add(qc1);

		QContainer qc2 = new QContainer("QC2");
		qc2.setName("Idle speed system");
		qc1.addChild(qc2);
		createdKB.add(qc2);

		QContainer qc3 = new QContainer("QC3");
		qc3.setName("Air filter");
		qc3.getInfoStore().addValue(BasicProperties.EXPLANATION,
				"Here you can enter your observations concerning the air filter.");
		qc1.addChild(qc3);
		createdKB.add(qc3);

		QContainer qc4 = new QContainer("QC4");
		qc4.setName("Ignition timing");
		qc1.addChild(qc4);
		createdKB.add(qc4);

		QContainer qc5 = new QContainer("QC5");
		qc5.setName("Technical Examinations");
		createdKB.getRootQASet().addChild(qc5);
		createdKB.add(qc5);

		// Set Init-Questions
		List<QContainer> initQuestions = new ArrayList<QContainer>();
		initQuestions.add(qc1);
		createdKB.setInitQuestions(initQuestions);

	}

	/**
	 * Creates the Question for the KnowledgeBase.
	 */
	private void createQuestions() {

		// Get QContainer
		// Observations
		QContainer qc1 = createdKBM.findQContainer("Observations");

		// Add Question:
		// - Exhaust fumes ~ "What is the color of the exhaust fumes?" [oc]
		// -- black
		// -- blue
		// -- invisible
		Question q0 = createdKBM.createQuestionOC("Q1", "Exhaust fumes", qc1,
				new String[] {
						"black", "blue", "invisible" });

		// Add MMInfo to Question "Exhaust fumes":
		// "What is the color of the exhaust fumes?"
		addMMInfo(q0, "LT", MMInfoSubject.PROMPT.getName(),
				"What is the color of the exhaust fumes?");

		// Add question:
		// --- Fuel [oc]
		// ---- diesel
		// ---- unleaded gasoline
		createdKBM.createQuestionOC("Q2", "Fuel", q0, new String[] {
				"diesel", "unleaded gasoline" });

		// Add question:
		// - "Average mileage /100km" [num] {liter} (0 30) #Q1337
		Question q1 = createdKBM.createQuestionNum("Q16", "Average mileage /100km", qc1);
		// q1.setName("Average mileage /100km");
		InfoStore infoStore = q1.getInfoStore();
		infoStore.addValue(BasicProperties.UNIT, "liter");
		q1.getInfoStore().addValue(BasicProperties.QUESTION_NUM_RANGE, new NumericalInterval(0, 30));

		// Add question:
		// -- "Num. Mileage evaluation" [num] <abstract>
		Question q2 = createdKBM.createQuestionNum("Q15", "Num. Mileage evaluation", q1);
		q2.getInfoStore().addValue(BasicProperties.ABSTRACTION_QUESTION, Boolean.TRUE);

		// Add question:
		// --- Mileage evaluation [oc] <abstract>
		// ---- normal
		// ---- increased
		Question q3 = createdKBM.createQuestionOC("Q5", "Mileage evaluation", q2,
				new String[] {
						"normal", "increased" });
		q3.getInfoStore().addValue(BasicProperties.ABSTRACTION_QUESTION, Boolean.TRUE);

		// Add question:
		// - "Real mileage  /100km" [num]
		createdKBM.createQuestionNum("Q3b", "Real mileage  /100km", qc1);

		// Add question:
		// - Driving [mc]
		// -- insufficient power on partial load
		// -- insufficient power on full load
		// -- unsteady idle speed
		// -- everything is fine
		Question q4 = createdKBM.createQuestionMC("Q7", "Driving", qc1,
				new String[] {
						"insufficient power on partial load",
						"insufficient power on full load",
						"unsteady idle speed",
						"everything is fine" });

		// Add question:
		// ---- Other [text]
		createdKBM.createQuestionText("QText", "Other", q4);

		// Get second QContainer and add Question
		// Technical Examinations
		QContainer qc2 = createdKBM.findQContainer("Technical Examinations");
		// - "Idle speed system o.k.?" [yn]
		// createdKBM.createQuestionYN("Q9", "Idle speed system o.k.?", qc2);
		createdKBM.createQuestionYN("Q9", "Idle speed system o.k.?", null, null, qc2);
	}

	/**
	 * Creates the Rules for the KnowledgeBase
	 */
	private void createRules() {
		createDTIndicationRules();
		createDTSetValueRules();
		createDTHeuristicRules();
		createDTRefinementRules();
		createSetValueRules();
		createHeuristicRules();
		createIndicationRules();
	}

	/**
	 * Creates the Indication-Rules
	 */
	private void createDTIndicationRules() {

		Choice answer;
		CondEqual condition;
		String ruleID;
		QuestionChoice condQuestion = (QuestionChoice) createdKBM.findQuestion("Exhaust fumes");
		QuestionChoice actionQuestion = (QuestionChoice) createdKBM.findQuestion("Fuel");

		// Create Rule R1:
		// - Exhaust fumes [oc]
		// -- black
		// --- Fuel [oc]
		answer = createdKBM.findChoice(condQuestion, "black");
		condition = new CondEqual(condQuestion, new ChoiceValue(answer));
		ruleID = createdKBM.createRuleID();
		RuleFactory.createIndicationRule(ruleID, actionQuestion, condition);

		// Create Rule R2:
		// - Exhaust fumes [oc]
		// -- blue
		// --- &REF Fuel
		answer = createdKBM.findChoice(condQuestion, "blue");
		condition = new CondEqual(condQuestion, new ChoiceValue(answer));
		ruleID = createdKBM.createRuleID();
		RuleFactory.createIndicationRule(ruleID, actionQuestion, condition);

		// Create Rule R3:
		// - Exhaust fumes [oc]
		// -- invisible
		// --- &REF Fuel
		answer = createdKBM.findChoice(condQuestion, "invisible");
		condition = new CondEqual(condQuestion, new ChoiceValue(answer));
		ruleID = createdKBM.createRuleID();
		RuleFactory.createIndicationRule(ruleID, actionQuestion, condition);

	}

	/**
	 * Creates the SetValue Rules from the DecisionTree
	 */
	private void createDTSetValueRules() {

		// Create Rule R4:
		// - Driving [mc]
		// -- insufficient power on partial load
		// --- "Num. Mileage evaluation" SET (110)
		QuestionChoice q1 = (QuestionChoice) createdKBM.findQuestion("Driving");
		Choice a1 = createdKBM.findChoice(q1, "insufficient power on partial load");
		CondEqual c1 = new CondEqual(q1, new ChoiceValue(a1));
		String ruleID = createdKBM.createRuleID();
		QuestionNum q2 = (QuestionNum) createdKBM.findQuestion("Num. Mileage evaluation");
		FormulaNumber fn1 = new FormulaNumber(110.0);
		RuleFactory.createSetValueRule(ruleID, q2, fn1, c1);

		// Create Rule R5:
		// - Driving [mc]
		// -- insufficient power on full load
		// --- "Num. Mileage evaluation" (20)
		Choice a2 = createdKBM.findChoice(q1, "insufficient power on full load");
		CondEqual c2 = new CondEqual(q1, new ChoiceValue(a2));
		ruleID = createdKBM.createRuleID();
		QuestionNum q3 = (QuestionNum) createdKBM.findQuestion("Num. Mileage evaluation");
		FormulaNumber fn2 = new FormulaNumber(20.0);
		RuleFactory.createSetValueRule(ruleID, q3, new Object[] { fn2 }, c2);
	}

	/**
	 * Creates the Heuristic Rules from the DecisionTree
	 */
	private void createDTHeuristicRules() {

		// Create Rule R6:
		// - Driving [mc]
		// -- everything is fine
		// --- Other problem (P7)
		QuestionChoice condQuestion = (QuestionChoice) createdKBM.findQuestion("Driving");
		Choice answer = createdKBM.findChoice(condQuestion, "everything is fine");
		CondEqual condition = new CondEqual(condQuestion, new ChoiceValue(
				answer));
		String ruleID = createdKBM.createRuleID();
		Solution diag = createdKBM.findSolution("Other problem");
		RuleFactory.createHeuristicPSRule(ruleID, diag, Score.P7, condition);

	}

	/**
	 * Creates the Refinement Rules from the DecisionTree
	 */
	private void createDTRefinementRules() {

		// Create Rule R7:
		// --- Other problem (P7)
		// ---- Other [text]
		Question question = createdKBM.findQuestion("Other");
		List<QASet> action = new ArrayList<QASet>();
		action.add(question);
		String ruleID = createdKBM.createRuleID();
		Solution diag = createdKBM.findSolution("Other problem");
		CondDState condition = new CondDState(diag,
				new Rating(State.ESTABLISHED));
		RuleFactory.createRefinementRule(ruleID, action, diag, condition);

	}

	/**
	 * Creates the Set-Value Rules corresponding to the rules of the
	 * Rules-section
	 */
	private void createSetValueRules() {

		// Create first Condition:
		// KNOWN["Real mileage  /100km"]
		QuestionNum q11 = (QuestionNum) createdKBM.findQuestion("Real mileage  /100km");
		CondKnown c11 = new CondKnown(q11);

		// Create second Condition:
		// "Average mileage /100km" > 0
		QuestionNum q12 = (QuestionNum) createdKBM.findQuestion("Average mileage /100km");
		CondNumGreater c12 = new CondNumGreater(q12, 0.0);

		// Create AND Condition:
		// "Average mileage /100km" > 0 AND KNOWN["Real mileage  /100km"]
		List<Condition> conditions = new LinkedList<Condition>();
		conditions.add(c11);
		conditions.add(c12);
		CondAnd c1 = new CondAnd(conditions);

		// Create Rule R8:
		// IF "Average mileage /100km" > 0 AND KNOWN["Real mileage  /100km"]
		// THEN "Num. Mileage evaluation" = (("Real mileage  /100km" /
		// "Average mileage /100km") * 100.0)
		String ruleID = createdKBM.createRuleID();
		QuestionNum q3 = (QuestionNum) createdKBM.findQuestion("Num. Mileage evaluation");
		Operator d = new Operator(new QNumWrapper(q11), new QNumWrapper(q12), Operation.Div);
		FormulaNumber fn = new FormulaNumber(100.0);
		Operator m = new Operator(d, fn, Operation.Mult);
		RuleFactory.createSetValueRule(ruleID, q3, m, c1);

		// Create Rule R9:
		// IF "Num. Mileage evaluation" > 130
		// THEN Mileage evaluation = increased
		CondNumGreater c2 = new CondNumGreater(q3, 130.0);
		ruleID = createdKBM.createRuleID();
		Question q4 = createdKBM.findQuestion("Mileage evaluation");
		Choice a = createdKBM.findChoice((QuestionChoice) q4, "increased");
		RuleFactory.createSetValueRule(ruleID, q4, new ChoiceValue(a), c2);

		// Create rule R10:
		// IF Driving = unsteady idle speed
		// THEN "Real mileage  /100km" += ( 2 )
		QuestionChoice questionIf4 = (QuestionChoice) createdKBM.findQuestion("Driving");
		Choice answerIf4 = createdKBM.findChoice(questionIf4, "unsteady idle speed");
		CondEqual conditionIf4 = new CondEqual(questionIf4, new ChoiceValue(
				answerIf4));
		ruleID = createdKBM.createRuleID();
		QuestionNum qnum = (QuestionNum) createdKBM.findQuestion("Real mileage  /100km");
		FormulaNumber fn2 = new FormulaNumber(2.0);
		Operator add = new Operator(new QNumWrapper(qnum), fn2, Operation.Add);
		RuleFactory.createSetValueRule(ruleID, qnum, add, conditionIf4);

		// Create Rule R11:
		// IF Driving = insufficient power on full load
		// THEN "Real mileage  /100km" = ("Average mileage /100km" + 2)
		QuestionChoice questionIf5 = (QuestionChoice) createdKBM.findQuestion("Driving");
		Choice answerIf5 = createdKBM.findChoice(questionIf5, "insufficient power on full load");
		CondEqual conditionIf5 = new CondEqual(questionIf5, new ChoiceValue(
				answerIf5));
		QuestionNum questionFormula = (QuestionNum) createdKBM.findQuestion("Average mileage /100km");
		FormulaNumber fn3 = new FormulaNumber(2.0);
		Operator addition = new Operator(new QNumWrapper(questionFormula), fn3, Operation.Add);
		QuestionNum questionThen = (QuestionNum) createdKBM.findQuestion("Real mileage  /100km");
		ruleID = createdKBM.createRuleID();
		RuleFactory.createSetValueRule(ruleID, questionThen, addition, conditionIf5);

		// Create Rule R12:
		// IF Driving = insufficient power on partial load
		// THEN "Real mileage  /100km" = ("Average mileage /100km" - 1)
		QuestionChoice questionIf6 = (QuestionChoice) createdKBM.findQuestion("Driving");
		Choice answerIf6 = createdKBM.findChoice(questionIf6, "insufficient power on partial load");
		CondEqual conditionIf6 = new CondEqual(questionIf6, new ChoiceValue(
				answerIf6));
		QuestionNum questionFormula2 = (QuestionNum) createdKBM.findQuestion("Average mileage /100km");
		FormulaNumber fn4 = new FormulaNumber(1.0);
		Operator subtraction = new Operator(new QNumWrapper(questionFormula2), fn4, Operation.Sub);
		QuestionNum questionThen2 = (QuestionNum) createdKBM.findQuestion("Real mileage  /100km");
		ruleID = createdKBM.createRuleID();
		RuleFactory.createSetValueRule(ruleID, questionThen2, subtraction, conditionIf6);

	}

	/**
	 * Creates the Heuristic Rules corresponding to the rules of the
	 * Rules-section
	 */
	private void createHeuristicRules() {

		// Create rule R13:
		// IF Exhaust fumes = black EXCEPT Fuel = diesel
		// THEN Air filter (P7)
		QuestionChoice questionIf = (QuestionChoice) createdKBM.findQuestion("Exhaust fumes");
		Choice answerIf = createdKBM.findChoice(questionIf, "black");
		CondEqual conditionIf = new CondEqual(questionIf, new ChoiceValue(
				answerIf));

		QuestionChoice questionExc = (QuestionChoice) createdKBM.findQuestion("Fuel");
		Choice answerExc = createdKBM.findChoice(questionExc, "diesel");
		CondEqual conditionExc = new CondEqual(questionExc, new ChoiceValue(
				answerExc));

		String ruleID = createdKBM.createRuleID();
		Solution diag = createdKBM.findSolution("Mechanical problem");
		RuleFactory.createHeuristicPSRule(ruleID, diag, Score.P7, conditionIf, conditionExc);

		// Create rule R14:
		// IF (NOT Fuel = unleaded gasoline) OR (NOT Exhaust fumes = black)
		// THEN Mechanical problem = N7
		QuestionChoice questionIf2 = (QuestionChoice) createdKBM.findQuestion("Fuel");
		Choice answerIf2 = createdKBM.findChoice(questionIf2, "unleaded gasoline");
		CondEqual conditionIf2 = new CondEqual(questionIf2, new ChoiceValue(
				answerIf2));
		CondNot condNot1 = new CondNot(conditionIf2);

		QuestionChoice questionIf3 = (QuestionChoice) createdKBM.findQuestion("Exhaust fumes");
		Choice answerIf3 = createdKBM.findChoice(questionIf3, "black");
		CondEqual conditionIf3 = new CondEqual(questionIf3, new ChoiceValue(
				answerIf3));
		CondNot condNot2 = new CondNot(conditionIf3);

		List<Condition> conditions = new LinkedList<Condition>();
		conditions.add(condNot2);
		conditions.add(condNot1);
		CondOr condOr = new CondOr(conditions);

		ruleID = createdKBM.createRuleID();
		RuleFactory.createHeuristicPSRule(ruleID, diag, Score.N7, condOr);

	}

	/**
	 * Creates the Indication-Rules
	 */
	private void createIndicationRules() {

		String ruleID;

		// Create Rule R15:
		// IF Driving = unsteady idle speed
		// THEN Technical Examinations
		QuestionChoice condQuestion = (QuestionChoice) createdKBM.findQuestion("Driving");
		QASet actionQuestion = createdKBM.findQContainer("Technical Examinations");
		Choice answer = createdKBM.findChoice(condQuestion, "unsteady idle speed");
		CondEqual condition = new CondEqual(condQuestion, new ChoiceValue(
				answer));
		ruleID = createdKBM.createRuleID();
		RuleFactory.createIndicationRule(ruleID, actionQuestion, condition);

		// Create Rule R16:
		// IF Mileage evaluation = increased
		// THEN "Idle speed system o.k.?"; Driving
		QuestionChoice condQuestion2 = (QuestionChoice) createdKBM.findQuestion("Mileage evaluation");
		QuestionChoice actionQuestion2 = (QuestionChoice) createdKBM.findQuestion("Idle speed system o.k.?");
		QuestionChoice actionQuestion3 = (QuestionChoice) createdKBM.findQuestion("Driving");
		Choice answer2 = createdKBM.findChoice(condQuestion2, "increased");
		CondEqual condition2 = new CondEqual(condQuestion2, new ChoiceValue(
				answer2));
		List<QASet> nextQuestions = new ArrayList<QASet>();
		nextQuestions.add(actionQuestion2);
		nextQuestions.add(actionQuestion3);
		ruleID = createdKBM.createRuleID();
		RuleFactory.createIndicationRule(ruleID, nextQuestions, condition2);

		// Create Rule R17:
		// IF KNOWN[Other]
		// THEN Technical Examinations
		Question condQuestion3 = createdKBM.findQuestion("Other");
		QASet actionQuestion4 = createdKBM.findQContainer("Technical Examinations");
		CondKnown condition3 = new CondKnown(condQuestion3);
		ruleID = createdKBM.createRuleID();
		RuleFactory.createIndicationRule(ruleID, actionQuestion4, condition3);

	}

	/**
	 * Creats a XCLModel similar to the one which is created in the
	 * KnowWEArticle
	 */
	private void createXCLModels() {

		Solution d = createdKBM.findSolution("Damaged idle speed system");

		// "Idle speed system o.k.?" = Yes [--]
		Question q1 = createdKBM.findQuestion("Idle speed system o.k.?");
		Choice a1 = createdKBM.findChoice((QuestionChoice) q1, "Yes");
		CondEqual c1 = new CondEqual(q1, new ChoiceValue(a1));
		XCLModel.insertXCLRelation(createdKB, c1, d, XCLRelationType.contradicted);

		// Driving = unsteady idle speed [!]
		Question q2 = createdKBM.findQuestion("Driving");
		Choice a2 = createdKBM.findChoice((QuestionChoice) q2, "unsteady idle speed");
		CondEqual c2 = new CondEqual(q2, new ChoiceValue(a2));
		XCLModel.insertXCLRelation(createdKB, c2, d, XCLRelationType.requires);

		// "Idle speed system o.k.?" = no [++]
		Question q3 = createdKBM.findQuestion("Idle speed system o.k.?");
		Choice a3 = createdKBM.findChoice((QuestionChoice) q3, "No");
		CondEqual c3 = new CondEqual(q3, new ChoiceValue(a3));
		XCLModel.insertXCLRelation(createdKB, c3, d, XCLRelationType.sufficiently);

		// Mileage evaluation = increased [3]
		Question q4 = createdKBM.findQuestion("Mileage evaluation");
		Choice a4 = createdKBM.findChoice((QuestionChoice) q4, "increased");
		CondEqual c4 = new CondEqual(q4, new ChoiceValue(a4));
		XCLModel.insertXCLRelation(createdKB, c4, d, XCLRelationType.explains, 3.0);

		// Exhaust fumes = black
		Question q5 = createdKBM.findQuestion("Exhaust fumes");
		Choice a5 = createdKBM.findChoice((QuestionChoice) q5, "black");
		CondEqual c5 = new CondEqual(q5, new ChoiceValue(a5));
		XCLModel.insertXCLRelation(createdKB, c5, d, XCLRelationType.explains);

	}

	/**
	 * Adds a MMInfo to the NamedObject
	 *
	 * @param o NamedObject
	 * @param title String
	 * @param subject String
	 * @param content String
	 */
	private void addMMInfo(NamedObject o, String title, String subject,
			String content) {

		MMInfoStorage mmis;
		DCMarkup dcm = new DCMarkup();
		dcm.setContent(DCElement.TITLE, title);
		dcm.setContent(DCElement.SUBJECT, subject);
		dcm.setContent(DCElement.SOURCE, o.getId());
		MMInfoObject mmi = new MMInfoObject(dcm, content);
		mmis = new MMInfoStorage();
		o.getInfoStore().addValue(BasicProperties.MMINFO, mmis);
		mmis.addMMInfo(mmi);

	}

}
