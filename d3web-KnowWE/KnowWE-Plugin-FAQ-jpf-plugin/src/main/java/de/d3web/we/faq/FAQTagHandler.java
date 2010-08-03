/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.faq;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import de.d3web.we.core.semantic.ISemanticCore;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
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
				// String[] majors = major.split(" ");
				// for (String m : majors) {
				try {
					querystring = querystring.replace("}", ". ?t lns:hasmajor \"" + major + "\" }");
					querystring = querystring.replace("}", ". FILTER (regex(?m, \"" + major
							+ "\"))}");
				}

				// FILTER (regex(?title, "SPARQL")) .
				catch (Exception e) {
					return "Illegal query String: " + querystring + "<br />"
								+ " no valid parameter for: " + "MAJOR";
				}
				// }

			}
			if (values.get("status") != null) {
				status = values.get("status");
				// String[] states = status.split(" ");
				// for (String s : states) {
				try {
					querystring = querystring.replace("}", ". ?t lns:hasstatus \"" + status
								+ "\" }");
				}
				catch (Exception e) {
					return "Illegal query String: " + querystring + "<br />"
								+ " no valid parameter for: " + "STATUS";
				}
				// }
			}
		}
		// System.out.println(querystring);

		/**
		 * establish connection and send query
		 */
		ISemanticCore sc = SemanticCoreDelegator.getInstance();
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

		List<String> sortedFAQs = sortAlphabetically(result);
		StringBuilder string = new StringBuilder();
		String[] resultVals = null;

		// go through all binding sets, i.e., result sets of the query
		for (int i = 0; i < sortedFAQs.size(); i++) {

			resultVals = sortedFAQs.get(i).split("----");

			// build the HTML
			string.append(KnowWEUtils.maskHTML("<div class='faq_question'> Q: "));
			string.append(KnowWEUtils.maskHTML(resultVals[0]));
			string.append(KnowWEUtils.maskHTML("</div>"));
			string.append(KnowWEUtils.maskHTML("<div class='faq_answer'> "));
			String answer = resolvePDFs(createLinks(resultVals[1]));
			string.append(KnowWEUtils.maskHTML(answer));
			string.append(KnowWEUtils.maskHTML("<div class='faq_tags'> "));
			string.append(KnowWEUtils.maskHTML(resultVals[2]));
			string.append(KnowWEUtils.maskHTML(" "));
			string.append(KnowWEUtils.maskHTML(resultVals[3]));
			string.append(KnowWEUtils.maskHTML("</div>"));
			string.append(KnowWEUtils.maskHTML("</div>"));

			System.out.println(string.toString());
		}
		return string.toString();
	}

	private List<String> sortAlphabetically(TupleQueryResult result) {

		List<String> faqList = new ArrayList<String>();

		try {
			while (result.hasNext()) {
				StringBuilder bui = new StringBuilder();
				BindingSet set = result.next();

				if (set.getValue("q") != null) {
					String value = set.getValue("q").stringValue();
					bui.append(value);
					bui.append("----");
				}
				else {
					bui.append("");
					bui.append("----");
				}
				if (set.getValue("a") != null) {
					bui.append("A: ");
					String a = set.getValue("a").stringValue();
					a = a.trim();
					a = createLinks(a);
					bui.append(a);
					bui.append("----");
				}
				else {
					bui.append("");
					bui.append("----");
				}
				if (set.getValue("s") != null) {
					bui.append(set.getValue("s").stringValue());
					bui.append("----");
				}
				else {
					bui.append("");
					bui.append("----");
				}
				if (set.getValue("m") != null) {
					bui.append(set.getValue("m").stringValue());
				}
				else {
					bui.append("");
				}
				faqList.add(bui.toString());
				System.out.println(bui.toString());
			}
		}
		catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Collator coll = Collator.getInstance(Locale.GERMAN);
		coll.setStrength(Collator.SECONDARY);
		Collections.sort(faqList, coll);

		return faqList;
	}

	private String createLinks(String a) {
		a = a.replaceAll("-L-", "<a href='");
		a = a.replaceAll("-/L-", "' ><b>" + "link" + "</b></a>");

		return a;
	}

	private String resolvePDFs(String a) {
		if (a.isEmpty() || a.equals(" ") || !a.contains("-A-")) {
			return a;
		}
		String name = a.substring(a.indexOf("-A-") + 3, a.indexOf("-/A-"));
		System.out.println(name);
		a = a.replaceAll("-A-", "<b><a href='/KnowWE/attach/FAQ%20Entry/" + name + "'>");
		a = a.replaceAll("-/A-", "</a></b>");
		System.out.println(a);
		return a;
	}

	/**
	 * Create the frame for correctly rendering the plugin
	 * 
	 * @created 13.07.2010
	 * @return
	 */
	private String renderPlugin(String pluginInner) {
		StringBuilder string = new StringBuilder();
		string.append(KnowWEUtils.maskHTML("<div class='panel'>"));
		string.append(KnowWEUtils.maskHTML("<h3>FAQ Plugin</h3>"));
		string.append(KnowWEUtils.maskHTML(pluginInner));
		string.append(KnowWEUtils.maskHTML("</div>"));
		return string.toString();
	}
}
