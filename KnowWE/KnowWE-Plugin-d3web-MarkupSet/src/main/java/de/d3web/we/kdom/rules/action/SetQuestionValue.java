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
package de.d3web.we.kdom.rules.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.values.Unknown;
import de.d3web.strings.Strings;
import de.d3web.we.kdom.auxiliary.Equals;
import de.d3web.we.kdom.condition.AnswerReferenceImpl;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.UnknownValueType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;

/**
 * @author Johannes Dienst
 * 
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
		AnswerReference a = new AnswerReferenceImpl();
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

				List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
				result.add(new SectionFinderResult(0, text.length()));
				return result;
			}

			return null;
		}
	}

	@Override
	public PSAction createAction(Article article, Section<SetQuestionValue> s) {

		Object value;

		if (Sections.findSuccessor(s, UnknownValueType.class) != null) {
			value = Unknown.getInstance();
		}
		else {
			Section<AnswerReference> aref = Sections.findSuccessor(s,
					AnswerReference.class);
			if (aref == null) return null;
			value = aref.get().getTermObject(article, aref);
		}

		Section<QuestionReference> qref = Sections.findSuccessor(s, QuestionReference.class);
		Question q = qref.get().getTermObject(article, qref);
		if (q != null && value != null) {
			ActionSetQuestion a = new ActionSetQuestion();
			a.setQuestion(q);
			a.setValue(value);
			return a;
		}
		else {
			return null;
		}
	}

	@Override
	public Class<? extends PSMethod> getActionPSContext() {
		return PSMethodAbstraction.class;
	}
}
