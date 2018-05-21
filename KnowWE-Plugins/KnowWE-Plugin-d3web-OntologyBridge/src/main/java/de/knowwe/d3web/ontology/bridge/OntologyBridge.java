/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.d3web.ontology.bridge;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.compile.OntologyCompiler;

/**
 * Util class providing a usable ontology during d3web compilation
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.05.18
 */
public class OntologyBridge {

	private static final Map<String, String> mapping = new HashMap<>();

	public static void registerBridge(String d3webCompileSectionID, String ontologyCompileSectionID) {
		mapping.put(d3webCompileSectionID, ontologyCompileSectionID);
	}

	public static void unregisterBridge(String d3webCompileSectionID) {
		mapping.remove(d3webCompileSectionID);
	}

	/**
	 * Provides an {@link OntologyCompiler} for the given section, if the D3webCompiler compiling this section imports
	 * an ontology. To import an ontology, use the @importOntology annotation in the %%KnowledgeBase markup to import an
	 * %%Ontology markup by the name specified by the @name annotation.
	 *
	 * @param section the section to get the bridged ontology for
	 * @return the ontology compiler bridged for the given section.
	 */
	public static OntologyCompiler getOntology(Section<?> section) {
		return getOntology(D3webUtils.getCompiler(section));
	}

	/**
	 * Provides an {@link OntologyCompiler} for the given {@link D3webCompiler}.
	 * To import an ontology, use the @importOntology annotation in the %%KnowledgeBase markup to import an
	 * %%Ontology markup by the name specified by the @name annotation.
	 *
	 * @param d3webCompiler the compiler to get the bridged ontology for
	 * @return the ontology compiler bridged for the given d3web compiler.
	 */
	public static OntologyCompiler getOntology(D3webCompiler d3webCompiler) {
		String ontologyId = mapping.get(d3webCompiler.getCompileSection().getID());
		if (ontologyId == null) throw new IllegalArgumentException("No ontology linked to the given d3web compiler");
		return Compilers.getCompiler(Sections.get(ontologyId), OntologyCompiler.class);
	}
}
