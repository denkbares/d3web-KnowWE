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
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
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
		if (decorate) renderer = new ValueTooltipRenderer(new SolutionIDHighlightingRenderer(renderer));
		setRenderer(renderer);
	}

	/**
	 * @author Johannes Dienst
	 * <p/>
	 * Highlights the Solutions in CoveringList according to state. Also Includes the ObjectInfoLinkRenderer.
	 */
	private class SolutionIDHighlightingRenderer implements Renderer {

		private final Renderer innerRenderer;

		private SolutionIDHighlightingRenderer(Renderer innerRenderer) {
			this.innerRenderer = innerRenderer;
		}

		@Override
		public void render(Section<?> sec, UserContext user, RenderResult string) {

			// determine color to highlight
			Rating.State state = getState(sec, user);
			String color = (state == State.ESTABLISHED) ? StyleRenderer.CONDITION_FULLFILLED :
					(state == State.EXCLUDED) ? StyleRenderer.CONDITION_FALSE : null;

			if (color != null) {
				string.appendHtml("<span style='background-color:").append(color).appendHtml(";'>");
			}
			innerRenderer.render(sec, user, string);
			if (color != null) {
				string.appendHtml("</span>");
			}
		}

		private Rating.State getState(Section<?> sec, UserContext user) {
			D3webCompiler compiler = Compilers.getCompiler(sec, D3webCompiler.class);
			if (compiler != null) {
				KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
				Session session = SessionProvider.getSession(user, kb);

				if (session != null) {
					Solution solution = getTermObject(compiler, Sections.cast(sec, SolutionDefinition.class));
					if (solution != null) {
						return session.getBlackboard().getRating(solution).getState();
					}
				}
			}
			return State.UNCLEAR;
		}
	}

	private static class CreateSolutionHandler implements D3webHandler<SolutionDefinition> {

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
