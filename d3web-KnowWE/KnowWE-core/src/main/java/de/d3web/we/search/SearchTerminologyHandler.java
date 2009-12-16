package de.d3web.we.search;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.search.termExpansion.SearchTermExpander;

public class SearchTerminologyHandler {
	
	private static SearchTerminologyHandler instance;
	
	public static SearchTerminologyHandler getInstance() {
		if (instance == null) {
			instance = new SearchTerminologyHandler();
			
		}

		return instance;
	}
	
	
	private List<SearchTermExpander> expanders = new ArrayList<SearchTermExpander>();
	
	public void addSearchTermExpander(SearchTermExpander expander) {
		if(!expanders.contains(expander)) {
			expanders.add(expander);
		}
	}
	
	public List<SearchTerm> expandSearchTerm(SearchTerm t) {
		List<SearchTerm> result = new ArrayList<SearchTerm>();
		
		for (SearchTermExpander expander : expanders) {
			result.addAll(expander.expandSearchTerm(t));
		}
		
		return result;
		
	}
	
	/**
	 * This method returns data for a word-cloud with relevant search terms for
	 * a given search-query. The idea is to suggest (known) search-terms (from the 
	 * ontology) for arbitrary user inputs.
	 * 
	 * @param query
	 * @return
	 */
	public List<SearchTerm> getRelevantSearchWords(String query) {
		
		List<SearchTerm> terms = new ArrayList<SearchTerm>();
		
		terms.add(new SearchTerm("apfel",2));
		terms.add(new SearchTerm("birne", 0.5));
		terms.add(new SearchTerm("banane",1));
		
		return terms;
	}
	
	
	/**
	 * Auto-completion for KnowWE search: Proposes search terms for (beginning) user inputs
	 * 
	 * @param typedString
	 * @return
	 */
	public List<String> getCompletionSuggestions(String typedString) {
		
		List<String> results = new ArrayList<String>();
		
		results.add("apfel");
		results.add("birne");
		results.add("banane");
		
		return results;
	}

}
