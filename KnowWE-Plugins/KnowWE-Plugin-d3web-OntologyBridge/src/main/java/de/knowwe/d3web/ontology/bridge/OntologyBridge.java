/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.d3web.ontology.bridge;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.MultiMap;
import com.denkbares.collections.N2MMap;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.CompilationLocal;
import de.knowwe.core.compile.CompilerManager;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyBridge.class);

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
	@NotNull
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
	@NotNull
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
	@NotNull
	public static OntologyCompiler getOntology(Section<?> section) {
		final D3webCompiler compiler = D3webUtils.getCompiler(section);
		if (compiler == null) return null;
		return getOntology(compiler);
	}

	/**
	 * Provides an {@link OntologyCompiler} for the given {@link D3webCompiler}. To import an ontology, use the
	 * <tt>importOntology</tt> annotation in the %%KnowledgeBase markup to import an %%Ontology markup by the name
	 * specified by the @name annotation.
	 *
	 * @param d3webCompiler the compiler to get the bridged ontology for
	 * @return the ontology compiler bridged for the given d3web compiler the @name annotation.
	 */
	@NotNull
	public static OntologyCompiler getOntology(@NotNull D3webCompiler d3webCompiler) {
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
	@NotNull
	public static OntologyCompiler getOntology(@NotNull D3webCompiler d3webCompiler, Priority priorityToAwait) {
		String ontologyId = mapping.getAnyValue(d3webCompiler.getCompileSection().getID());
		if (ontologyId == null) {
			throw new IllegalArgumentException("No ontology linked to the given d3web compiler: " + Compilers.getCompilerName(d3webCompiler));
		}
		OntologyCompiler compiler = getOntologyCompilerCached(d3webCompiler, ontologyId);
		if (compiler == null) {
			throw new IllegalStateException("Ontology compiler not yet available for d3web compiler: " + Compilers.getCompilerName(d3webCompiler));
		}
		try {
			compiler.getCompilerManager().awaitCompilePriorityCompleted(compiler, priorityToAwait);
		}
		catch (InterruptedException e) {
			if (CompilerManager.isCompileThread()) {
				LOGGER.error("Interrupted while waiting", e);
			} else {
				LOGGER.info("Interrupted while waiting for compiler in CompilerBridge");
			}
		}
		return compiler;
	}

	private static OntologyCompiler getOntologyCompilerCached(@NotNull D3webCompiler d3webCompiler, String ontologyId) {
		return CompilationLocal.getCached(d3webCompiler, OntologyCompiler.class + ":" + ontologyId,
				() -> Compilers.getCompiler(Sections.get(ontologyId), OntologyCompiler.class));
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
		if (d3webCompiler == null) return false;
		String ontologyId = mapping.getAnyValue(d3webCompiler.getCompileSection().getID());
		return ontologyId != null && getOntologyCompilerCached(d3webCompiler, ontologyId) != null;
	}

	/**
	 * Provides the {@link D3webCompiler} that links to the given {@link OntologyCompiler}. This is the reverse of the
	 * method {@link #getOntology(D3webCompiler)}.
	 * <p>
	 * <b>Attention: Do not use this method in a CompileScript or in util methods that are commonly used in
	 * CompileScripts!</b><br>
	 * If the method is used in CompileScripts and further on in the stack {@link #getOntology(Section)} is called, the
	 * OntologyCompiler might wait on itself and cause a deadlock.<br>
	 * If it the method has to be used, at least make sure, that {@link #getOntology(Section)} is not used further on in
	 * the stack with the {@link D3webCompiler} returned in this method.
	 * </p>
	 *
	 * @param ontologyCompiler the ontology compiler for which to get the linking d3web compiler
	 * @return the d3web compiler linking to the given ontology compiler
	 */
	@NotNull
	public static D3webCompiler getCompiler(@NotNull OntologyCompiler ontologyCompiler) {
		String d3webId = mapping.getAnyKey(ontologyCompiler.getCompileSection().getID());
		if (d3webId == null) {
			throw new IllegalArgumentException("The given ontology is not linked to any d3web compiler: " + Compilers.getCompilerName(ontologyCompiler));
		}
		Section<?> section = Sections.get(d3webId);
		if (section == null) {
			throw new IllegalStateException("Bridge mapping outdated, section for D3webCompiler not found");
		}
		D3webCompiler compiler = CompilationLocal.getCached(ontologyCompiler, D3webCompiler.class + ":" + d3webId,
				() -> Compilers.getCompiler(section, D3webCompiler.class));
		if (compiler == null) {
			// should not happen
			throw new IllegalArgumentException("Mapping not up to date, this is probably a failure of the OntologyBridge");
		}
		return compiler;
	}
}
