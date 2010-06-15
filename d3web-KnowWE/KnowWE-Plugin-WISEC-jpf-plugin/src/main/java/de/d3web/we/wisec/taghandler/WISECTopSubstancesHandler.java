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
package de.d3web.we.wisec.taghandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wisec.util.Criteria;

/**
 * Displays the substances with the highest rating. The taghandlers' parameters
 * specify the weights of the criterias.
 * 
 * e.g. [{KnowWEPlugin wisec-substances=20; P=3; B=1; Risk_related=2;}] sets the
 * renders the top 20 substances with the following weights:
 * 
 * P: 3, B: 1, Risk_related: 2
 * 
 * The rating of a substance is the weighted sum of each criteria. If a criteria
 * is not defined the weight is 0.
 * 
 * @author Sebastian Furth
 * @created 15.06.2010
 */
public class WISECTopSubstancesHandler extends AbstractTagHandler {

	public WISECTopSubstancesHandler() {
		super("wisec-substances");
	}
	
	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin wisec-substances=n, P=3, B=1, Risk_related=2, ... }]";
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		// TODO: This should probably moved to a resource bundle
		return "Displays the n top substances with the highest rating with respect to the specified weights. If n is not defined (correctly) 10 substances will be displayed.";
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		HashMap<String, RatedSubstance> ratedSubstances = new HashMap<String, RatedSubstance>();
		HashMap<Criteria, Double> underlyingData = new HashMap<Criteria, Double>();
		double weight = 0;

		for (Criteria criteria : Criteria.values()) {

			// try to get the weight for the current criteria
			try {
				String weightStr = values.get(criteria.name());
				if (weightStr != null) weight = Double.parseDouble(weightStr);
				else continue;
			}
			catch (NumberFormatException e) {
				return "Weight for criteria \"" + criteria.name() + "\" is not valid!";
			}
			
			// process the current criteria with the extracted weight
			if (weight != 0) {
				try {
					processCriteria(criteria.name(), weight, ratedSubstances);
					underlyingData.put(criteria, weight);
				}
				catch (Exception e) {
					return "An error occurred: " + e.getMessage();
				}
			}
		}
		
		// try to get the maximum number of displayed substances
		double numberSubstances = 10;
		try {
			String numberSubstancesStr = values.get("wisec-substances");
			if (numberSubstancesStr != null) numberSubstances = Double.parseDouble(numberSubstancesStr);
		}
		catch (NumberFormatException e) {
			numberSubstances = 10;
		}
		return renderSubstances(toList(ratedSubstances), numberSubstances, underlyingData);
	}

	/**
	 * 
	 * @created 15.06.2010
	 * @param criteria
	 * @param weight
	 * @param toplist
	 * @throws MalformedQueryException
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 */
	private void processCriteria(String criteria, double weight, HashMap<String, RatedSubstance> ratedSubstances) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		Query query = createQuery(criteria);
		TupleQueryResult result = evaluateQuery(query);
		updateScores(result, weight, ratedSubstances);
	}

	/**
	 * 
	 * 
	 * @created 15.06.2010
	 * @param criteria
	 * @return
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 */
	private Query createQuery(String criteria) throws RepositoryException, MalformedQueryException {

		String queryString = SemanticCore.getInstance().getSparqlNamespaceShorts() +
								"SELECT ?substance ?score " +
								"WHERE { " +
								"?substance w:onListRelation ?substancelistrelation . " +
								"?list w:hasSubstanceRelation ?substancelistrelation . " +
								"?list w:" + criteria + " ?score " +
								"}";

		RepositoryConnection con = SemanticCore.getInstance().getUpper().getConnection();
		return con.prepareQuery(QueryLanguage.SPARQL, queryString);
	}

	/**
	 * Evaluates a SPARQL-Query and returns a TupleQueryResult containing the
	 * required numerical values.
	 * 
	 * @created 15.06.2010
	 * @param query the SPARQL query
	 * @return an evaluated query result
	 * @throws QueryEvaluationException
	 */
	private TupleQueryResult evaluateQuery(Query query) throws QueryEvaluationException {
		if (!(query instanceof TupleQuery)) throw new IllegalStateException(
				"Query needs to be an instance of TupleQuery.");

		return ((TupleQuery) query).evaluate();
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
			double score = scoreStr.equals("u") ? -1 : Double.parseDouble(scoreStr);
			ratedSubstance.addValue(score * weight);
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
	 * @return HTML formatted string representing the substances
	 */
	private String renderSubstances(List<RatedSubstance> sortedSubstances, double numberSubstances, HashMap<Criteria, Double> underlyingData) {
		StringBuilder result = new StringBuilder();

		result.append("\n!!Top Substances*\n||Substance ||Score \n");
		double limit = numberSubstances < sortedSubstances.size()
				? numberSubstances
				: sortedSubstances.size();

		// Render Substances
		for (int i = 0; i < limit; i++) {
			RatedSubstance rs = sortedSubstances.get(i);
			result.append("|");
			result.append(rs.getSubstance());
			result.append("|");
			result.append(rs.getScore());
			result.append("\n");
		}

		// Render Legend
		result.append("%%sub (");
		for (Criteria c : underlyingData.keySet()) {
			result.append(c.name());
			result.append("=");
			result.append(underlyingData.get(c));
			result.append(", ");
		}
		result.delete(result.length() - 2, result.length());
		result.append(")/%");

		return result.toString();
	}

	/**
	 * Private class which encapsulates the name of a substance and it's current
	 * rating.
	 * 
	 * @author Sebastian Furth
	 * @created 15.06.2010
	 */
	private class RatedSubstance implements Comparable<RatedSubstance> {

		private final String substance;
		private double score = 0;
		
		public RatedSubstance(String substance) {
			if (substance == null)
				throw new IllegalArgumentException();
			
			this.substance = substance;
		}
		
		public double getScore() {
			return score;
		}
		
		public void addValue(double value) {
			this.score += value;
		}

		public String getSubstance() {
			return substance;
		}

		@Override
		public int compareTo(RatedSubstance o) {
			// We return the reversed values by purpose to get the correct
			// sorting order!
			if (this.score < o.score) return 1;
			if (this.score > o.score) return -1;
			return this.substance.compareTo(o.substance);
		}
		
		@Override
		public String toString() {
			return substance + " (" + score + ")";
		}

	}
	
}
