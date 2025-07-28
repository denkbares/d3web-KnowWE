/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile;

import java.util.Collection;
import java.util.Set;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.07.2025
 */
public interface GroupingPackageCompiler extends GroupingCompiler, PackageCompiler {

	/**
	 * This method makes, that any grouping compiler the given compiler belongs to and other child compilers of that
	 * grouping compiler are recompiled.
	 *
	 * @param packageCompiler the compiler to recompile package compiler and grouping compiler for
	 */
	static void recompile(PackageRegistrationCompiler packageRegistrationCompiler, PackageCompiler packageCompiler) {
		ArticleManager articleManager = packageCompiler.getCompileSection().getArticleManager();
		if (articleManager == null) return;
		String compilerName = packageCompiler.getName();
		Collection<GroupingPackageCompiler> groupingCompilers = Compilers.getCompilers(articleManager, GroupingPackageCompiler.class);
		for (GroupingPackageCompiler groupingCompiler : groupingCompilers) {
			Set<Section<? extends PackageCompileType>> referencedCompileSections = groupingCompiler.getReferencedCompileSections(groupingCompiler.getCompileSection());
			if (referencedCompileSections.stream().map(s -> s.get().getName(s)).noneMatch(compilerName::equals)) {
				continue;
			}
			for (Section<? extends PackageCompileType> compileSection : referencedCompileSections) {
				if (compileSection == packageCompiler.getCompileSection()) continue;
				Collection<PackageCompiler> packageCompilers = compileSection.get().getPackageCompilers(compileSection);
				for (PackageCompiler childPackageCompiler : packageCompilers) {
					Compilers.destroyAndRecompileSection(packageRegistrationCompiler, childPackageCompiler.getCompileSection());
				}
			}
			Compilers.destroyAndRecompileSubtree(packageRegistrationCompiler, groupingCompiler.getCompileSection());
		}
	}

	/**
	 * Get the compile sections of child package compilers (non-blocking)
	 *
	 * @param groupingCompilerCompileSection the compile section of this compiler
	 * @return the compile sections of the child compilers
	 */
	Set<Section<? extends PackageCompileType>> getReferencedCompileSections(Section<? extends PackageCompileType> groupingCompilerCompileSection);
}
