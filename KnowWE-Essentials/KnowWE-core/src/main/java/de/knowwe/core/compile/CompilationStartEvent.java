/*
 * Copyright (C) 2016 denkbares GmbH, Germany
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

import com.denkbares.events.Event;

/**
 * Gets fired before a compilation frame starts (before any single compiler compiles).
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.03.2016
 */
public class CompilationStartEvent implements Event {

	private final CompilerManager compilerManager;

	public CompilationStartEvent(CompilerManager compilerManager) {
		this.compilerManager = compilerManager;
	}

	public CompilerManager getCompilerManager() {
		return compilerManager;
	}
}
