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

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
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
		this.addCompileScript(Priority.LOW, new TerminologyLoopDetectionHandler<Solution>());
		this.addCompileScript(Priority.LOWER, new TerminologyLoopResolveHandler<Solution>());
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return Solution.class;
	}

	public SolutionDefinition(Priority p) {
		this.setRenderer(StyleRenderer.SOLUTION, true);
		this.addCompileScript(p, new CreateSolutionHandler());
	}

	public void setRenderer(Renderer renderer, boolean decorate) {
		if (decorate) renderer = new ValueTooltipRenderer(new SolutionHighlightingRenderer(renderer));
		setRenderer(renderer);
	}

	@Override
	public Solution getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<Solution>> section) {
		Solution solution = super.getTermObject(compiler, section);
		if (solution == null) {
			solution = compiler.getKnowledgeBase().getManager().searchSolution(section.get().getTermName(section));
		}
		return solution;
	}

	public static class CreateSolutionHandler implements D3webHandler<SolutionDefinition> {

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<SolutionDefinition> section) {

			Identifier termIdentifier = section.get().getTermIdentifier(compiler, section);
			String name = section.get().getTermName(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(compiler, section);
			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			terminologyHandler.registerTermDefinition(compiler, section, termObjectClass, termIdentifier);

			AbortCheck abortCheck = section.get().canAbortTermObjectCreation(compiler, section);
			if (abortCheck.hasErrors()) {
				// we clear term objects from previous compilations that didn't have errors
				section.get().storeTermObject(compiler, section, null);
				return abortCheck.getErrors();
			}

			if (abortCheck.termExist()) {
				section.get().storeTermObject(compiler, section, (Solution) abortCheck.getNamedObject());
				return abortCheck.getErrors();
			}

			// check is already been created
			KnowledgeBase kb = getKnowledgeBase(compiler);
			TerminologyObject object = kb.getManager().search(name);
			if (object != null) {
				if (object instanceof Solution) {
					// if the created one is not a solution, store the object and done
					section.get().storeTermObject(compiler, section, (Solution) object);
					return Messages.noMessage();
				}
				else {
					// otherwise, add an error
					return Messages.asList(Messages.error("The term '" + name + "' is already used."));
				}
			}

			// if not available, create a new one and store it for later usage
			section.get().storeTermObject(compiler, section, new Solution(kb, name));
			return Messages.noMessage();
		}
	}
}
