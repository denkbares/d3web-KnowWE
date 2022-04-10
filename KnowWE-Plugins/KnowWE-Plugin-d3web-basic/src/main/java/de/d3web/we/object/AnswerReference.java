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

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * This is the type to be used in markup for referencing (d3web-) Choice-Answers. It checks whether the referenced
 * object is existing. In case it creates the Answer object in the knowledge base.
 *
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 26.07.2010
 */
public abstract class AnswerReference
		extends D3webTermReference<Choice> {

	public AnswerReference() {
		this.setRenderer(StyleRenderer.CHOICE);
		this.addCompileScript(Priority.LOW, new AnswerReferenceRegistrationHandler());
	}

	@Override
	public Choice getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<Choice>> section) {
		Choice choice = super.getTermObject(compiler, section);
		if (choice == null) {
			Section<QuestionReference> questionSection = getQuestionSection(Sections.cast(section, AnswerReference.class));
			if (questionSection == null) return null;
			Question question = questionSection.get().getTermObject(compiler, questionSection);
			if (!(question instanceof QuestionChoice)) return null;
			choice = KnowledgeBaseUtils.findChoice((QuestionChoice) question, getTermName(section), false);
		}
		return choice;
	}

	@Override
	public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> s) {
		if (s.get() instanceof AnswerReference) {
			Section<AnswerReference> answerSection = Sections.cast(s, AnswerReference.class);
			Section<? extends QuestionReference> questionSection = answerSection.get().getQuestionSection(
					answerSection);
			Identifier questionIdentifier = questionSection == null
					? new Identifier("")
					: questionSection.get().getTermIdentifier(
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

	/**
	 * returns the section of the corresponding question-reference for this answer.
	 *
	 * @param section the section of this choice
	 * @return the section of the question
	 * @created 26.07.2010
	 */
	public abstract Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> section);

}
