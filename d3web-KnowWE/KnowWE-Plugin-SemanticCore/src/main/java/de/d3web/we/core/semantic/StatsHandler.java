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

/**
 * 
 */
package de.d3web.we.core.semantic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.UpperOntology;
import de.d3web.we.core.semantic.tagging.TaggingMangler;
import de.d3web.we.taghandler.AbstractHTMLTagHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author kazamatzuri Einfacher TagHandler der unterschiedliche Statistiken des
 *         Wikis darstellt.
 * 
 */
public class StatsHandler extends AbstractHTMLTagHandler {

	/**
	 * @param name
	 */
	public StatsHandler() {
		super("stats");
	}

	private String renderline(String name, String value) {
		return "<tr><th>" + name + "</th><td>" + value
				+ "</td></tr>\n";
	}

	/** inner class to do soring of the map **/
	private class ValueComparer implements Comparator {

		private Map<String, Float> _data = null;

		public ValueComparer(Map<String, Float> data) {
			super();
			_data = data;
		}

		public int compare(Object o1, Object o2) {
			Float e1 = _data.get(o1);
			Float e2 = _data.get(o2);
			return e2.compareTo(e1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.we.taghandler.TagHandler#render(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String renderHTML(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		UpperOntology uo = UpperOntology.getInstance();
		StringBuffer buffy = new StringBuffer();
		buffy.append("<table>");
		Map<String, String> arts = KnowWEEnvironment.getInstance()
				.getWikiConnector().getAllArticles(
						KnowWEEnvironment.DEFAULT_WEB);
		buffy.append(renderline("Artikel", arts.size() + ""));
		Map<String, Integer> edits = KnowWEEnvironment.getInstance()
				.getWikiConnector().getVersionCounts();

		// int alledits = 0;
		// SortedMap<String,Integer> sortedarts = new TreeMap(new
		// MapValueSort.ValueComparer(unsortedData));
		// Map<String,Integer> sortedarts=sortByValue(edits);
		// for (Entry<String, Integer> c : edits.entrySet()) {
		// alledits += c.getValue();
		// }
		// if (alledits==arts.size()){
		// buffy.append(renderline("Edits",
		// "A versioning File Provider has to be used"));
		// } else {
		// buffy.append(renderline("Edits", alledits + ""));}

		String querystring = "select DISTINCT ?t where { ?x rdf:predicate ns:hasTag . ?x rdf:subject ?t }";
		ArrayList<String> erg = SemanticCoreDelegator.getInstance().simpleQueryToList(
				querystring, "t");
		buffy.append(renderline("getaggte Artikel", erg.size() + ""));
		querystring = "select DISTINCT ?tag where { ?x rdf:predicate ns:hasTag . ?x rdf:object ?tag }";
		erg = SemanticCoreDelegator.getInstance().simpleQueryToList(querystring, "tag");
		buffy.append(renderline("Tags", erg.size() + ""));
		buffy.append(renderline("Tags:", ""));

		HashMap<String, Float> tags = TaggingMangler.getInstance().getAllTagsWithWeight();
		SortedMap<String, Float> sortedtags = new TreeMap<String, Float>(
				new ValueComparer(tags));
		sortedtags.putAll(tags);
		int count = 0;
		for (Entry<String, Float> c : sortedtags.entrySet()) {
			buffy.append(renderline(c.getKey(), c.getValue() + ""));
			count++;
			if (count > 4) break;
		}

		buffy.append("</table>");
		return KnowWEUtils.maskHTML(buffy.toString());
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString(
				"KnowWE.FactSheet.description");
	}

}
