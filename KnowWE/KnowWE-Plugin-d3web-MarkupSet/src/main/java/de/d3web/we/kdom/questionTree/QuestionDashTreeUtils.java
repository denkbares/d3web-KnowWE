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

package de.d3web.we.kdom.questionTree;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QASetDefinition;
import de.d3web.we.object.QuestionDefinition;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.dashtree.DashSubtree;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.kdom.dashtree.DashTreeUtils;

public class QuestionDashTreeUtils {

	/**
	 * Creates a condition from a List of DashTreeElements. The List is supposed
	 * to start with a Section containing a QuestionTreeAnswerID, followed by a
	 * Section containing a QuestionID. After that it starts again with a
	 * QuestionTreeAnswerID and so forth. If thats not the case, <tt>null</tt>
	 * will be returned. Such a List of DashTreeElements can be created with
	 * <tt>DashTreeElement.getDashTreeAncestors(Section s)</tt>, if <tt>s</tt>
	 * is the child Section of an answer in a valid DashTree.
	 * 
	 */
	public static Condition createCondition(
			D3webCompiler compiler, List<Section<? extends DashTreeElement>> ancestors) {

		List<Condition> simpleConds = new ArrayList<Condition>();

		for (int i = 0; i + 2 <= ancestors.size(); i += 2) {
			Condition simpleCond = createSimpleCondition(compiler, ancestors
					.get(i), ancestors.get(i + 1));
			if (simpleCond != null) {
				simpleConds.add(simpleCond);
			}
		}

		if (simpleConds.isEmpty()) {
			return null;
		}
		else if (simpleConds.size() == 1) {
			return simpleConds.get(0);
		}
		else {
			return new CondAnd(simpleConds);
		}

	}

	/**
	 * Creates a condition from the two DashTreeElements father an grandfather.
	 * Father is supposed to contain a QuestionTreeAnswerID, the grandfather a
	 * QuestionID. If thats not the case, <tt>null</tt> will be returned.
	 */
	public static Condition createSimpleCondition(
			D3webCompiler compiler,
			Section<? extends DashTreeElement> father,
			Section<? extends DashTreeElement> grandFather) {

		if (father.hasErrorInSubtree(compiler) || grandFather.hasErrorInSubtree(compiler)) {
			return null;
		}

		Section<QuestionTreeAnswerDefinition> answerSec =
				Sections.successor(father, QuestionTreeAnswerDefinition.class);
		Section<QuestionDefinition> qSec = Sections.successor(grandFather,
				QuestionDefinition.class);

		if (qSec == null) {
			return null;
		}

		Question q = qSec.get().getTermObject(compiler, qSec);

		if (answerSec != null && q instanceof QuestionChoice) {
			Choice a = answerSec.get().getTermObject(compiler, answerSec);
			if (a != null) {
				CondEqual c = new CondEqual(q, new ChoiceValue(
						a));
				return c;
			}
		}

		Section<NumericCondLine> numCondSec =
				Sections.successor(father, NumericCondLine.class);

		if (numCondSec != null && q instanceof QuestionNum) {
			if (NumericCondLine.isIntervall(numCondSec)) {
				NumericalInterval ival = NumericCondLine.getNumericalInterval(numCondSec);
				if (ival != null) return new CondNumIn((QuestionNum) q, ival);
			}
			else {
				Double d = NumericCondLine.getValue(numCondSec);
				if (d == null) return null;
				String comp = NumericCondLine.getComparator(numCondSec);

				if (d != null && comp != null) return createCondNum(compiler,
						numCondSec, comp, d,
						(QuestionNum) q);
				;
			}
		}

		return null;

	}

	private static Condition createCondNum(PackageCompiler compiler,
			Section<NumericCondLine> comp, String comparator, Double valueOf,
			QuestionNum questionNum) {
		Messages.clearMessages(compiler, comp, QuestionDashTreeUtils.class);

		if (comparator.equals("=")) return new CondNumEqual(questionNum, valueOf);
		else if (comparator.equals(">")) return new CondNumGreater(questionNum, valueOf);
		else if (comparator.equals(">=")) return new CondNumGreaterEqual(questionNum,
				valueOf);
		else if (comparator.equals("<")) return new CondNumLess(questionNum, valueOf);
		else if (comparator.equals("<=")) return new CondNumLessEqual(questionNum,
				valueOf);
		else {
			Messages.storeMessage(compiler, comp,
					QuestionDashTreeUtils.class,
					Messages.error("Unkown comparator '" + comparator + "'."));
			return null;
		}
	}

	/**
	 * Checks if the Subtree of the root Question has changed. Ignores
	 * TermReferences!
	 */
	@SuppressWarnings("unchecked")
	public static Section<DashSubtree> getRootQuestionSubtree(Article article, Section<?> s) {

		Section<DashSubtree> rootQuestionSubtree = null;

		Section<DashTreeElement> thisElement = Sections.ancestor(s,
				DashTreeElement.class);
		if (s.get() instanceof QuestionDefinition
				&& DashTreeUtils.getDashLevel(thisElement) == 0) {
			rootQuestionSubtree = Sections.ancestor(thisElement,
					DashSubtree.class);
		}

		if (rootQuestionSubtree == null) {
			Section<DashSubtree> lvl1SubtreeAncestor = DashTreeUtils.getAncestorDashSubtree(
					s, 1);
			if (lvl1SubtreeAncestor != null) {
				Section<DashTreeElement> lvl1Element = Sections.child(
						lvl1SubtreeAncestor, DashTreeElement.class);
				Section<? extends Term> termRefSection = Sections.successor(
						lvl1Element,
						Term.class);

				if (termRefSection.get() instanceof QASetDefinition) {
					rootQuestionSubtree = lvl1SubtreeAncestor;
				}
				else {
					rootQuestionSubtree = (Section<DashSubtree>) lvl1SubtreeAncestor.getParent();
				}
			}
		}

		return rootQuestionSubtree;
	}

}
