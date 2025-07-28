/*
 * Copyright (C) 2020 denkbares GmbH. All rights reserved.
 *
 */

package de.knowwe.core.compile;

import java.util.Collection;

/**
 * A compiler that consists of several child compilers
 *
 * @author Tobias Schmee (denkbares GmbH)
 * @created 11.11.20
 */
public interface GroupingCompiler extends Compiler {

	/**
	 * Gets all child compilers for a given section
	 *
	 * @return collection containing all child compilers of this compiler
	 */
	Collection<Compiler> getChildCompilers();
}
