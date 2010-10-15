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

package de.d3web.we.hermes.taghandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.semantic.OwlHelper;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.hermes.TimeStamp;
import de.d3web.we.hermes.util.TimeEventSPARQLUtils;
import de.d3web.we.taghandler.AbstractHTMLTagHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class LocalTimeEventsHandler extends AbstractHTMLTagHandler {

	public LocalTimeEventsHandler() {
		super("lokaleZeitlinie");
	}

	private static final String TIME_SPARQL = "SELECT  ?t ?title ?imp ?desc ?y WHERE {  ?t rdfs:isDefinedBy ?to . ?to ns:hasTopic TOPIC . ?t lns:hasDescription ?desc . ?t lns:hasTitle ?title . ?t lns:hasImportance ?imp . ?t lns:hasDateDescription ?y .}";
	private static final String TIME_AFTER = "nach";

	// ?t lns:isDefinedBy ?to . ?to lns:hasTopic TOPIC .
	@Override
	public String renderHTML(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		OwlHelper helper = SemanticCoreDelegator.getInstance().getUpper().getHelper();

		String yearAfter = getIntAsString(-10000, values, TIME_AFTER);
		String querystring = null;
		try {
			querystring = TIME_SPARQL.replaceAll("TOPIC",
					"<" + helper.createlocalURI(topic).toString() + ">");
		}
		catch (Exception e) {
			return "Illegal query String: " + querystring + "<br />"
					+ " no valid parameter for: " + TIME_AFTER;
		}

		TupleQueryResult qResult = TimeEventSPARQLUtils
				.executeQuery(querystring);

		return KnowWEUtils.maskHTML(renderQueryResult(qResult, values));

	}

	private String getIntAsString(int defaultValue,
			Map<String, String> valueMap, String valueFromMap) {
		try {
			return String.valueOf(Integer.parseInt(valueMap.get(valueFromMap)));
		}
		catch (NumberFormatException nfe) {
			return String.valueOf(defaultValue);
		}
	}

	private String renderQueryResult(TupleQueryResult result,
			Map<String, String> params) {
		// List<String> bindings = result.getBindingNames();
		StringBuffer buffy = new StringBuffer();
		try {
			buffy.append("<ul>");
			boolean found = false;
			TreeMap<TimeStamp, String> queryResults = new TreeMap<TimeStamp, String>();
			while (result.hasNext()) {
				found = true;
				BindingSet set = result.next();
				try {
					String importance = URLDecoder.decode(set.getBinding("imp")
							.getValue().stringValue(), "UTF-8");
					if (importance.equals("(1)")) {

						String title = URLDecoder.decode(set
								.getBinding("title").getValue().stringValue(),
								"UTF-8");
						String timeString = URLDecoder.decode(set.getBinding(
								"y").getValue().stringValue(), "UTF-8");
						TimeStamp timeStamp = new TimeStamp(timeString);
						String timeDescr = timeStamp.getDescription();
						queryResults.put(timeStamp, "<li>" + timeDescr + ": " + title
								+ "</li>");
					}
				}
				catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				// Set<String> names = set.getBindingNames();
				// for (String string : names) {
				// Binding b = set.getBinding(string);
				// Value event = b.getValue();
				// buffy.append(URLDecoder.decode(event.toString(), "UTF-8")
				// + "<br>");
				// }
			}
			for (String s : queryResults.values()) {
				buffy.append(s);
			}
			if (!found) buffy.append("no results found");
			buffy.append("</ul>");
		}
		catch (QueryEvaluationException e) {
			return "error";
		}
		return buffy.toString();
	}
}
