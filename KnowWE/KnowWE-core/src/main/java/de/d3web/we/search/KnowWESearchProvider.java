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

package de.d3web.we.search;

import java.util.Collection;
import java.util.Locale;

import de.d3web.we.core.KnowWEParameterMap;

/**
 * Interface for a KnowWE SearchProvider. To add another search-mechanism to
 * KnowWE, this interface needs to be implemented
 * 
 * @author Jochen
 * 
 */
public interface KnowWESearchProvider {

	/**
	 * Core search method: returns a collection of GenericSearchResults for a
	 * given set of SearchTerms
	 * 
	 * @param words
	 * @param map
	 * @return
	 */
	public abstract Collection<GenericSearchResult> search(Collection<SearchTerm> words, KnowWEParameterMap map);

	/**
	 * A stable unique identifier for this searchprovider
	 * 
	 * @return
	 */
	public abstract String getID();

	/**
	 * A verbalization of this searchprovider to show in the UI
	 * 
	 * @param local
	 * @return
	 */
	public abstract String getVerbalization(Locale local);

	/**
	 * OPTIONAL: (may return null/empty string) Can be used to renderer the
	 * SearchResults in a specifc way
	 * 
	 * @param results
	 * @return
	 */
	public abstract String renderResults(Collection<GenericSearchResult> results, String queryString);

	/**
	 * OPTIONAL: (may return null/empty collection) To provide a set of helpful
	 * terms (keywords?)
	 * 
	 * @return
	 */
	public abstract Collection<SearchTerm> getAllTerms();

	/**
	 * OPTIONAL: (may return null/empty collection) Can add specific SearchTerms
	 * to be also recommended to the user, when SearchTerm t is searched.
	 * 
	 * @param t
	 * @return
	 */
	public abstract Collection<SearchTerm> expandTermForRecommendation(SearchTerm t);

	/**
	 * OPTIONAL: (may return null/empty collection)
	 * 
	 * can expand SearchTerm t (Query-Expansion - e.g., along taxonomic
	 * relations)
	 * 
	 * @param t
	 * @return
	 */
	public abstract Collection<SearchTerm> expandTermForSearch(SearchTerm t);

}
