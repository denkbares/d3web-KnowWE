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

import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Solution;
import com.denkbares.strings.Identifier;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.SolutionDefinition;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.dashtree.DashTreeTermRelationScript;

public class SolutionTreeSolutionDefinition extends SolutionDefinition {

	public SolutionTreeSolutionDefinition() {
		this.addCompileScript(Priority.ABOVE_DEFAULT, new DashTreeTermRelationScript<D3webCompiler>() {
			@Override
			protected void createObjectRelations(Section<TermDefinition> parentSection, D3webCompiler compiler, Identifier parentIdentifier, List<Identifier> childrenIdentifier) {
				Solution parentSolution = (Solution) D3webUtils.getTermObject(compiler, parentIdentifier);
				if (parentSolution == null) return;
				TerminologyObject[] parents = parentSolution.getParents();
				if (parents.length == 0) {
					parentSolution.getKnowledgeBase().getRootSolution().addChild(parentSolution);
				}
				for (Identifier childIdentifier : childrenIdentifier) {
					Solution childSolution = (Solution) D3webUtils.getTermObject(compiler, childIdentifier);
					if (childSolution == null) continue;
					parentSolution.getKnowledgeBase().getRootSolution().removeChild(childSolution);
					parentSolution.addChild(childSolution);
				}
			}

			@Override
			public Class<D3webCompiler> getCompilerClass() {
				return D3webCompiler.class;
			}
		});
	}

}
