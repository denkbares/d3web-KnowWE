/*
 * Copyright (C) 2024 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.objects;

import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Interface for incrementally compiled terms, adding a few (optional/default) methods that can be implemented to help
 * with incremental compilation of the term.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 26.01.2024
 */
public interface IncrementalTerm extends Term {

	default Sections<?> getDependingSectionsToDestroy(IncrementalCompiler compiler, Section<IncrementalTerm> section, Class<?>... scriptFilter) {
		return getDependingSections(compiler, section, scriptFilter);
	}

	default Sections<?> getDependingSectionsToCompile(IncrementalCompiler compiler, Section<IncrementalTerm> section, Class<?>... scriptFilter) {
		return getDependingSections(compiler, section, scriptFilter);
	}

	default Sections<?> getDependingSections(IncrementalCompiler compiler, Section<IncrementalTerm> section, Class<?>... scriptFilter) {
		// recompile ancestors and successors that are also incremental terms
		return $(section).ancestor(IncrementalTerm.class).append($(section).successor(IncrementalTerm.class));
	}
}
