/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.CompilationFinishedEvent;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageNotCompiledWarningScript;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;

/**
 * Handles registration of packages both to the terminology and package manager.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 */
public class DefaultMarkupPackageRegistrationScript extends PackageRegistrationScript<DefaultMarkupType> implements EventListener {

	private static final String REGISTERED_PACKAGE_KEY = "registeredPackages";

	public DefaultMarkupPackageRegistrationScript() {
		EventManager.getInstance().registerListener(this);
	}

	@Override
	public void compile(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) throws CompilerMessage {

		String[] packageNames;
		boolean isCompileMarkup = isCompileMarkup(section);
		if (isCompileMarkup) {
			Section<DefaultMarkupPackageCompileType> compileSection = Sections.cast(section, DefaultMarkupPackageCompileType.class);
			packageNames = compileSection.get().getPackagesToCompile(compileSection);
		}
		else {
			packageNames = section.get().getPackages(section);
		}

		// while destroying, the default packages will already be removed, so we
		// have to store artificially
		storeRegisteredPackages(compiler, section, packageNames);

		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		PackageManager packageManager = compiler.getPackageManager();
		for (String packageName : packageNames) {
			boolean isNewPackage = packageManager.getSectionsOfPackage(packageName).isEmpty();
			packageManager.addSectionToPackage(section, packageName);

			Identifier packageIdentifier = new Identifier(packageName);

			if (isCompileMarkup) {
				terminologyManager.registerTermDefinition(compiler, section, Package.class, packageIdentifier);
				Compilers.recompileReferences(compiler, packageIdentifier, PackageNotCompiledWarningScript.class);
			}
			else {
				terminologyManager.registerTermReference(compiler, section, Package.class, packageIdentifier);
			}

			// Special case for package patterns: Since we don't know which new package names will match on a package
			// pattern added in the future, we have to check and register/add as new packages get added
			if (isNewPackage && !isCompileMarkup) {
				for (Section<? extends PackageCompileType> compileSection : packageManager.getCompileSections()) {
					if (packageManager.getSectionsOfPackage(packageName).contains(compileSection)) continue;
					if (!compilesPackageViaPatternMatch(packageName, compileSection)) continue;
					packageManager.addSectionToPackage(compileSection, packageName);
					packageManager.registerPackageCompileSection(compileSection);
					terminologyManager.registerTermDefinition(compiler, compileSection, Package.class, packageIdentifier);
					String[] packagesToCompile = compileSection.get().getPackagesToCompile(compileSection);
					storeRegisteredPackages(compiler, compileSection, packagesToCompile);
				}
			}
		}
	}

	private void storeRegisteredPackages(PackageRegistrationCompiler compiler, Section<?> section, String[] packagesToCompile) {
		section.storeObject(compiler, REGISTERED_PACKAGE_KEY, packagesToCompile);
	}

	private String[] getRegisteredPackages(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {
		return (String[]) section.getObject(compiler, REGISTERED_PACKAGE_KEY);
	}

	private boolean compilesPackageViaPatternMatch(String packageName, Section<? extends PackageCompileType> compileSection) {
		return Stream.of(compileSection.get().getPackagePatterns(compileSection))
				.anyMatch(p -> p.matcher(packageName).matches());
	}

	private boolean isCompileMarkup(Section<DefaultMarkupType> section) {
		return section.get() instanceof DefaultMarkupPackageCompileType;
	}

	@Override
	public void destroy(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {
		compiler.getPackageManager().removeSectionFromAllPackages(section);

		String[] packageNames = getRegisteredPackages(compiler, section);
		if (packageNames == null) return;
		boolean isCompileMarkup = isCompileMarkup(section);
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		for (String annotationString : packageNames) {

			Identifier packageIdentifier = new Identifier(annotationString);
			if (isCompileMarkup) {
				terminologyManager.unregisterTermDefinition(compiler, section, Package.class, packageIdentifier);
				Compilers.destroyAndRecompileReferences(compiler, packageIdentifier, PackageNotCompiledWarningScript.class);
			}
			else {
				terminologyManager.unregisterTermReference(compiler, section, Package.class, packageIdentifier);
			}
		}
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return Collections.singletonList(CompilationFinishedEvent.class);
	}

	@Override
	public void notify(Event event) {
		cleanupPatternRegistrations(((CompilationFinishedEvent) event).getCompilerManager());
	}

	/**
	 * After we finished compilation, see if we have to clean up some package registrations, where the compile section
	 * only belongs to the sections of the package, because there once were actual sections of that package and this
	 * compile section had package patterns matching that package. After all ordinary sections of that package are
	 * removed, the compile sections should also be removed, because the package no longer really exists.
	 * We therefore check if the compile section is the last one of the package and was only in the package via a
	 * package pattern. If yes, we remove it from the package, which als removes the package.
	 * <p>
	 * We have to do this AFTER the compilation, because during the compilation we still want to appear as compiling
	 * such a package, so knowledge of the package can properly be removed.
	 */
	private void cleanupPatternRegistrations(CompilerManager compilerManager) {
		PackageManager packageManager = Compilers.getPackageRegistrationCompiler(compilerManager.getArticleManager())
				.getPackageManager();

		Collection<Section<? extends PackageCompileType>> compileSections = packageManager.getCompileSections();
		packageLoop:
		for (String packageName : new ArrayList<>(packageManager.getAllPackageNames())) {
			if (!packageManager.hasChanged(packageName)) continue;

			Collection<Section<?>> sectionsOfPackage = packageManager.getSectionsOfPackage(packageName);

			for (Section<?> section : sectionsOfPackage) {
				// as soon as we find at least one normal (not compiling) section, we don't have to clean up for that package
				if (!compileSections.contains(section)) continue packageLoop;
			}

			// seems like we only have compile sections in that package left
			for (Section<?> section : sectionsOfPackage) {
				Section<PackageCompileType> compileSection = Sections.cast(section, PackageCompileType.class);

				// if non of the directly specified packages of the compile section matches the given package name,
				// the only other possibility is matching via pattern
				boolean matchedOnlyViaPattern = Stream.of(compileSection.get().getPackages(compileSection))
						.noneMatch(p -> p.equals(packageName));
				if (matchedOnlyViaPattern) {
					packageManager.removeSectionFromPackage(compileSection, packageName);
				}
			}
		}
	}
}
