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
import de.d3web.we.object.D3webTerm;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
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

		ConstraintSectionFinder questionFinder = new ConstraintSectionFinder(AllTextFinderTrimmed.getInstance());
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

		Section<D3webTerm> qRef = getQuestionSection(section);

		Section<D3webTerm> aRef = getAnswerSection(section);
		if (qRef != null && aRef != null) {
			//noinspection unchecked
			Question question = (Question) qRef.get().getTermObject(compiler, qRef);
			Value value = null;
			if (question instanceof QuestionChoice) {
				//noinspection unchecked
				Choice answer = (Choice) aRef.get().getTermObject(compiler, aRef);
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

	protected Section<D3webTerm> getQuestionSection(Section<Finding> section) {
		return Sections.cast(Sections.child(section, QuestionReference.class), D3webTerm.class);
	}

	protected Section<D3webTerm> getAnswerSection(Section<Finding> section) {
		return Sections.cast(Sections.successor(section, AnswerReference.class), D3webTerm.class);
	}

}
