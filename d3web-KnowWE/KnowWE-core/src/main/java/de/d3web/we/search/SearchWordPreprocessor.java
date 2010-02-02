/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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
package de.d3web.we.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.d3web.we.utils.SplitUtility;

public class SearchWordPreprocessor {
	
	private static SearchWordPreprocessor instance;
	
	public static SearchWordPreprocessor getInstance() {
		if (instance == null) {
			instance = new SearchWordPreprocessor();
			
		}

		return instance;
	}
	
	
	public Collection<SearchTerm> processForSearch(String searchText) {
		List<String> terms = SplitUtility.splitUnquoted(searchText," ");
		
		Set<SearchTerm> resultTmp = new HashSet<SearchTerm>();
		Set<SearchTerm> result = new HashSet<SearchTerm>();
		
		for (String word : terms) {
			if (word.startsWith("\"") && word.endsWith("\"")) {
				word = word.substring(1, word.length() - 1);
			}
			resultTmp.add(new SearchTerm(word));
		}
		
		for (SearchTerm searchTerm : resultTmp) {
			Collection<SearchTerm> expandSearchTermForSearch = SearchTerminologyHandler.getInstance().expandSearchTermForSearch(searchTerm);
			result.addAll(expandSearchTermForSearch);
		}
		
		
		
		return result;
		
	}
	
	public Collection<SearchTerm> processForRecommendation(String searchText) {
		List<String> terms = SplitUtility.splitUnquoted(searchText," ");
		
		Set<SearchTerm> resultTmp = new HashSet<SearchTerm>();
		Set<SearchTerm> result = new HashSet<SearchTerm>();
		
		for (String word : terms) {
			if (word.startsWith("\"") && word.endsWith("\"")) {
				word = word.substring(1, word.length() - 1);
			}
			resultTmp.add(new SearchTerm(word));
		}
		
		
		if( searchText != null && searchText.equals("")) {
			SearchTerm t = new SearchTerm( searchText );
			result.addAll(SearchTerminologyHandler.getInstance().expandSearchTermForRecommendation( t ));
		} else {
			for (SearchTerm searchTerm : resultTmp) {
				result.addAll(SearchTerminologyHandler.getInstance().expandSearchTermForRecommendation(searchTerm));
			}
		}
		
		
		
		return result;
		
	}

}
