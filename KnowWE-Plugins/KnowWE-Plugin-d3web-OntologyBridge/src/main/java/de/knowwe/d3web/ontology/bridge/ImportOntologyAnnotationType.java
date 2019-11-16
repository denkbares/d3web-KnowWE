/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.d3web.ontology.bridge;

import de.d3web.we.knowledgebase.KnowledgeBaseType;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyDefinition;
import de.knowwe.ontology.compile.OntologyReference;
import de.knowwe.ontology.compile.OntologyType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Annotation to make an ontology of an existing %%Ontology markup usable during d3web compilation.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.05.18
 */
public class ImportOntologyAnnotationType extends OntologyReference {

	private static final String KEY_BRIDGE = ImportOntologyAnnotationType.class.getName() + "#bridge";

	public ImportOntologyAnnotationType() {
		setRenderer(StyleRenderer.PACKAGE);
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		addCompileScript(new ImportOntologyCompileScript());
	}

	public static class ImportOntologyCompileScript extends DefaultGlobalCompiler.DefaultGlobalScript<ImportOntologyAnnotationType> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<ImportOntologyAnnotationType> section) throws CompilerMessage {
			Section<? extends PackageCompileType> ontologyCompileSection = getOntologyCompileSection(section);
			if (ontologyCompileSection == null) {
				throw CompilerMessage.error("Ontology with name '" + section.getText() + "' could not be found.");
			}
			OntologyType.setCompilerPriority(ontologyCompileSection, 4);
			Section<? extends PackageCompileType> d3webCompileSection = getD3webCompileSection(section);
			OntologyBridge.registerBridge(d3webCompileSection.getID(), ontologyCompileSection.getID());
			section.storeObject(compiler, KEY_BRIDGE, ontologyCompileSection);
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<ImportOntologyAnnotationType> section) {
			// we do not search for the referenced section itself, because during destroy it might probably not be available
			// instead we have stored the section, and use exactly the section the has been used to register the bridge
			//noinspection unchecked
			Section<? extends PackageCompileType> ontologyCompileSection =
					(Section<? extends PackageCompileType>) section.removeObject(compiler, KEY_BRIDGE);
			if (ontologyCompileSection == null) return;
			OntologyType.resetCompilerPriority(ontologyCompileSection);
			Section<? extends PackageCompileType> d3webCompileSection = getD3webCompileSection(section);
			OntologyBridge.unregisterBridge(d3webCompileSection.getID());
		}

		private Section<? extends PackageCompileType> getOntologyCompileSection(Section<ImportOntologyAnnotationType> section) {
			return section.get().getDefinition(section).map(OntologyDefinition::getCompiler)
					.map(OntologyCompiler::getCompileSection).findFirst().orElse(null);
		}

		private Section<? extends PackageCompileType> getD3webCompileSection(Section<ImportOntologyAnnotationType> section) {
			return $(section).ancestor(KnowledgeBaseType.class).cast(PackageCompileType.class).getFirst();
		}
	}
}
