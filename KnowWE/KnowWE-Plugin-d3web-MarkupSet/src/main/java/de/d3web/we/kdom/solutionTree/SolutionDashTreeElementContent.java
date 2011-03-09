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

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.objects.IncrementalMarker;
import de.d3web.we.kdom.questionTree.ObjectDescription;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.RelationCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.subtreeHandler.ConstraintModule.Operator;
import de.d3web.we.kdom.subtreeHandler.ConstraintModule.Purpose;
import de.d3web.we.object.SolutionDefinition;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;
import de.knowwe.core.dashtree.AncestorSubtreeChangeConstraint;
import de.knowwe.core.dashtree.DashTreeElementContent;
import de.knowwe.core.dashtree.DashTreeUtils;
import de.knowwe.core.renderer.ReRenderSectionMarkerRenderer;

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
public class SolutionDashTreeElementContent extends DashTreeElementContent implements IncrementalMarker {

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
		this.setCustomRenderer(new ReRenderSectionMarkerRenderer<Type>(
				new SolutionDashTreeElementContentRenderer()));
	}

	/**
	 * 
	 * @author volker_belli
	 * @created 08.12.2010
	 */
	private static final class SolutionDashTreeElementContentRenderer extends KnowWEDomRenderer<Type> {

		@Override
		public void render(KnowWEArticle article, Section<Type> sec, UserContext user, StringBuilder string) {
			string.append(KnowWEUtils.maskHTML("<span id='" + sec.getID() + "'>"));
			DelegateRenderer.getInstance().render(article, sec, user, string);
			string.append(KnowWEUtils.maskHTML("</span>"));
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

		public CreateSubSolutionRelationHandler() {
			this.registerConstraintModule(new AncestorSubtreeChangeConstraint<SolutionDashTreeElementContent>(
					0, Operator.COMPILE_IF_VIOLATED, Purpose.CREATE));
		}

		@Override
		public void destroy(KnowWEArticle article, Section<SolutionDashTreeElementContent> s) {
			// will be destroyed by SolutionDefinition#destroy()

		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<SolutionDashTreeElementContent> s) {
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
					// here the actual taxonomic relation is established

					// remove this solution if already registered as child of
					// root
					KnowledgeBase kb = getKB(article);
					kb.getRootSolution().removeChild(localSolution);
					superSolution.addChild(localSolution);

					return Arrays.asList((KDOMReportMessage) new RelationCreatedMessage(
							s.getClass().getSimpleName()
									+ " " + localSolution.getName() + "sub-solution of "
									+ superSolution.getName()));
				}
			}

			return null;
		}

	}

}
