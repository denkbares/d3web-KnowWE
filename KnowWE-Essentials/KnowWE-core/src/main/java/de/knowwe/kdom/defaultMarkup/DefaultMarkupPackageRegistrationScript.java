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

import java.util.Collections;
import java.util.List;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.PredicateParser;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageRegistrationNotCompiledWarningScript;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;

/**
 * Handles registration of packages both to the terminology and package manager.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 */
public class DefaultMarkupPackageRegistrationScript extends DefaultMarkupPackageScript implements PackageRegistrationCompiler.PackageRegistrationScript<DefaultMarkupType> {

	@Override
	public void compile(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) throws CompilerMessage {

		PackageInfo packageInfo;
		boolean isCompileMarkup = isCompileMarkup(section);
		if (isCompileMarkup) {
			Section<DefaultMarkupPackageCompileType> compileSection = Sections.cast(section, DefaultMarkupPackageCompileType.class);
			packageInfo = new PackageInfo(List.of(compileSection.get()
					.getPackagesToCompile(compileSection)), Collections.emptyList());
		}
		else {
			packageInfo = getPackageInfo(section);
		}

		// while destroying, the default packages will already be removed, so we
		// have to store artificially
		storePackageInfo(section, packageInfo);

		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		PackageManager packageManager = compiler.getPackageManager();

		// do term registrations of all package terms, regular packages and packages inside rules
		List<String> allPackageNames = getAllPackageNames(packageInfo);
		for (String packageName : allPackageNames) {
			Identifier packageIdentifier = new Identifier(packageName);
			if (isCompileMarkup) {
				terminologyManager.registerTermDefinition(compiler, section, Package.class, packageIdentifier);
				Compilers.recompileReferences(compiler, packageIdentifier, PackageRegistrationNotCompiledWarningScript.class);
			}
			else {
				terminologyManager.registerTermReference(compiler, section, Package.class, packageIdentifier);
			}
		}

		// in case of recompilation, unmark for removal
		packageManager.unmarkForRemoval(section);

		// register section for all regular packages in the package manager
		for (String packageName : packageInfo.packageNames) {
			boolean isNewPackage = !packageManager.containsPackage(packageName);
			packageManager.addSectionToPackage(section, packageName);

			// Special case for package patterns: Since we don't know which new package names will match on a package
			// pattern added in the future, we have to check and register/add as new packages get added
			if (isNewPackage && !isCompileMarkup) {
				for (Section<? extends DefaultMarkupPackageCompileType> compileSection : packageManager.getCompileSections()) {
					if (packageManager.getSectionsOfPackage(packageName).contains(compileSection)) continue;
					if (!compilesPackageViaPatternMatch(packageName, compileSection)) continue;
					packageManager.addSectionToPackage(compileSection, packageName);
					packageManager.registerPackageCompileSection(compileSection);
					terminologyManager.registerTermDefinition(compiler, compileSection, Package.class, new Identifier(packageName));
					String[] packagesToCompile = compileSection.get().getPackagesToCompile(compileSection);
					storePackageInfo(compileSection, new PackageInfo(List.of(packagesToCompile), List.of()));
				}
			}
		}

		// register section for all package rules in the package manager
		for (PredicateParser.ParsedPredicate packageRule : packageInfo.packageRules) {
			packageManager.addSectionToPackageRule(section, packageRule);
		}
	}

	@Override
	public void destroy(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {
		compiler.getPackageManager().markForRemoval(section);
	}
}
