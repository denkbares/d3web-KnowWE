package de.d3web.we.search.termExpansion;

import java.util.List;

import de.d3web.we.search.SearchTerm;

/**
 * @author Jochen
 * 
 * Interface for a SearchTermExpander to be used in graph-based query-expansion
 *
 */
public interface SearchTermExpander {
	
	
	public List<SearchTerm> expandSearchTerm(SearchTerm t);

}
