/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.d3web.we.knowledgebase;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.denkbares.events.EventManager;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.NamedCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.ScriptCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.objects.SimpleDefinitionRegistrationScript;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Compiles d3web knowledge bases.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.11.2013
 */
public class D3webCompiler extends AbstractPackageCompiler implements TermCompiler, NamedCompiler, IncrementalCompiler {

	private TerminologyManager terminologyManager;
	private KnowledgeBase knowledgeBase;
	private final Section<? extends PackageCompileType> compileSection;
	private final boolean caseSensitive;
	private D3webScriptCompiler compileScriptCompiler;
	private D3webScriptCompiler destroyScriptCompiler;
	private final boolean allowIncrementalCompilation = true;
	private boolean isIncrementalBuild = false;

	public D3webCompiler(PackageManager packageManager,
						 Section<? extends PackageCompileType> compileSection,
						 Class<? extends Type> compilingType,
						 boolean caseSensitive) {
		super(packageManager, compileSection, compilingType);
		this.compileSection = compileSection;
		this.caseSensitive = caseSensitive;
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public TerminologyManager getTerminologyManager() {
		if (terminologyManager == null) {
			// in case the compiler doesn't have anything to compile...
			return new TerminologyManager();
		}
		return terminologyManager;
	}

	public KnowledgeBase getKnowledgeBase() {
		if (knowledgeBase == null) {
			return KnowledgeBaseUtils.createKnowledgeBase();
		}
		return knowledgeBase;
	}

	/**
	 * Returns true, if the last compilation of the compiler was done incrementally, false otherwise
	 */
	public boolean isIncrementalBuild() {
		return isIncrementalBuild;
	}

	@NotNull
	@Override
	public Section<KnowledgeBaseType> getCompileSection() {
		return Sections.cast(compileSection, KnowledgeBaseType.class);
	}

	/**
	 * Returns the name of this compiler, normally given in the content %%KnowledgeBase section.
	 */
	@Override
	public String getName() {
		return $(getCompileSection()).successor(KnowledgeBaseDefinition.class)
				.stream().map(s -> s.get().getTermName(s)).findAny()
				.orElseGet(() -> getCompileSection().getTitle());
	}

	/**
	 * FIXME: This method is currently only needed by the AnnotationLoadKnowledgeBaseHandler where a knowledge base is
	 * loaded from a file. The better way would be though to instead fill an existing knowledge base with the contents
	 * read from the file. We should implement this later and then remove this method.
	 *
	 * @created 06.01.2014
	 */
	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	@Override
	public void compilePackages(String[] packagesToCompile) {

		if (!tryIncrementalCompilation(packagesToCompile)) {
			fullCompilation(packagesToCompile);
		}

		EventManager.getInstance().fireEvent(new D3webCompilerFinishedEvent(this));
	}

	/**
	 * Try to compile incrementally. This can fail, if some scripts can (not yet) handle incremental compilation. In
	 * this case, the method returns false.
	 *
	 * @param packagesToCompile the packages to try to compile incrementally
	 * @return true if the incremental compilation was successful, false otherwise
	 */
	private boolean tryIncrementalCompilation(String[] packagesToCompile) {
		if (knowledgeBase == null) return false; // first compilation, no need to do checking
		if (!allowIncrementalCompilation) return false;

		this.compileScriptCompiler = new D3webScriptCompiler(this);
		this.destroyScriptCompiler = new D3webScriptCompiler(this, true);

		Collection<Section<?>> removedSections = getPackageManager().getRemovedSections(packagesToCompile);
		this.destroyScriptCompiler.addSections(removedSections);
		if (!destroyScriptCompiler.isIncrementalCompilationPossible()) {
			logAndCleanup();
			return false;
		}

		Collection<Section<?>> addedSections = getPackageManager().getAddedSections(packagesToCompile);
		this.compileScriptCompiler.addSections(addedSections);
		if (!destroyScriptCompiler.isIncrementalCompilationPossible()) {
			logAndCleanup();
			return false;
		}

		destroyScriptCompiler.destroy();
		compileScriptCompiler.compile();

		isIncrementalBuild = true;

		logAndCleanup();
		return true;
	}

	private void logAndCleanup() {
		logFailingIncrementalCompilationScripts();

		this.compileScriptCompiler = null;
		this.destroyScriptCompiler = null;
	}

	@SuppressWarnings("rawtypes")
	private void logFailingIncrementalCompilationScripts() {
		Set<Class<? extends CompileScript>> failedDestroyScripts = destroyScriptCompiler.getCompileScriptsNotSupportingIncrementalCompilation();
		Set<Class<? extends CompileScript>> failedCompileScripts = compileScriptCompiler.getCompileScriptsNotSupportingIncrementalCompilation();
		int failedScriptsCount = failedCompileScripts.size() + failedDestroyScripts.size();
		if (failedScriptsCount > 0) {
			Log.info("The following " + Strings.pluralOf(failedScriptsCount, "script")
					+ " prevented incremental compilation: "
					+ Stream.concat(failedCompileScripts.stream(), failedDestroyScripts.stream())
					.map(Class::getSimpleName).sorted().collect(Collectors.joining(", ")));
		}
	}

	private void fullCompilation(String[] packagesToCompile) {
		knowledgeBase = KnowledgeBaseUtils.createKnowledgeBase();
		knowledgeBase.setId(getKnowledgeBaseId());
		terminologyManager = new TerminologyManager(caseSensitive);

		EventManager.getInstance().fireEvent(new D3webCompilerStartEvent(this));

		Messages.clearMessages(this);

		getCompilerManager().setCurrentCompilePriority(this, Priority.PREPARE);

		// we don't use the script compiler fields for full compilation to be able avoid work
		// only necessary for incremental compilation
		D3webScriptCompiler scriptCompiler = new D3webScriptCompiler(this);
		scriptCompiler.addSections(getPackageManager().getSectionsOfPackage(packagesToCompile));
		scriptCompiler.compile();

		isIncrementalBuild = false;

		knowledgeBase.initPluggedPSMethods();
	}

	private String getKnowledgeBaseId() {
		// if no id is given, generate from article title
		List<? extends Section<? extends PackageCompileType>> kbSections = $(compileSection.getArticle()
				.getRootSection()).successor(compileSection.get().getClass()).asList();
		String kbId;
		if (kbSections.size() > 1) {
			// append number if more than on knowledge base is compiled per article
			kbId = compileSection.getTitle() + (kbSections.indexOf(compileSection) + 1);
		}
		else {
			kbId = compileSection.getTitle();
		}
		return kbId;
	}

	@Override
	public void addSectionToDestroy(Section<?> section, Class<?>... scriptFilter) {
		if (destroyScriptCompiler != null) {
			destroyScriptCompiler.addSection(section, scriptFilter);
			assertIncrementalCompilationStillPossible(destroyScriptCompiler);
		}
	}

	@Override
	public void addSectionToCompile(Section<?> section, Class<?>... scriptFilter) {
		if (compileScriptCompiler != null) {
			compileScriptCompiler.addSection(section, scriptFilter);
			assertIncrementalCompilationStillPossible(compileScriptCompiler);
		}
	}

	@Override
	public void addSubtreeToDestroy(Section<?> section, Class<?>... scriptFilter) {
		if (destroyScriptCompiler != null) {
			destroyScriptCompiler.addSubtree(section, scriptFilter);
			assertIncrementalCompilationStillPossible(destroyScriptCompiler);
		}
	}

	@Override
	public void addSubtreeToCompile(Section<?> section, Class<?>... scriptFilter) {
		if (compileScriptCompiler != null) {
			compileScriptCompiler.addSubtree(section, scriptFilter);
			assertIncrementalCompilationStillPossible(compileScriptCompiler);
		}
	}

	private void assertIncrementalCompilationStillPossible(D3webScriptCompiler scriptCompiler) {
		if (!scriptCompiler.isIncrementalCompilationPossible()) {
			logFailingIncrementalCompilationScripts();
			throw new IllegalStateException("Non-incremental script was added during incremental compilation. " +
					"Please inform your administrator and try refresh of the knowledge base to recover.");
		}
	}

	private static class D3webScriptCompiler extends ScriptCompiler<D3webCompiler> {

		public D3webScriptCompiler(D3webCompiler compiler) {
			this(compiler, false);
		}

		public D3webScriptCompiler(D3webCompiler compiler, boolean reverseOrder) {
			super(compiler, reverseOrder);
		}

		public void addSections(Collection<Section<?>> sectionsOfPackage) {
			for (Section<?> section : sectionsOfPackage) {
				// only compile the KnowledgeBaseType sections belonging to this compiler
				if (!(section.get() instanceof KnowledgeBaseType) || getCompiler().getCompileSection() == section) {
					addSubtree(section);
				}
			}
		}

		@Override
		protected <T extends Type> boolean isIncrementalCompilationPossible(Section<T> section, CompileScript<D3webCompiler, T> script) {
			return isSupportedD3webCompileScript(section, script) || isWhiteListed(script);
		}

		private <T extends Type> boolean isSupportedD3webCompileScript(Section<T> section, CompileScript<D3webCompiler, T> script) {
			return script instanceof D3webCompileScript && ((D3webCompileScript<T>) script).isIncrementalCompilationSupported(section);
		}

		@SuppressWarnings("RedundantIfStatement")
		private <T extends Type> boolean isWhiteListed(CompileScript<D3webCompiler, T> script) {
			if (script instanceof SimpleReferenceRegistrationScript) return true;
			if (script instanceof SimpleDefinitionRegistrationScript) return true;
			return false;
		}
	}
}
