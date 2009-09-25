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
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.Annotation.FindingAnswer;
import de.d3web.we.kdom.Annotation.FindingComparator;
import de.d3web.we.kdom.Annotation.FindingQuestion;

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
	private static AbstractCondition createCondition(Question kbQuest, Answer kbAns, Section comp, Section question, Section answer) {
		// CondEqual(Yes/No), CondNumEqual, CondNumGreater, CondNumGreaterEqual,
		// CondNumLess, CondNumLessEqual
		// Unhandled Conditions: CondKnown, CondNumIn, CondTextContains, CondTextEqual, CondUnknown
		
		// Return if comp is null because no Condition can be created
		if (comp == null)
			return null;
		
		String c = comp.getOriginalText().replaceAll(p.toString(), "").trim();
		String a = answer.getOriginalText().replaceAll(p.toString(), "").trim();
		
		// CondEqual and CondNumEqual have same Comparator
		if (c.equals("=")) {
			if (a.equals("Yes") || a.equals("No") || answerIsString(a))
				return new CondEqual(kbQuest, kbAns);
			return new CondNumEqual((QuestionNum)kbQuest, Double.valueOf(a));
		}
	
		else if (c.equals(">"))
			return new CondNumGreater((QuestionNum)kbQuest, Double.valueOf(a));
		
		else if (c.equals(">="))
			return new CondNumGreaterEqual((QuestionNum)kbQuest, Double.valueOf(a));
			
		else if (c.equals("<"))
			return new CondNumLess((QuestionNum)kbQuest, Double.valueOf(a));
		
		else if (c.equals("<="))
			return new CondNumLessEqual((QuestionNum)kbQuest, Double.valueOf(a));
		
		return null;
	}
	
    /**
     * Tests if text is a Double.
     * 
     * @param text
     * @return
     */
	private static boolean answerIsString(String text) {
		try {
			Double.valueOf(text);
			return false;
		} catch (Exception e) {
			// Do Nothing
		}
		return true;
	}
	
	/**
	 * Delegates the finding of a XCLRelation to its destination method.
	 * 
	 * @param f
	 * @param kbm
	 * @return
	 */
	public static AbstractCondition analyseAnyRelation(Section f, KnowledgeBaseManagement kbm) {

		if (f.findChildOfType(ComplexFinding.class) != null) {
			return FindingToConditionBuilder.analyseComplexFinding(f.findChildOfType(ComplexFinding.class), kbm);
		}
			
		if (f.findChildOfType(Finding.class) != null) {
			return FindingToConditionBuilder.analyseFinding(f.findChildOfType(Finding.class), kbm);
		}

		if (f.findChildOfType(NegatedFinding.class) != null) {
			return FindingToConditionBuilder.analyseNegatedFinding(f.findChildOfType(NegatedFinding.class), kbm);
		}
		
		return null;
	}
	
	/**
	 * Creates a condition from a Finding like this:
	 * Not (Complex)Finding
	 * 
	 * @param findChildOfType
	 * @param kbm
	 * @return
	 */
	public static AbstractCondition analyseNegatedFinding(Section f, KnowledgeBaseManagement kbm) {
		// Structure is like this:
		// Not (Complex)Finding
		List <Section> fl = f.getChildren();
		AbstractCondition c = analyseAnyFinding(fl.get(1), kbm);
		if (c != null)
			return new CondNot(c);
		
		return null;
	}

	/**
	 * Delegates a given finding to its destination method.
	 * 
	 * @param f
	 * @param kbm
	 * @return
	 */
	public static AbstractCondition analyseAnyFinding(Section f, KnowledgeBaseManagement kbm) {
		if (f.getObjectType() instanceof Finding)
			return analyseFinding(f, kbm);
		if (f.getObjectType() instanceof ComplexFinding)
			return analyseComplexFinding(f,kbm);
		if (f.getObjectType() instanceof NegatedFinding)
			return analyseNegatedFinding(f, kbm);
		return null;
	}

	/**
	 * Creates an Condition from a Finding.
	 * 
	 * @param f
	 * @return
	 */
	public static AbstractCondition analyseFinding(Section f, KnowledgeBaseManagement kbm) {
	
		// Get Question Comparator Answer (Who? = You)
		Section comp = f.findChildOfType(FindingComparator.class);
		Section question = f.findChildOfType(FindingQuestion.class);
		Section answer = f.findChildOfType(FindingAnswer.class);
		
		String questiontext = question.getOriginalText().replaceAll(p.toString(), "").toString();
		String answertext = answer.getOriginalText().replaceAll(p.toString(), "").toString();

		// Look up the Question in the KnowledgeBase
		Question kbQuest = kbm.findQuestion(questiontext);
		if (kbQuest != null) {
			// Look up the Answer for the Question
			// Can be null if it is a Numerical Question
			Answer kbAns = kbm.findAnswer(kbQuest, answertext);
			return createCondition(kbQuest, kbAns, comp, question, answer);
		}
				
		return null;
	}

	/**
	 * Creates a Condition from a ComplexFinding
	 * 
	 * @param cf
	 * @param kbm 
	 * @return
	 */
	public static AbstractCondition analyseComplexFinding(Section cf, KnowledgeBaseManagement kbm) {
			
		// Structure is like this:
		// (Complex)Finding NonTerminalCondition (Complex)Finding or like that
		List <Section> cfl = cf.getChildren();
			
		// Check if the CF is constructed with CF
		boolean leftSide = (cfl.get(0).getObjectType() instanceof ComplexFinding);
		boolean rightSide = (cfl.get(2).getObjectType() instanceof ComplexFinding);
			
		// TODO: NOT has to be treated specially
		String c = cfl.get(1).getOriginalText().replaceAll(p.toString(), "").trim();
		ArrayList <AbstractCondition> conds = new ArrayList <AbstractCondition>();
			
		// check if left or right finding is Complex
		if (leftSide)
			conds.add(analyseComplexFinding(cfl.get(0), kbm));
		else
			conds.add(analyseFinding(cfl.get(0), kbm));
			
		if (rightSide)
			conds.add(analyseComplexFinding(cfl.get(2), kbm));
		else
			conds.add(analyseFinding(cfl.get(2), kbm));
			
		// Then create the Condition
		if (c.equals("AND")) {
			return new CondAnd(conds);
		}
							
	//		else if (c.equals(" NOT "))
	//			return new CondNot();
			
		else if (c.equals("OR"))
			return new CondOr(conds);
			
	//		else if (c.equals(" MOFN "))
	//			return new CondMofN();
		return null;
	}
}
