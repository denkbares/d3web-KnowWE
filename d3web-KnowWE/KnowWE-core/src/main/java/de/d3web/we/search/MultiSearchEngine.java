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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEParameterMap;

public class MultiSearchEngine {
	
	private static MultiSearchEngine instance;
	
	public static MultiSearchEngine getInstance() {
		if (instance == null) {
			instance = new MultiSearchEngine();
			
		}
		return instance;
	}
	
	
	
	private Map<String,KnowWESearchProvider> searchProvider = new HashMap<String,KnowWESearchProvider>();
	private SearchWordPreprocessor proz = new SearchWordPreprocessor();
	
	public void addProvider(KnowWESearchProvider p) {
		this.searchProvider.put(p.getID(),p);
	}
	
	public KnowWESearchProvider getProvider(String id) {
		if(id == null) return null;
		return searchProvider.get(id);
	}
	
	public Map<String, Collection<GenericSearchResult>> search(String searchText, KnowWEParameterMap map) {
		return search(proz.process(searchText), map);
	}
	
	public Map<String, Collection<GenericSearchResult>> search(Collection<SearchTerm> terms, KnowWEParameterMap map) {
		
		Map<String, Collection<GenericSearchResult>> all = new HashMap<String, Collection<GenericSearchResult>>();
		
		
		for (KnowWESearchProvider provider : searchProvider.values()) {
			Collection<GenericSearchResult> singleResultSet = provider.search(terms, map);
			all.put(provider.getID(), singleResultSet);
		}
		
		
		return all;
		
	}

}
