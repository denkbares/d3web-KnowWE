/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.d3web.ontology.bridge;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.denkbares.collections.MultiMap;
import com.denkbares.collections.N2MMap;
import com.denkbares.utils.Log;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
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

	private static final MultiMap<String, String> mapping = new N2MMap<>();

	public static void registerBridge(String d3webCompileSectionID, String ontologyCompileSectionID) {
		mapping.put(d3webCompileSectionID, ontologyCompileSectionID);
	}

	public static void unregisterBridge(String d3webCompileSectionID) {
		mapping.removeKey(d3webCompileSectionID);
	}

	/**
	 * Provides all {@link OntologyCompiler}s for the given section, of the D3webCompilers compiling this section, and
	 * if any of them imports an ontology. To import an ontology, use the @importOntology annotation in the
	 * %%KnowledgeBase markup to import an %%Ontology markup by the name specified by the @name annotation.
	 *
	 * @param section the section to get the bridged ontology for
	 * @return the ontology compilers bridged for the given section
	 */
	public static Set<OntologyCompiler> getOntologies(Section<?> section) {
		return getOntologies(Compilers.getCompilers(section, D3webCompiler.class));
	}

	/**
	 * Provides all {@link OntologyCompiler}s that are bridged to the given {@link D3webCompiler}s. To import an
	 * ontology, use the <tt>importOntology</tt> annotation in the %%KnowledgeBase markup to import an %%Ontology markup
	 * by the name specified by the @name annotation.
	 *
	 * @param d3webCompiler the compiler to get the bridged ontology for
	 * @return the ontology compiler bridged for the given d3web compiler the @name annotation.
	 */
	public static Set<OntologyCompiler> getOntologies(Collection<D3webCompiler> d3webCompiler) {
		return d3webCompiler.stream().map(OntologyBridge::getOntology).collect(Collectors.toSet());
	}

	/**
	 * Provides an {@link OntologyCompiler} for the given section, if the D3webCompiler compiling this section imports
	 * an ontology. To import an ontology, use the @importOntology annotation in the %%KnowledgeBase markup to import an
	 * %%Ontology markup by the name specified by the @name annotation.
	 *
	 * @param section the section to get the bridged ontology for
	 * @return the ontology compiler bridged for the given section
	 */
	public static OntologyCompiler getOntology(Section<?> section) {
		return getOntology(D3webUtils.getCompiler(section));
	}

	/**
	 * Provides an {@link OntologyCompiler} for the given {@link D3webCompiler}. To import an ontology, use the
	 * <tt>importOntology</tt> annotation in the %%KnowledgeBase markup to import an %%Ontology markup by the name
	 * specified by the @name annotation.
	 *
	 * @param d3webCompiler the compiler to get the bridged ontology for
	 * @return the ontology compiler bridged for the given d3web compiler the @name annotation.
	 */
	public static OntologyCompiler getOntology(D3webCompiler d3webCompiler) {
		return getOntology(d3webCompiler, Priority.ABOVE_DEFAULT);
	}

	/**
	 * Provides an {@link OntologyCompiler} for the given {@link D3webCompiler}. To import an ontology, use the
	 * <tt>importOntology</tt> annotation in the %%KnowledgeBase markup to import an %%Ontology markup by the name
	 * specified by the @name annotation.
	 *
	 * @param d3webCompiler   the compiler to get the bridged ontology for
	 * @param priorityToAwait if the bridged compiler is currently compiling, we wait until the given priority is done
	 *                        in the compiler
	 * @return the ontology compiler bridged for the given d3web compiler the @name annotation.
	 */
	public static OntologyCompiler getOntology(D3webCompiler d3webCompiler, Priority priorityToAwait) {
		String ontologyId = mapping.getAnyValue(d3webCompiler.getCompileSection().getID());
		if (ontologyId == null) throw new IllegalArgumentException("No ontology linked to the given d3web compiler");
		OntologyCompiler compiler = Compilers.getCompiler(Sections.get(ontologyId), OntologyCompiler.class);
		if (compiler == null) throw new IllegalStateException("Ontology compiler not yet available");
		try {
			compiler.getCompilerManager().awaitCompilePriorityCompleted(compiler, priorityToAwait);
		}
		catch (InterruptedException e) {
			Log.severe("Interrupted while waiting", e);
		}
		return compiler;
	}

	/**
	 * Returns true if there is an imported {@link OntologyCompiler} bridged by the given {@link D3webCompiler}. To
	 * import an ontology, use the @importOntology annotation in the %%KnowledgeBase markup to import an %%Ontology
	 * markup by the name specified by the @name annotation.
	 *
	 * @param d3webCompiler the compiler to get the bridged ontology for
	 * @return if there is an ontology compiler bridged for the given d3web compiler
	 */
	public static boolean hasOntology(D3webCompiler d3webCompiler) {
		String ontologyId = mapping.getAnyValue(d3webCompiler.getCompileSection().getID());
		if (ontologyId == null) return false;
		return Compilers.getCompiler(Sections.get(ontologyId), OntologyCompiler.class) != null;
	}

	/**
	 * Provides the {@link D3webCompiler} that links to the given {@link OntologyCompiler}. This is the reverse of the
	 * method {@link #getOntology(D3webCompiler)}.
	 *
	 * @param ontologyCompiler the ontology compiler for which to get the linking d3web compiler
	 * @return the d3web compiler linking to the given ontology compiler
	 */
	public static D3webCompiler getCompiler(OntologyCompiler ontologyCompiler) {
		String d3webId = mapping.getAnyKey(ontologyCompiler.getCompileSection().getID());
		if (d3webId == null) {
			throw new IllegalArgumentException("The given ontology is not linked to any d3web compiler");
		}
		D3webCompiler compiler = Compilers.getCompiler(Sections.get(d3webId), D3webCompiler.class);
		return compiler;
	}
}
