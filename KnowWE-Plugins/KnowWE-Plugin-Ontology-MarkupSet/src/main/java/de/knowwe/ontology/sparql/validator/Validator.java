/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.ontology.sparql.validator;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;

import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * 
 * @author Sebastian Furth
 * @created 20.12.2011
 */
public class Validator {

	private static final SPARQLParser parser = new SPARQLParser();
	private static final int LINESTOSUBTRACT = 9;

	private Validator() {
	}

	/**
	 * Validates the committed SPARQL-Query by parsing it using Sesame's
	 * SPARQLParser.
	 * 
	 * @created 20.12.2011
	 * @param query A SPARQL-Query
	 * @param baseURI (optional) The base URI for the SPARQL-Query
	 * @return ValidatorResult wrapping the validation result
	 */
	public static ValidatorResult validate(Rdf2GoCore core, String query) {
		String prefixes = Rdf2GoUtils.getSparqlNamespaceShorts(core);
		// add default prefixes if necessary
		if (!query.startsWith(prefixes)) {
			query = prefixes + query;
		}
		ValidatorResult result = new ValidatorResult(query);

		try {
			parser.parseQuery(query, Rdf2GoCore.getInstance().getLocalNamespace());
		}
		catch (MalformedQueryException e) {
			// correct line numbers (caused by auto adding prefixes)
			String msg = e.getMessage();
			if (msg.contains("line")) {
				try {
					// find the line number
					int start = msg.indexOf("line ") + 5;
					int i = start;
					while (Character.isDigit(msg.charAt(i))) {
						i++;
					}
					String digits = msg.substring(start, i);
					// correct the line number
					int lineNumber = Integer.parseInt(digits);
					lineNumber = lineNumber - LINESTOSUBTRACT;
					msg = msg.replace("line " + digits, "line " + lineNumber);
					e = new MalformedQueryException(msg);
				}
				catch (NumberFormatException nfe) {
					// unable to find number, we have to live with the wrong
					// line number
				}
			}
			result.addException(e);
		}

		return result;
	}

}
