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

import java.util.Collection;

import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.kdom.objectproperty.Property;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;

/**
 * SELECT DISTINCT ?resource WHERE { ?resource rdf:type rdfs:Resource . FILTER(REGEX(STR(?resource
 * ), "^http://www.w3.org/1999/02/22-rdf-syntax-ns")) . }
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 04.03.2013
 */
public class InitTerminologyHandler extends OntologyHandler<PackageCompileType> {

	private static final String NAMESPACE_FILTER = "FILTER(REGEX(STR(?resource), " +
			"\"^http://www.w3.org/(" +
			"1999/02/22-rdf-syntax-ns#|2000/01/rdf-schema#|2002/07/owl#|2001/XMLSchema#|2005/xpath-functions#)\"))";

	@Override
	public Collection<Message> create(OntologyCompiler compiler, Section<PackageCompileType> section) {

		InitTerminologyHelper helper = new InitTerminologyHelper();
		String query = new SparqlQuery().SELECT("?resource")
				.WHERE("{ ?resource rdf:type rdfs:Resource } UNION { ?resource rdf:type rdfs:Class } MINUS { ?resource rdf:type rdf:Property }")
				.AND_WHERE(
						NAMESPACE_FILTER).toString();
		helper.registerTerminology(compiler, section, query, Resource.class);

		query = new SparqlQuery().SELECT("?resource")
				.WHERE("?resource rdf:type rdf:Property")
				.AND_WHERE(
						NAMESPACE_FILTER).toString();
		helper.registerTerminology(compiler, section, query, Property.class);

		// TODO: @albi: please check and discuss --> remove or extend as appropriate
		helper.registerTerm(compiler, section, "http://www.w3.org/2005/xpath-functions#string-length", Resource.class);
		helper.registerTerm(compiler, section, "http://www.w3.org/2001/XMLSchema#decimal", Resource.class);

		return Messages.noMessage();
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<PackageCompileType> section) {
		// no need to remove something, we get a new TerminologyManager
		// anyway...
	}

	private static class InitTerminologyHelper extends TerminologyHelper {

		@Override
		protected String getAbbreviation(String string) {
			String abbreviation;
			if (string.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
				abbreviation = "rdf";
			}
			else if (string.startsWith("http://www.w3.org/2000/01/rdf-schema#")) {
				abbreviation = "rdfs";
			}
			else if (string.startsWith("http://www.w3.org/2001/XMLSchema#")) {
				abbreviation = "xsd";
			}
			else if (string.startsWith("http://www.w3.org/2005/xpath-functions#")) {
				abbreviation = "fn";
			}
			else {
				abbreviation = "owl";
			}
			return abbreviation;
		}
	}

}
