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

import java.util.List;

import de.d3web.core.inference.condition.CondNumEqual;
import de.d3web.core.inference.condition.CondNumGreater;
import de.d3web.core.inference.condition.CondNumGreaterEqual;
import de.d3web.core.inference.condition.CondNumLess;
import de.d3web.core.inference.condition.CondNumLessEqual;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.OneOfStringEnumUnquotedFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.util.SplitUtility;
import de.d3web.we.object.QuestionReference;

/**
 * A type implementing a cond-num TerminalCondition {@link TerminalCondition} It
 * has a allowed list of comparators
 * 
 * syntax: <questionID> <comp> <number> e.g.: mileage evaluation >= 130
 * 
 * @author Jochen
 * 
 */
public class NumericalFinding extends D3webCondition<NumericalFinding> {

	private static String[] comparators = {
			"<=", ">=", "==", "=", "<", ">", };

	@Override
	protected void init() {
		this.setSectionFinder(new NumericalFindingFinder());

		// comparator
		Comparator comparator = new Comparator();
		comparator.setSectionFinder(new OneOfStringEnumUnquotedFinder(comparators));
		this.childrenTypes.add(comparator);

		// question
		QuestionReference question = new QuestionNumReference();
		ConstraintSectionFinder questionFinder = new ConstraintSectionFinder(
				new AllTextFinderTrimmed());
		questionFinder.addConstraint(SingleChildConstraint.getInstance());
		question.setSectionFinder(questionFinder);
		this.childrenTypes.add(question);

		// answer
		Number num = new Number();
		num.setSectionFinder(new AllTextFinderTrimmed());
		this.childrenTypes.add(num);
	}

	class NumericalFindingFinder extends SectionFinder {

		private final AllTextFinderTrimmed textFinder = new AllTextFinderTrimmed();

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
			for (String comp : comparators) {
				if (SplitUtility.containsUnquoted(text, comp)) {

					return textFinder.lookForSections(text,
							father, type);
				}
			}

			return null;
		}

	}

	@Override
	protected Condition createCondition(KnowWEArticle article, Section<NumericalFinding> s) {
		Section<QuestionReference> qRef = s.findSuccessor(QuestionReference.class);

		Section<Number> numberSec = s.findSuccessor(Number.class);

		String comparator = s.findSuccessor(Comparator.class).getOriginalText();

		Double number = numberSec.get().getNumber(numberSec);

		Question q = qRef.get().getTermObject(article, qRef);

		if (!(q instanceof QuestionNum)) {
			// TODO some reasonable error handling here!
			return null;
		}

		if (number != null && q != null && q instanceof QuestionNum) {

			QuestionNum qnum = (QuestionNum) q;

			if (comparator.equals("<=")) {
				return new CondNumLessEqual(qnum, number);
			}
			else if (comparator.equals(">=")) {
				return new CondNumGreaterEqual(qnum, number);
			}
			else if (comparator.equals("<")) {
				return new CondNumLess(qnum, number);
			}
			else if (comparator.equals(">")) {
				return new CondNumGreater(qnum, number);
			}
			else if (comparator.equals("==")) {
				return new CondNumEqual(qnum, number);
			}
			else if (comparator.equals("=")) {
				return new CondNumEqual(qnum, number);
			}

		}
		return null;
	}


}
