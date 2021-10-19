/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Use this for script for which we want to explicitly allow incremental compilation, e.g. because the compiler was
 * non-incremental in the beginning and transitioned to incremental compilation later
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 19.10.21
 */
public interface OptInIncrementalCompileScript<T extends Type> {

	default boolean isIncrementalCompilationSupported(Section<T> section) {
		return false; // be default, we don't support incremental compilation
	}
}
