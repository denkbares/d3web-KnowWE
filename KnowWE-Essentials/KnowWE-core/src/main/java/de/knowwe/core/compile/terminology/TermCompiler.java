/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.core.compile.terminology;

import org.jetbrains.annotations.NotNull;

/**
 * Compilers that uses a TerminologyManager.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 12.12.2013
 */
public interface TermCompiler extends de.knowwe.core.compile.Compiler {

	/**
	 * Defines how the compiler should handle multiple definitions of the same term.
	 */
	enum MultiDefinitionMode {
		/**
		 * Multiple definitions are ignored (they are considered ok, no warning/error is generated).
		 */
		ignore,
		/**
		 * If there are multiple definitions of the same term, a warning message is generated.
		 */
		warn,
		/**
		 * If there are multiple definitions of the same term, an error message is generated.
		 */
		error
	}

	/**
	 * Defines how the compiler should handle term references without definition
	 */
	enum ReferenceValidationMode {
		/**
		 * Invalid references are ignored (they are considered ok, no warning/error is generated).
		 */
		ignore,
		/**
		 * If a term reference is not valid, a warning is generated.
		 */
		warn,
		/**
		 * If a term reference is not valid, an error is generated.
		 */
		error
	}

	/**
	 * Returns the {@link TerminologyManager} that will hold all defined
	 * terminology of this compiler. Each compiler has its own terminology
	 * manager that will be distinct by all other compilers.
	 * 
	 * @created 30.10.2013
	 * @return the {@link TerminologyManager} of this compiler
	 */
	@NotNull
	TerminologyManager getTerminologyManager();

	/**
	 * Defines how the compiler should handle multiple definitions of the same term.
	 *
	 * @return the current {@link MultiDefinitionMode} of this compiler.
	 */
	@NotNull
	default MultiDefinitionMode getMultiDefinitionRegistrationMode() {
		return MultiDefinitionMode.ignore;
	}

	/**
	 * Defines how the compiler should handle invalid term references (undefined term).
	 *
	 * @return the current {@link ReferenceValidationMode} of this compiler.
	 */
	@NotNull
	default ReferenceValidationMode getReferenceValidationMode() {
		return ReferenceValidationMode.error;
	}

}
