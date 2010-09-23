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
package de.d3web.we.wisec.action;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;

import common.Logger;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.semantic.SPARQLUtil;
import de.d3web.we.wisec.util.Criteria;
import de.d3web.wisec.model.RatedSubstance;
import de.d3web.wisec.writers.SubstanceInfoWriter;

/**
 * 
 * 
 * @author Sebastian Furth
 * @created 23/09/2010
 */
public class WISECRankingAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {

		String result = "";
		HashMap<String, RatedSubstance> ratedSubstances = new HashMap<String, RatedSubstance>();
		HashMap<String, Double> underlyingData = new HashMap<String, Double>();
		double weight = 0;

		for (String criteria : Criteria.getAllCriterias()) {

			// try to get the weight for the current criteria
			String weightStr = context.getParameter(criteria);
			if (weightStr != null) {
				weight = Double.parseDouble(weightStr);
			}
			else {
				continue;
			}

			// process the current criteria with the extracted weight
			if (weight != 0) {
				try {
					processCriteria(criteria, weight, ratedSubstances);
					underlyingData.put(criteria, weight);
				}
				catch (Exception e) {
					result = "An error occurred: " + e.getMessage();
				}
			}
		}

		// try to get the maximum number of displayed substances
		double numberSubstances = 10;
		try {
			String numberSubstancesStr = context.getParameter("substances");
			if (numberSubstancesStr != null) numberSubstances = Double.parseDouble(numberSubstancesStr);
		}
		catch (NumberFormatException e) {
			numberSubstances = 10;
		}

		try {
			result = renderSubstances(toList(ratedSubstances), numberSubstances, underlyingData,
					checkPrintInfo(context));
		}
		catch (Exception e) {
			result = "Error during list counting. Unable to create ranking.\n" + e.getMessage();
		}
		context.getWriter().write(result);
	}

	/**
	 * Checks if a legend of the underlying data should be printed by evaluating
	 * the optional 'printinfo' parameter.
	 * 
	 * @created 22.06.2010
	 * @param values all parameters of the taghandler
	 * @return true if printinfo = true, else false
	 */
	private boolean checkPrintInfo(ActionContext context) {
		String param = context.getParameter("printinfo");
		if (param != null && param.equals("true")) return true;
		return false;
	}

	private void processCriteria(String criteria, double weight, HashMap<String, RatedSubstance> ratedSubstances) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		TupleQueryResult result = SPARQLUtil.executeTupleQuery(createCriteriaQuery(criteria));
		updateScores(result, weight, ratedSubstances);
	}

	private String createCriteriaQuery(String criteria) throws RepositoryException, MalformedQueryException {
		return "SELECT ?substance ?score " +
				"WHERE { " +
				"?substance w:onListRelation ?substancelistrelation . " +
				"?list w:hasSubstanceRelation ?substancelistrelation . " +
				"?list w:" + criteria + " ?score " +
				"}";

	}

	/**
	 * Adds the values of the current criteria to the rated substances.
	 * 
	 * @created 15.06.2010
	 * @param result Result of the SPARQL-Query (contains substances and scores)
	 * @param weight the weight of the current criteria
	 * @param ratedSubstances all rated substances
	 * @throws QueryEvaluationException
	 */
	private void updateScores(TupleQueryResult result, double weight, HashMap<String, RatedSubstance> ratedSubstances) throws QueryEvaluationException {

		while (result.hasNext()) {
			BindingSet binding = result.next();
			String substance = binding.getValue("substance").stringValue();
			substance = substance.substring(substance.lastIndexOf("#") + 1);
			RatedSubstance ratedSubstance = ratedSubstances.get(substance);
			if (ratedSubstance == null) {
				ratedSubstance = new RatedSubstance(substance);
				ratedSubstances.put(substance, ratedSubstance);
			}

			String scoreStr = binding.getValue("score").stringValue();
			double score = Double.parseDouble(scoreStr);

			// Just an "intermediate" score from one list
			ratedSubstance.addValue(score * weight);
		}

		// Adds all intermediate scores to the real score
		for (RatedSubstance rs : ratedSubstances.values()) {
			rs.updateScore();
		}

	}

	/**
	 * Returns a sorted list of all rated substances.
	 * 
	 * @created 15.06.2010
	 * @param ratedSubstances
	 * @return sorted list of rated substances.
	 */
	private List<RatedSubstance> toList(HashMap<String, RatedSubstance> ratedSubstances) {
		List<RatedSubstance> topSubstances = new LinkedList<RatedSubstance>();
		for (RatedSubstance rs : ratedSubstances.values())
			topSubstances.add(rs);

		Collections.sort(topSubstances);
		return topSubstances;
	}

	/**
	 * Renders the top substances in descending order.
	 * 
	 * @created 15.06.2010
	 * @param sortedSubstances
	 * @param numberSubstances
	 * @param underlyingData
	 * @param printinfo
	 * @return HTML formatted string representing the substances
	 * @throws QueryEvaluationException
	 * @throws MalformedQueryException
	 * @throws RepositoryException
	 */
	private String renderSubstances(List<RatedSubstance> sortedSubstances, double numberSubstances, HashMap<String, Double> underlyingData, boolean printinfo) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

		DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(
				new Locale("en", "US")));
		StringBuilder result = new StringBuilder();
		result.append("<div class=\"zebra-table\"><table class=\"wikitable\" border=\"1\"><tr class=\"odd\"><th>Substance</th>");
		result.append(printinfo ? "<th>Score *</th>" : "<th>Score</th>");
		result.append("<th>Lists</th></tr>\n");
		double limit = numberSubstances < sortedSubstances.size()
				? numberSubstances
				: sortedSubstances.size();

		// Render Substances
		for (int i = 0; i < limit; i++) {
			RatedSubstance rs = sortedSubstances.get(i);
			result.append((i + 1) % 2 == 0 ? "<tr class=\"odd\"><td>" : "<tr><td>");
			// result.append(generateLink(KnowWEUtils.urldecode(rs.getSubstance())));
			result.append(generateLink(rs.getSubstance()));
			result.append("</td><td>");
			result.append(df.format(rs.getScore()));
			result.append("</td><td>");
			result.append(getListOccurences(rs.getSubstance()));
			result.append("</td></tr>\n");
		}
		result.append("</table></div>\n");

		// Render Legend (if printinfo = true)
		if (printinfo) {
			// used criterias
			result.append("<sub>* (");
			for (String criteria : underlyingData.keySet()) {
				result.append(criteria);
				result.append("=");
				result.append(underlyingData.get(criteria));
				result.append(", ");
			}
			result.delete(result.length() - 2, result.length());
			result.append(")</sub><br/><br/>");
		}

		return result.toString();
	}

	private int getListOccurences(String substance) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

		TupleQueryResult result = SPARQLUtil.executeTupleQuery(createListOccurenceQuery(substance));
		return countLists(result);
	}

	private String createListOccurenceQuery(String substance) throws RepositoryException, MalformedQueryException {
		return "SELECT ?list " +
				"WHERE { " +
				"<http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#" + substance
				+ "> w:onListRelation ?list " +
				"}";
	}

	private int countLists(TupleQueryResult result) throws QueryEvaluationException {
		int counter = 0;
		while (result.hasNext()) {
			result.next();
			counter++;
		}
		return counter;
	}

	private Object generateLink(String substance) {
		if (substance == null) {
			Logger.getLogger(this.getClass()).warn("Substance was null, unable to render link!");
			return "";
		}

		return "<a href='Wiki.jsp?page=" + SubstanceInfoWriter.getWikiFileNameFor(substance) + "'>"
				+ substance + "</a>";

	}

}
