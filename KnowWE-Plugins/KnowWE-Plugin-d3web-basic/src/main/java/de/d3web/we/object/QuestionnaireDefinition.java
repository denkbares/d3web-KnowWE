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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.we.reviseHandler.D3webTerminologyObjectCreationHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Abstract Type for the definition of questionnaires. A questionnaire is created and hooked into the root QASet
 * of the knowledge base. The hierarchical position in the terminology needs to be handled the subclass.
 *
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class QuestionnaireDefinition extends QASetDefinition<QContainer> {

	public QuestionnaireDefinition() {
		addCompileScript(Priority.HIGHEST, new CreateQuestionnaireHandler());
		this.addCompileScript(Priority.LOW, new TerminologyLoopDetectionHandler<QContainer>());
		this.addCompileScript(Priority.LOWER, new TerminologyLoopResolveHandler<QContainer>());
		setRenderer(StyleRenderer.QUESTIONNAIRE);
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return QContainer.class;
	}

	static class CreateQuestionnaireHandler extends D3webTerminologyObjectCreationHandler<QContainer, QuestionnaireDefinition> {

		@NotNull
		@Override
		protected QContainer createTermObject(String name, KnowledgeBase kb) {
			return new QContainer(kb, name);
		}
	}
}
