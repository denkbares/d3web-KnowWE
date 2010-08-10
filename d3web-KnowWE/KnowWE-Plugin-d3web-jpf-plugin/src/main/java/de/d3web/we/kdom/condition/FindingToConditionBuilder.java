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

package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.CondOr;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.report.Message;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.Annotation.FindingAnswer;
import de.d3web.we.kdom.Annotation.FindingComparator;
import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.filter.TypeSectionFilter;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Supplies a method for creating a condition from a (Complex)Finding
 * 
 * @author Johannes Dienst
 * 
 */
public class FindingToConditionBuilder {

	private static Pattern p = Pattern.compile("\"");

	/**
	 * Creates the right Condition according to the given Comparator.
	 * 
	 * @param kbQuest
	 * @param kbAns
	 * @param comp
	 * @param question
	 * @param answer
	 * @return
	 */
	private static Condition createCondition(KnowWEArticle article, Question kbQuest, Value kbAns, Section comp, Section question, Section answer) {
		// CondEqual(Yes/No), CondNumEqual, CondNumGreater, CondNumGreaterEqual,
		// CondNumLess, CondNumLessEqual
		// Unhandled Conditions: CondKnown, CondNumIn, CondTextContains,
		// CondTextEqual, CondUnknown

		// Return if comp is null because no Condition can be created
		if (comp == null) return null;

		String comparator = comp.getOriginalText().replaceAll(p.toString(), "").trim();
		String answerText = answer.getOriginalText().replaceAll(p.toString(), "").trim();

		// CondEqual and CondNumEqual have same Comparator

		if (kbQuest instanceof QuestionNum) {
			Double valueOf = new Double(-1);
			try {
				valueOf = Double.valueOf(answerText);
			}
			catch (NumberFormatException e) {
				KnowWEUtils.storeSingleMessage(article, answer,
						FindingToConditionBuilder.class, Message.class, new Message(
						"Numerical value expected, got: '" + answerText + "'."));
			}

			QuestionNum questionNum = (QuestionNum) kbQuest;
			return createCondNum(article, comp, comparator, valueOf,
					questionNum);
		}
		else {
			if (comparator.equals("=")) return new CondEqual(kbQuest, kbAns);
			else {
				KnowWEUtils.storeSingleMessage(article, comp,
						FindingToConditionBuilder.class, Message.class, new Message(
						"Unkown comparator '" + comparator + "'."));
				return null;
			}
		}

	}

	public static Condition createCondNum(KnowWEArticle article,
			Section comp, String comparator, Double valueOf,
			QuestionNum questionNum) {
		KnowWEUtils.clearMessages(article, comp, FindingToConditionBuilder.class, Message.class);

		if (comparator.equals("=")) return new CondNumEqual(questionNum, valueOf);
		else if (comparator.equals(">")) return new CondNumGreater(questionNum, valueOf);
		else if (comparator.equals(">=")) return new CondNumGreaterEqual(questionNum, valueOf);
		else if (comparator.equals("<")) return new CondNumLess(questionNum, valueOf);
		else if (comparator.equals("<=")) return new CondNumLessEqual(questionNum, valueOf);
		else {
			KnowWEUtils.storeSingleMessage(article, comp,
					FindingToConditionBuilder.class, Message.class, new Message(
							"Unkown comparator '" + comparator + "'."));
			return null;
		}
	}

	// private static Double parseNumAnswer(KnowWEArticle article, String value,
	// Section answer) {
	// try {
	// return Double.valueOf(value);
	// } catch (NumberFormatException e) {
	// storeMessage(article, "Numerical value expected, got: '" + value +"'.",
	// answer);
	// return new Double(-1);
	// }
	//		
	// }

	/**
	 * Delegates the finding of a XCLRelation to its destination method.
	 * 
	 * @param f
	 * @param kbm
	 * @return
	 */
	public static Condition analyseAnyRelation(KnowWEArticle article, Section f, KnowledgeBaseManagement kbm) {

		Section child = f.findChildOfType(ComplexFinding.class);
		if (child != null) {
			return FindingToConditionBuilder.analyseComplexFinding(article, child, kbm);
		}
		else return null;
	}

