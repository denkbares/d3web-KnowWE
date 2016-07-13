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

import com.denkbares.utils.Log;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Abstract class for PackageCompilers.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 14.11.2013
 */
public abstract class AbstractPackageCompiler implements PackageCompiler {

	private final Section<? extends PackageCompileType> compileSection;
	private final PackageManager packageManager;
	private final Class<? extends Type> compilingType;

	private CompilerManager compilerManager;

	public AbstractPackageCompiler(PackageManager manager,
								   Section<? extends PackageCompileType> compileSection,
								   Class<? extends Type> compilingType) {
		this.compileSection = compileSection;
		this.packageManager = manager;
		this.compilingType = compilingType;
		compileSection.get().registerPackageCompiler(this, compileSection);
	}

	@Override
	public boolean isCompiling(Section<?> section) {
		if (section == compileSection
				|| packageManager.getCompileSections(section).contains(compileSection)) {
			return true;
		}
		Section<?> compilingMarkupSection;
		if (section.get().getClass().isInstance(compilingType)) {
			compilingMarkupSection = Sections.cast(section, compilingType);
		}
		else {
			compilingMarkupSection = Sections.ancestor(section, compilingType);
		}
		if (compilingMarkupSection != null) {
			Section<PackageCompileType> compileSection = Sections.successor(compilingMarkupSection, PackageCompileType.class);
			if (getCompileSection() == compileSection) return true;
		}
		return false;
	}

	@Override
	public void init(CompilerManager compilerManager) {
		this.compilerManager = compilerManager;
	}

	@Override
	public PackageManager getPackageManager() {
		return this.packageManager;
	}

	@Override
	public CompilerManager getCompilerManager() {
		return this.compilerManager;
	}

	@Override
	public Section<? extends PackageCompileType> getCompileSection() {
		return this.compileSection;
	}

	@Override
	public void compile(Collection<Section<?>> added, Collection<Section<?>> removed) {
		String[] packagesToCompile = getCompileSection().get()
				.getPackagesToCompile(getCompileSection());
		if (getPackageManager().hasChanged(packagesToCompile)) {
			long start = System.currentTimeMillis();
			compilePackages(packagesToCompile);
			Log.info(toString() + " finished after "
					+ (System.currentTimeMillis() - start) + "ms");
		}
	}

	public abstract void compilePackages(String[] packagesToCompile);

	@Override
	public String toString() {
		String[] packagesToCompile = getCompileSection().get().getPackagesToCompile(
				getCompileSection());
		return this.getClass().getSimpleName() + " ("
				+ compileSection.getTitle() + ")";
	}

}
