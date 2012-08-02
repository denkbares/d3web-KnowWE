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

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * 
 * This is the type to be used in markup for referencing (d3web-)
 * Choice-Answers. It checks whether the referenced object is existing. In case
 * it creates the Answer object in the knowledge base.
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @author Albrecht Striffler (denkbares GmbH)
 * 
 * @created 26.07.2010
 * 
 */
public abstract class AnswerReference
		extends D3webTermReference<Choice> {

	public AnswerReference() {
		this.setRenderer(StyleRenderer.CHOICE);
		this.addSubtreeHandler(new AnswerReferenceRegistrationHandler());
	}

	@Override
	public TermIdentifier getTermIdentifier(Section<? extends SimpleTerm> s) {
		if (s.get() instanceof AnswerReference) {
			Section<AnswerReference> answerSection = Sections.cast(s, AnswerReference.class);
			Section<? extends QuestionReference> questionSection = answerSection.get().getQuestionSection(
					answerSection);
			TermIdentifier questionIdentifier = questionSection == null
					? new TermIdentifier("")
					: questionSection.get().getTermIdentifier(
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

	@Override
	public Choice getTermObject(Article article, Section<? extends D3webTerm<Choice>> section) {

		Choice choice = null;
		if (section.get() instanceof AnswerReference) {
			TerminologyManager terminologyManager = KnowWEUtils.getTerminologyManager(article);
			TermIdentifier termIdentifier = getTermIdentifier(section);
			Section<?> answerDef = terminologyManager.getTermDefiningSection(termIdentifier);
			if (answerDef != null) {
				choice = (Choice) KnowWEUtils.getStoredObject(article, answerDef,
						AnswerDefinition.ANSWER_STORE_KEY);
				if (choice != null) return choice;
			}
			if (answerDef == null || choice == null) {
				TermIdentifier questionIdentifier = new TermIdentifier(
						termIdentifier.getPathElements()[0]);
				Section<?> termDef = terminologyManager.getTermDefiningSection(questionIdentifier);
				if (termDef != null && termDef.get() instanceof QuestionDefinition) {
					Section<QuestionDefinition> questionDef = Sections.cast(termDef,
							QuestionDefinition.class);
					Question question = questionDef.get().getTermObject(article, questionDef);
					if (question instanceof QuestionChoice) {
						choice = KnowledgeBaseUtils.findChoice((QuestionChoice) question,
								termIdentifier.getLastPathElement(), false);
					}
				}
			}
		}
		return choice;
	}

	/**
	 * returns the section of the corresponding question-reference for this
	 * answer.
	 * 
	 * @created 26.07.2010
	 * @param section the section of this choice
	 * @return the section of the question
	 */
	public abstract Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> section);

}
