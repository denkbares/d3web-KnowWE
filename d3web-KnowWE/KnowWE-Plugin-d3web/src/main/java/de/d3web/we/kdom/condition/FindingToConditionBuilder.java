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

package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondAnd;
import de.d3web.kernel.domainModel.ruleCondition.CondEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNot;
import de.d3web.kernel.domainModel.ruleCondition.CondNumEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreater;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreaterEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLess;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLessEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondOr;
import de.d3web.report.Message;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.Annotation.FindingAnswer;
import de.d3web.we.kdom.Annotation.FindingComparator;
import de.d3web.we.kdom.Annotation.FindingQuestion;
import de.d3web.we.kdom.filter.TypeSectionFilter;

/**
 * Supplies a method for creating a condition from a
 * (Complex)Finding
 * 
 * @author Johannes Dienst
 *
 */
public class FindingToConditionBuilder {

	private static Pattern p = Pattern.compile("\"");

	 /** Creates the right Condition according to the given Comparator.
	 * 
	 * @param kbQuest
	 * @param kbAns
	 * @param comp
	 * @param question
	 * @param answer
	 * @return
	 */
	private static AbstractCondition createCondition(KnowWEArticle article, Question kbQuest, Answer kbAns, Section comp, Section question, Section answer) {
		// CondEqual(Yes/No), CondNumEqual, CondNumGreater, CondNumGreaterEqual,
		// CondNumLess, CondNumLessEqual
		// Unhandled Conditions: CondKnown, CondNumIn, CondTextContains, CondTextEqual, CondUnknown
		
		// Return if comp is null because no Condition can be created
		if (comp == null)
			return null;
		
		String comparator = comp.getOriginalText().replaceAll(p.toString(), "").trim();
		String answerText = answer.getOriginalText().replaceAll(p.toString(), "").trim();
		
		// CondEqual and CondNumEqual have same Comparator
		
		if (kbQuest instanceof QuestionNum) {

			Double valueOf = parseNumAnswer(article, answerText, answer);
			
			QuestionNum questionNum = (QuestionNum) kbQuest;
			
			return createCondNum(article, comp, comparator, valueOf,
					questionNum);
		} else {
			if (comparator.equals("="))
				return new CondEqual(kbQuest, kbAns);
			else { 
				storeMessage(article, "Unkown comparator '" + comparator +"'.", comp);
				return null;
			}
		}
	
		
	}

	public static AbstractCondition createCondNum(KnowWEArticle article,
			Section comp, String comparator, Double valueOf,
			QuestionNum questionNum) {
		if (comparator.equals("="))
			return new CondNumEqual(questionNum, valueOf);
		else if (comparator.equals(">"))
			return new CondNumGreater(questionNum, valueOf);
		else if (comparator.equals(">="))
			return new CondNumGreaterEqual(questionNum, valueOf);
		else if (comparator.equals("<"))
			return new CondNumLess(questionNum, valueOf);
		else if (comparator.equals("<="))
			return new CondNumLessEqual(questionNum, valueOf);
		else {
			storeMessage(article, "Unkown comparator '" + comparator +"'.", comp);
			return null;
		}
	}

