/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * This interface defines a compilation unit that is used to destroy one or more
 * objects that were previously created by an {@link CompileScript}.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 04.11.2013
 * @param <T> the type of the section to be compiled
 * @param <C> the compiler this compilation unit is for
 */
public interface DestroyScript<C extends Compiler, T extends Type> {

	void destroy(C compiler, Section<T> section);

	/**
	 * Returns the class instance of the {@link Compiler} the script is intended
	 * for.
	 * 
	 * @created 30.10.2013
	 * @return the compiler class of this script
	 */
	Class<C> getCompilerClass();
}