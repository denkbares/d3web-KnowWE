/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile;

/**
 * A compiler that provides a name for the different instances. That name can for example be given/specified in the
 * markup that defines the compiler.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 2019-06-01
 */
public interface NamedCompiler extends Compiler {

	String getName();
}
