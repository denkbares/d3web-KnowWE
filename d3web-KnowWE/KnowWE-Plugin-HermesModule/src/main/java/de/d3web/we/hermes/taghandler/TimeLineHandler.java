/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.hermes.taghandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

	private static final String TIME_SPARQL = "SELECT ?a WHERE { ?t lns:hasTitle ?a . ?t lns:hasImportance ?x . ?t lns:hasStartDate ?y . FILTER ( ?y > \"YEAR\" ^^xsd:double) .}";
	private static final String TIME_AFTER = "nach";

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		boolean asList = false;
		if (values.containsKey("renderType")
				&& values.get("renderType").equals("list")) {
			asList = true;
		}

		String yearAfter = getIntAsString(-10000, values, TIME_AFTER);
		String querystring = null;
		try {
			querystring = TIME_SPARQL.replaceAll("YEAR", yearAfter);
		} catch (Exception e) {
			return "Illegal query String: " + querystring + "<br />"
					+ " no valid parameter for: " + TIME_AFTER;
		}

		SemanticCore sc = SemanticCore.getInstance();
		RepositoryConnection con = sc.getUpper().getConnection();
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
				return KnowWEEnvironment.maskHTML(renderQueryResult(result,
						values, asList));
			} else if (query instanceof GraphQuery) {
				// GraphQueryResult result = ((GraphQuery) query).evaluate();
				return "graphquery output implementation: TODO";
			} else if (query instanceof BooleanQuery) {
				boolean result = ((BooleanQuery) query).evaluate();
				return result + "";
			}
		} catch (QueryEvaluationException e) {
			return kwikiBundle.getString("KnowWE.owl.query.evaluation.error")
					+ ":" + e.getMessage();
		} finally {

		}
		return null;
	}

	private String getIntAsString(int defaultValue,
			Map<String, String> valueMap, String valueFromMap) {
		try {
			return String.valueOf(Integer.parseInt(valueMap.get(valueFromMap)));
		} catch (NumberFormatException nfe) {
			return String.valueOf(defaultValue);
		}
	}

	private String renderQueryResult(TupleQueryResult result,
			Map<String, String> params, boolean asList) {
		// List<String> bindings = result.getBindingNames();
		StringBuffer buffy = new StringBuffer();
		try {
			while (result.hasNext()) {
				BindingSet set = result.next();
				Set<String> names = set.getBindingNames();
				for (String string : names) {
					Binding b = set.getBinding(string);
					Value event = b.getValue();
					buffy.append(URLDecoder.decode(event.toString(), "UTF-8")
							+ "<br>");
				}

			}
		} catch (QueryEvaluationException e) {
			return kwikiBundle.getString("KnowWE.owl.query.evalualtion.error")
					+ ":" + e.getMessage();
		} catch (UnsupportedEncodingException e) {
			return e.toString();
		}

		return buffy.toString();
	}

}
