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

import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.kdom.questionTree.ObjectDescription;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.dashtree.DashTreeElementContent;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;

/**
 * A DashTreeElementContent for the Solution-DashTree. It is injected into a dash-tree @see {@link SolutionsDashTree} It
 * contains a SolutionDef type (which itself internally creates a solution object) and CreateSubSolutionRelationHandler
 * which established the hierarchical relations defined by the dashtree
 *
 * @author Jochen
 */
public class SolutionDashTreeElementContent extends DashTreeElementContent {

	public SolutionDashTreeElementContent() {

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

}
