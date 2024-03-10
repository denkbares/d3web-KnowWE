/*
 * Copyright (C) 2024 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile;

import de.knowwe.core.event.CompilerEvent;

/**
 * Is fired, when a ScriptCompiler of a Compiler starts its compile phase. This is not necessarily the start of the
 * compilation itself, because in case of incremental compilers, there might be a destroy phase prior to the compile
 * phase.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 10.03.24
 */
public class ScriptCompilerCompilePhaseStartEvent<C extends Compiler> implements CompilerEvent<C> {

	private final ScriptCompiler<C> scriptCompiler;

	public ScriptCompilerCompilePhaseStartEvent(ScriptCompiler<C> scriptCompiler) {
		this.scriptCompiler = scriptCompiler;
	}

	@Override
	public C getCompiler() {
		return scriptCompiler.getCompiler();
	}
}
