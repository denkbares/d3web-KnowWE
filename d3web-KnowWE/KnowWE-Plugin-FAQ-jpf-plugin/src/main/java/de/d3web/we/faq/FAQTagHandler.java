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

package de.d3web.we.faq;

import java.util.Map;
import java.util.ResourceBundle;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.core.SemanticCore;
import de.d3web.we.kdom.sparql.SparqlDelegateRenderer;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * e.g. [{KnowWEPlugin faq=all; status=I; major=BAInf;}] renders all faq entries
 * where status = I (Informatics) and major = BAInf (Bachelor Informatics)
 *
 * falls faq=all --> alle Einträge rendern = SPARQL_ALL
 *
 * falls nicht --> der Reihe nach die Attribute abfragen was gegeben ist, und
 * wenn ja dynamisch ins SPARQL_BASE reinziehen
 *
 * TODO für die Attribute prüfen ob nur eins oder mehrere gegeben sind, ggf den
 * SPARQL anders dynamisch zusammensetzen. Solche mehreren Attribute müssen wenn
 * mit Leerzeichen getrennt werden
 *
 * @author Martina Freiberg
 * @date July 2010
 */
public class FAQTagHandler extends AbstractTagHandler {

	private static ResourceBundle kwikiBundle = ResourceBundle
			.getBundle("KnowWE_messages");

	/**
	 * Basic SPARQL query, which fetches all the RDF Data (FAQ entries) if not
	 * altered
	 */
	private static String SPARQL_BASE =
			"SELECT ?q ?a ?s ?m WHERE { " +
					"?t lns:hasquestion ?q . " +
					"?t lns:hasContent ?a . " +
					"?t lns:hasstatus ?s . " +
					"?t lns:hasmajor ?m }";

	/**
	 * Create the TagHandler --> "faq" defines the "name" of the tag, so the tag
	 * is inserted in the wiki page like [KnowWEPlugin faq]
	 */
	public FAQTagHandler() {
		super("faq");
		KnowWERessourceLoader.getInstance().add("faq.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);

	}

	@Override
	/**
	 * assembles the HTML String that is rendered into the page based on the
	 * SPARQL query
	 */
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		String querystring = SPARQL_BASE;

		// check, whether all faqs should be fetched ("all") or if there are
		// further specifications‚
		String faqVal = values.get("faq");
		if (faqVal != null && faqVal.equals("all")) {
			querystring = SPARQL_BASE;
		}
		else { // further specifications‚
			String major = "";
			String status = "";

			// check for all annotations whether they are provided. In case they
			// are, replace the variables with the given annotation. Then, the
			// first variable needs to be reset to SPARQL variable style
			// also, the String with the variable needs to be inserted again to
			// get the value nevertheless
			if (values.get("major") != null) {
				major = values.get("major");
				String[] majors = major.split(" ");
				for (String m : majors) {
					try {
						querystring = querystring.replace("}", ". ?t lns:hasmajor \"" + m + "\" }");
					}
					catch (Exception e) {
						return "Illegal query String: " + querystring + "<br />"
								+ " no valid parameter for: " + "MAJOR";
					}
				}

			}
			if (values.get("status") != null) {
				status = values.get("status");
				String[] states = status.split(" ");
				for (String s : states) {
					try {
						querystring = querystring.replace("}", ". ?t lns:hasstatus \"" + s
								+ "\" }");
					}
					catch (Exception e) {
						return "Illegal query String: " + querystring + "<br />"
							+ " no valid parameter for: " + "STATUS";
					}
				}
			}
		}

		/**
		 * establish connection and send query
		 */
		SemanticCore sc = SemanticCore.getInstance();
		RepositoryConnection con = sc.getUpper().getConnection();
		Query query = null;
		try {
			query = con.prepareQuery(QueryLanguage.SPARQL,
					SparqlDelegateRenderer.addNamespaces(querystring));
		}
		catch (RepositoryException e) {
			return e.getMessage();
		}
		catch (MalformedQueryException e) {
			return e.getMessage();
		}

		// System.out.println(querystring);

		/**
		 * If query returned result, evaluate result
		 */
		TupleQueryResult result = null;
		try {
			if (query instanceof TupleQuery) {
				result = ((TupleQuery) query).evaluate();
			}
		}
		catch (QueryEvaluationException e) {
			return kwikiBundle.getString("KnowWE.owl.query.evaluation.error") + ":" +
					e.getMessage();
		}
		finally {

		}

		/**
		 * get actual HTML String based on the query result and return it
		 */
		String resultString = "";
		String inner = "";
		try {

			inner = renderQueryResult(result);
			resultString = renderPlugin(inner);
		}
		catch (QueryEvaluationException e) {
		}

		return resultString;
	}

	/**
	 * Assembles the actual HTML String for display in the wiki page
	 *
	 * @created 06.07.2010
	 * @param result the result of the SPARQL query that is to be displayed in
	 *        the page
	 * @return String the HTML String that is returned to be rendered in the
	 *         page
	 * @throws QueryEvaluationException
	 */
	private String renderQueryResult(TupleQueryResult result) throws QueryEvaluationException {


		StringBuilder string = new StringBuilder();

		String answer = "";
		String question = "";
		String status = "";
		String major = "";

		// go through all binding sets, i.e., result sets of the query
		while (result.hasNext()) {

			// check for each annotation if there exists corresponding binding,
			// in case yes, write result to variable
			BindingSet set = result.next();
			if (set.getValue("a") != null) {
				answer = set.getValue("a").stringValue();
			}
			if (set.getValue("q") != null) {
				question = set.getValue("q").stringValue();
			}
			if (set.getValue("s") != null) {
				status = set.getValue("s").stringValue();
			}
			if (set.getValue("m") != null) {
				major = set.getValue("m").stringValue();
			}

			// build the HTML
			string.append(KnowWEUtils.maskHTML("<div class='faq_question'> Question: "));
			string.append(KnowWEUtils.maskHTML(question));
			string.append(KnowWEUtils.maskHTML("</div>"));
			string.append(KnowWEUtils.maskHTML("<div class='faq_answer'>"));
			string.append(KnowWEUtils.maskHTML(answer));
			string.append(KnowWEUtils.maskHTML("<div class='faq_tags'>"));
			string.append(KnowWEUtils.maskHTML(status));
			string.append(KnowWEUtils.maskHTML(" "));
			string.append(KnowWEUtils.maskHTML(major));
			string.append(KnowWEUtils.maskHTML("</div>"));
			string.append(KnowWEUtils.maskHTML("</div>"));
		}
		return string.toString();
	}


	/**
	 * Create the frame for correctly rendering the plugin
	 *
	 * @created 13.07.2010
	 * @return
	 */
	private String renderPlugin(String pluginInner) {
		StringBuilder string = new StringBuilder();
		string.append(KnowWEUtils.maskHTML("<div class='faqpanel'>"));
		string.append(KnowWEUtils.maskHTML("<div class='panel'>"));
		string.append(KnowWEUtils.maskHTML("<h3>FAQ Plugin</h3>"));
		string.append(KnowWEUtils.maskHTML(pluginInner));
		string.append(KnowWEUtils.maskHTML("</div>"));
		string.append(KnowWEUtils.maskHTML("</div>"));
		return string.toString();
	}
}
