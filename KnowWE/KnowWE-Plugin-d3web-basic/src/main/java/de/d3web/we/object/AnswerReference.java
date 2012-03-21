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

package de.d3web.we.object;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.objects.SimpleTermReferenceRegistrationHandler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 * 
 *          This is the type to be used in markup for referencing (d3web-)
 *          Choice-Answers. It checks whether the referenced object is existing.
 *          In case it creates the Answer object in the knowledge base.
 * 
 */
public abstract class AnswerReference
		extends D3webTermReference<Choice> {

	public AnswerReference() {
		this.setRenderer(StyleRenderer.CHOICE);
		this.addSubtreeHandler(new AnswerReferenceRegistrationHandler());
	}

	@Override
	@SuppressWarnings(value = { "unchecked" })
	public String getTermIdentifier(Section<? extends SimpleTerm> s) {
		// here we should return a unique identifier including the question name
		// as namespace

		if (s.get() instanceof AnswerReference) {
			Section<AnswerReference> sec = ((Section<AnswerReference>) s);
			Section<QuestionReference> questionSection = sec.get().getQuestionSection(sec);
			String question = questionSection.get().getTermIdentifier(questionSection);

			return AnswerDefinition.createAnswerIdentifierForQuestion(getAnswerName(sec),
					question);
		}

		// should not happen
		return getAnswerName((Section<? extends AnswerReference>) s);
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends SimpleTerm> section) {
		return Choice.class;
	}

	public String getAnswerName(Section<? extends AnswerReference> answerReference) {
		return KnowWEUtils.trimQuotes(answerReference.getText());
	}

	@Override
	public Choice getTermObject(Article article, Section<? extends D3webTerm<Choice>> s) {

		if (s.get() instanceof AnswerReference) {
			@SuppressWarnings("unchecked")
			Section<AnswerReference> sec = (Section<AnswerReference>) s;

			Section<QuestionReference> ref = sec.get().getQuestionSection(sec);
			Question question = ref.get().getTermObject(article, ref);

			String answerName = sec.get().getAnswerName(sec);

			if (question != null && question instanceof QuestionChoice) {
				return KnowledgeBaseUtils.findChoice((QuestionChoice) question,
						answerName, false);

			}
		}
		return null;
	}

	/**
	 * returns the section of the corresponding question-reference for this
	 * answer.
	 * 
	 * @created 26.07.2010
	 * @param s the section of this choice
	 * @return the section of the question
	 */
	public abstract Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s);

	private static class AnswerReferenceRegistrationHandler extends SimpleTermReferenceRegistrationHandler {

		public AnswerReferenceRegistrationHandler() {
			super(TermRegistrationScope.LOCAL);
		}

		@Override
		public Collection<Message> validateReference(Article article, Section<SimpleTerm> simpleTermSection) {
			Section<AnswerReference> section = Sections.cast(simpleTermSection,
					AnswerReference.class);
			Section<QuestionReference> ref = section.get().getQuestionSection(section);
			Question question = ref.get().getTermObject(article, ref);
			if (question != null) {
				if (question instanceof QuestionYN) {
					Choice choice = KnowledgeBaseUtils.findChoice((QuestionYN) question,
							section.get().getAnswerName(section), false);
					if (choice != null) return Messages.noMessage();

				}
				else if (question instanceof QuestionNum) {
					NumericalInterval range = question.getInfoStore().getValue(
							BasicProperties.QUESTION_NUM_RANGE);
					try {
						Double value = Double.parseDouble(section.get().getAnswerName(section).trim());
						if (range == null || range.contains(value)) {
							return Messages.noMessage();
						}
						else {
							return Arrays.asList(Messages.error("The value '" + value
									+ "' is not in the defined range " + range.toString()
									+ " of question '" + question.getName() + "'."));
						}
					}
					catch (NumberFormatException e) {
						return Arrays.asList(Messages.error("The value "
								+ section.get().getAnswerName(section) + " is not a numeric answer"));
					}
				}
			}
			return super.validateReference(article, simpleTermSection);
		}
	}

}
