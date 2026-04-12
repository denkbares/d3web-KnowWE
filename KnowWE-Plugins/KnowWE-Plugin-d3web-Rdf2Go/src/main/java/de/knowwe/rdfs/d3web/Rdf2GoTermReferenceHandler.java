/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.rdfs.d3web;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.we.object.D3webTermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;

public class Rdf2GoTermReferenceHandler extends OntologyCompileScript<D3webTermReference<NamedObject>> {

	@Override
	public void compile(OntologyCompiler compiler, Section<D3webTermReference<NamedObject>> section) throws CompilerMessage {
		Rdf2GoD3webUtils.registerTermReference(compiler, section);
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<D3webTermReference<NamedObject>> section) {
		Rdf2GoD3webUtils.unregisterTermReference(compiler, section);
	}

}
