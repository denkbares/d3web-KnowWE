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
package de.knowwe.core.compile;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Stopwatch;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Abstract class for PackageCompilers.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 14.11.2013
 */
public abstract class AbstractPackageCompiler implements PackageCompiler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPackageCompiler.class);

	private final Section<? extends PackageCompileType> compileSection;
	private final PackageManager packageManager;
	private final Class<? extends Type> compilingType;

	private CompilationLocal<String[]> compiledPackages;
	private CompilerManager compilerManager;
	private boolean newlyCreated = true;

	public AbstractPackageCompiler(@NotNull PackageManager manager,
								   @NotNull Section<? extends PackageCompileType> compileSection,
								   @NotNull Class<? extends Type> compilingType) {
		this.compileSection = compileSection;
		this.packageManager = manager;
		this.compilingType = compilingType;
		compileSection.get().registerPackageCompiler(this, compileSection);
	}

	@Override
	public boolean isCompiling(Section<?> section) {
		if (section == compileSection) {
			return true;
		}
		if (packageManager.getCompileSections(section).contains(compileSection)) {
			return true;
		}
		Section<?> compilingMarkupSection = $(section).closest(compilingType).getFirst();
		if (compilingMarkupSection != null && compilingMarkupSection.get() instanceof PackageCompileType) {
			return getCompileSection() == compilingMarkupSection;
		}
		return false;
	}

	@Override
	public void init(CompilerManager compilerManager) {
		this.compilerManager = compilerManager;
		this.compiledPackages = CompilationLocal.create(getCompilerManager(),
				() -> getCompileSection().get().getPackagesToCompile(getCompileSection()));
	}

	public void refreshCompiledPackages() {
		this.compiledPackages.invalidate();
	}

	@NotNull
	@Override
	public PackageManager getPackageManager() {
		return this.packageManager;
	}

	/**
	 * Get all sections that are currently compiled by this compiler, based on used packages and available sections in
	 * those packages.
	 */
	public Collection<Section<?>> getCompiledSections() {
		return this.packageManager.getSectionsOfPackage(getCompiledPackages());
	}

	@Override
	public CompilerManager getCompilerManager() {
		return this.compilerManager;
	}

	@Override
	@NotNull
	public Section<? extends PackageCompileType> getCompileSection() {
		return this.compileSection;
	}

	@Override
	public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {
		String[] packagesToCompile = getCompiledPackages();
		if (newlyCreated || hasChangedForCompiler(packagesToCompile)) {
			newlyCreated = false;
			Stopwatch stopwatch = new Stopwatch();
			compilePackages(packagesToCompile);
			LOGGER.info(this + " finished after " + stopwatch.getDisplay());
		}
	}

	private boolean hasChangedForCompiler(String... packagesToCompile) {
		final ScriptManager<AbstractPackageCompiler> scriptManager = CompilerManager.getScriptManager(this);
		return hasKnowledgeForCompiler(getPackageManager().getRemovedSections(packagesToCompile), scriptManager)
			   || hasKnowledgeForCompiler(getPackageManager().getAddedSections(packagesToCompile), scriptManager);
	}

	private boolean hasKnowledgeForCompiler(Collection<Section<?>> addedSections, ScriptManager<AbstractPackageCompiler> scriptManager) {
		for (Section<?> section : addedSections) {
			if (scriptManager.hasScriptsForSubtree(section.get())) return true;
		}
		return false;
	}

	public String[] getCompiledPackages() {
		return compiledPackages.get();
	}

	public abstract void compilePackages(String[] packagesToCompile);

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (" + this.getName() + ")";
	}
}
