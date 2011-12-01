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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.IncrementalConstraint;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.objects.KnowWETerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 * 
 * 
 *          This is the type to be used for markup defining new (d3web-)
 *          Choice-Answers. It checks whether the corresponding question is
 *          existing and is compatible. In case it creates the Answer object in
 *          the knowledge base.
 * 
 * 
 * 
 */
public abstract class AnswerDefinition
		extends D3webTermDefinition<Choice>
		implements IncrementalConstraint<AnswerDefinition> {

	private static final String QUESTION_FOR_ANSWER_KEY = "QUESTION_FOR_ANSWER_KEY";

	public AnswerDefinition() {
		super(Choice.class);
		this.addSubtreeHandler(Priority.HIGH, new CreateAnswerHandler());
		this.setCustomRenderer(
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
	public boolean violatedConstraints(KnowWEArticle article, Section<AnswerDefinition> s) {
		return false;
	}

	@Override
	@SuppressWarnings(value = { "unchecked" })
	public String getTermIdentifier(Section<? extends KnowWETerm<Choice>> s) {
		// here we should return a unique identifier including the question name
		// as namespace
		KnowWETerm<Choice> knowWETerm = s.get();
		if (knowWETerm instanceof AnswerDefinition) {
			Section<AnswerDefinition> sec = ((Section<AnswerDefinition>) s);
			Section<? extends QuestionDefinition> questionSection = sec.get().getQuestionSection(
					sec);
			String question = questionSection.get().getTermIdentifier(questionSection);

			return createAnswerIdentifierForQuestion(super.getTermIdentifier(sec), question);
		}

		// should not happen
		return super.getTermIdentifier(s);
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
		public Collection<Message> create(KnowWEArticle article,
				Section<AnswerDefinition> s) {

			String name = s.get().getTermName(s);

			Section<? extends QuestionDefinition> qDef = s.get().getQuestionSection(s);
			KnowWEUtils.storeObject(article, s, AnswerDefinition.QUESTION_FOR_ANSWER_KEY,
					qDef);
			// storing the current question needs to happen first, so the method
			// getUniqueTermIdentifier() can use the right question.

			if (!KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
					article, s)) {
				return new ArrayList<Message>(0);
			}

			if (qDef == null) {
				// this situation can only occur with incremental update
				// -> fullparse
				if (article != s.getArticle()) {
					KnowWEEnvironment.getInstance().getArticleManager(s.getWeb()).registerArticle(
							KnowWEArticle.createArticle(
									s.getArticle().getSection().getOriginalText(),
									s.getTitle(),
									KnowWEEnvironment.getInstance().getRootType(),
									s.getWeb(), true), false);
				}
				article.setFullParse(CreateAnswerHandler.class);
				return null;
			}

			// if having error somewhere, do nothing and report error
			if (qDef.hasErrorInSubtree(article)) {
				return Arrays.asList(Messages.objectCreationError(
						"no valid question - " + name,
						this.getClass()));
			}

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
								"only '" + qyn.getAnswerChoiceYes().getName() + "' and '"
										+ qyn.getAnswerChoiceNo().getName()
										+ "' is allowed for this question type"));
					}
				}
				else {
					a = KnowledgeBaseUtils.addChoiceAnswer((QuestionChoice) q,
							s.get().getTermName(s),
							s.get().getPosition(s));

				}

				s.get().storeTermObject(article, s, a);

				return Messages.asList(Messages.objectCreatedNotice(
						a.getClass().getSimpleName() + "  "
								+ a.getName()));

			}
			return Messages.asList(Messages.objectCreationError(
					"no choice question - " + name,
					this.getClass()));
		}

		@Override
		public void destroy(KnowWEArticle article, Section<AnswerDefinition>
				s) {

			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
					article, s);
			// why does this work?
			// explanation:
			// the answer is (un)registered using the uniqueTermIdentifier,
			// which uses either the stored father question or, if there
			// is no stored father question, retrieves the father question
			// again... both variants work correctly:
			//
			// 1) the answer is not reused in the new KDOM... in this case
			// the uniqueTermIdentifier will not find a stores question
			// section, but since the answer wasn't reused it is still
			// hooked in the same father question -> new retrieval of the
			// father Question still returns the correct one
			//
			// 2) the answer section is reused but needs to be destroyed
			// anyway, because for example the position has changed -> since
			// the answer section is reused, uniqueTermIdentifier will find
			// the stored question which is still the one the answer was
			// hooked in in the last KDOM.
		}
	}

	@Override
	public String getTermName(Section<? extends KnowWETerm<Choice>> s) {
		return KnowWEUtils.trimQuotes(s.getOriginalText());
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
		return question + " " + answer;
	}

}
