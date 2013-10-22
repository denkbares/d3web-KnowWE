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
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.kdom.objectproperty.Property;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.rdf2go.sparql.utils.SparqlQuery;

/**
 * SELECT DISTINCT ?resource WHERE { ?resource rdf:type rdfs:Resource .
 * FILTER(REGEX(STR(?resource ), "^http://www.w3.org/1999/02/22-rdf-syntax-ns"))
 * . }
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 04.03.2013
 */
public class InitTerminologyHandler extends SubtreeHandler<PackageCompileType> {

	@Override
	public Collection<Message> create(Article article, Section<PackageCompileType> section) {

		String query = new SparqlQuery().SELECT("?resource")
				.WHERE("?resource rdf:type rdfs:Resource MINUS { ?resource rdf:type rdf:Property }")
				.AND_WHERE(
						"FILTER(REGEX(STR(?resource), \"^http://www.w3.org/(1999/02/22-rdf-syntax-ns#|2000/01/rdf-schema#|2002/07/owl#)\"))").toString();
		Class<? extends Resource> termClass = Resource.class;
		new InitTerminologyHelper().registerTerminology(article, section, query, termClass);

		query = new SparqlQuery().SELECT("?resource")
				.WHERE("?resource rdf:type rdf:Property")
				.AND_WHERE(
						"FILTER(REGEX(STR(?resource), \"^http://www.w3.org/(1999/02/22-rdf-syntax-ns#|2000/01/rdf-schema#|2002/07/owl#)\"))").toString();
		termClass = Property.class;
		new InitTerminologyHelper().registerTerminology(article, section, query, termClass);
		return Messages.noMessage();
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
			else {
				abbreviation = "owl";
			}
			return abbreviation;
		}
	}

}
