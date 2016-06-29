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
package de.d3web.we.kdom.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.strings.Strings;
import de.d3web.we.kdom.auxiliary.Equals;
import de.d3web.we.kdom.condition.FindingAnswerReference;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.UnknownValueType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;

/**
 * @author Johannes Dienst
 */
public class SetQuestionValue extends D3webRuleAction<SetQuestionValue> {

	public SetQuestionValue() {
		this.setSectionFinder(new SetQuestionValueSectionFinder());
		Equals equals = new Equals();
		QuestionReference qr = new QuestionReference();
		qr.setSectionFinder(new AllBeforeTypeSectionFinder(equals));
		this.addChildType(equals);
		this.addChildType(qr);

		this.addChildType(new UnknownValueType());
		AnswerReference a = new FindingAnswerReference();
		a.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(a);

	}

	/**
	 * This works because it is no DiagnosisRuleAction.
	 */
	private class SetQuestionValueSectionFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

			if (Strings.containsUnquoted(text, " =")) {

				List<SectionFinderResult> result = new ArrayList<>();
				result.add(new SectionFinderResult(0, text.length()));
				return result;
			}

			return null;
		}
	}

	@Override
	public PSAction createAction(D3webCompiler compiler, Section<SetQuestionValue> section) throws CompilerMessage {

		Object value;
		Section<AnswerReference> answerReferenceSection = null;

		if (Sections.successor(section, UnknownValueType.class) != null) {
			value = Unknown.getInstance();
		}
		else {
			answerReferenceSection = Sections.successor(section,
					AnswerReference.class);
			if (answerReferenceSection == null) return null;
			value = answerReferenceSection.get().getTermObject(compiler, answerReferenceSection);
		}

		Section<QuestionReference> questionReferenceSection = Sections.successor(section, QuestionReference.class);
		Question question = questionReferenceSection.get().getTermObject(compiler, questionReferenceSection);
		if (question instanceof QuestionDate && answerReferenceSection != null) {
			ActionSetQuestion actionSetQuestion = new ActionSetQuestion();
			actionSetQuestion.setQuestion(question);
			String text = answerReferenceSection.getText();
			try {
				DateValue dateValue = ValueUtils.createDateValue((QuestionDate) question, text);
				actionSetQuestion.setValue(dateValue);
			}
			catch (IllegalArgumentException e) {
				return null;
			}
			return actionSetQuestion;
		}
		if (question instanceof QuestionText && answerReferenceSection != null) {
			ActionSetQuestion actionSetQuestion = new ActionSetQuestion();
			actionSetQuestion.setQuestion(question);
			String text = answerReferenceSection.getText();
			TextValue textValue = new TextValue(Strings.unquote(text));
			actionSetQuestion.setValue(textValue);
			return actionSetQuestion;
		}
		if (question != null && value != null) {
			ActionSetQuestion actionSetQuestion = new ActionSetQuestion();
			actionSetQuestion.setQuestion(question);
			actionSetQuestion.setValue(value);
			return actionSetQuestion;
		}
		else {
			//throw CompilerMessage.error("Unable create action from " + section.getText());
			return null;
		}
	}

	@Override
	public Class<? extends PSMethod> getProblemSolverContext() {
		return PSMethodAbstraction.class;
	}
}
