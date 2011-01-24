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
import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.StyleRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.subtreeHandler.IncrementalConstraint;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.tools.ToolMenuDecoratingRenderer;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.knowwe.core.renderer.ObjectInfoLinkRenderer;

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
				KnowWEUserContext user, StringBuilder string) {

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
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<SolutionDefinition> s) {

			String name = s.get().getTermName(s);

			if (!KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
					article, s)) {
				return new ArrayList<KDOMReportMessage>(0);
			}

			KnowledgeBaseManagement mgn = getKBM(article);

			IDObject o = mgn.findSolution(name);

			if (o != null) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(
						o.getClass()
								.getSimpleName()));
			}
			else {

				Solution solution = mgn.createSolution(name);

				if (solution != null) {
					s.get().storeTermObject(article, s, solution);
					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(
							solution.getClass().getSimpleName()
									+ " " + solution.getName()));
				}
				else {
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
							name,
							this.getClass()));
				}

			}

		}

		@Override
		public void destroy(KnowWEArticle article, Section<SolutionDefinition> solution) {
			Solution kbsol = solution.get().getTermObjectFromLastVersion(article, solution);
			if (kbsol != null) {
				D3webUtils.removeRecursively(kbsol);
				KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
						article, solution);
			}
		}

	}

}
