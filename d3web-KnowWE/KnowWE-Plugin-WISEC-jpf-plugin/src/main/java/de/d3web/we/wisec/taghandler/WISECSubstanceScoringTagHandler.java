/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.wisec.taghandler;

import java.util.Map;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.semantic.SPARQLUtil;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wisec.util.Criteria;
import de.d3web.we.wisec.util.WISECUtil;
import de.d3web.wisec.writers.SubstanceListWriter;

/**
 * Displays the scoring of all criterias for a specified substance. The
 * substance is specified with the parameter substance in the taghandler. The
 * substance has to be represented by a valid / known CAS number otherwise this
 * taghandler won't show anything.
 * 
 * @author Sebastian Furth
 * @created 02/10/2010
 */
public class WISECSubstanceScoringTagHandler extends AbstractTagHandler {

	public WISECSubstanceScoringTagHandler() {
		super("wisec-substance-scoring");
	}

	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin wisec-substance-scoring, substance=<CAS> }]";
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		// TODO: This should probably moved to a resource bundle
		return "Displays the criteria scoring of the specified substance (CAS Number).";
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {

		String substance = values.get("substance");
		if (substance == null || substance.isEmpty()) {
			// TODO: Maybe we can check if the substance is active / exist
			return "Please specify a <strong>valid</strong> substance (CAS).";
		}

		StringBuilder html = new StringBuilder();
		appendHeader(html);
		double score = 0;

		for (String group : Criteria.getCriteriaGroups()) {
			appendGroupHeader(html, group);
			// Java always uses Call-by-Value and Double class is immutable!
			double[] groupScore = { 0.0 };

			for (String criteria : Criteria.getCriteriasFor(group)) {
				try {
					TupleQueryResult result = SPARQLUtil.executeTupleQuery(createCriteriaQuery(
						substance, criteria));
					processCriteria(html, result, criteria, groupScore);
				}
				catch (QueryEvaluationException e) {
					return "Unexpected exception during the rendering of the criteria scoring: "
							+ e.getMessage();
				}
			}

			appendIntermediateScore(html, groupScore[0]);
			score += groupScore[0];
		}

		appendFooter(html, score);
		return html.toString();
	}

	private void appendHeader(StringBuilder html) {
		html.append("\n!!Criteria Scoring\n\n");
		html.append("<table class=\"wikitable\" border=\"1\">\n");
		html.append("<tr class=\"odd\">");
		html.append("<th>Criteria</th>");
		html.append("<th>List</th>");
		html.append("<th>Scoring</th>");
		html.append("<th>On Lists</th>");
		html.append("</tr>\n");
	}

	private void appendGroupHeader(StringBuilder html, String group) {
		html.append("<tr class=\"odd\">");
		html.append("<th colspan=\"4\">");
		html.append(group);
		html.append("</th>");
		html.append("</tr>\n");
	}

	private String createCriteriaQuery(String substance, String criteria) {
		return "SELECT ?list ?score " +
				"WHERE { " +
				"<http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#" + substance
				+ "> w:onListRelation ?substancelistrelation . " +
				"?list w:hasSubstanceRelation ?substancelistrelation . " +
				"?list w:" + criteria + " ?score " +
				"}";
	}

	private void processCriteria(StringBuilder html, TupleQueryResult result, String criteria, double[] groupScore) throws QueryEvaluationException {

		double scores = 0;
		double count = 0;

		html.append("<tr");

		// Criteria
		html.append("<td>");
		html.append(criteria);
		html.append("</td>\n");

		// Lists
		html.append("<td>");
		while (result.hasNext()) {
			BindingSet binding = result.next();
			String list = binding.getValue("list").stringValue();
			String score = binding.getValue("score").stringValue();
			html.append("<a href=\"");
			html.append(SubstanceListWriter.getWikiFileNameFor(list));
			html.append("\"> ");
			html.append(score);
			html.append(" </a>");
			scores += Integer.parseInt(score);
			count++;
		}
		html.append("</td>\n");

		// Scoring
		double scoring = count > 0 ? scores / count : 0;
		groupScore[0] = groupScore[0] + scoring;
		html.append("<td style=\"background-color:");
		html.append(WISECUtil.getTrafficLightColor(scoring));
		html.append("\" >");
		html.append(WISECUtil.format(scoring));
		html.append("</td>\n");

		// On Lists
		html.append("<td>");
		html.append(WISECUtil.format(count, "#0"));
		html.append("</td>\n");

		html.append("</tr>\n");
	}

	private void appendIntermediateScore(StringBuilder html, double groupScore) {
		html.append("<tr class=\"odd\">");
		html.append("<th colspan=\"4\">Intermediate score: ");
		html.append(WISECUtil.format(groupScore));
		html.append("</th>");
		html.append("</tr>\n");
	}

	private void appendFooter(StringBuilder html, double score) {
		html.append("</table>\n\n");
		html.append("!Total score: ");
		html.append(WISECUtil.format(score));
	}

}
