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

package de.d3web.we.kdom.xcl.list;

import java.util.Collection;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.kdom.rules.Indent;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.SolutionDefinition;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.xcl.XCLModel;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.sectionFinder.NonEmptyLineSectionFinder;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * @author Jochen
 * 
 *         A type for the head of a covering-list defining the solution that is
 *         described by that list. The solution is created from the term found.
 *         Further, a covering-model is created.
 * 
 * 
 */
public class ListSolutionType extends AbstractType {

	public ListSolutionType() {
		ConstraintSectionFinder solutionFinder = new ConstraintSectionFinder(
				new NonEmptyLineSectionFinder());
		solutionFinder.addConstraint(AtMostOneFindingConstraint.getInstance());
		this.setSectionFinder(solutionFinder);

		this.addCompileScript(Priority.HIGH, new XCLModelCreator());

		// cut indent
		addChildType(new Indent());

		// cut the optional '{'
		AnonymousType closing = new AnonymousType("bracket");
		closing.setSectionFinder(new StringSectionFinderUnquoted("{"));
		this.addChildType(closing);

		XCListSolutionDefinition solDef = new XCListSolutionDefinition();
		ConstraintSectionFinder allFinder = new ConstraintSectionFinder(new AllTextFinderTrimmed());
		allFinder.addConstraint(AtMostOneFindingConstraint.getInstance());
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
	class XCLModelCreator implements D3webHandler<ListSolutionType> {

		@Override
		public Collection<Message> create(D3webCompiler article, Section<ListSolutionType> s) {

			Section<SolutionDefinition> solutionDef = Sections.successor(s,
					SolutionDefinition.class);

			Solution solution = solutionDef.get().getTermObject(article, solutionDef);

			Section<CoveringListMarkup> defaultMarkupType = Sections.ancestor(s,
					CoveringListMarkup.class);

			if (solution != null) {
				XCLModel xclModel = solution.getKnowledgeStore().getKnowledge(
						XCLModel.KNOWLEDGE_KIND);

				// create it lazy if not already exists
				if (xclModel == null) {
					xclModel = new XCLModel(solution);
					solution.getKnowledgeStore().addKnowledge(XCLModel.KNOWLEDGE_KIND, xclModel);
				}

				// initialize xcl model parameters
				String otherQuestions = DefaultMarkupType.getAnnotation(defaultMarkupType,
						CoveringListMarkup.OTHER_QUESTIONS);
				if (otherQuestions != null) {
					boolean considerOnlyRelevantRelations = CoveringListMarkup.OTHER_QUESTIONS_IGNORE.equalsIgnoreCase(otherQuestions);
					xclModel.setConsiderOnlyRelevantRelations(considerOnlyRelevantRelations);
				}
				setThresholdsAndMinSupport(defaultMarkupType, xclModel);
				String description = DefaultMarkupType.getAnnotation(defaultMarkupType,
						CoveringListMarkup.DESCRIPTION);
				if (description != null) {
					xclModel.getSolution().getInfoStore().addValue(MMInfo.DESCRIPTION, description);
				}
			}
			return null;
		}

		/**
		 * reads out the respective annotations for suggestedThreshold,
		 * establishedThreshold and minSupport and sets them in the XCLModel if
		 * existing
		 * 
		 * @param defaultMarkupType
		 * @param m
		 */
		private void setThresholdsAndMinSupport(Section<CoveringListMarkup> defaultMarkupType, XCLModel m) {

			// handle ESTABLISHED_THRESHOLD
			Section<? extends AnnotationContentType> estaAnnoSection = DefaultMarkupType.getAnnotationContentSection(
					defaultMarkupType,
					CoveringListMarkup.ESTABLISHED_THRESHOLD);

			if (estaAnnoSection != null) {
				String estaText = estaAnnoSection.getText();
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
			Section<? extends AnnotationContentType> suggAnnoSection = DefaultMarkupType.getAnnotationContentSection(
					defaultMarkupType,
					CoveringListMarkup.SUGGESTED_THRESHOLD);

			if (suggAnnoSection != null) {
				String suggText = suggAnnoSection.getText();
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
			Section<? extends AnnotationContentType> minAnnoSection = DefaultMarkupType.getAnnotationContentSection(
					defaultMarkupType,
					CoveringListMarkup.MIN_SUPPORT);
			if (minAnnoSection != null) {
				String minText = minAnnoSection.getText();
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
