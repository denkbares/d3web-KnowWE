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
		terms.add(new SearchTerm("birnenkuchen", 2));
		terms.add(new SearchTerm("birnen marmelade", 1));
		terms.add(new SearchTerm("birnenkompott",0.5));
		terms.add(new SearchTerm("birnen einkochen",3));
		terms.add(new SearchTerm("birnensorten",0.5));
		terms.add(new SearchTerm("advent",0.5));
		terms.add(new SearchTerm("weihnachten",0.5));
		terms.add(new SearchTerm("geschenke",0.5));
		terms.add(new SearchTerm("freude",0.5));
		terms.add(new SearchTerm("sterne",0.5));
		terms.add(new SearchTerm("könige",0.5));
		terms.add(new SearchTerm("kinder",2.5));
		terms.add(new SearchTerm("plätzchen",1.5));
		terms.add(new SearchTerm("gelächter",0.5));
		terms.add(new SearchTerm("baum",0.4));
		terms.add(new SearchTerm("kugel",1.0));
		terms.add(new SearchTerm("schnee",1.0));
		terms.add(new SearchTerm("kalt",1.5));
		terms.add(new SearchTerm("kerzen",2.0));
		terms.add(new SearchTerm("schneemann",0.8));
		terms.add(new SearchTerm("schneefrau",1.2));
		terms.add(new SearchTerm("wasser",1.5));
		terms.add(new SearchTerm("feuer",2.0));
		terms.add(new SearchTerm("erde",0.4));
		terms.add(new SearchTerm("luft",3.5));
		terms.add(new SearchTerm("vogel",0.5));
		terms.add(new SearchTerm("stein",0.5));
		terms.add(new SearchTerm("ameise",0.5));
		terms.add(new SearchTerm("sonne",0.5));
		terms.add(new SearchTerm("wolke",0.5));
		terms.add(new SearchTerm("farben",0.5));
		terms.add(new SearchTerm("main",0.5));
		terms.add(new SearchTerm("fisch",0.5));
		
		List<SearchTerm> filtered = new ArrayList<SearchTerm>();
		
		if( query != null && query.length() > 0){
			for (SearchTerm term : terms) {
				if(term.getTerm().contains( query )){
					filtered.add( term );
				}
			}
		} else {
			filtered.addAll( terms );
		}		
		return filtered;
	}
	
	
	/**
	 * Auto-completion for KnowWE search: Proposes search terms for (beginning) user inputs
	 * 
	 * @param typedString - The input of the user in the search box.
	 * @return A list of suggestions the user might search for.
	 */
	public List<String> getCompletionSuggestions(String typedString) {
		
		List<String> results = new ArrayList<String>();
		
		results.add("apfel");
		results.add("birne");
		results.add("banane");
		results.add("birnenkuchen");
		results.add("birnen marmelade");
		results.add("birnenkompott");
		results.add("birnen einkochen");
		results.add("birnensorten");
		
		
		List<String> filtered = new ArrayList<String>();
		
		if( typedString != null && typedString.length() > 0){
			for (String string : results) {
				if(string.contains( typedString )){
					filtered.add( wrap(string, typedString ));
				}
			}
		} else {
			filtered.addAll( results );
		}
		return filtered;
	}
	
	/**
	 * Wraps the query that is found in a suggestion with strong HTML element
	 * to indicate the finding visually.
	 *  
	 * @param suggestion
	 * @param query
	 * @return
	 */
	private String wrap(String suggestion, String query){
		return suggestion.replaceAll( query, "<strong>" + query + "</strong>");
	}
}
