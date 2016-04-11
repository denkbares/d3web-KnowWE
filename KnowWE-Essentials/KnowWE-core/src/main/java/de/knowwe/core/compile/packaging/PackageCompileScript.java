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
package de.knowwe.core.compile.packaging;

import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Type;

/**
 * Marker interface of script using the package compiler.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 17.11.2013
 */
public interface PackageCompileScript<C extends PackageCompiler, T extends Type>
		extends CompileScript<C, T>, DestroyScript<C, T> {

}
