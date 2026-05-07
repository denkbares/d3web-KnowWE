/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.d3web.ontology.bridge;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.MultiMap;
import com.denkbares.collections.N2MMap;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.CompilationLocal;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageCompiler;
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

	/**
	 * Throttles {@link #logBridgeFailure} so a single root cause does not flood the log.
	 * Keyed on (reason, callerSectionId, mappedTargetId); the value is the timestamp of the last
	 * verbose log. Within the throttle window we only count suppressions; the next verbose log
	 * after the window includes the suppression count.
	 */
	private static final long LOG_THROTTLE_MILLIS = 60_000L;
	private static final ConcurrentMap<String, AtomicLong> lastLogMillisByKey = new ConcurrentHashMap<>();
	private static final ConcurrentMap<String, AtomicLong> suppressedSinceLastLog = new ConcurrentHashMap<>();

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
		String d3webSectionId = d3webCompiler.getCompileSection().getID();
		String ontologyId = mapping.getAnyValue(d3webSectionId);
		if (ontologyId == null) {
			logBridgeFailure("getOntology - no ontology linked", d3webCompiler, d3webSectionId, null);
			throw new IllegalArgumentException("No ontology linked to the given d3web compiler: " + Compilers.getCompilerName(d3webCompiler));
		}
		OntologyCompiler compiler = getOntologyCompilerCached(d3webCompiler, ontologyId);
		if (compiler == null) {
			logBridgeFailure("getOntology - ontology compiler not yet available", d3webCompiler, d3webSectionId, ontologyId);
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
		String ontologySectionId = ontologyCompiler.getCompileSection().getID();
		String d3webId = mapping.getAnyKey(ontologySectionId);
		if (d3webId == null) {
			logBridgeFailure("getCompiler - ontology not linked", ontologyCompiler, ontologySectionId, null);
			throw new IllegalArgumentException("The given ontology is not linked to any d3web compiler: " + Compilers.getCompilerName(ontologyCompiler));
		}
		Section<?> section = Sections.get(d3webId);
		if (section == null) {
			logBridgeFailure("getCompiler - mapped d3web section not found", ontologyCompiler, ontologySectionId, d3webId);
			throw new IllegalStateException("Bridge mapping outdated, section for D3webCompiler not found");
		}
		D3webCompiler compiler = CompilationLocal.getCached(ontologyCompiler, D3webCompiler.class + ":" + d3webId,
				() -> Compilers.getCompiler(section, D3webCompiler.class));
		if (compiler == null) {
			// should not happen
			logBridgeFailure("getCompiler - mapping not up to date", ontologyCompiler, ontologySectionId, d3webId);
			throw new IllegalArgumentException("Mapping not up to date, this is probably a failure of the OntologyBridge");
		}
		return compiler;
	}

	/**
	 * Diagnostic logging for the OntologyBridge throw-paths. Captures everything we need to tell apart
	 * the suspected root causes (stale mapping, race during initial compile, attachment-update lifecycle,
	 * concurrent access from non-compile threads, etc.) without changing the throw behaviour.
	 */
	private static void logBridgeFailure(String reason, Object caller, String callerSectionId, String mappedTargetId) {
		try {
			String throttleKey = reason + '|' + callerSectionId + '|' + mappedTargetId;
			long now = System.currentTimeMillis();
			AtomicLong lastLog = lastLogMillisByKey.computeIfAbsent(throttleKey, k -> new AtomicLong(0L));
			long previous = lastLog.get();
			if (now - previous < LOG_THROTTLE_MILLIS) {
				suppressedSinceLastLog.computeIfAbsent(throttleKey, k -> new AtomicLong(0L)).incrementAndGet();
				return;
			}
			if (!lastLog.compareAndSet(previous, now)) {
				// another thread just logged — count this as suppressed
				suppressedSinceLastLog.computeIfAbsent(throttleKey, k -> new AtomicLong(0L)).incrementAndGet();
				return;
			}
			long suppressedCount = suppressedSinceLastLog.computeIfAbsent(throttleKey, k -> new AtomicLong(0L)).getAndSet(0L);

			Thread thread = Thread.currentThread();
			boolean isCompileThread = CompilerManager.isCompileThread();
			Section<?> callerSection = Sections.get(callerSectionId);
			Section<?> mappedSection = mappedTargetId == null ? null : Sections.get(mappedTargetId);

			StringBuilder sb = new StringBuilder();
			sb.append("OntologyBridge failure: ").append(reason);
			if (suppressedCount > 0) {
				sb.append(" [suppressed ").append(suppressedCount)
						.append(" similar within last ").append(LOG_THROTTLE_MILLIS / 1000).append("s]");
			}
			sb.append('\n');
			sb.append("  caller             = ").append(describeCompiler(caller)).append('\n');
			sb.append("  caller sectionId   = ").append(callerSectionId)
					.append(" (").append(describeSection(callerSection)).append(")\n");
			sb.append("  mapped targetId    = ").append(mappedTargetId)
					.append(" (").append(describeSection(mappedSection)).append(")\n");
			sb.append("  thread             = ").append(thread.getName())
					.append(" (compileThread=").append(isCompileThread).append(")\n");
			sb.append("  mapping size       = ").append(mapping.size()).append('\n');
			sb.append("  mapping entries    = ").append(mapping.entrySet()).append('\n');
			CompilerManager cm = caller instanceof Compiler c ? c.getCompilerManager() : null;
			if (cm != null) {
				sb.append("  compilerManager.isCompiling = ").append(cm.isCompiling()).append('\n');
				sb.append("  active D3webCompilers       =\n");
				cm.getCompilers().stream().filter(D3webCompiler.class::isInstance)
						.forEach(c -> appendCompilerVsSection(sb, (Compiler) c, callerSection));
				sb.append("  active OntologyCompilers    =\n");
				cm.getCompilers().stream().filter(OntologyCompiler.class::isInstance)
						.forEach(c -> appendCompilerVsSection(sb, (Compiler) c, mappedSection));
			}
			LOGGER.error(sb.toString(), new Throwable("OntologyBridge failure stacktrace (informational)"));
		}
		catch (Throwable t) {
			// never let diagnostics break the actual throw path
			LOGGER.warn("OntologyBridge failure diagnostic logging itself failed: {}", t.toString());
		}
	}

	private static String describeCompiler(Object o) {
		if (o == null) return "null";
		if (o instanceof Compiler c) {
			String name = Compilers.getCompilerName(c);
			String secDesc;
			try {
				secDesc = c instanceof PackageCompiler pc ? describeSection(pc.getCompileSection()) : "?";
			}
			catch (Throwable t) {
				secDesc = "?";
			}
			return c.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(c))
					+ "[" + name + ", section=" + secDesc + "]";
		}
		return o.toString();
	}

	/**
	 * Renders a section as "id@<identityHash>(live=<bool>)" so we can tell apart different Java
	 * instances that share the same logical ID (Section IDs can be re-bound to a new instance via
	 * {@code Section.unregisterOrUpdateSectionID} after an article rebuild).
	 */
	private static String describeSection(Section<?> section) {
		if (section == null) return "null";
		String id;
		try {
			id = section.getID();
		}
		catch (Throwable t) {
			id = "?";
		}
		return id + "@" + Integer.toHexString(System.identityHashCode(section))
				+ "(live=" + Sections.isLive(section) + ")";
	}

	/**
	 * Per-compiler diagnostic line that compares the compile-section the compiler holds against the
	 * section the bridge actually wants to find. Reveals stale-instance issues where the IDs match
	 * but the Java identity does not.
	 */
	private static void appendCompilerVsSection(StringBuilder sb, Compiler compiler, Section<?> bridgeTargetSection) {
		sb.append("    ").append(describeCompiler(compiler));
		if (bridgeTargetSection == null || !(compiler instanceof PackageCompiler pc)) {
			sb.append('\n');
			return;
		}
		try {
			Section<?> compilerSection = pc.getCompileSection();
			boolean identityMatch = compilerSection == bridgeTargetSection;
			boolean idMatch = compilerSection.getID().equals(bridgeTargetSection.getID());
			boolean articleMatch = compilerSection.getArticle() == bridgeTargetSection.getArticle();
			boolean isCompilingResult;
			try {
				isCompilingResult = compiler.isCompiling(bridgeTargetSection);
			}
			catch (Throwable t) {
				isCompilingResult = false;
			}
			sb.append(" vs bridgeTarget: identityMatch=").append(identityMatch)
					.append(", idMatch=").append(idMatch)
					.append(", articleMatch=").append(articleMatch)
					.append(", isCompiling=").append(isCompilingResult);
		}
		catch (Throwable t) {
			sb.append(" (comparison failed: ").append(t).append(")");
		}
		sb.append('\n');
	}
}
