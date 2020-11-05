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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.PredicateParser;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.AttachmentManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.CompilationFinishedEvent;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageNotCompiledWarningScript;
import de.knowwe.core.compile.packaging.PackageRule;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.attachment.AttachmentMarkup;

import static de.knowwe.core.kdom.parsing.Sections.$;

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
		storePackageInfo(compiler, section, packageInfo);

		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		PackageManager packageManager = compiler.getPackageManager();

		// do term registrations of all package terms, regular packages and packages inside rules
		List<String> allPackageNames = getAllPackageNames(packageInfo);
		for (String packageName : allPackageNames) {
			Identifier packageIdentifier = new Identifier(packageName);
			if (isCompileMarkup) {
				terminologyManager.registerTermDefinition(compiler, section, Package.class, packageIdentifier);
				Compilers.recompileReferences(compiler, packageIdentifier, PackageNotCompiledWarningScript.class);
			}
			else {
				terminologyManager.registerTermReference(compiler, section, Package.class, packageIdentifier);
			}
		}

		// register section for all regular packages in the package manager
		for (String packageName : packageInfo.packageNames) {
			boolean isNewPackage = packageManager.getSectionsOfPackage(packageName).isEmpty();
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
					storePackageInfo(compiler, compileSection, new PackageInfo(List.of(packagesToCompile), List.of()));
				}
			}
		}

		// register section for all package rules in the package manager
		for (PredicateParser.ParsedPredicate packageRule : packageInfo.packageRules) {
			packageManager.addSectionToPackageRule(section, packageRule);
		}
	}

	@NotNull
	private List<String> getAllPackageNames(PackageInfo packageInfo) {
		return Stream.concat(packageInfo.packageNames.stream(), packageInfo.packageRules.stream()
				.flatMap((PredicateParser.ParsedPredicate parsedPredicate) -> parsedPredicate.getVariables().stream()))
				.collect(Collectors.toList());
	}

	/**
	 * Returns the packages the given default markup section belongs to according to the defined annotations. If there
	 * are no such annotations, the default packages for the article are returned. In case the section is part of an
	 * article based on a compiled attachment, we also check the compiling %%Attachment markups for packages.
	 *
	 * @param section the section to be check for packages
	 * @created 12.03.2012
	 */
	private PackageInfo getPackageInfo(Section<?> section) {
		List<PredicateParser.ParsedPredicate> packageRules = new ArrayList<>();
		List<String> packageNames = new ArrayList<>();
		$(DefaultMarkupType.getAnnotationContentSection(section, PackageManager.PACKAGE_ATTRIBUTE_NAME))
				.successor(PackageRule.class)
				.forEach(packageRule -> {
					if (packageRule.get().isOrdinaryPackage(packageRule)) {
						packageNames.add(packageRule.get().getOrdinaryPackage(packageRule));
					}
					else {
						packageRules.add(packageRule.get().getRule(packageRule));
					}
				});

		if (packageNames.isEmpty() && packageRules.isEmpty()) {
			packageNames.addAll(KnowWEUtils.getPackageManager(section).getDefaultPackages(section.getArticle()));
			packageRules.addAll(KnowWEUtils.getPackageManager(section).getDefaultPackageRules(section.getArticle()));
		}
		// if we only have the default package, check if this is an article based on an compiled attachment
		// and if yes, get package info from compiling %%Attachment markups
		ArticleManager articleManager = section.getArticleManager();
		if (packageRules.isEmpty()
				&& packageNames.size() == 1 && packageNames.get(0).equals(PackageManager.DEFAULT_PACKAGE)
				&& articleManager instanceof DefaultArticleManager) {
			AttachmentManager attachmentManager = ((DefaultArticleManager) articleManager).getAttachmentManager();
			PackageInfo attachmentMarkupPackageInfo = attachmentManager.getCompilingAttachmentSections(section
					.getArticle())
					.stream()
					.map(s -> $(s).ancestor(AttachmentMarkup.class).getFirst())
					.filter(Objects::nonNull)
					.map(this::getPackageInfo)
					.findFirst()
					.orElse(new PackageInfo(List.of(), List.of()));
			if (!attachmentMarkupPackageInfo.isEmpty()) {
				packageNames.clear();
				packageNames.addAll(attachmentMarkupPackageInfo.packageNames);
				packageRules.addAll(attachmentMarkupPackageInfo.packageRules);
			}
		}
		return new PackageInfo(packageNames, packageRules);
	}

	private void storePackageInfo(PackageRegistrationCompiler compiler, Section<?> section, PackageInfo packagesToCompile) {
		section.storeObject(compiler, REGISTERED_PACKAGE_KEY, packagesToCompile);
	}

	private PackageInfo getPackageInfo(PackageRegistrationCompiler compiler, Section<DefaultMarkupType> section) {
		return section.getObject(compiler, REGISTERED_PACKAGE_KEY);
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
		compiler.getPackageManager().removeSectionFromAllPackagesAndRules(section);

		PackageInfo packageInfo = getPackageInfo(compiler, section);
		if (packageInfo == null) return;
		boolean isCompileMarkup = isCompileMarkup(section);
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		for (String packageName : getAllPackageNames(packageInfo)) {

			Identifier packageIdentifier = new Identifier(packageName);
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
		PackageRegistrationCompiler compiler = Compilers.getPackageRegistrationCompiler(compilerManager
				.getArticleManager());
		PackageManager packageManager = compiler.getPackageManager();

		Collection<Section<? extends DefaultMarkupPackageCompileType>> compileSections = packageManager.getCompileSections();
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
				boolean matchedOnlyViaPattern = compileSection.get().getPackages(compileSection).stream()
						.noneMatch(p -> p.equals(packageName));
				if (matchedOnlyViaPattern) {
					packageManager.removeSectionFromPackage(compileSection, packageName);
					compileSection.get().getPackageCompilers(compileSection).forEach(c -> {
						if (c instanceof AbstractPackageCompiler) {
							((AbstractPackageCompiler) c).refreshCompiledPackages();
						}
					});
				}
			}
		}
	}

	private static final class PackageInfo {

		public final List<String> packageNames;
		public final List<PredicateParser.ParsedPredicate> packageRules;

		public PackageInfo(List<String> packageNames, List<PredicateParser.ParsedPredicate> packageRules) {
			this.packageNames = packageNames;
			this.packageRules = packageRules;
		}

		public boolean isEmpty() {
			return packageNames.isEmpty() && packageRules.isEmpty();
		}
	}
}
