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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import com.denkbares.strings.Identifier;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * Abstract Type for the definition of solutions. A solution is created and hooked into the root solution
 * of the knowledge base. The hierarchical position in the terminology needs to be handled the subclass.
 *
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class SolutionDefinition
		extends D3webTermDefinition<Solution> {

	public SolutionDefinition() {
		this(Priority.HIGHEST);
		this.addCompileScript(Priority.LOW, new TerminologyLoopDetectionHandler<Solution>());
		this.addCompileScript(Priority.LOWER, new TerminologyLoopResolveHandler<Solution>());
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends Term> section) {
		return Solution.class;
	}

	public SolutionDefinition(Priority p) {
		this.setRenderer(new ValueTooltipRenderer(new SolutionIDHighlightingRenderer()));
		// this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
		this.addCompileScript(p, new CreateSolutionHandler());
	}

	/**
	 * @author Johannes Dienst
	 *         <p/>
	 *         Highlights the Solutions in CoveringList according to state. Also Includes the ObjectInfoLinkRenderer.
	 */
	class SolutionIDHighlightingRenderer implements Renderer {

		@Override
		public void render(Section<?> sec, UserContext user,
						   RenderResult string) {

			D3webCompiler compiler = Compilers.getCompiler(sec, D3webCompiler.class);

			if (compiler != null) {
				KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
				Session session = SessionProvider.getSession(user, kb);

				if (session != null) {
					@SuppressWarnings("unchecked")
					Solution solution = getTermObject(compiler,
							(Section<? extends D3webTerm<Solution>>) sec);
					if (solution != null) {
						Rating state = session.getBlackboard().getRating(solution);

						string.appendHtml("<span style=\"background-color:");
						if (state.hasState(State.ESTABLISHED)) {
							string.append(StyleRenderer.CONDITION_FULLFILLED);
						}
						else if (state.hasState(State.EXCLUDED)) {
							string.append(StyleRenderer.CONDITION_FALSE);
						}
						else {
							string.appendHtml(" rgb()");
						}
						string.appendHtml(";\">");
					}
				}
			}

			StyleRenderer.SOLUTION.render(sec, user, string);
			string.appendHtml("</span>");
		}
	}

	static class CreateSolutionHandler implements D3webHandler<SolutionDefinition> {

		@Override
		public Collection<Message> create(D3webCompiler compiler,
										  Section<SolutionDefinition> section) {

			Identifier termIdentifier = section.get().getTermIdentifier(section);
			String name = section.get().getTermName(section);
			Class<?> termObjectClass = section.get().getTermObjectClass(section);
			TerminologyManager terminologyHandler = compiler.getTerminologyManager();
			terminologyHandler.registerTermDefinition(compiler, section, termObjectClass,
					termIdentifier);

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

			KnowledgeBase kb = getKnowledgeBase(compiler);

			TerminologyObject o = kb.getManager().search(name);

			if (o != null) {
				if (!(o instanceof Solution)) {
					return Messages.asList(Messages.error("The term '"
							+ name + "' is reserved by the system."));
				}
				return Messages.asList();
			}

			section.get().storeTermObject(compiler, section, new Solution(kb, name));

			return Messages.noMessage();

		}

	}

}
