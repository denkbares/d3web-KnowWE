package de.knowwe.core.compile.packaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

public interface PackageCompileType extends Type {

	String COMPILER_STORY_KEY = "compilerStoreKey";

	/**
	 * Needs to return a Collection of package names. These are the packages to be compiled by this compile
	 * section.<br/>
	 * <b>Attention:</b> Packages matching any of the wild card package patterns of this compile section will also be
	 * returned!
	 *
	 * @param section should always be the Section calling this method.
	 * @return a Collection of package names.
	 * @created 02.10.2010
	 */
	String[] getPackagesToCompile(Section<? extends PackageCompileType> section);

	/**
	 * Returns the package wild card patterns of the PackageCompile-Section (if any) as regex patterns
	 *
	 * @param section should always be the Section calling this method.
	 * @return a stream of regex patterns
	 */
	Pattern[] getPackagePatterns(Section<? extends PackageCompileType> section);

	/**
	 * Returns the package names that are specified for the compile section.<br/>
	 * <b>Attention:</b> These are not necessarily all the packages that should be compiled by this sections. use
	 * {@link #getPackagesToCompile(Section)} for this purpose. Here we only return the package names directly
	 * given a such, NOT derived from the Patterns retrieved via {@link #getPackagePatterns(Section)}
	 *
	 * @param section should always be the Section calling this method.
	 * @return a stream of package name given for the section
	 */
	String[] getPackages(Section<?> section);

	@SuppressWarnings("unchecked")
	default Collection<PackageCompiler> getPackageCompilers(Section<? extends PackageCompileType> section) {
		Collection<PackageCompiler> compilers = (Collection<PackageCompiler>)
				section.getObject(COMPILER_STORY_KEY);
		return compilers == null ? Collections.emptyList() : Collections.unmodifiableCollection(compilers);
	}

	default void registerPackageCompiler(PackageCompiler compiler, Section<? extends PackageCompileType> section) {
		@SuppressWarnings("unchecked")
		Collection<PackageCompiler> compilers = (Collection<PackageCompiler>)
				section.getObject(COMPILER_STORY_KEY);
		if (compilers == null) {
			compilers = new ArrayList<>(5);
			section.storeObject(COMPILER_STORY_KEY, compilers);
		}
		compilers.add(compiler);
	}
}
