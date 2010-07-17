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

package de.d3web.we.kdom.rulesNew.terminalCondition;

import java.util.List;

import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.session.values.Choice;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.objects.AnswerRef;
import de.d3web.we.kdom.objects.AnswerRefImpl;
import de.d3web.we.kdom.objects.QuestionRef;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

/**
 * A type implementing simple choice condition as child-type of
 * TerminalCondition {@link TerminalCondition}
 *
 * syntax: <questionID> = <answerID>
 *
 *
 * @author Jochen
 *
 */
public class Finding extends D3webTerminalCondition<Finding> {

	@Override
	protected void init() {

		this.sectionFinder = new FindingFinder();

		// comparator
		Comparator comparator = new Comparator();
		comparator.setSectionFinder(new RegexSectionFinder("="));
		this.childrenTypes.add(comparator);

		// question
		QuestionRef question = new QuestionRef();
		AllTextFinderTrimmed questionFinder = new AllTextFinderTrimmed();
		questionFinder.addConstraint(SingleChildConstraint.getInstance());
		question.setSectionFinder(questionFinder);
		this.childrenTypes.add(question);

		// answer
		AnswerRef answer = new AnswerRefImpl();
		answer.setSectionFinder(new AllTextFinderTrimmed());
		this.childrenTypes.add(answer);
	}

	@Override
	public TerminalCondition getTerminalCondition(KnowWEArticle article, Section<Finding> s) {

		Section<QuestionRef> qRef = s.findSuccessor(QuestionRef.class);

		Section<AnswerRef> aRef = s.findSuccessor(AnswerRef.class);

		if (qRef != null && aRef != null) {
			Choice answer = aRef.get().getObject(article, aRef);
			if (answer == null) {
				return null;
			}
			ChoiceValue value = new ChoiceValue(
					answer);
			return new CondEqual(qRef.get().getObject(article, qRef), value);
		}

		return null;
	}

}

class FindingFinder extends SectionFinder {

	private final AllTextFinderTrimmed textFinder = new AllTextFinderTrimmed();

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		if (SplitUtility.containsUnquoted(text, "=")) {

			// if the value is a number this is not taken as a Finding (but left
			// for NumericalFinding)
			List<String> list = SplitUtility.splitUnquoted(text, "=");
			String answer = list.get(1);
			boolean isNumber = false;
			try {
				Double d = Double.parseDouble(answer);
				if (answer.contains("d") || answer.contains("D")) {
					// TODO find better way to check
					// '5D' is parsed to a valid double '5.0'
				}
				else {
					isNumber = true;
				}
			}
			catch (NumberFormatException e) {
			}
			// return it if answer is NOT a number
			if (!isNumber) {
				return textFinder.lookForSections(text, father);
			}
		}
		return null;
	}

}

class Comparator extends DefaultAbstractKnowWEObjectType {

}
