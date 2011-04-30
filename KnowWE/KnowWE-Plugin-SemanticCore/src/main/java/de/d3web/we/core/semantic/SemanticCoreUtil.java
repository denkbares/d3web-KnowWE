/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.core.semantic;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * 
 * Copied from SPARQLUtil to prevent dependency to Research.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created Nov 15, 2010
 */
public class SemanticCoreUtil {

	public static TupleQueryResult executeTupleQuery(String q, String topic) {
		ISemanticCore sc = SemanticCoreDelegator.getInstance();
		RepositoryConnection con = sc.getUpper().getConnection();
		Query query = null;
		try {
			query = con.prepareQuery(QueryLanguage.SPARQL,
					addNamespaces(q));
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		catch (MalformedQueryException e) {
			e.printStackTrace();
		}
		try {
			if (query instanceof TupleQuery) {
				TupleQueryResult result = ((TupleQuery) query).evaluate();
				return result;
			}
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String addNamespaces(String newns) {
		if (newns == null) newns = "";
		String rawquery = newns.trim();
		String querystring = SemanticCoreDelegator.getInstance()
				.getSparqlNamespaceShorts()
				+ rawquery;
		return querystring;
	}

}
