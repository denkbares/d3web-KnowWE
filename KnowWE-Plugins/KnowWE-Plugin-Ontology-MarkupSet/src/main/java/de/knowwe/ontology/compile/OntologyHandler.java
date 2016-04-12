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
package de.knowwe.ontology.compile;

import java.util.Collection;

import de.knowwe.core.compile.packaging.PackageCompileScript;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.12.2013
 */
public abstract class OntologyHandler<T extends Type> implements PackageCompileScript<OntologyCompiler, T> {

	public abstract Collection<Message> create(OntologyCompiler compiler, Section<T> section);

	@Override
	public Class<OntologyCompiler> getCompilerClass() {
		return OntologyCompiler.class;
	}

	@Override
	public void compile(OntologyCompiler compiler, Section<T> section) throws CompilerMessage {
		throw new CompilerMessage(create(compiler, section));
	}

}
