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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.kdom.rendering.StyleRenderer;

/**
 * 
 * Type for question references
 * 
 * @author Jochen
 * @created 26.07.2010
 */
public class SolutionReference extends D3webTermReference<Solution> {

	public SolutionReference() {
		super(Solution.class);
		this.setCustomRenderer(StyleRenderer.SOLUTION);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Solution getTermObjectFallback(KnowWEArticle article, Section<? extends TermReference<Solution>> s) {
		if (s.get() instanceof SolutionReference) {

			Section<SolutionReference> sec = (Section<SolutionReference>) s;
			String solutionName = sec.get().getTermIdentifier(sec);

			KnowledgeBase kb = D3webModule.getKnowledgeRepresentationHandler(
					article.getWeb()).getKB(article.getTitle());

			Solution solution = kb.getManager().searchSolution(solutionName);
			return solution;
		}
		return null;
	}

	@Override
	public String getTermObjectDisplayName() {
		return "Solution";
	}

}
