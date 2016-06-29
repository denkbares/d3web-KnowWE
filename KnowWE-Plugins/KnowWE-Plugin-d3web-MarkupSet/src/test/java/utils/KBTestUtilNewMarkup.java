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
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.indication.ActionIndication;
import de.d3web.indication.ActionNextQASet;
import de.d3web.indication.inference.PSMethodStrategic;
import de.d3web.scoring.Score;
import de.d3web.we.utils.D3webUtils;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelationType;
import de.knowwe.core.kdom.Article;

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

	/**
	 * Returns the KnowledgeBase of a specific article
	 * 
	 * @created 01.09.2010
	 * @param article
	 */
	public KnowledgeBase getKnowledgeBase(Article article) {
		return D3webUtils.getKnowledgeBase(article.getWeb(), article.getTitle());
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

		Solution p0 = new Solution(createdKB, "P000");
		createdKB.setRootSolution(p0);

		Solution p1 = new Solution(createdKB, "Mechanical problem");
		createdKB.getRootSolution().addChild(p1);

		Solution p2 = new Solution(createdKB, "Damaged idle speed system");
		p1.addChild(p2);

		Solution p3 = new Solution(createdKB, "Leaking air intake system");
		p3.getInfoStore().addValue(MMInfo.DESCRIPTION,
				"The air intake system is leaking.");
		p1.addChild(p3);

		Solution p4 = new Solution(createdKB, "Other problem");
		createdKB.getRootSolution().addChild(p4);

	}

	/**
	 * Creates the Questionnaires for the KnowledgeBase.
	 */
	private void createQuestionnaires() {

		QContainer qc0 = new QContainer(createdKB, "Q000");
		createdKB.setRootQASet(qc0);

		QContainer qc1 = new QContainer(createdKB, "Observations");
		createdKB.getRootQASet().addChild(qc1);

		QContainer qc2 = new QContainer(createdKB, "Idle speed system");
		qc1.addChild(qc2);

		QContainer qc3 = new QContainer(createdKB, "Air filter");
		qc3.getInfoStore().addValue(MMInfo.PROMPT,
				"Here you can enter your observations concerning the air filter.");
		qc1.addChild(qc3);

		QContainer qc4 = new QContainer(createdKB, "Ignition timing");
		qc1.addChild(qc4);

		QContainer qc5 = new QContainer(createdKB, "Technical Examinations");
		createdKB.getRootQASet().addChild(qc5);

		// Set Init-Questions
		List<QContainer> initQuestions = new ArrayList<>();
		initQuestions.add(qc1);
		createdKB.setInitQuestions(initQuestions);

	}

	/**
	 * Creates the Question for the KnowledgeBase.
	 */
	private void createQuestions() {

		// Get QContainer
		// Observations
		QContainer qc1 = createdKB.getManager().searchQContainer("Observations");

		// Add Question:
		// - Exhaust fumes ~ "What is the color of the exhaust fumes?" [oc]
		// -- black
		// -- blue
		// -- invisible
		Question q0 = new QuestionOC(qc1, "Exhaust fumes", "black", "blue", "invisible");

		// Add MMInfo to Question "Exhaust fumes":
		// "What is the color of the exhaust fumes?"
		q0.getInfoStore().addValue(MMInfo.PROMPT,
				"What is the color of the exhaust fumes?");

		// Add question:
		// --- Fuel [oc]
		// ---- diesel
		// ---- unleaded gasoline
		new QuestionOC(q0, "Fuel", "diesel", "unleaded gasoline");

		// Add question:
		// - "Average mileage /100km" [num] {liter} (0 30) #Q1337
		Question q1 = new QuestionNum(qc1, "Average mileage /100km");
		// q1.setName("Average mileage /100km");
		InfoStore infoStore = q1.getInfoStore();
		infoStore.addValue(MMInfo.UNIT, "liter");
		q1.getInfoStore().addValue(BasicProperties.QUESTION_NUM_RANGE,
				new NumericalInterval(0, 30));

		// Add question:
		// -- "Num. Mileage evaluation" [num] <abstract>
		Question q2 = new QuestionNum(q1, "Num. Mileage evaluation");
		q2.getInfoStore().addValue(BasicProperties.ABSTRACTION_QUESTION, Boolean.TRUE);

		// Add question:
		// --- Mileage evaluation [oc] <abstract>
		// ---- normal
		// ---- increased
		Question q3 = new QuestionOC(q2, "Mileage evaluation",
				"normal", "increased");
		q3.getInfoStore().addValue(BasicProperties.ABSTRACTION_QUESTION, Boolean.TRUE);

		// Add question:
		// - "Real mileage  /100km" [num]
		new QuestionNum(qc1, "Real mileage  /100km");

		// Add question:
		// - Driving [mc]
		// -- insufficient power on partial load
		// -- insufficient power on full load
		// -- unsteady idle speed
		// -- everything is fine
		new QuestionMC(qc1, "Driving",
				"insufficient power on partial load",
				"insufficient power on full load",
				"unsteady idle speed",
				"everything is fine");

		// Add question:
		// - Other [text]
		new QuestionText(qc1, "Other");

		// Get second QContainer and add Question
		// Technical Examinations
		QContainer qc2 = createdKB.getManager().searchQContainer("Technical Examinations");
		// - "Idle speed system o.k.?" [yn]
		// createdKBM.createQuestionYN("Q9", "Idle speed system o.k.?", qc2);

		// new QuestionYN(qc2, "Idle speed system o.k.?");
		new QuestionOC(qc2, "Idle speed system o.k.?", "Yes", "No");

		new QuestionYN(qc2, "SomeQuestion");
	}

	/**
	 * Creates the Rules for the KnowledgeBase
	 */
	private void createRules() {
		createDTIndicationRules();
		createDTSetValueRules();
		createDTHeuristicRules();
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
		QuestionChoice condQuestion = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Exhaust fumes");
		QuestionChoice actionQuestion = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Fuel");

		// Create Rule R1:
		// - Exhaust fumes [oc]
		// -- black
		// --- Fuel [oc]
		answer = KnowledgeBaseUtils.findChoice(condQuestion, "black");
		condition = new CondEqual(condQuestion, new ChoiceValue(answer));
		RuleFactory.createIndicationRule(actionQuestion, condition);

		// Create Rule R2:
		// - Exhaust fumes [oc]
		// -- blue
		// --- &REF Fuel
		answer = KnowledgeBaseUtils.findChoice(condQuestion, "blue");
		condition = new CondEqual(condQuestion, new ChoiceValue(answer));
		RuleFactory.createIndicationRule(actionQuestion, condition);

		// Create Rule R3:
		// - Exhaust fumes [oc]
		// -- invisible
		// --- &REF Fuel
		answer = KnowledgeBaseUtils.findChoice(condQuestion, "invisible");
		condition = new CondEqual(condQuestion, new ChoiceValue(answer));
		RuleFactory.createIndicationRule(actionQuestion, condition);

	}

	/**
	 * Creates the SetValue Rules from the DecisionTree
	 */
	private void createDTSetValueRules() {

		// Create Rule R4:
		// - Driving [mc]
		// -- insufficient power on partial load
		// --- "Num. Mileage evaluation" SET (110)
		QuestionChoice q1 = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Driving");
		Choice a1 = KnowledgeBaseUtils.findChoice(q1,
				"insufficient power on partial load");
		CondEqual c1 = new CondEqual(q1, new ChoiceValue(a1));
		QuestionNum q2 = (QuestionNum) createdKB.getManager().searchQuestion(
				"Num. Mileage evaluation");
		FormulaNumber fn1 = new FormulaNumber(110.0);
		RuleFactory.createSetValueRule(q2, fn1, c1);

		// Create Rule R5:
		// - Driving [mc]
		// -- insufficient power on full load
		// --- "Num. Mileage evaluation" (20)
		Choice a2 = KnowledgeBaseUtils.findChoice(q1, "insufficient power on full load");
		CondEqual c2 = new CondEqual(q1, new ChoiceValue(a2));
		QuestionNum q3 = (QuestionNum) createdKB.getManager().searchQuestion(
				"Num. Mileage evaluation");
		FormulaNumber fn2 = new FormulaNumber(20.0);
		RuleFactory.createSetValueRule(q3, fn2, c2);
	}

	/**
	 * Creates the Heuristic Rules from the DecisionTree
	 */
	private void createDTHeuristicRules() {

		// Create Rule R6:
		// - Driving [mc]
		// -- everything is fine
		// --- Other problem (P7)
		QuestionChoice condQuestion = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Driving");
		Choice answer = KnowledgeBaseUtils.findChoice(condQuestion, "everything is fine");
		CondEqual condition = new CondEqual(condQuestion, new ChoiceValue(
				answer));
		Solution diag = createdKB.getManager().searchSolution("Other problem");
		RuleFactory.createHeuristicPSRule(diag, Score.P7, condition);

	}

	/**
	 * Creates the Set-Value Rules corresponding to the rules of the
	 * Rules-section
	 */
	private void createSetValueRules() {

		// Create first Condition:
		// KNOWN["Real mileage  /100km"]
		QuestionNum q11 = (QuestionNum) createdKB.getManager().searchQuestion(
				"Real mileage  /100km");
		CondKnown c11 = new CondKnown(q11);

		// Create second Condition:
		// "Average mileage /100km" > 0
		QuestionNum q12 = (QuestionNum) createdKB.getManager().searchQuestion(
				"Average mileage /100km");
		CondNumGreater c12 = new CondNumGreater(q12, 0.0);

		// Create AND Condition:
		// "Average mileage /100km" > 0 AND KNOWN["Real mileage  /100km"]
		List<Condition> conditions = new LinkedList<>();
		conditions.add(c11);
		conditions.add(c12);
		CondAnd c1 = new CondAnd(conditions);

		// Create Rule R8:
		// IF "Average mileage /100km" > 0 AND KNOWN["Real mileage  /100km"]
		// THEN "Num. Mileage evaluation" = (("Real mileage  /100km" /
		// "Average mileage /100km") * 100.0)
		QuestionNum q3 = (QuestionNum) createdKB.getManager().searchQuestion(
				"Num. Mileage evaluation");
		Operator d = new Operator(new QNumWrapper(q11), new QNumWrapper(q12),
				Operation.Div);
		FormulaNumber fn = new FormulaNumber(100.0);
		Operator m = new Operator(d, fn, Operation.Mult);
		RuleFactory.createSetValueRule(q3, m, c1);

		// Create Rule R9:
		// IF "Num. Mileage evaluation" > 130
		// THEN Mileage evaluation = increased
		CondNumGreater c2 = new CondNumGreater(q3, 130.0);
		Question q4 = createdKB.getManager().searchQuestion("Mileage evaluation");
		Choice a = KnowledgeBaseUtils.findChoice((QuestionChoice) q4, "increased");
		RuleFactory.createSetValueRule(q4, a, c2);

		// Create rule R10:
		// IF Driving = unsteady idle speed
		// THEN "Real mileage  /100km" += ( 2 )
		QuestionChoice questionIf4 = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Driving");
		Choice answerIf4 = KnowledgeBaseUtils.findChoice(questionIf4,
				"unsteady idle speed");
		CondEqual conditionIf4 = new CondEqual(questionIf4, new ChoiceValue(
				answerIf4));
		QuestionNum qnum = (QuestionNum) createdKB.getManager().searchQuestion(
				"Real mileage  /100km");
		FormulaNumber fn2 = new FormulaNumber(2.0);
		Operator add = new Operator(new QNumWrapper(qnum), fn2, Operation.Add);
		RuleFactory.createSetValueRule(qnum, add, conditionIf4);

		// Create Rule R11:
		// IF Driving = insufficient power on full load
		// THEN "Real mileage  /100km" = ("Average mileage /100km" + 2)
		QuestionChoice questionIf5 = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Driving");
		Choice answerIf5 = KnowledgeBaseUtils.findChoice(questionIf5,
				"insufficient power on full load");
		CondEqual conditionIf5 = new CondEqual(questionIf5, new ChoiceValue(
				answerIf5));
		QuestionNum questionFormula = (QuestionNum) createdKB.getManager().searchQuestion(
				"Average mileage /100km");
		FormulaNumber fn3 = new FormulaNumber(2.0);
		Operator addition = new Operator(new QNumWrapper(questionFormula), fn3,
				Operation.Add);
		QuestionNum questionThen = (QuestionNum) createdKB.getManager().searchQuestion(
				"Real mileage  /100km");
		RuleFactory.createSetValueRule(questionThen, addition, conditionIf5);

		// Create Rule R12:
		// IF Driving = insufficient power on partial load
		// THEN "Real mileage  /100km" = ("Average mileage /100km" - 1)
		QuestionChoice questionIf6 = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Driving");
		Choice answerIf6 = KnowledgeBaseUtils.findChoice(questionIf6,
				"insufficient power on partial load");
		CondEqual conditionIf6 = new CondEqual(questionIf6, new ChoiceValue(
				answerIf6));
		QuestionNum questionFormula2 = (QuestionNum) createdKB.getManager().searchQuestion(
				"Average mileage /100km");
		FormulaNumber fn4 = new FormulaNumber(1.0);
		Operator subtraction = new Operator(new QNumWrapper(questionFormula2), fn4,
				Operation.Sub);
		QuestionNum questionThen2 = (QuestionNum) createdKB.getManager().searchQuestion(
				"Real mileage  /100km");
		RuleFactory.createSetValueRule(questionThen2, subtraction, conditionIf6);

	}

	/**
	 * Creates the Heuristic Rules corresponding to the rules of the
	 * Rules-section
	 */
	private void createHeuristicRules() {

		// Create rule R13:
		// IF Exhaust fumes = black
		// THEN Air filter (P7)
		// EXCEPT Fuel = diesel
		QuestionChoice questionIf = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Exhaust fumes");
		Choice answerIf = KnowledgeBaseUtils.findChoice(questionIf, "black");
		CondEqual conditionIf = new CondEqual(questionIf, new ChoiceValue(
				answerIf));

		QuestionChoice questionExc = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Fuel");
		Choice answerExc = KnowledgeBaseUtils.findChoice(questionExc, "diesel");
		CondEqual conditionExc = new CondEqual(questionExc, new ChoiceValue(
				answerExc));

		Solution diag = createdKB.getManager().searchSolution("Mechanical problem");
		RuleFactory.createHeuristicPSRule(diag, Score.P7, conditionIf, conditionExc);

		// Create rule R14:
		// IF (NOT Fuel = unleaded gasoline) OR (NOT Exhaust fumes = black)
		// THEN Mechanical problem = N7
		QuestionChoice questionIf2 = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Fuel");
		Choice answerIf2 = KnowledgeBaseUtils.findChoice(questionIf2, "unleaded gasoline");
		CondEqual conditionIf2 = new CondEqual(questionIf2, new ChoiceValue(
				answerIf2));
		CondNot condNot1 = new CondNot(conditionIf2);

		QuestionChoice questionIf3 = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Exhaust fumes");
		Choice answerIf3 = KnowledgeBaseUtils.findChoice(questionIf3, "black");
		CondEqual conditionIf3 = new CondEqual(questionIf3, new ChoiceValue(
				answerIf3));
		CondNot condNot2 = new CondNot(conditionIf3);

		List<Condition> conditions = new LinkedList<>();
		conditions.add(condNot2);
		conditions.add(condNot1);
		CondOr condOr = new CondOr(conditions);

		RuleFactory.createHeuristicPSRule(diag, Score.N7, condOr);

	}

	/**
	 * Creates the Indication-Rules
	 */
	private void createIndicationRules() {

		// Create Rule R15:
		// IF Driving = unsteady idle speed
		// THEN Technical Examinations
		QuestionChoice condQuestion = (QuestionChoice) createdKB.getManager().searchQuestion(
				"Driving");
		QASet actionQuestion = createdKB.getManager().searchQContainer(
				"Technical Examinations");
		Choice answer = KnowledgeBaseUtils.findChoice(condQuestion, "unsteady idle speed");
		CondEqual condition = new CondEqual(condQuestion, new ChoiceValue(
				answer));
		RuleFactory.createIndicationRule(actionQuestion, condition);

		// Create Rule R16:
		// IF KNOWN[Other]
		// THEN Technical Examinations
		Question condQuestion3 = createdKB.getManager().searchQuestion("Other");
		QASet actionQuestion4 = createdKB.getManager().searchQContainer(
				"Technical Examinations");
		CondKnown condition3 = new CondKnown(condQuestion3);
		RuleFactory.createIndicationRule(actionQuestion4, condition3);

		// Create Rule R17:
		// IF Other problem = ESTABLISHED
		// THEN Other
		Question question = createdKB.getManager().searchQuestion("Other");
		List<QASet> action = new ArrayList<>();
		action.add(question);
		Solution diag = createdKB.getManager().searchSolution("Other problem");
		CondDState condState = new CondDState(diag,
				new Rating(State.ESTABLISHED));
		ActionNextQASet ruleAction = new ActionIndication();
		ruleAction.setQASets(action);
		RuleFactory.createRule(ruleAction, condState, null, PSMethodStrategic.class);
	}

	/**
	 * Creats a XCLModel similar to the one which is created in the Article
	 */
	private void createXCLModels() {

		Solution d = createdKB.getManager().searchSolution("Damaged idle speed system");

		// "Idle speed system o.k.?" = Yes [--]
		Question q1 = createdKB.getManager().searchQuestion("Idle speed system o.k.?");
		Choice a1 = KnowledgeBaseUtils.findChoice((QuestionChoice) q1, "Yes");
		CondEqual c1 = new CondEqual(q1, new ChoiceValue(a1));
		XCLModel.insertXCLRelation(createdKB, c1, d, XCLRelationType.contradicted);

		// Driving = unsteady idle speed [!]
		Question q2 = createdKB.getManager().searchQuestion("Driving");
		Choice a2 = KnowledgeBaseUtils.findChoice((QuestionChoice) q2,
				"unsteady idle speed");
		CondEqual c2 = new CondEqual(q2, new ChoiceValue(a2));
		XCLModel.insertXCLRelation(createdKB, c2, d, XCLRelationType.requires);

		// "Idle speed system o.k.?" = no [++]
		Question q3 = createdKB.getManager().searchQuestion("Idle speed system o.k.?");
		Choice a3 = KnowledgeBaseUtils.findChoice((QuestionChoice) q3, "No");
		CondEqual c3 = new CondEqual(q3, new ChoiceValue(a3));
		XCLModel.insertXCLRelation(createdKB, c3, d, XCLRelationType.sufficiently);

		// Mileage evaluation = increased [3]
		Question q4 = createdKB.getManager().searchQuestion("Mileage evaluation");
		Choice a4 = KnowledgeBaseUtils.findChoice((QuestionChoice) q4, "increased");
		CondEqual c4 = new CondEqual(q4, new ChoiceValue(a4));
		XCLModel.insertXCLRelation(createdKB, c4, d, XCLRelationType.explains, 3.0);

		// Exhaust fumes = black
		Question q5 = createdKB.getManager().searchQuestion("Exhaust fumes");
		Choice a5 = KnowledgeBaseUtils.findChoice((QuestionChoice) q5, "black");
		CondEqual c5 = new CondEqual(q5, new ChoiceValue(a5));
		XCLModel.insertXCLRelation(createdKB, c5, d, XCLRelationType.explains);

	}
}
