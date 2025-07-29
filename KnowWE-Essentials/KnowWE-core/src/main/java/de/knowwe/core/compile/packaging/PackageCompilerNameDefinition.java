/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile.packaging;

import java.util.List;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Type to define and register the name of a package compiler via #getTermName
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.07.2025
 */
public interface PackageCompilerNameDefinition extends TermDefinition, RenamableTerm {

	@Override
	default String getTermName(Section<? extends Term> section) {
		String termName = Strings.trimQuotes(section.getText());
		if (Strings.isBlank(termName)) {
			return generateNameFromTitle(section);
		}
		return termName;
	}

	static String generateNameFromTitle(Section<?> section) {
		List<? extends Section<?>> compileTypeSections = $(section).parent()
				.successor(section.get().getClass())
				.asList();
		if (compileTypeSections.size() > 1) {
			for (int i = 0; i < compileTypeSections.size(); i++) {
				Section<?> compileTypeSection = compileTypeSections.get(i);
				if (compileTypeSection == section) return section.getTitle() + "-" + (i + 1);
			}
		}
		return section.getTitle() == null ? section.getText() : section.getTitle();
	}
}
