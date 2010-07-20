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

package de.d3web.we.kdom.xcl.list;

import java.util.Collection;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.constraint.ExactlyOneFindingConstraint;
import de.d3web.we.kdom.defaultMarkup.AnnotationType;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.objects.SolutionDefinition;
import de.d3web.we.kdom.objects.TermRelationDefinition;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.NonEmptyLineSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.inference.PSMethodXCL;

/**
 * @author Jochen
 *
 *         A type for the head of a covering-list defining the solution that is
 *         described by that list. The solution is created from the term found.
 *         Further, a covering-model is created.
 *
 *
 */
public class ListSolutionType extends TermRelationDefinition {

	public ListSolutionType() {
		SectionFinder solutionFinder = new NonEmptyLineSectionFinder();
		solutionFinder.addConstraint(ExactlyOneFindingConstraint.getInstance());
		this.setSectionFinder(solutionFinder);

		this.addSubtreeHandler(Priority.HIGH, new XCLModelCreator());

		// cut the optional '{'
		AnonymousType closing = new AnonymousType("bracket");
		closing.setSectionFinder(new StringSectionFinderUnquoted("{"));
		this.addChildType(closing);

		SolutionDefinition solDef = new SolutionDefinition();
		SectionFinder allFinder = new AllTextFinderTrimmed();
		allFinder.addConstraint(ExactlyOneFindingConstraint.getInstance());
		solDef.setSectionFinder(allFinder);
		this.addChildType(solDef);
	}

	/**
	 * @author Jochen
	 *
	 *         This handler creates the solution in the KB and also a
	 *         covering-model
	 *
	 */
	class XCLModelCreator extends D3webSubtreeHandler<ListSolutionType> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ListSolutionType> s) {

			Section<SolutionDefinition> solutionDef = s.findSuccessor(SolutionDefinition.class);
			Solution solution = solutionDef.get().getTermObject(article, solutionDef);

			Section<DefaultMarkupType> defaultMarkupType = s.findAncestor(DefaultMarkupType.class);

			if (solution != null) {
				KnowledgeSlice xclModel = solution.getKnowledge(PSMethodXCL.class,
						XCLModel.XCLMODEL);
				if (xclModel == null) {
					XCLModel m = new XCLModel(solution);

					setThresholdsAndMinSupport(defaultMarkupType, m);

					solution.addKnowledge(PSMethodXCL.class, m, XCLModel.XCLMODEL);
				}
			}
			return null;
		}

		@Override
		public void destroy(KnowWEArticle article, Section<ListSolutionType> s) {
			// nothing to do, the solution, along with its attached model, will
			// be destroyed in the SolutionDef
			return;
		}

		/**
		 * reads out the respective annotations for suggestedThreshold,
		 * establishedThreshold and minSupport and sets them in the XCLModel if
		 * existing
		 *
		 * @param defaultMarkupType
		 * @param m
		 */
		private void setThresholdsAndMinSupport(Section<DefaultMarkupType> defaultMarkupType, XCLModel m) {


			// handle ESTABLISHED_THRESHOLD
			Section<? extends AnnotationType> estaAnnoSection = DefaultMarkupType.getAnnotationSection(
					defaultMarkupType,
					CoveringListMarkup.ESTABLISHED_THRESHOLD);

			if (estaAnnoSection != null) {
				String estaText = estaAnnoSection.getOriginalText();
				// set estaThreashold if defined
				if (estaText != null) {
					try {
						Double estaThreshold = Double.parseDouble(estaText);
						m.setEstablishedThreshold(estaThreshold);
					}
					catch (NumberFormatException e) {

					}
				}

			}

			// handle SUGGESTED_THRESHOLD
			Section<? extends AnnotationType> suggAnnoSection = DefaultMarkupType.getAnnotationSection(
					defaultMarkupType,
					CoveringListMarkup.SUGGESTED_THRESHOLD);

			if (suggAnnoSection != null) {
				String suggText = suggAnnoSection.getOriginalText();
				// set suggThreashold if defined
				if (suggText != null) {
					try {
						Double suggThreashold = Double.parseDouble(suggText);
						m.setSuggestedThreshold(suggThreashold);
					}
					catch (NumberFormatException e) {

					}
				}
			}

			// handle MIN_SUPPORT
			Section<? extends AnnotationType> minAnnoSection = DefaultMarkupType.getAnnotationSection(
					defaultMarkupType,
					CoveringListMarkup.MIN_SUPPORT);
			if (minAnnoSection != null) {
				String minText = minAnnoSection.getOriginalText();
				// set minSupport if defined
				if (minText != null) {
					try {
						Double minSupport = Double.parseDouble(minText);
						m.setMinSupport(minSupport);
					}
					catch (NumberFormatException e) {

					}
				}
			}

		}

	}

}
