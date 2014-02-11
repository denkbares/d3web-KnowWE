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
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
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

	public static final String ANSWER_STORE_KEY = "answerStoreKey";

	public AnswerDefinition() {
		this.addCompileScript(Priority.HIGH, new CreateAnswerHandler());
		this.setRenderer(StyleRenderer.CHOICE);
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
	public Choice getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<Choice>> section) {

		Choice choice = null;
		if (section.get() instanceof AnswerDefinition) {
			TerminologyManager terminologyManager = compiler.getTerminologyManager();
			Section<?> def = terminologyManager.getTermDefiningSection(getTermIdentifier(section));
			if (def != null) {
				choice = (Choice) KnowWEUtils.getStoredObject((PackageCompiler) compiler, def,
						ANSWER_STORE_KEY);
			}

			if (choice == null) {
				Section<AnswerDefinition> answerDef = Sections.cast(section, AnswerDefinition.class);
				Section<? extends QuestionDefinition> ref = answerDef.get().getQuestionSection(
						answerDef);
				if (ref != null) {
					Question question = ref.get().getTermObject(compiler, ref);
					if (question != null && question instanceof QuestionChoice) {
						String answerName = answerDef.get().getTermName(answerDef);
						choice = KnowledgeBaseUtils.findChoice((QuestionChoice) question,
								answerName, false);
						KnowWEUtils.storeObject((PackageCompiler) compiler, answerDef,
								ANSWER_STORE_KEY, choice);
					}
				}
			}
		}

		return choice;
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> s) {
		if (s.get() instanceof AnswerDefinition) {
			Section<AnswerDefinition> answerSection = Sections.cast(s, AnswerDefinition.class);
			Section<? extends QuestionDefinition> questionSection = answerSection.get().getQuestionSection(
					answerSection);
			Identifier questionIdentifier = questionSection.get().getTermIdentifier(
					questionSection);

			return questionIdentifier.append(new Identifier(answerSection.get().getTermName(
					answerSection)));
		}

		// should not happen
		return new Identifier(Strings.trimQuotes(s.getText()));
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends Term> section) {
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
	static class CreateAnswerHandler extends D3webHandler<AnswerDefinition> {

		@Override
		public Collection<Message> create(D3webCompiler compiler,
				Section<AnswerDefinition> section) {

			String name = section.get().getTermName(section);

			Section<? extends QuestionDefinition> qDef = section.get().getQuestionSection(section);

			// if having error somewhere, do nothing and report error
			if (qDef == null || qDef.hasErrorInSubtree(compiler)) {
				return Arrays.asList(Messages.objectCreationError(
						"No valid question for choice '" + name + "'"));
			}

			// storing the current question needs to happen first, so the method
			// getUniqueTermIdentifier() can use the right question.
			Identifier termIdentifier = section.get().getTermIdentifier(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(section);

			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			terminologyHandler.registerTermDefinition(compiler, section, termObjectClass,
					termIdentifier);

			AbortCheck abortCheck = section.get().canAbortTermObjectCreation(
					compiler, section);
			if (abortCheck.hasErrors() || abortCheck.termExist()) return abortCheck.getErrors();

			Question q = qDef.get().getTermObject(compiler, qDef);

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

				KnowWEUtils.storeObject(compiler, section, ANSWER_STORE_KEY, choice);

				return Messages.asList(Messages.objectCreatedNotice(
						choice.getClass().getSimpleName() + "  "
								+ choice.getName()));

			}
			return Messages.asList(Messages.objectCreationError(
					"'" + name + "' is not a choice question"));
		}

	}

}