	private static Double parseNumAnswer(KnowWEArticle article, String value, Section answer) {
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			storeMessage(article, "Numerical value expected, got: '" + value +"'.", answer);
			return new Double(-1);
		}
		
	}
	
	
	/**
	 * Delegates the finding of a XCLRelation to its destination method.
	 * 
	 * @param f
	 * @param kbm
	 * @return
	 */
	public static AbstractCondition analyseAnyRelation(KnowWEArticle article, Section f, KnowledgeBaseManagement kbm) {

		Section child = f.findChildOfType(ComplexFinding.class);
		if (child != null) {
			return FindingToConditionBuilder.analyseComplexFinding(article, child, kbm);
		} else 
			return null;
	}
	


	/**
	 * Creates an Condition from a Finding.
	 * 
	 * @param f
	 * @return s null if the question was not found by KBM, a condition otherwise.
	 */
	private static AbstractCondition analyseFinding(KnowWEArticle article, Section f, KnowledgeBaseManagement kbm) {
	
		if (!f.getObjectType().getClass().equals(Finding.class))
			return null;
	
		
		boolean negated = f.findChildOfType(NOT.class) != null;
		
		
		// Get Question Comparator Answer (Who? = You)
		Section comp = f.findChildOfType(FindingComparator.class);
		Section question = f.findSuccessor(FindingQuestion.class);
		Section answer = f.findSuccessor(FindingAnswer.class);

		storeMessage(article, "", question);
		storeMessage(article, "", answer);
		
		String questiontext = question.getOriginalText().replaceAll(p.toString(), "").trim();
		if (answer == null) {
			storeMessage(article, "No answer-section found for finding: '" + f.getOriginalText() + "'.", f);
			return null;
		}
		String answertext = answer.getOriginalText().replaceAll(p.toString(), "").trim();

		// Look up the Question in the KnowledgeBase
		Question kbQuest = kbm.findQuestion(questiontext);
		if (kbQuest != null) {
			// Look up the Answer for the Question
			// Can be null if it is a Numerical Question
			Answer kbAns = kbm.findAnswer(kbQuest, answertext);
			
			//TODO: errors when answer is not found for certain question types (YN, OC)
			
			if (kbQuest instanceof QuestionOC && kbAns == null) {
				storeMessage(article, "Not a valid answer for question: '" + answertext + "'.", answer);
				return null;
			}
			
			AbstractCondition condition = createCondition(article, kbQuest, kbAns, comp, question, answer);
			return negated ? new CondNot(condition) : condition;
		} else {
			storeMessage(article, "Question '" + question.getOriginalText() +"' not found.", question);
			storeMessage(article, "Question '" + question.getOriginalText() +"' not found.", answer);
			return null;
			
		}
		
		
	}

	private static void storeMessage(KnowWEArticle article, String messageText, Section section) {
		
		Message message = new Message(messageText);
		
		List<Message> messages = new ArrayList<Message>(1);
		messages.add(message);
		
		((AbstractKnowWEObjectType) section.getObjectType()).storeMessages(article, section, messages);
		

		
	}

	/**
	 * Creates a Condition from a ComplexFinding
	 * Removes every sub-condition that could not be parsed.  
	 * 
	 * @param cf
	 * @param kbm 
	 * @return s the according Condition or null if neither side could be parsed
	 */
	private static AbstractCondition analyseComplexFinding(KnowWEArticle article, Section cf, KnowledgeBaseManagement kbm) {
			
		
		TypeSectionFilter filter = new TypeSectionFilter("Disjunct");
		return analyseDisjunction(article, cf.getChildren(filter), kbm);
		
	}
	
	
	private static AbstractCondition analyseDisjunction(KnowWEArticle article, List<Section> disjunction, KnowledgeBaseManagement kbm) {
		
		List<AbstractCondition> disjuncts = new ArrayList<AbstractCondition>();
		TypeSectionFilter filter = new TypeSectionFilter("Conjunct");
		

		for (Section child : disjunction) {
			AbstractCondition conjunction = analyzeConjunction(article, child.getChildren(filter), kbm);
			if (conjunction != null)
				disjuncts.add(conjunction);
		}
		
		if (disjuncts.isEmpty())
			return null;
		else if (disjuncts.size() == 1)
			return disjuncts.get(0); // No OR for single argument
		else
			return new CondOr(disjuncts);
		
	}
	

	private static AbstractCondition analyzeConjunction(KnowWEArticle article, List<Section> conjunction,
			KnowledgeBaseManagement kbm) {
		
		List<AbstractCondition> conjuncts = new ArrayList<AbstractCondition>();
		
		for (Section section : conjunction) {
			AbstractCondition condition = analyseFinding(article, (Section) section.getChildren().get(0), kbm);
			if (condition != null)
				conjuncts.add(condition);
		}
		
		if (conjuncts.isEmpty())
			return null;
		else if (conjuncts.size() == 1) //no AND for single argument
			return conjuncts.get(0); 
		else
			return new CondAnd(conjuncts);
		
	}
	
	
	
}
