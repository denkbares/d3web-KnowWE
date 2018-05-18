/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.d3web.ontology.bridge;

import java.util.Arrays;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Annotation to make an ontology of an existing %%Ontology markup usable during d3web compilation.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.05.18
 */
public class ImportOntologyAnnotationType extends AbstractType {

	public ImportOntologyAnnotationType() {
		setRenderer(StyleRenderer.PACKAGE);
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		addCompileScript(new ImportOntologyCompileScript());
	}

	public static class ImportOntologyCompileScript extends DefaultGlobalCompiler.DefaultGlobalScript<ImportOntologyAnnotationType> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<ImportOntologyAnnotationType> section) throws CompilerMessage {
			Section<PackageCompileType> ontologyCompileSection = getOntologyCompileSection(section);
			if (ontologyCompileSection == null) {
				throw CompilerMessage.error("Ontology with name '" + section.getText() + "' could not be found.");
			}
			OntologyType.setCompilerPriority(ontologyCompileSection, 4);
		}

		private Section<PackageCompileType> getOntologyCompileSection(Section<ImportOntologyAnnotationType> section) {
			String importedOntologyName = section.getText();
			// TODO: maybe register %%Ontology markups by name, so we don't have to search the complete wiki...
			return $(section.getArticleManager()).successor(OntologyType.class)
					.filter(s -> Arrays.asList(DefaultMarkupType.getAnnotations(s, "name"))
							.contains(importedOntologyName))
					.successor(PackageCompileType.class).getFirst();
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<ImportOntologyAnnotationType> section) {
			Section<PackageCompileType> ontologyCompileSection = getOntologyCompileSection(section);
			if (ontologyCompileSection == null) return;
			OntologyType.resetCompilerPriority(ontologyCompileSection);
		}
	}
}
