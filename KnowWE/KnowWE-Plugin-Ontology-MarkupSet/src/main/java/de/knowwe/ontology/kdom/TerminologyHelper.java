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
package de.knowwe.ontology.kdom;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryRow;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.namespace.AbbreviationDefinition;
import de.knowwe.ontology.kdom.objectproperty.Property;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 05.03.2013
 */
public abstract class TerminologyHelper {

	public void registerTerminology(OntologyCompiler compiler, Section<?> section, String namespace) {
		String query = new SparqlQuery().SELECT("?resource")
				.WHERE("?resource rdf:type ?any MINUS { ?resource rdf:type rdf:Property }")
				.AND_WHERE(
						"FILTER(REGEX(STR(?resource), \"^" + namespace + "\"))").toString();
		Class<? extends Resource> termClass = Resource.class;
		registerTerminology(compiler, section, query, termClass);

		query = new SparqlQuery().SELECT("?resource")
				.WHERE("?resource rdf:type rdf:Property")
				.AND_WHERE("FILTER(REGEX(STR(?resource), \"^" + namespace + "\"))").toString();
		termClass = Property.class;
		registerTerminology(compiler, section, query, termClass);
	}

	public void registerTerminology(OntologyCompiler compiler, Section<?> section, String query, Class<? extends Resource> termClass) {
		ClosableIterator<QueryRow> iterator =
				Rdf2GoCore.getInstance(compiler).sparqlSelectIt(query);
		while (iterator.hasNext()) {
			QueryRow row = iterator.next();
			String value = row.getValue("resource").toString();
			registerTerm(compiler, section, value, termClass);

		}
	}

	public void registerTerm(OntologyCompiler compiler, Section<?> section, String uri, Class<?> termClass) {
		String resource = uri.substring(uri.indexOf("#") + 1);
		String abbreviation = getAbbreviation(uri);
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		Identifier abbrIdentifier = new Identifier(abbreviation, resource);
		terminologyManager.registerTermDefinition(compiler, section,
				AbbreviationDefinition.class, new Identifier(abbreviation));
		terminologyManager.registerTermDefinition(
				compiler, section, termClass, abbrIdentifier);
	}

	protected abstract String getAbbreviation(String string);

}
