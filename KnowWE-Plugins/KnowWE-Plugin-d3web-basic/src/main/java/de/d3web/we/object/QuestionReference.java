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

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Type for question references
 *
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public class QuestionReference extends D3webTermReference<Question> {

	public QuestionReference() {
		this.setRenderer(new ValueTooltipRenderer(StyleRenderer.QUESTION));
		this.addCompileScript(new SimpleReferenceRegistrationScript<>(D3webCompiler.class));
	}

	@Override
	public Question getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<Question>> section) {
		Question question = super.getTermObject(compiler, section);
		if (question == null) {
			question = compiler.getKnowledgeBase().getManager().searchQuestion(section.get().getTermName(section));
		}
		return question;
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return Question.class;
	}

}
