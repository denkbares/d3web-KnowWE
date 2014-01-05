/*
 * Copyright (C) ${year} denkbares GmbH, Germany
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

package de.knowwe.core.compile.packaging;

import java.util.Collection;
import java.util.regex.Pattern;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Implementation of a compile type to handle the uses-annotations of a default
 * markup. This Type does not add a specific compiler, add a Compiler when you
 * add this Type to your parent type with a new
 * {@link PackageRegistrationScript}.
 * 
 * @author Albrecht Striffler, Volker Belli (denkbares GmbH)
 * @created 13.10.2010
 */
public class DefaultMarkupPackageCompileType extends PackageCompileType {

	public DefaultMarkupPackageCompileType() {
		this.setSectionFinder(new RegexSectionFinder(".*", Pattern.DOTALL));

		// script to add the default markup section as the term definition of
		// the compiled packages
		this.addCompileScript(Priority.HIGH,
				new PackageRegistrationScript<DefaultMarkupPackageCompileType>() {

					private static final String PACKAGE_DEFINITIONS_KEY = "packageDefinitions";

					@Override
					public void compile(PackageRegistrationCompiler compiler, Section<DefaultMarkupPackageCompileType> section) {
						Section<DefaultMarkupType> markupSection = Sections.findAncestorOfType(
								section, DefaultMarkupType.class);
						String[] packageNames = DefaultMarkupType.getPackages(markupSection,
								PackageManager.COMPILE_ATTRIBUTE_NAME);
						// while destroying, the default packages will already
						// be removed, so we have to store artificially
						markupSection.getSectionStore().storeObject(compiler,
								PACKAGE_DEFINITIONS_KEY, packageNames);
						for (String annotationString : packageNames) {
							compiler.getTerminologyManager().registerTermDefinition(compiler,
									markupSection, Package.class, new Identifier(annotationString));
						}

					}

					@Override
					public void destroy(PackageRegistrationCompiler compiler, Section<DefaultMarkupPackageCompileType> section) {
						Section<DefaultMarkupType> markupSection = Sections.findAncestorOfType(
								section, DefaultMarkupType.class);
						String[] packageNames = (String[]) markupSection.getSectionStore().getObject(
								compiler, PACKAGE_DEFINITIONS_KEY);
						for (String annotationString : packageNames) {
							compiler.getTerminologyManager().unregisterTermDefinition(compiler,
									markupSection, Package.class, new Identifier(annotationString));
						}

					}

				});

		// script to check whether the compiled packages contain any packages
		this.addCompileScript(Priority.LOW,
				new PackageRegistrationScript<DefaultMarkupPackageCompileType>() {

					@Override
					public void compile(PackageRegistrationCompiler compiler, Section<DefaultMarkupPackageCompileType> section) {
						String[] packagesToCompile = getPackagesToCompile(section);
						Collection<Section<?>> sectionsOfPackages = compiler.getPackageManager().getSectionsOfPackage(
								packagesToCompile);
						Section<DefaultMarkupType> markupSection = Sections.findAncestorOfType(
								section, DefaultMarkupType.class);
						boolean emptyPackages = sectionsOfPackages.isEmpty()
								// the parents markup section does not count
								|| (sectionsOfPackages.size() == 1
										&& markupSection != null
										&& sectionsOfPackages.contains(markupSection));
						if (emptyPackages) {
							String packagesString = Strings.concat(" ,", packagesToCompile);
							if (packagesToCompile.length > 1) {
								packagesString = "s '" + packagesString + "' do";
							}
							else {
								packagesString = " '" + packagesString + "' does";
							}
							Message warning = Messages.warning("The package"
									+ packagesString
									+ " not contain any sections.");
							Messages.storeMessage(section, getClass(), warning);
							return;
						}
						Messages.clearMessages(section, getClass());
					}

					@Override
					public void destroy(PackageRegistrationCompiler compiler, Section<DefaultMarkupPackageCompileType> section) {
						// nothing to do
					}
				});
	}

	@Override
	public String[] getPackagesToCompile(Section<? extends PackageCompileType> section) {
		Section<DefaultMarkupType> markupSection = Sections.findAncestorOfType(section,
				DefaultMarkupType.class);
		String[] uses = DefaultMarkupType.getAnnotations(markupSection,
				PackageManager.COMPILE_ATTRIBUTE_NAME);
		if (uses.length == 0) {
			return Compilers.getPackageManager(section).getDefaultPackages(
					section.getArticle());
		}
		return uses;
	}
}
