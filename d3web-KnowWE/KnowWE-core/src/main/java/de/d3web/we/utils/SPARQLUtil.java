package de.d3web.we.utils;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.sparql.SparqlDelegateRenderer;

public class SPARQLUtil {
	
	public static TupleQueryResult executeTupleQuery(String q, String topic) {
		SemanticCore sc = SemanticCore.getInstance();
		RepositoryConnection con = sc.getUpper().getConnection();
		try {
			con.setAutoCommit(false);
		} catch (RepositoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Query query = null;
		try {
			query = con.prepareQuery(QueryLanguage.SPARQL, SparqlDelegateRenderer.addNamespaces(q,topic));
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		}
		try {
			if (query instanceof TupleQuery) {
				TupleQueryResult result = ((TupleQuery) query).evaluate();
				return result;
			} 
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} finally {

		}
		return null;
	}

}
