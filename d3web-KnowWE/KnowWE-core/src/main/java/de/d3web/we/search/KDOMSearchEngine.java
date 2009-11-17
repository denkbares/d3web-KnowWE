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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

/**
 * KDOM Search Engine
 * @author Alex Legler
 */
public class KDOMSearchEngine {
	private KnowWEArticleManager articleManager;
	
	private List<KDOMSearchOption> options = new LinkedList<KDOMSearchOption>();
	
	public KDOMSearchEngine(KnowWEArticleManager articleManager) {
		if (articleManager == null) {
			throw new IllegalArgumentException("An article manager is needed.");
		}
		
		this.articleManager = articleManager;
	}
	
	/**
	 * Performs a search in all articles belonging to the articleManager.
	 * @param query Query string
	 * @return Collection containing all articles and results
	 */
	public Map<KnowWEArticle, Collection<KDOMSearchResult>> search(String query) {
		Iterator<KnowWEArticle> iter = articleManager.getArticleIterator();
		Map<KnowWEArticle, Collection<KDOMSearchResult>> results = new HashMap<KnowWEArticle, Collection<KDOMSearchResult>>();
				
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();
			
			Collection<KDOMSearchResult> articleResults = search(query, article);
			
			if (articleResults.size() > 0) {
				results.put(article, articleResults);
			}
		}
		
		return results;
	}
	
	/**
	 * Performs a search in the given article.
	 * @param query Query string
	 * @param article Article to search in
	 * @return List of all results on the article
	 */
	public Collection<KDOMSearchResult> search(String query, KnowWEArticle article) {
		if (article == null || query == null) {
			throw new IllegalArgumentException("Need an article and a query");
		}
				
		Collection<KDOMSearchResult> results = new LinkedList<KDOMSearchResult>();
		
		// Enable the usual wild card characters * and ?
		String queryString = Pattern.quote(query).replaceAll("\\*", ".*?").replaceAll("\\?", ".");
		
		Pattern p;
		if (options.contains(KDOMSearchOption.CASE_INSENSITIVE))
			p = Pattern.compile(queryString, Pattern.CASE_INSENSITIVE);
		else
			p = Pattern.compile(queryString);
		
		Matcher m = p.matcher(article.getSection().getOriginalText());
		
		while (m.find()) {
			Section s = article.findSmallestNodeContaining(m.start(), m.end());
			
			results.add(new KDOMSearchResult(query, article, s));
		}
		
		return results;
	}
	
	public KDOMSearchEngine setOption(KDOMSearchOption option) {
		options.add(option);
		return this;
	}
}
