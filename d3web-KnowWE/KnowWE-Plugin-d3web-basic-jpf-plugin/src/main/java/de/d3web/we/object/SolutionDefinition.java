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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.we.kdom.IncrementalConstraints;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.renderer.ObjectInfoLinkRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.inference.PSMethodXCL;

/**
 * 
 * Type for the definition of solution
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010
 */
public abstract class SolutionDefinition
		extends D3webTermDefinition<Solution>
		implements IncrementalConstraints {

	public SolutionDefinition() {
		super(Solution.class);
		this.setCustomRenderer(new SolutionIDHighlightingRenderer());
		// this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
		this.addSubtreeHandler(Priority.HIGHEST, new CreateSolutionHandler());
	}

	@Override
	public boolean hasViolatedConstraints(KnowWEArticle article, Section<?> s) {
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
		public void render(KnowWEArticle article, Section sec,
				KnowWEUserContext user, StringBuilder string) {
			String solution = sec.getOriginalText().replace("\"", "").trim();

			Session session = D3webUtils.getSession(article.getTitle(), user,
					article.getWeb());

			String spanStart = KnowWEUtils
					.maskHTML("<span style=\"background-color: rgb(");
			String spanStartEnd = KnowWEUtils.maskHTML(";\">");
			String spanEnd = KnowWEUtils.maskHTML("</span>");

			if (session != null) {

				List<Solution> diags = session.getKnowledgeBase().getSolutions();
				Collection<KnowledgeSlice> slices = session.getKnowledgeBase()
						.getAllKnowledgeSlicesFor(PSMethodXCL.class);

				for (Solution d : diags) {

					if (d.getName().equals(solution)) {
						Rating state;
						XCLModel diagModel = this.findModel(solution, slices);

						if (diagModel == null) state = new Rating(State.UNCLEAR);
						else state = diagModel.getState(session);

						if (state.hasState(State.ESTABLISHED)) {
							string
									.append(spanStart + "51, 255, 51)"
											+ spanStartEnd);
						}

						if (state.hasState(State.EXCLUDED)) {
							string
									.append(spanStart + "255, 153, 0)"
											+ spanStartEnd);
						}

						if (state.hasState(State.SUGGESTED)) {
							string.append(spanStart + "220, 200, 11)"
									+ spanStartEnd);
						}

						if (state.hasState(State.UNCLEAR)) {
							string.append(spanStart + ")" + spanStartEnd);
						}
					}
				}
			}
			else {
				string.append("");
			}

			new ObjectInfoLinkRenderer(FontColorRenderer
					.getRenderer(FontColorRenderer.COLOR1)).render(article, sec,
					user, string);
			string.append(spanEnd);
		}

		/**
		 * Finds a Model from a KnowledgeSlice list.
		 * 
		 * @param solution
		 * @return
		 */
		private XCLModel findModel(String solution, Collection<KnowledgeSlice> slices) {
			for (KnowledgeSlice s : slices) {
				if (s instanceof XCLModel) {
					if (((XCLModel) s).getSolution().getName().equals(solution)) return (XCLModel) s;
				}
			}
			return null;
		}
	}

	static class CreateSolutionHandler extends D3webSubtreeHandler<SolutionDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<SolutionDefinition> solutionSection) {

			if (KnowWEUtils.getTerminologyHandler(article.getWeb()).isDefinedTerm(article,
					solutionSection)) {
				KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(article,
						solutionSection);
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(
						solutionSection.get().getTermName(solutionSection)));
			}

			String name = solutionSection.get().getTermName(solutionSection);

			KnowledgeBaseManagement mgn = getKBM(article);

			IDObject o = mgn.findSolution(name);

			if (o != null) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(
						o.getClass()
								.getSimpleName()));
			}
			else {

				Solution s = mgn.createSolution(name);

				if (s != null) {
					// ok everything went well
					// register term
					KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
							article, solutionSection);
					solutionSection.get().storeTermObject(article, solutionSection, s);
					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(
							s.getClass().getSimpleName()
									+ " " + s.getName()));
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
