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

package de.knowwe.rdfs.d3web;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

import static de.knowwe.diaflux.type.FlowchartXMLHeadType.FlowchartTermDef;

/**
 * @author Volker Belli (denkbares GmbH)
 * @created 12.08.2014
 */
public class Rdf2GoFlowScript extends OntologyCompileScript<FlowchartType> {

	@Override
	public void compile(OntologyCompiler compiler, Section<FlowchartType> section) throws CompilerMessage {
		Section<FlowchartTermDef> definition = getDefinition(section);
		IRI termIdentifierURI = Rdf2GoD3webUtils.registerTermDefinition(compiler, definition);

		Class<?> termObjectClass = definition.get().getTermObjectClass(compiler, definition);
		Rdf2GoCore core = compiler.getRdf2GoCore();
		List<Statement> statements = new ArrayList<>();

		// rdf:type
		Rdf2GoUtils.addStatement(core, termIdentifierURI, RDF.TYPE,
				termObjectClass.getSimpleName(), statements);

		// lns:name
		Rdf2GoUtils.addStatement(core, termIdentifierURI, core.createLocalIRI("name"),
				core.createLiteral(FlowchartType.getFlowchartName(section)), statements);

		// lns:isAutoStart
		Rdf2GoUtils.addStatement(core, termIdentifierURI, core.createLocalIRI("isAutoStart"),
				core.createDatatypeLiteral(FlowchartType.isAutoStart(section)),
				statements);

		// lns:icon
		String icon = FlowchartType.getIcon(section);
		if (!Strings.isBlank(icon)) {
			Rdf2GoUtils.addStatement(core, termIdentifierURI, core.createLocalIRI("icon"),
					core.createLiteral(icon), statements);
		}

		core.addStatements(section, Rdf2GoUtils.toArray(statements));
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<FlowchartType> section) {
		compiler.getRdf2GoCore().removeStatements(section);
		Rdf2GoD3webUtils.unregisterTermDefinition(compiler, getDefinition(section));
	}

	private Section<FlowchartTermDef> getDefinition(Section<FlowchartType> section) {
		return Sections.successor(section, FlowchartTermDef.class);
	}

}
