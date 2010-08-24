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
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * e.g. [{KnowWEPlugin faq=all; status=stud; major=inf;}] renders all faq
 * entries where status = stud (student) and major = inf (informatics)
 *
 * if isset faq=all --> render all entries = SPARQL_ALL-querystring
 *
 * if not isset faq=all --> query the attributes major and status and adapt the
 * SPARQL accordingly
 *
 * @author M. Freiberg
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

		// in case faq-attribute is not "all", the SPARQL needs adaption
		if (faqVal != null && !faqVal.equals("all")) {

			String major = "";
			String status = "";
			boolean maj = false;

			// check for all annotations if provided. In case they are, replace
			// the variables with the given annotation.

			// check for major, first
			if (values.get("major") != null) {

				major = values.get("major");
				querystring =
						querystring.replace("}", ". " + "FILTER (regex(?m, \"" + major
								+ "\"))" + "}");
				maj = true;
			}

			if (values.get("status") != null) {

				status = values.get("status");

				if (maj) {
					querystring =
							querystring.replace("}", " FILTER (regex(?s, \"" + status
									+ "\"))" + "}");
				}
				else {
					querystring =
							querystring.replace("}", ". " + " FILTER (regex(?s, \""
									+ status + "\"))" + "}");
				}
			}
		}

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
		try {
			resultString = renderQueryResult(result);
			resultString = FAQUtils.renderFAQPluginFrame(resultString);
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
	// * TODO wirft noch Fehler falls leeres Markup in der SEITE
	private String renderQueryResult(TupleQueryResult result) throws QueryEvaluationException {

		List<String> sortedFAQs = sortAlphabetically(result);
		StringBuilder string = new StringBuilder();
		String[] resultVals = null;
	
		// render the topmost category-link line
		// string.append(FAQUtils.renderCategoriesAnchorLinks());

		// go through all possible FAQ categories, for each check whether the
		// current entry from the list starts with that symbol, if yes append
		 for (FAQCats symbol : FAQCats.values()) {

			// print the category symbol
			string.append(FAQUtils.printCategory(symbol));

			for (int i = 0; i < sortedFAQs.size(); i++) {
				resultVals = sortedFAQs.get(i).split("----");

				if (resultVals[0].startsWith(symbol.toString()) ||
					resultVals[0].startsWith(symbol.toString().toLowerCase())) {
			
					string.append(FAQUtils.renderFAQPluginInner(
							resultVals[0], resultVals[1], resultVals[2], resultVals[3]));
				}
			}
		}
		return string.toString();
	}

	/**
	 * Takes a TupleQueryResult from the SPARQL query and creates a list with
	 * all FAQ entries, the entry vals each separated by "----", that is sorted
	 * alphabetically
	 *
	 * @created 03.08.2010
	 * @param result the TupleQueryResult from the SPARQL query
	 * @return the sorted list of FAQ entries in String representation
	 */
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
					a = FAQUtils.resolveLinks(a);
					System.out.println(a);
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
			}
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		// Sorts the FAQ-Strings correctly regarding the alphabet
		Collator coll = Collator.getInstance(Locale.GERMAN);
		coll.setStrength(Collator.SECONDARY);
		Collections.sort(faqList, coll);

		return faqList;
	}
}
