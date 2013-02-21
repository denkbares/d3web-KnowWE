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
package de.d3web.we.kdom.solutionTree;

import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.kdom.questionTree.ObjectDescription;
import de.d3web.we.object.SolutionDefinition;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.dashtree.DashTreeElementContent;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;

/**
 * @author Jochen
 * 
 *         A DashTreeElementContent for the Solution-DashTree. It is injected
 *         into a dash-tree @see {@link SolutionsDashTree} It contains a
 *         SolutionDef type (which itself internally creates a solution object)
 *         and CreateSubSolutionRelationHandler which established the
 *         hierarchical relations defined by the dashtree
 * 
 * 
 */
public class SolutionDashTreeElementContent extends DashTreeElementContent {

	public SolutionDashTreeElementContent() {
		this.addSubtreeHandler(new CreateSubSolutionRelationHandler());

		// add description-type via '~'
		this.addChildType(new ObjectDescription(MMInfo.DESCRIPTION));

		// add solution-Def type
		SolutionTreeSolutionDefinition solutionDef = new SolutionTreeSolutionDefinition();
		ConstraintSectionFinder f = new ConstraintSectionFinder(new AllTextFinderTrimmed());
		f.addConstraint(SingleChildConstraint.getInstance());
		solutionDef.setSectionFinder(f);
		this.addChildType(solutionDef);
		this.setRenderer(new ReRenderSectionMarkerRenderer(
				new SolutionDashTreeElementContentRenderer()));
	}

	/**
	 * 
	 * @author volker_belli
	 * @created 08.12.2010
	 */
	private static final class SolutionDashTreeElementContentRenderer implements Renderer {

		@Override
		public void render(Section<?> sec, UserContext user, RenderResult string) {
			string.appendHtml("<span id='" + sec.getID() + "'>");
			DelegateRenderer.getInstance().render(sec, user, string);
			string.appendHtml("</span>");
		}
	}

	/**
	 * @author Jochen
	 * 
	 *         This handler establishes sub-solution-relations defined by the
	 *         solutionDashTree in the knowledge base i.e., if a solution is a
	 *         dashTree-child of another solution we add it as child in the
	 *         knowledge base
	 * 
	 */
	class CreateSubSolutionRelationHandler extends D3webSubtreeHandler<SolutionDashTreeElementContent> {

		@Override
		public Collection<Message> create(Article article, Section<SolutionDashTreeElementContent> s) {
			Section<? extends DashTreeElementContent> fatherSolutionContent = DashTreeUtils.getFatherDashTreeElementContent(
					s);
			Section<SolutionDefinition> localSolutionDef = Sections.findSuccessor(s,
					SolutionDefinition.class);
			Solution localSolution = localSolutionDef.get().getTermObject(article, localSolutionDef);

			if (fatherSolutionContent != null && localSolution != null) {

				Section<SolutionDefinition> solutionDef = Sections.findSuccessor(
						fatherSolutionContent, SolutionDefinition.class);
				if (solutionDef != null) {
					Solution superSolution = solutionDef.get().getTermObject(article, solutionDef);
					if (superSolution != null) {
						// here the actual taxonomic relation is established

						// remove this solution if already registered as child
						// of
						// root
						KnowledgeBase kb = getKB(article);
						kb.getRootSolution().removeChild(localSolution);
						superSolution.addChild(localSolution);

						return Messages.asList(Messages.relationCreatedNotice(
								s.getClass().getSimpleName()
										+ " " + localSolution.getName() + "sub-solution of "
										+ superSolution.getName()));
					}
				}
			}

			return null;
		}

	}

}
