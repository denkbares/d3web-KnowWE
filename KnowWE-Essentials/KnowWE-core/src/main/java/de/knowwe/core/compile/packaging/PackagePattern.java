/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile.packaging;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.ConditionalSectionFinder;

/**
 * A package pattern to determine a package namespace to be included/compiled into an ontology
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 29.10.19.
 */
public class PackagePattern extends AbstractType {


	public PackagePattern() {
		this.setSectionFinder(new ConditionalSectionFinder(new AllTextFinderTrimmed()) {
			@Override
			protected boolean condition(String text, Section<?> father) {
				return text.contains(PackageTerm.WILDCARD_OPERATOR);
			}
		});
	}

	/**
	 * Resolves the pattern of the section on all existing ontology package names
	 *
	 * @param packageManager the package manager to be used for resolution
	 * @param section the section containing the pattern
	 * @return all package names that match the wild card pattern
	 */
	public static Collection<String> resolvePackages(PackageManager packageManager, Section<PackagePattern> section) {
		Set<String> allPackageNames = packageManager.getAllPackageNames();
		Pattern pattern = Pattern.compile(section.getText().replace("*", ".*"));
		return allPackageNames.stream().filter(s -> pattern.matcher(s).matches()).collect(Collectors.toList());
	}
}
