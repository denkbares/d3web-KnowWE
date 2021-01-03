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

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
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

	private static final String CHOICE_KEY = "CHOICE_KEY";

	public AnswerDefinition() {
		this.addCompileScript(Priority.HIGH, new CreateAnswerHandler());
		this.setRenderer(StyleRenderer.CHOICE);
	}

	/**
	 * returns the section of the question this answer belongs to
	 *
	 * @created 26.07.2010
	 */
	public abstract Section<? extends QuestionDefinition> getQuestionSection(Section<? extends AnswerDefinition> s);

	@Override
	public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> s) {
		if (s.get() instanceof AnswerDefinition) {
			Section<AnswerDefinition> answerSection = Sections.cast(s, AnswerDefinition.class);
			Section<? extends QuestionDefinition> questionSection = answerSection.get().getQuestionSection(
					answerSection);
			Identifier questionIdentifier = questionSection.get().getTermIdentifier(
					compiler, questionSection);

			return questionIdentifier.append(new Identifier(answerSection.get().getTermName(
					answerSection)));
		}

		// should not happen
		return new Identifier(Strings.trimQuotes(s.getText()));
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return Choice.class;
	}

	@Override
	public Choice getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<Choice>> section) {
		return section.getObject(compiler, CHOICE_KEY);
	}

	/**
	 * This handler actually creates the Answer as an object of the
	 * knowledge base
	 *
	 * @author Jochen
	 * @created 26.07.2010
	 */
	static class CreateAnswerHandler implements D3webHandler<AnswerDefinition> {

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<AnswerDefinition> section) {

			String name = section.get().getTermName(section);
			Section<? extends QuestionDefinition> questionDefinition = section.get().getQuestionSection(section);

			// if having error somewhere, do nothing and report error
			if (questionDefinition == null || questionDefinition.hasErrorInSubtree(compiler)) {
				return Collections.singletonList(Messages.objectCreationError(
						"No valid question for choice '" + name + "'"));
			}

			// storing the current question needs to happen first, so the method
			// getUniqueTermIdentifier() can use the right question.
			Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
			Class<?> termObjectClass = section.get().getTermObjectClass(compiler, section);

			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			terminologyHandler.registerTermDefinition(compiler, section, termObjectClass,
					termIdentifier);

			AbortCheck<Choice> abortCheck = section.get().canAbortTermObjectCreation(compiler, section);
			if (abortCheck.hasErrors()) {
				return abortCheck.getErrors();
			}
			if (abortCheck.termExist()) {
				return Messages.noMessage();
			}

			Question question = questionDefinition.get().getTermObject(compiler, questionDefinition);

			if (question instanceof QuestionChoice) {
				Choice choice;

				// special treatment for QuestionYN
				// Answers are not created, but mapped to the already existing Choices
				if (question instanceof QuestionYN) {
					QuestionYN questionYN = (QuestionYN) question;
					if (name.equals(questionYN.getAnswerChoiceYes().getName())) {
						choice = questionYN.getAnswerChoiceYes();
					}
					else if (name.equals(questionYN.getAnswerChoiceNo().getName())) {
						choice = questionYN.getAnswerChoiceNo();
					}
					else {
						return Messages.asList(Messages.syntaxError(
								"Only '" + questionYN.getAnswerChoiceYes().getName() + "' and '"
										+ questionYN.getAnswerChoiceNo().getName()
										+ "' is allowed for this question type"
						));
					}
				}
				else {
					choice = new Choice(section.get().getTermName(section));
				}
				section.storeObject(compiler, CHOICE_KEY, choice);

				return Messages.noMessage();
			}
			return Messages.asList(Messages.objectCreationError("'" + name + "' is not a choice question"));
		}
	}
}
