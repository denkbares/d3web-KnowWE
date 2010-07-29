/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.kdom.objects;


import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.IncrementalConstraints;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;

/**
 * 
 * Type for the definition of questions
 * 
 * @author Jochen/Albrecht
 * @created 26.07.2010 
 */
public abstract class SolutionDefinition
		extends D3webTermDefinition<Solution>
		implements IncrementalConstraints {

	public SolutionDefinition() {
		super(Solution.class);
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
		this.addSubtreeHandler(Priority.HIGHEST, new CreateSolutionHandler());
	}

	@Override
	public boolean hasViolatedConstraints(KnowWEArticle article, Section<?> s) {
		return false;
	}

	static class CreateSolutionHandler extends D3webSubtreeHandler<SolutionDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<SolutionDefinition> solutionSection) {


			String name = solutionSection.get().getTermName(solutionSection);

			KnowledgeBaseManagement mgn = getKBM(article);
			if (mgn == null) return null;

			IDObject o = mgn.findSolution(name);

			if (o != null) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(o.getClass()
						.getSimpleName()));
			} else {




				Solution s = mgn.createSolution(name);

				if (s != null) {
					// ok everything went well
					// register term
					KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
							article, solutionSection);
					solutionSection.get().storeTermObject(article, solutionSection, s);
					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(s.getClass().getSimpleName()
							+ " " + s.getName()));
				} else {
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(name, this.getClass()));
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