	/**
	 * Creates an Condition from a Finding.
	 * 
	 * @param f
	 * @return s null if the question was not found by KBM, a condition
	 *         otherwise.
	 */
	private static Condition analyseFinding(KnowWEArticle article, Section f, KnowledgeBaseManagement kbm) {

		if (!f.getObjectType().getClass().equals(Finding.class)) return null;

		boolean negated = f.findChildOfType(NOT.class) != null;

		// Get Question Comparator Answer (Who? = You)
		Section comp = f.findChildOfType(FindingComparator.class);
		Section question = f.findSuccessor(FindingQuestion.class);
		Section answer = f.findSuccessor(FindingAnswer.class);

		// clean out all old Messages from a previous run of this builder...
		if(comp != null) KnowWEUtils.clearMessages(article, comp, FindingToConditionBuilder.class, Message.class);
		if(question != null) KnowWEUtils.clearMessages(article, question, FindingToConditionBuilder.class, Message.class);
		if(answer != null) KnowWEUtils.clearMessages(article, answer, FindingToConditionBuilder.class, Message.class);

		String questiontext = question.getOriginalText().replaceAll(p.toString(), "").trim();
		if (answer == null) {
			KnowWEUtils.storeSingleMessage(article, f,
					FindingToConditionBuilder.class, Message.class, new Message(
					"No answer-section found for finding: '"
					+ f.getOriginalText() + "'."));
			return null;
		}
		String answertext = answer.getOriginalText().replaceAll(p.toString(), "").trim();

		// Look up the Question in the KnowledgeBase
		Question kbQuest = kbm.findQuestion(questiontext);
		if (kbQuest != null) {
			// Look up the Answer for the Question
			// Can be null if it is a Numerical Question
			Value kbValue = null;
			if (Unknown.getInstance().getId().equals(answertext)) {
				kbValue = Unknown.getInstance();
			}
			if (kbQuest instanceof QuestionChoice) {
				Choice choice = kbm.findChoice((QuestionChoice) kbQuest, answertext);
				if (choice != null) {
					kbValue = new ChoiceValue(choice);
				}
				else {
					KnowWEUtils.storeSingleMessage(article, answer,
							FindingToConditionBuilder.class, Message.class, new Message(
							"Not a valid answer for question: '" + answertext + "'."));
					return null;
				}
			}
			else if (kbQuest instanceof QuestionNum) {
				kbValue = new NumValue(Double.valueOf(answertext));
			}
			else if (kbQuest instanceof QuestionText) {
				kbValue = new TextValue(answertext);
			}
			else if (kbQuest instanceof QuestionDate) {
				kbValue = new DateValue(new Date(answertext));
			}
			else {
				kbValue = UndefinedValue.getInstance();
			}

			// TODO: errors when answer is not found for certain question types
			// (YN, OC)

			Condition condition = createCondition(article, kbQuest, kbValue, comp,
					question, answer);
			return negated ? new CondNot(condition) : condition;

		}
		else {
			KnowWEUtils.storeSingleMessage(article, question,
					FindingToConditionBuilder.class, Message.class, new Message("Question '"
					+ question.getOriginalText() + "' not found."));
			KnowWEUtils.storeSingleMessage(article, answer,
					FindingToConditionBuilder.class, Message.class, new Message("Question '"
					+ question.getOriginalText() + "' not found."));
			return null;

		}

	}

	// private static void storeMessage(KnowWEArticle article, String
	// messageText, Section section) {
	//		
	// Message message = new Message(messageText);
	//		
	// List<Message> messages = new ArrayList<Message>(1);
	// messages.add(message);
	//		
	// AbstractKnowWEObjectType.storeMessages(article, section, messages);
	//		
	//
	//		
	// }

	/**
	 * Creates a Condition from a ComplexFinding Removes every sub-condition
	 * that could not be parsed.
	 * 
	 * @param cf
	 * @param kbm
	 * @return s the according Condition or null if neither side could be parsed
	 */
	private static Condition analyseComplexFinding(KnowWEArticle article, Section cf, KnowledgeBaseManagement kbm) {

		TypeSectionFilter filter = new TypeSectionFilter("Disjunct");
		return analyseDisjunction(article, cf.getChildren(filter), kbm);

	}

	private static Condition analyseDisjunction(KnowWEArticle article, List<Section> disjunction, KnowledgeBaseManagement kbm) {

		List<Condition> disjuncts = new ArrayList<Condition>();
		TypeSectionFilter filter = new TypeSectionFilter("Conjunct");

		for (Section child : disjunction) {
			Condition conjunction = analyzeConjunction(article, child.getChildren(filter), kbm);
			if (conjunction != null) disjuncts.add(conjunction);
		}

		if (disjuncts.isEmpty()) return null;
		else if (disjuncts.size() == 1) return disjuncts.get(0); // No OR for
		// single
		// argument
		else return new CondOr(disjuncts);

	}

	private static Condition analyzeConjunction(KnowWEArticle article, List<Section> conjunction,
			KnowledgeBaseManagement kbm) {

		List<Condition> conjuncts = new ArrayList<Condition>();

		for (Section section : conjunction) {
			Condition condition = analyseFinding(article, (Section) section.getChildren().get(0),
					kbm);
			if (condition != null) conjuncts.add(condition);
		}

		if (conjuncts.isEmpty()) return null;
		else if (conjuncts.size() == 1) // no AND for single argument
		return conjuncts.get(0);
		else return new CondAnd(conjuncts);

	}

}
