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
package de.knowwe.ontology.kdom.table;

import java.util.LinkedList;
import java.util.List;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.URI;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableUtils;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.objectproperty.AbbreviatedPropertyReference;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.ontology.turtle.Object;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 27.04.15
 */
public class LineHandler extends OntologyCompileScript<TableLine> {

	@Override
	public void compile(OntologyCompiler compiler, Section<TableLine> section) throws CompilerMessage {

		if (TableUtils.isHeaderRow(section)) {
			Messages.clearMessages(compiler, section, getClass());
			return;
		}

		Rdf2GoCore core = compiler.getRdf2GoCore();
		List<Statement> statements = new LinkedList<>();
		Section<AbbreviatedResourceReference> subjectReference = findSubject(section);
		URI subjectUri = subjectReference.get().getResourceURI(core, subjectReference);
		List<Section<Object>> objects = findObjects(section);
		for (Section<Object> objectReference : objects) {
			Section<AbbreviatedPropertyReference> propertyReference
				= TableUtils.getColumnHeader(objectReference, AbbreviatedPropertyReference.class);
			URI propertyUri = propertyReference.get().getPropertyURI(core, propertyReference);
			Node objectNode = objectReference.get().getNode(objectReference, compiler);
			statements.add(core.createStatement(subjectUri, propertyUri, objectNode));
		}

		core.addStatements(section, statements);
	}

	private List<Section<Object>> findObjects(Section<TableLine> section) {
		return Sections.successors(section, Object.class);
	}

	private Section<AbbreviatedResourceReference> findSubject(Section<TableLine> section) {
		return Sections.successor(section, AbbreviatedResourceReference.class);
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<TableLine> section) {
		Rdf2GoCore core = compiler.getRdf2GoCore();
		core.removeStatements(section);
	}
}
