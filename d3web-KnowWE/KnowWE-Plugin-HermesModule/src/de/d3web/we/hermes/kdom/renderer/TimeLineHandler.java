package de.d3web.we.hermes.kdom.renderer;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
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
import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.sparql.SparqlDelegateRenderer;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TimeLineHandler extends AbstractTagHandler {

	public TimeLineHandler() {
		super("zeitlinie");
	}

	private static ResourceBundle kwikiBundle = ResourceBundle
			.getBundle("KnowWE_messages");

	private static final String TIME_SPARQL = "SELECT ?t WHERE { ?t lns:hasImportance ?x . ?t lns:hasStartDate ?y . FILTER ( ?y > \"YEAR\" ^^xsd:double) .}";
	private static final String TIME_AFTER = "nach";

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		String yearAfter = values.get(TIME_AFTER);
		String querystring = null;

		try {
			int i = Integer.parseInt(yearAfter);
			querystring = TIME_SPARQL.replaceAll("YEAR", yearAfter);

		} catch (Exception e) {
			return "no valid parameter for" + TIME_AFTER;
		}

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
			query = con.prepareQuery(QueryLanguage.SPARQL, SparqlDelegateRenderer.addNamespaces(querystring,topic));
		} catch (RepositoryException e) {
			return e.getMessage();
		} catch (MalformedQueryException e) {
			return e.getMessage();
		}
		try {
			if (query instanceof TupleQuery) {
				TupleQueryResult result = ((TupleQuery) query).evaluate();
				return KnowWEEnvironment.maskHTML(renderQueryResult(result,
						values));
			} else if (query instanceof GraphQuery) {
				// GraphQueryResult result = ((GraphQuery) query).evaluate();
				return "graphquery ouput implementation: TODO";
			} else if (query instanceof BooleanQuery) {
				boolean result = ((BooleanQuery) query).evaluate();
				return result + "";
			}
		} catch (QueryEvaluationException e) {
			return kwikiBundle.getString("KnowWE.owl.query.evalualtion.error")
					+ ":" + e.getMessage();
		} finally {

		}
		return null;
	}

	private String renderQueryResult(TupleQueryResult result,
			Map<String, String> params) {
		//List<String> bindings = result.getBindingNames();
		StringBuffer buffy = new StringBuffer();
		try {
			while (result.hasNext()) {
				BindingSet set = result.next();
				Set<String> names = set.getBindingNames();
				for (String string : names) {
					Binding b = set.getBinding(string);
					Value event = b.getValue();
					buffy.append(URLDecoder.decode(event.toString(), "UTF-8")+"<br>");
				}
				
				
			}
		} catch (QueryEvaluationException e) {
			return kwikiBundle.getString("KnowWE.owl.query.evalualtion.error")
					+ ":" + e.getMessage();
		}
		catch (UnsupportedEncodingException e) {
			return e.toString();
		}
		
		return buffy.toString();
	}

}
