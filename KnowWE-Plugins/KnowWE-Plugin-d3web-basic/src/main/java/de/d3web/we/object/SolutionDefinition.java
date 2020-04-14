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

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webTerminologyObjectCreationHandler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Abstract Type for the definition of solutions. A solution is created and hooked into the root solution of the
 * knowledge base. The hierarchical position in the terminology needs to be handled the subclass.
 *
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class SolutionDefinition extends D3webTermDefinition<Solution> {

	public SolutionDefinition() {
		this(Priority.HIGHEST);
	}

	public SolutionDefinition(Priority p) {
		this.setRenderer(StyleRenderer.SOLUTION, true);
		this.addCompileScript(p, new CreateSolutionHandler());
		this.addCompileScript(Priority.LOW, new TerminologyLoopDetectionHandler<Solution>());
		this.addCompileScript(Priority.LOWER, new TerminologyLoopResolveHandler<Solution>());
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return Solution.class;
	}

	public void setRenderer(Renderer renderer, boolean decorate) {
		if (decorate) renderer = new ValueTooltipRenderer(new SolutionHighlightingRenderer(renderer));
		setRenderer(renderer);
	}

	@Override
	public Solution getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<Solution>> section) {
		return compiler.getKnowledgeBase().getManager().searchSolution(section.get().getTermName(section));
	}

	public static class CreateSolutionHandler extends D3webTerminologyObjectCreationHandler<Solution, SolutionDefinition> {

		@NotNull
		@Override
		protected Solution createTermObject(String name, KnowledgeBase kb) {
			return new Solution(kb, name);
		}

		@Override
		protected void recompile(D3webCompiler compiler, Section<SolutionDefinition> section, Identifier termIdentifier) {
			section.get().recompile(compiler, section, termIdentifier);
			super.recompile(compiler, section, termIdentifier);
		}

		@Override
		protected void destroyAndRecompile(D3webCompiler compiler, Section<SolutionDefinition> section, Identifier identifier) {
			section.get().destroyAndRecompile(compiler, section, identifier);
			super.destroyAndRecompile(compiler, section, identifier);
		}
	}

	protected void recompile(D3webCompiler compiler, Section<SolutionDefinition> section, Identifier identifier) {
		Compilers.recompileRegistrations(compiler, identifier);
	}

	protected void destroyAndRecompile(D3webCompiler compiler, Section<SolutionDefinition> section, Identifier identifier) {
		Compilers.destroyAndRecompileRegistrations(compiler, identifier);
	}

}
