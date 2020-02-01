/*
 * Copyright (C) 2019 denkbares GmbH, Germany
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
 *
 */

package de.d3web.we.kdom.xcl.list;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.xcl.XCLModel;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * This handler creates the solution in the KB and also a covering-model
 *
 * @author Jochen Reutelshöfer
 */
public class XCLModelCreator implements D3webCompileScript<XCLSolutionDefinition> {
	@Override
	public void compile(D3webCompiler compiler, Section<XCLSolutionDefinition> definition) throws CompilerMessage {

		// prepare sections and solution
		Section<DefaultMarkupType> markup = Sections.ancestor(definition, DefaultMarkupType.class);
		Solution solution = definition.get().getTermObject(compiler, definition);
		if (solution == null) {
			throw CompilerMessage.warning("Cannot create XCL model, solution is not available.");
		}

		if (isRestrictedSolution(compiler, definition)) return;

		// create it lazy if not already exists
		XCLModel xclModel = solution.getKnowledgeStore()
				.computeIfAbsent(XCLModel.KNOWLEDGE_KIND, () -> new XCLModel(solution));

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

	public boolean isRestrictedSolution(D3webCompiler compiler, Section<XCLSolutionDefinition> section) {
		return false;
	}

	private Optional<Double> asDouble(Section<DefaultMarkupType> markup, String annotationName) {
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

	@Override
	public void destroy(D3webCompiler compiler, Section<XCLSolutionDefinition> section) {
		Set<Section<?>> solutionDefinitions = new HashSet<>(compiler.getTerminologyManager()
				.getTermDefiningSections(section.get().getTermIdentifier(compiler, section)));

		solutionDefinitions.removeIf(s -> s == section || !(s.get() instanceof XCLSolutionDefinition));
		if (!solutionDefinitions.isEmpty()) return; // there are other xcl, dont remove model just yet

		Solution solution = section.get().getTermObject(compiler, section);
		if (solution == null) return;

		XCLModel model = solution.getKnowledgeStore().getKnowledge(XCLModel.KNOWLEDGE_KIND);
		if (model == null) return;
		solution.getKnowledgeStore().removeKnowledge(XCLModel.KNOWLEDGE_KIND, model);
	}

	@Override
	public boolean isIncrementalCompilationSupported(Section<XCLSolutionDefinition> section) {
		return true;
	}
}
