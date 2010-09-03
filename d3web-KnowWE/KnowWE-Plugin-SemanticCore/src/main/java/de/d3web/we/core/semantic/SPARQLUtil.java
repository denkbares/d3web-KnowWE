/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

import org.openrdf.model.URI;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.sparql.SparqlDelegateRenderer;

public class SPARQLUtil {

	@Deprecated
	/**
	 * Executes a boolean sparql-query (like 'ASK {...}')
	 * 
	 * @param q
	 * @return
	 */
	public static Boolean executeBooleanQuery(String q) {
		ISemanticCore sc = SemanticCoreDelegator.getInstance();
		RepositoryConnection con = sc.getUpper().getConnection();
		// try {
		// con.setAutoCommit(false);
		// } catch (RepositoryException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		Query query = null;
		try {
			query = con.prepareQuery(QueryLanguage.SPARQL,
					SparqlDelegateRenderer.addNamespaces(q));
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		catch (MalformedQueryException e) {
			e.printStackTrace();
		}
		try {
			if (query instanceof BooleanQuery) {
				boolean result = ((BooleanQuery) query).evaluate();
				return Boolean.valueOf(result);
			}
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		finally {

		}
		return false;
	}

	public static TupleQueryResult executeTupleQuery(String q, String topic) {
		ISemanticCore sc = SemanticCoreDelegator.getInstance();
		RepositoryConnection con = sc.getUpper().getConnection();
		// try {
		// con.setAutoCommit(false);
		// } catch (RepositoryException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		Query query = null;
		try {
			query = con.prepareQuery(QueryLanguage.SPARQL,
					SparqlDelegateRenderer.addNamespaces(q));
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
		finally {

		}
		return null;
	}

	public static TupleQueryResult executeTupleQuery(String q) {
		ISemanticCore sc = SemanticCoreDelegator.getInstance();
		RepositoryConnection con = sc.getUpper().getConnection();
		// try {
		// con.setAutoCommit(false);
		// } catch (RepositoryException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		Query query = null;
		try {
			query = con.prepareQuery(QueryLanguage.SPARQL,
					SparqlDelegateRenderer.addNamespaces(q));
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
		finally {

		}
		return null;
	}

	private static final String CLASS_SPARQL = "SELECT ?x WHERE { <URI> rdf:type ?x.} ";

	public static TupleQueryResult findClassesOfEntity(URI uri) {
		return executeTupleQuery(CLASS_SPARQL.replaceAll("URI", uri.toString()));
	}

	private static final String SUBCLASS_SPARQL = "SELECT ?x WHERE { ?x rdfs:subClassOf <URI>.} ";

	public static TupleQueryResult findSubClasses(URI uri) {
		return executeTupleQuery(SUBCLASS_SPARQL.replaceAll("URI", uri
				.toString()));
	}

	private static final String SUBJECT_SPARQL = "SELECT ?x WHERE { ?x <PRED-URI> <OBJECT-URI>.} ";

	public static TupleQueryResult findSubjects(URI object, URI predicate) {
		String query = SUBJECT_SPARQL.replaceAll("OBJECT-URI", object
				.toString());
		query = query.replaceAll("PRED-URI", predicate.toString());
		return executeTupleQuery(query);
	}

	private static final String OBJECT_SPARQL = "SELECT ?x WHERE { <SUBJECT-URI> <PRED-URI> ?x.} ";

	public static TupleQueryResult findObjects(URI subject, URI predicate) {
		String query = OBJECT_SPARQL.replaceAll("SUBJECT-URI", subject
				.toString());
		query = query.replaceAll("PRED-URI", predicate.toString());
		return executeTupleQuery(query);
	}

	private static final String SUPERCLASS_SPARQL = "SELECT ?x WHERE { <URI> rdfs:subClassOf ?x.} ";

	public static TupleQueryResult findSuperClasses(URI uri) {
		return executeTupleQuery(SUPERCLASS_SPARQL.replaceAll("URI", uri
				.toString()));
	}

}
