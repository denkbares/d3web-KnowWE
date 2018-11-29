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
import java.util.Optional;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.SolutionDefinition;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.xcl.XCLModel;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.LineSectionFinderNonBlankTrimmed;
import de.knowwe.kdom.sectionFinder.StringSectionFinderUnquoted;

/**
 * A type for the head of a covering-list defining the solution that is described by that list. The solution is created
 * from the term found. Further, a covering-model is created.
 *
 * @author Jochen Reutelshöfer
 */
public class ListSolutionType extends AbstractType {

	public ListSolutionType() {
		// this section finder takes all content until the opening bracket "{",
		// or the first line if there is no such bracket
		this.setSectionFinder(new RegexSectionFinder("\\A\\s*([^\\n\\r]*([^{}]*\\{)?)"));
		this.addCompileScript(Priority.HIGH, new XCLModelCreator());

		// cut indent
		// addChildType(new Indent());

		// cut the optional '{'
		this.addChildType(new AnonymousType("bracket",
				new StringSectionFinderUnquoted("{"), StyleRenderer.COMMENT));

		// split multiple solutions by comma and/or semicolon
		this.addChildType(new AnonymousType("split",
				new StringSectionFinderUnquoted(",", ";"), StyleRenderer.COMMENT));

		// and take the remaining ranges as solution definitions,
		// but also split multiple lines into individual solutions
		this.addChildType(new XCListSolutionDefinition(LineSectionFinderNonBlankTrimmed.getInstance()));
	}

	/**
	 * This handler creates the solution in the KB and also a covering-model
	 *
	 * @author Jochen Reutelshöfer
	 */
	static class XCLModelCreator implements D3webHandler<ListSolutionType> {

		@Override
		public Collection<Message> create(D3webCompiler article, Section<ListSolutionType> s) {

			Section<SolutionDefinition> solutionDef = Sections.successor(s, SolutionDefinition.class);
			Solution solution = solutionDef.get().getTermObject(article, solutionDef);
			Section<CoveringListMarkup> markup = Sections.ancestor(s, CoveringListMarkup.class);

			if (solution != null) {
				XCLModel xclModel = solution.getKnowledgeStore().getKnowledge(XCLModel.KNOWLEDGE_KIND);

				// create it lazy if not already exists
				if (xclModel == null) {
					xclModel = new XCLModel(solution);
					solution.getKnowledgeStore().addKnowledge(XCLModel.KNOWLEDGE_KIND, xclModel);
				}

				// initialize xcl model parameters
				String otherQuestions = DefaultMarkupType.getAnnotation(markup, CoveringListMarkup.OTHER_QUESTIONS);
				if (otherQuestions != null) {
					boolean considerOnlyRelevantRelations = CoveringListMarkup.OTHER_QUESTIONS_IGNORE.equalsIgnoreCase(otherQuestions);
					xclModel.setConsiderOnlyRelevantRelations(considerOnlyRelevantRelations);
				}

				// handle ESTABLISHED_THRESHOLD, SUGGESTED_THRESHOLD, and MIN_SUPPORT
				asDouble(markup, CoveringListMarkup.ESTABLISHED_THRESHOLD).ifPresent(xclModel::setEstablishedThreshold);
				asDouble(markup, CoveringListMarkup.SUGGESTED_THRESHOLD).ifPresent(xclModel::setSuggestedThreshold);
				asDouble(markup, CoveringListMarkup.MIN_SUPPORT).ifPresent(xclModel::setMinSupport);

				// set description
				String description = DefaultMarkupType.getAnnotation(markup, CoveringListMarkup.DESCRIPTION);
				if (description != null) {
					xclModel.getSolution().getInfoStore().addValue(MMInfo.DESCRIPTION, description);
				}
			}
			return null;
		}

		private Optional<Double> asDouble(Section<CoveringListMarkup> markup, String annotationName) {
			Section<?> annotation = DefaultMarkupType.getAnnotationContentSection(markup, annotationName);
			if (annotation != null) {
				String text = annotation.getText();
				try {
					return Optional.of(Double.parseDouble(text));
				}
				catch (NumberFormatException ignore) {
				}
			}
			return Optional.empty();
		}
	}
}
