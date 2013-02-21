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
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;
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

	public static final String ANSWER_STORE_KEY = "answerStoreKey";

	public AnswerDefinition() {
		this.addSubtreeHandler(Priority.HIGH, new CreateAnswerHandler());
		this.setRenderer(StyleRenderer.CHOICE);
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

		Choice choice = null;
		if (s.get() instanceof AnswerDefinition) {
			TerminologyManager terminologyManager = KnowWEUtils.getTerminologyManager(article);
			Section<?> def = terminologyManager.getTermDefiningSection(getTermIdentifier(s));
			if (def != null) {
				choice = (Choice) KnowWEUtils.getStoredObject(article, def,
						ANSWER_STORE_KEY);
			}

			if (choice == null) {
				Section<AnswerDefinition> answerDef = Sections.cast(s, AnswerDefinition.class);
				Section<? extends QuestionDefinition> ref = answerDef.get().getQuestionSection(
						answerDef);
				if (ref != null) {
					Question question = ref.get().getTermObject(article, ref);
					if (question != null && question instanceof QuestionChoice) {
						String answerName = answerDef.get().getTermName(answerDef);
						choice = KnowledgeBaseUtils.findChoice((QuestionChoice) question,
								answerName, false);
						KnowWEUtils.storeObject(article, answerDef, ANSWER_STORE_KEY, choice);
					}
				}
			}
		}

		return choice;
	}

	@Override
	public TermIdentifier getTermIdentifier(Section<? extends SimpleTerm> s) {
		if (s.get() instanceof AnswerDefinition) {
			Section<AnswerDefinition> answerSection = Sections.cast(s, AnswerDefinition.class);
			Section<? extends QuestionDefinition> questionSection = answerSection.get().getQuestionSection(
					answerSection);
			TermIdentifier questionIdentifier = questionSection.get().getTermIdentifier(
					questionSection);

			return questionIdentifier.append(new TermIdentifier(answerSection.get().getTermName(
					answerSection)));
		}

		// should not happen
		return new TermIdentifier(Strings.trimQuotes(s.getText()));
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

			String name = section.get().getTermName(section);

			Section<? extends QuestionDefinition> qDef = section.get().getQuestionSection(section);

			// if having error somewhere, do nothing and report error
			if (qDef == null || qDef.hasErrorInSubtree(article)) {
				return Arrays.asList(Messages.objectCreationError(
						"No valid question for choice '" + name + "'"));
			}

			// storing the current question needs to happen first, so the method
			// getUniqueTermIdentifier() can use the right question.
			TermIdentifier termIdentifier = section.get().getTermIdentifier(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(section);

			TerminologyManager terminologyHandler = KnowWEUtils.getTerminologyManager(article);
			terminologyHandler.registerTermDefinition(section, termObjectClass, termIdentifier);

			AbortCheck abortCheck = section.get().canAbortTermObjectCreation(
					article, section);
			if (abortCheck.hasErrors() || abortCheck.termExist()) return abortCheck.getErrors();

			Question q = qDef.get().getTermObject(article, qDef);

			if (q instanceof QuestionChoice) {

				Choice choice;

				// special treatment for QuestionYN
				// Answers are not created, but mapped to the already existing
				// Choices.
				if (q instanceof QuestionYN) {
					QuestionYN qyn = (QuestionYN) q;
					if (name.equals(qyn.getAnswerChoiceYes().getName())) {
						choice = qyn.getAnswerChoiceYes();
					}
					else if (name.equals(qyn.getAnswerChoiceNo().getName())) {
						choice = qyn.getAnswerChoiceNo();
					}
					else {
						return Messages.asList(Messages.syntaxError(
								"Only '" + qyn.getAnswerChoiceYes().getName() + "' and '"
										+ qyn.getAnswerChoiceNo().getName()
										+ "' is allowed for this question type"));
					}
				}
				else {
					choice = KnowledgeBaseUtils.addChoiceAnswer((QuestionChoice) q,
							section.get().getTermName(section),
							section.get().getPosition(section));
				}

				KnowWEUtils.storeObject(article, section, ANSWER_STORE_KEY, choice);

				return Messages.asList(Messages.objectCreatedNotice(
						choice.getClass().getSimpleName() + "  "
								+ choice.getName()));

			}
			return Messages.asList(Messages.objectCreationError(
					"'" + name + "' is not a choice question"));
		}

	}

}
