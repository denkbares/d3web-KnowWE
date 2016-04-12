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
package de.knowwe.rdf2go;

import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.kdom.Type;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public interface Rdf2GoCompileScript<C extends Rdf2GoCompiler, T extends Type> extends CompileScript<C, T>, DestroyScript<C, T> {

}
