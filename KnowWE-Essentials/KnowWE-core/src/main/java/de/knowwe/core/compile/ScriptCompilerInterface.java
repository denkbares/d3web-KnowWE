/*
 * Copyright (C) 2024 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile;

/**
 * Common interface for script compilers in KnowWE.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.03.2024
 */
public interface ScriptCompilerInterface<C extends Compiler> {

	C getCompiler();

	void compile();

	void destroy();
}
