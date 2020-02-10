/*
 * Copyright (C) 2020 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.core.compile;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * A container for a variable that is only valid during one compilation cycle of the {@link CompilerManager}. If the
 * variable gets accessed again after a new compilations has started, the variable from the last compilation gets
 * discarded and retrieved from the supplier again.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 10.02.2020
 */
public class CompilationLocal<E> {

	private final CompilerManager compilerManager;
	private final Supplier<E> supplier;
	private volatile int compileId = -1;
	private volatile E variable = null;

	public CompilationLocal(@NotNull CompilerManager compilerManager, @NotNull Supplier<E> supplier) {
		this.compilerManager = compilerManager;
		this.supplier = supplier;
	}

	/**
	 * Provides the desired cached variable checked for validity for the current compilation cycle.
	 */
	public E get() {
		if (invalid()) {
			synchronized (this) {
				if (invalid()) {
					variable = supplier.get();
					compileId = compilerManager.getCompilationId();
				}
			}
		}
		return variable;
	}

	private boolean invalid() {
		return variable == null || compileId != compilerManager.getCompilationId();
	}
}
