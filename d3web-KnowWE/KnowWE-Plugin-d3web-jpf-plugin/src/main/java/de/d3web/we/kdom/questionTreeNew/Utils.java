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

package de.d3web.we.kdom.questionTreeNew;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNumIn;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.FindingToConditionBuilder;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionDefinition;

public class Utils {

	/**
	 * Creates a condition from a List of DashTreeElements. The List is supposed
	 * to start with a Section containing a QuestionTreeAnswerID, followed by a
	 * Section containing a QuestionID. After that it starts again with a
	 * QuestionTreeAnswerID and so forth. If thats not the case, <tt>null</tt>
	 * will be returned. Such a List of DashTreeElements can be created with
	 * <tt>DashTreeElement.getDashTreeAncestors(Section s)</tt>, if <tt>s</tt>
	 * is the child Section of an answer in a valid DashTree.
	 * @param article TODO
	 */
	public static Condition createCondition(
			KnowWEArticle article, List<Section<? extends DashTreeElement>> ancestors) {

		List<Condition> simpleConds = new ArrayList<Condition>();

		for (int i = 0; i + 2 <= ancestors.size(); i += 2) {
			Condition simpleCond = createSimpleCondition(article, ancestors
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
			KnowWEArticle article,
			Section<? extends DashTreeElement> father,
			Section<? extends DashTreeElement> grandFather) {

		Section<QuestionTreeAnswerDefinition> answerSec = father
				.findSuccessor(QuestionTreeAnswerDefinition.class);
		Section<QuestionDefinition> qSec = grandFather.findSuccessor(QuestionDefinition.class);

		if(qSec == null) {
			return null;
		}
		
		Question q = qSec.get().getTermObject(article, qSec);

		if (answerSec != null && q instanceof QuestionChoice) {
			Choice a = answerSec.get().getTermObject(article, answerSec);
			if (a != null) {
				CondEqual c = new CondEqual(q, new ChoiceValue(
						a));
				return c;
			}
		}

		Section<NumericCondLine> numCondSec = father
				.findSuccessor(NumericCondLine.class);

		if (numCondSec != null && q instanceof QuestionNum) {
			if (NumericCondLine.isIntervall(numCondSec)) {
				NumericalInterval ival = NumericCondLine.getNumericalInterval(numCondSec);
				if (ival != null) return new CondNumIn((QuestionNum) q, ival);
			}
			else {
				Double d = NumericCondLine.getValue(numCondSec);
				if(d == null) return null;
				String comp = NumericCondLine.getComparator(numCondSec);

				if (d != null && comp != null) return FindingToConditionBuilder
						.createCondNum(father.getArticle(), numCondSec, comp, d,
						(QuestionNum) q);;
			}
		}

		return null;

	}

}
