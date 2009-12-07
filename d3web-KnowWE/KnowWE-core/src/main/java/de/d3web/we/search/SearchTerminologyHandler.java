package de.d3web.we.search;

import java.util.ArrayList;
import java.util.List;

public class SearchTerminologyHandler {
	
	private static SearchTerminologyHandler instance;
	
	public static SearchTerminologyHandler getInstance() {
		if (instance == null) {
			instance = new SearchTerminologyHandler();
			
		}

		return instance;
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
