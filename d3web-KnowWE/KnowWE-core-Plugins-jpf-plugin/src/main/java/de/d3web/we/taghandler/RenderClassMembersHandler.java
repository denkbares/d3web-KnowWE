package de.d3web.we.taghandler;

import java.util.Map;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.semantic.ISemanticCore;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.kdom.sparql.DefaultSparqlRenderer;
import de.d3web.we.kdom.sparql.SparqlDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class RenderClassMembersHandler extends AbstractTagHandler {

	public RenderClassMembersHandler() {
		super("listClassMembers");
	}

	private static final String TIME_SPARQL = "SELECT ?x WHERE { ?x rdf:type lns:CLASS .} ORDER BY ASC(?x)";

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		String className = values.get("class");

		if (className == null) {
			return "No class given for list class members tag!";
		}
		String querystring = TIME_SPARQL.replaceAll("CLASS", className);

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
					SparqlDelegateRenderer.addNamespaces(querystring));
		} catch (RepositoryException e) {
			return e.getMessage();
		} catch (MalformedQueryException e) {
			return e.getMessage();
		}
		try {
			if (query instanceof TupleQuery) {
				TupleQueryResult result = ((TupleQuery) query).evaluate();

				// values.put("render", "links");
				return KnowWEEnvironment.maskHTML(DefaultSparqlRenderer
						.getInstance().renderResults(result, true));
			} else if (query instanceof GraphQuery) {
				// GraphQueryResult result = ((GraphQuery) query).evaluate();
				return "graphquery ouput implementation: TODO";
			} else if (query instanceof BooleanQuery) {
				boolean result = ((BooleanQuery) query).evaluate();
				return result + "";
			}
		} catch (QueryEvaluationException e) {
			return ("KnowWE.owl.query.evalualtion.error") + ":"
					+ e.getMessage();
		} finally {

		}
		return null;
	}
}
