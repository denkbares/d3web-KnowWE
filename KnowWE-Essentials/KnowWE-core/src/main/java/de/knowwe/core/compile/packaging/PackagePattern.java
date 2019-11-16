/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile.packaging;

import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

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

	@NotNull
	public Pattern getPattern(Section<PackagePattern> section) {
		return Pattern.compile(section.getText().replace("*", ".*"));
	}
}
