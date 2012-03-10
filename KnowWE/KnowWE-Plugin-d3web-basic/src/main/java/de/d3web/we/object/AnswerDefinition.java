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
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * This is the type to be used for markup defining new (d3web-) Choice-Answers.
 * It checks whether the corresponding question is existing and is compatible.
 * In case it creates the Answer object in the knowledge base.
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class AnswerDefinition
		extends D3webTermDefinition<Choice> {

	private static final String QUESTION_FOR_ANSWER_KEY = "QUESTION_FOR_ANSWER_KEY";

	public AnswerDefinition() {
		this.addSubtreeHandler(Priority.HIGH, new CreateAnswerHandler());
		this.setRenderer(
				StyleRenderer.CHOICE);
		this.setOrderSensitive(true);
	}

	public abstract int getPosition(Section<? extends AnswerDefinition> s);

	/**
	 * 
	 * returns the section of the question this answer belongs to
	 * 
	 * @created 26.07.2010
	 * @param s
	 * @return
	 */
	public abstract Section<? extends QuestionDefinition> getQuestionSection(Section<? extends AnswerDefinition> s);

	@Override
	public Choice getTermObject(Article article, Section<? extends D3webTerm<Choice>> s) {

		if (s.get() instanceof AnswerDefinition) {
			@SuppressWarnings("unchecked")
			Section<AnswerDefinition> sec = (Section<AnswerDefinition>) s;

			Section<? extends QuestionDefinition> ref = sec.get().getQuestionSection(sec);
			Question question = ref.get().getTermObject(article, ref);

			String answerName = sec.get().getAnswerName(sec);

			if (question != null && question instanceof QuestionChoice) {
				return KnowledgeBaseUtils.findChoice((QuestionChoice) question,
						answerName, false);

			}
		}
		return null;
	}

	@Override
	@SuppressWarnings(value = { "unchecked" })
	public String getTermIdentifier(Section<? extends SimpleTerm> s) {
		if (s.get() instanceof AnswerDefinition) {
			Section<AnswerDefinition> sec = ((Section<AnswerDefinition>) s);
			Section<? extends QuestionDefinition> questionSection = sec.get().getQuestionSection(
					sec);
			String question = questionSection.get().getTermIdentifier(questionSection);

			return createAnswerIdentifierForQuestion(getAnswerName(sec),
					question);
		}

		// should not happen
		return KnowWEUtils.trimQuotes(s.getText());
	}

	public String getAnswerName(Section<? extends AnswerDefinition> answerDefinition) {
		return KnowWEUtils.trimQuotes(answerDefinition.getText());
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends SimpleTerm> section) {
		return Choice.class;
	}

	/**
	 * 
	 * @author Jochen
	 * @created 26.07.2010
	 * 
	 *          This handler actually creates the Answer as an object of the
	 *          knowledge base
	 */
	static class CreateAnswerHandler extends D3webSubtreeHandler<AnswerDefinition> {

		@Override
		public Collection<Message> create(Article article,
				Section<AnswerDefinition> section) {

			String name = section.get().getAnswerName(section);

			Section<? extends QuestionDefinition> qDef = section.get().getQuestionSection(section);
			KnowWEUtils.storeObject(article, section, AnswerDefinition.QUESTION_FOR_ANSWER_KEY,
					qDef);
			// if having error somewhere, do nothing and report error
			if (qDef == null || qDef.hasErrorInSubtree(article)) {
				return Arrays.asList(Messages.objectCreationError(
						"No valid question for choice '" + name + "'"));
			}

			// storing the current question needs to happen first, so the method
			// getUniqueTermIdentifier() can use the right question.
			String termIdentifier = section.get().getTermIdentifier(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(section);

			TerminologyManager terminologyHandler = KnowWEUtils.getTerminologyManager(article);
			terminologyHandler.registerTermDefinition(section, termObjectClass, termIdentifier);

			Collection<Message> msgs = section.get().canAbortTermObjectCreation(article, section);
			if (msgs != null) return msgs;

			Question q = qDef.get().getTermObject(article, qDef);

			if (q instanceof QuestionChoice) {

				Choice a;

				// special treatment for QuestionYN
				// Answers are not created, but mapped to the already existing
				// Choices.
				if (q instanceof QuestionYN) {
					QuestionYN qyn = (QuestionYN) q;
					if (name.equals(qyn.getAnswerChoiceYes().getName())) {
						a = qyn.getAnswerChoiceYes();
					}
					else if (name.equals(qyn.getAnswerChoiceNo().getName())) {
						a = qyn.getAnswerChoiceNo();
					}
					else {
						return Messages.asList(Messages.syntaxError(
								"Only '" + qyn.getAnswerChoiceYes().getName() + "' and '"
										+ qyn.getAnswerChoiceNo().getName()
										+ "' is allowed for this question type"));
					}
				}
				else {
					a = KnowledgeBaseUtils.addChoiceAnswer((QuestionChoice) q,
							section.get().getAnswerName(section),
							section.get().getPosition(section));

				}

				return Messages.asList(Messages.objectCreatedNotice(
						a.getClass().getSimpleName() + "  "
								+ a.getName()));

			}
			return Messages.asList(Messages.objectCreationError(
					"'" + name + "' is not a choice question"));
		}

	}

	/**
	 * 
	 * creates a global unique identifier for an answer by use of the question
	 * name (which is globally unique)
	 * 
	 * 
	 * @created 06.06.2011
	 * @param answer
	 * @param question
	 * @return
	 */
	public static String createAnswerIdentifierForQuestion(String answer, String question) {
		return question + "#" + answer;
	}

}
