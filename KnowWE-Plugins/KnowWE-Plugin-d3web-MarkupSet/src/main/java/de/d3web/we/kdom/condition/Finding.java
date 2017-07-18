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

import com.denkbares.strings.StringFragment;
import com.denkbares.strings.Strings;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNot;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * A type implementing simple choice condition as child-type of
 * TerminalCondition {@link TerminalCondition}
 * <p/>
 * syntax: <questionID> = <answerID>
 *
 * @author Jochen
 */
public class Finding extends D3webCondition<Finding> {

	private static final String NOT_EQUAL = "notEqual";
	private static final String EQUAL = "equal";

	public Finding() {

		this.setSectionFinder(new FindingFinder());

		// not equal
		AnonymousType unEquals = new AnonymousType(NOT_EQUAL);
		unEquals.setSectionFinder(new StringSectionFinderUnquoted("!="));
		this.addChildType(unEquals);

		// equal
		AnonymousType equals = new AnonymousType(EQUAL);
		equals.setSectionFinder(new StringSectionFinderUnquoted("="));
		this.addChildType(equals);

		// question
		QuestionReference question = new QuestionReference();

		ConstraintSectionFinder questionFinder = new ConstraintSectionFinder(
				new AllTextFinderTrimmed());
		questionFinder.addConstraint(SingleChildConstraint.getInstance());
		question.setSectionFinder(questionFinder);
		this.addChildType(question);

		// answer
		AnswerReference answer = new FindingAnswerReference();
		answer.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(answer);
	}

	@Override
	protected Condition createCondition(D3webCompiler compiler, Section<Finding> section) {

		Section<QuestionReference> qRef = Sections.child(section,
				QuestionReference.class);

		Section<AnswerReference> aRef = Sections.successor(section, AnswerReference.class);
		if (qRef != null && aRef != null) {
			Question question = qRef.get().getTermObject(compiler, qRef);
			Value value = null;
			if (question instanceof QuestionChoice) {
				Choice answer = aRef.get().getTermObject(compiler, aRef);
				if (answer == null) {
					return null;
				}
				else {
					value = new ChoiceValue(answer);
				}
			}
			else if (question instanceof QuestionText) {
				value = new TextValue(Strings.trimQuotes(aRef.getText()));
			}
			else if (question instanceof QuestionDate) {
				try {
					value = ValueUtils.createDateValue((QuestionDate) question, Strings.trimQuotes(aRef.getText()));
				}
				catch (IllegalArgumentException e) {
					return null;
				}
			}
			if (question == null) {
				return null;
			}
			if (value != null) {
				Section<AnonymousType> operator = Sections.child(section, AnonymousType.class);
				CondEqual condEqual = new CondEqual(question, value);
				if (operator != null && operator.get().getName().equals(NOT_EQUAL)) {
					return new CondNot(condEqual);
				}
				else {
					return condEqual;
				}
			}
		}
		return null;
	}

}

class FindingFinder implements SectionFinder {

	private final AllTextFinderTrimmed textFinder = new AllTextFinderTrimmed();

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		if (Strings.containsUnquoted(text, "=")) {

			// if the value is a number this is not taken as a Finding (but left
			// for NumericalFinding)
			List<StringFragment> list = Strings.splitUnquoted(text, "=");
			// Hotfix for AOB when there is nothing behind the "="
			if (list.size() < 2) return null;
			StringFragment answer = list.get(1);
			boolean isNumber = false;
			try {
				//noinspection ResultOfMethodCallIgnored
				Double.parseDouble(answer.getContent().trim());
				isNumber = true;
			}
			catch (NumberFormatException ignored) {
			}
			// return it if answer is NOT a number
			if (!isNumber) {
				return textFinder.lookForSections(text, father, type);
			}
		}
		return null;
	}

}
