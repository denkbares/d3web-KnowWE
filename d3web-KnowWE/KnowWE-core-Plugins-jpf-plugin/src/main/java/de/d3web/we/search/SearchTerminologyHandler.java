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

package de.d3web.we.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.wcohen.ss.Levenstein;

/**
 * This class is part of the MultiSearchEngine. It allows to expand some *
 * SearchTerm to multiple (related) SearchTerms. Further, for some SearchTerm it
 * generates a set of possibly interesting related SearchTerms to be recommended
 * to the user (e.g., by a cloud of search words). It also provides a method a
 * calculate auto-completion suggestions for some entered character sequence
 * based on the known terms.
 * 
 * @author Jochen
 * 
 */
public class SearchTerminologyHandler {

	private static SearchTerminologyHandler instance;

	/**
	 * A Levenstein object that is used to compute the edit-distance between two
	 * Strings in some of the methods used in this class.
	 */
	private final Levenstein levenstein = new Levenstein();

	public static SearchTerminologyHandler getInstance() {
		if (instance == null) {
			instance = new SearchTerminologyHandler();

		}

		return instance;
	}


	/**
	 * expands some SearchTerm by delegating expansion to existiting SearchProviders
	 * 
	 * @param t
	 * @return
	 */
	public Collection<SearchTerm> expandSearchTermForSearch(SearchTerm t) {
		return expandSearchTermForSearchByProviders(t);
	}

	/**
	 * This method returns data for a word-cloud with relevant search terms for
	 * a given search-query. The idea is to suggest (known) search-terms (from
	 * the ontology) for arbitrary user inputs.
	 * 
	 * @param query
	 * @return
	 */
	public Collection<SearchTerm> expandSearchTermForRecommendation(SearchTerm t) {

		String query = t.getTerm();
		
		//Collect all known terms from the installed searchProviders
		List<SearchTerm> generalSystemTerms = new ArrayList<SearchTerm>();
		Collection<KnowWESearchProvider> providers = MultiSearchEngine
				.getInstance().getSearchProvider().values();
		if (providers != null) {
			for (KnowWESearchProvider knowWESearchProvider : providers) {
				Collection<SearchTerm> allTerms = knowWESearchProvider
						.getAllTerms();
				if (allTerms != null) {
					generalSystemTerms.addAll(allTerms);
				}
			}
		}

		//calculate foreach term the (Levenstein-)similarity to the SearchTerm
		Map<SearchTerm, Double> similarities = new HashMap<SearchTerm, Double>();
		if (query != null) {
			for (SearchTerm searchTerm : generalSystemTerms) {
				double sim = levenstein.score(query, searchTerm.getTerm());
				similarities.put(searchTerm, sim);

			}
		}


		// create a new set of ranked proposed terms
		Set<SearchTerm> filtered = new HashSet<SearchTerm>();
		int maxTerms = 20;

		//terms that match the query will be added in advance
		for (SearchTerm term : generalSystemTerms) {
			// exact match will be expanded by providers
			if (term.getTerm().equals(query)) {
				filtered.addAll(expandSearchTermForRecommendationByProviders(term));
			} //infix matches just added
			else if (term.getTerm().contains(query)) {
				filtered.add(term);
			}
		}


		// decrease number of free slots by found matches
		maxTerms -= filtered.size();

		int cnt = 0;
		while (cnt < maxTerms) { //while still slots free
			cnt++;
			// find max-sim-object
			double max = Double.NEGATIVE_INFINITY;
			SearchTerm maxSimTerm = null;
			for (SearchTerm searchTerm : similarities.keySet()) {
				Double double1 = similarities.get(searchTerm);
				if (double1 > max) {
					max = double1;
					maxSimTerm = searchTerm;
				}
			}
			similarities.remove(maxSimTerm);
			// add most similar term to filtered list
			if (maxSimTerm != null) filtered.add(maxSimTerm);
		}
		
		
		// reduce to constant number if exceeding
		if (filtered.size() > 25) {
			Set<SearchTerm> cutResultList = new HashSet<SearchTerm>();
			int cntLimit = 0;
			for (SearchTerm searchTerm : filtered) {
				cntLimit++;
				if (cntLimit > 25) break;
				cutResultList.add(searchTerm);
			}

			filtered = cutResultList;

		}

		return filtered;
	}

	private Collection<SearchTerm> expandSearchTermForRecommendationByProviders(
			SearchTerm term) {
		Set<SearchTerm> result = new HashSet<SearchTerm>();
		Set<Entry<String, KnowWESearchProvider>> entrySet = MultiSearchEngine
				.getInstance().getSearchProvider().entrySet();
		for (Entry<String, KnowWESearchProvider> entry : entrySet) {
			Collection<SearchTerm> expanded = entry.getValue()
					.expandTermForRecommendation(term);
			if (expanded != null) {
				result.addAll(expanded);
			}
		}
		return result;
	}

	private Collection<SearchTerm> expandSearchTermForSearchByProviders(
			SearchTerm term) {
		Set<SearchTerm> result = new HashSet<SearchTerm>();
		result.add(term);
		Set<Entry<String, KnowWESearchProvider>> entrySet = MultiSearchEngine
				.getInstance().getSearchProvider().entrySet();
		for (Entry<String, KnowWESearchProvider> entry : entrySet) {
			Collection<SearchTerm> expanded = entry.getValue()
					.expandTermForSearch(term);
			if (expanded != null) {
				result.addAll(expanded);
			}
		}
		return result;
	}

	/**
	 * Auto-completion for KnowWE search: Proposes search terms for (beginning)
	 * user inputs
	 * 
	 * @param typedString - The input of the user in the search box.
	 * @return A list of suggestions the user might search for.
	 */
	public List<String> getCompletionSuggestions(String typedString) {

		List<SearchTerm> generalSystemTerms = new ArrayList<SearchTerm>();
		List<String> filtered = new ArrayList<String>();

		Collection<KnowWESearchProvider> providers = MultiSearchEngine.getInstance().getSearchProvider().values();
		if (providers != null) {
			for (KnowWESearchProvider knowWESearchProvider : providers) {
				Collection<SearchTerm> allTerms = knowWESearchProvider
						.getAllTerms();
				if (allTerms != null) {
					generalSystemTerms.addAll(allTerms);
				}
			}
		}
		// filter the search result according to the typed string
		if (typedString != null && typedString.length() > 0) {
			for (SearchTerm term : generalSystemTerms) {
				if (term.getTerm().contains(typedString)) {
					filtered.add(wrap(term.getTerm(), typedString));
				}
			}
		}
		return filtered;
	}

	/**
	 * Wraps the query that is found in a suggestion with strong HTML element to
	 * indicate the finding visually.
	 * 
	 * @param suggestion
	 * @param query
	 * @return
	 */
	private String wrap(String suggestion, String query) {
		return suggestion.replaceAll(query, "<strong>" + query + "</strong>");
	}
}
