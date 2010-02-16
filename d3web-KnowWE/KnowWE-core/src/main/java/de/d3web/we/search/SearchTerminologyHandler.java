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

import de.d3web.we.search.termExpansion.SearchTermExpander;

public class SearchTerminologyHandler {

	private static SearchTerminologyHandler instance;

	/**
	 * A Levenstein object that is used to compute the edit-distance between two
	 * Strings in some of the methods used in this class.
	 */
	private Levenstein levenstein = new Levenstein();

	public static SearchTerminologyHandler getInstance() {
		if (instance == null) {
			instance = new SearchTerminologyHandler();

		}

		return instance;
	}

	private List<SearchTermExpander> expanders = new ArrayList<SearchTermExpander>();

	public void addSearchTermExpander(SearchTermExpander expander) {
		if (!expanders.contains(expander)) {
			expanders.add(expander);
		}
	}

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
		
		
		List<SearchTerm> generalSystemTerms = new ArrayList<SearchTerm>();

		// terms.add(new SearchTerm("apfel", 2));
		// terms.add(new SearchTerm("birne", 0.5));
		// terms.add(new SearchTerm("banane", 1));
		// terms.add(new SearchTerm("birnenkuchen", 2));
		// terms.add(new SearchTerm("birnen marmelade", 1));
		// terms.add(new SearchTerm("birnenkompott", 0.5));
		// terms.add(new SearchTerm("birnen einkochen", 3));
		// terms.add(new SearchTerm("birnensorten", 0.5));
		// terms.add(new SearchTerm("advent", 0.5));
		// terms.add(new SearchTerm("weihnachten", 0.5));
		// terms.add(new SearchTerm("geschenke", 0.5));
		// terms.add(new SearchTerm("freude", 0.5));
		// terms.add(new SearchTerm("sterne", 0.5));
		// terms.add(new SearchTerm("könige", 0.5));
		// terms.add(new SearchTerm("kinder", 2.5));
		// terms.add(new SearchTerm("plätzchen", 1.5));
		// terms.add(new SearchTerm("gelächter", 0.5));
		// terms.add(new SearchTerm("baum", 0.4));
		// terms.add(new SearchTerm("kugel", 1.0));
		// terms.add(new SearchTerm("schnee", 1.0));
		// terms.add(new SearchTerm("kalt", 1.5));
		// terms.add(new SearchTerm("kerzen", 2.0));
		// terms.add(new SearchTerm("schneemann", 0.8));
		// terms.add(new SearchTerm("schneefrau", 1.2));
		// terms.add(new SearchTerm("wasser", 1.5));
		// terms.add(new SearchTerm("feuer", 2.0));
		// terms.add(new SearchTerm("erde", 0.4));
		// terms.add(new SearchTerm("luft", 3.5));
		// terms.add(new SearchTerm("vogel", 0.5));
		// terms.add(new SearchTerm("stein", 0.5));
		// terms.add(new SearchTerm("ameise", 0.5));
		// terms.add(new SearchTerm("sonne", 0.5));
		// terms.add(new SearchTerm("wolke", 0.5));
		// terms.add(new SearchTerm("farben", 0.5));
		// terms.add(new SearchTerm("landschaft", 0.5));
		// terms.add(new SearchTerm("Grünland", 0.5));
		// terms.add(new SearchTerm("Boden", 1.5));
		// terms.add(new SearchTerm("Bewuchs", 0.5));
		// terms.add(new SearchTerm("vielfalt", 0.4));
		// terms.add(new SearchTerm("Pflanzen", 1.0));
		// terms.add(new SearchTerm("Käfer", 1.0));
		// terms.add(new SearchTerm("lebensraum", 1.5));
		// terms.add(new SearchTerm("diversität", 2.0));
		// terms.add(new SearchTerm("bio", 0.8));
		// terms.add(new SearchTerm("natur", 1.2));

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

		Map<SearchTerm, Double> similarities = new HashMap<SearchTerm, Double>();
		if (query != null) {
			for (SearchTerm searchTerm : generalSystemTerms) {
				double sim = levenstein.score(query, searchTerm.getTerm());
				similarities.put(searchTerm, sim);

			}
		}

		Set<SearchTerm> filtered = new HashSet<SearchTerm>();

		int maxTerms = 20;

		SearchTerm exactMatch = null;

		for (SearchTerm term : generalSystemTerms) {
			if (term.getTerm().equals(query)) {
				exactMatch = term;
				filtered.addAll(expandSearchTermForRecommendationByProviders( term));
			} else if (term.getTerm().contains(query)) {
				filtered.add(term);
			}
		}

//		if (exactMatch != null) {
//			filtered.addAll(OWLSibblingClassExpander.getInstance()
//					.expandSearchTerm(exactMatch));
//		}

		maxTerms -= filtered.size();

		// TODO implement more efficiently
		int cnt = 0;
		while (cnt < maxTerms) {
			cnt++;
			double max = Double.NEGATIVE_INFINITY;
			SearchTerm minTerm = null;
			for (SearchTerm searchTerm : similarities.keySet()) {
				Double double1 = similarities.get(searchTerm);
				if (double1 > max) {
					max = double1;
					minTerm = searchTerm;
				}
			}
			similarities.remove(minTerm);
			if (minTerm != null)
				filtered.add(minTerm);
		}
		
		if(filtered.size() > 25) {
			Set<SearchTerm> cutResultList = new HashSet<SearchTerm>();
			int cntLimit = 0;
			for (SearchTerm searchTerm : filtered) {
				cntLimit++;
				if( cntLimit > 25) break;
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
	 * @param typedString
	 *            - The input of the user in the search box.
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
		//filter the search result according to the typed string
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
