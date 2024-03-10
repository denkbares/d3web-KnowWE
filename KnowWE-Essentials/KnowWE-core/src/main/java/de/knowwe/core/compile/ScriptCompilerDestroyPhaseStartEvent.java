/*
 * Copyright (C) 2024 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile;

import de.knowwe.core.event.CompilerEvent;

/**
 * Is fired, when a ScriptCompiler of a Compiler starts its destroy phase. This is usually the first step in an
 * incremental compilation.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 10.03.24
 */
public class ScriptCompilerDestroyPhaseStartEvent<C extends Compiler> implements CompilerEvent<C> {

	private final ScriptCompiler<C> scriptCompiler;

	public ScriptCompilerDestroyPhaseStartEvent(ScriptCompiler<C> scriptCompiler) {
		this.scriptCompiler = scriptCompiler;
	}

	@Override
	public C getCompiler() {
		return scriptCompiler.getCompiler();
	}
}
