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

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.IncrementalConstraint;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.TerminologyHandler;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.KnowWEDomRenderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.ObjectInfoLinkRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.tools.ToolMenuDecoratingRenderer;

/**
 * 
 * Type for the definition of solution
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class SolutionDefinition
		extends D3webTermDefinition<Solution>
		implements IncrementalConstraint<SolutionDefinition> {

	public SolutionDefinition() {
		this(Priority.HIGHEST);
	}

	public SolutionDefinition(Priority p) {
		super(Solution.class);
		this.setCustomRenderer(
				new ToolMenuDecoratingRenderer<SolutionDefinition>(
						new SolutionIDHighlightingRenderer()));
		// this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
		this.addSubtreeHandler(p, new CreateSolutionHandler());
	}

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<SolutionDefinition> s) {
		return false;
	}

	/**
	 * 
	 * @author Johannes Dienst
	 * 
	 *         Highlights the Solutions in CoveringList according to state. Also
	 *         Includes the ObjectInfoLinkRenderer.
	 * 
	 */
	class SolutionIDHighlightingRenderer extends KnowWEDomRenderer<SolutionDefinition> {

		@Override
		public void render(KnowWEArticle article, Section<SolutionDefinition> sec,
				UserContext user, StringBuilder string) {

			Session session = D3webUtils.getSession(article.getTitle(), user,
					article.getWeb());

			String spanStart = KnowWEUtils
					.maskHTML("<span style=\"background-color: rgb(");
			String spanStartEnd = KnowWEUtils.maskHTML(";\">");
			String spanEnd = KnowWEUtils.maskHTML("</span>");

			if (session != null) {
				Solution solution = getTermObject(article, sec);
				if (solution != null) {
					Rating state = session.getBlackboard().getRating(solution);

					if (state.hasState(State.ESTABLISHED)) {
						string.append(spanStart + "207, 255, 207)" + spanStartEnd);
					}
					else if (state.hasState(State.EXCLUDED)) {
						string.append(spanStart + "255, 207, 207)" + spanStartEnd);
					}
					else {
						string.append(spanStart + ")" + spanStartEnd);
					}
				}
			}

			new ObjectInfoLinkRenderer(StyleRenderer.SOLUTION).render(article, sec,
					user, string);
			string.append(spanEnd);
		}
	}

	static class CreateSolutionHandler extends D3webSubtreeHandler<SolutionDefinition> {

		@Override
		public Collection<Message> create(KnowWEArticle article,
				Section<SolutionDefinition> s) {

			String name = s.get().getTermIdentifier(s);

			TerminologyHandler terminologyHandler = KnowWEUtils.getTerminologyHandler(article.getWeb());
			terminologyHandler.registerTermDefinition(article, s);
			if (terminologyHandler.getTermDefiningSection(article, s) != s) {
				Solution existingSolution = s.get().getTermObject(article, s);
				if (existingSolution == null) {
					return Messages.asList(D3webUtils.alreadyDefinedButErrors("solution",
							name));
				}
				// Solution is already defined somewhere else, abort
				return new ArrayList<Message>(0);
			}

			KnowledgeBase kb = getKB(article);

			TerminologyObject o = kb.getManager().search(name);

			if (o != null) {
				return Messages.asList(Messages.objectAlreadyDefinedWarning(
						o.getClass().getSimpleName()));
			}
			else {

				Solution solution = new Solution(kb.getRootSolution(), name);

				s.get().storeTermObject(article, s, solution);
				return Messages.asList(Messages.objectCreatedNotice(
						solution.getClass().getSimpleName()
								+ " " + solution.getName()));

			}

		}

		@Override
		public void destroy(KnowWEArticle article, Section<SolutionDefinition> solution) {
			Solution kbsol = solution.get().getTermObject(article, solution);
			if (kbsol != null) {
				D3webUtils.removeRecursively(kbsol);
				KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
						article, solution);
			}
		}

	}

}
