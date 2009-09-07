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
public class SearchEngine {
	private KnowWEArticleManager articleManager;
	
	private List<SearchOption> options = new LinkedList<SearchOption>();
	
	public SearchEngine(KnowWEArticleManager articleManager) {
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
	public Map<KnowWEArticle, Collection<Result>> search(String query) {
		Iterator<KnowWEArticle> iter = articleManager.getArticleIterator();
		Map<KnowWEArticle, Collection<Result>> results = new HashMap<KnowWEArticle, Collection<Result>>();
				
		while (iter.hasNext()) {
			KnowWEArticle article = iter.next();
			
			Collection<Result> articleResults = search(query, article);
			
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
	public Collection<Result> search(String query, KnowWEArticle article) {
		if (article == null || query == null) {
			throw new IllegalArgumentException("Need an article and a query");
		}
				
		Collection<Result> results = new LinkedList<Result>();
		
		// Enable the usual wild card characters * and ?
		String queryString = Pattern.quote(query).replaceAll("\\*", ".*?").replaceAll("\\?", ".");
		
		Pattern p;
		if (options.contains(SearchOption.CASE_INSENSITIVE))
			p = Pattern.compile(queryString, Pattern.CASE_INSENSITIVE);
		else
			p = Pattern.compile(queryString);
		
		Matcher m = p.matcher(article.getSection().getOriginalText());
		
		while (m.find()) {
			Section s = article.findSmallestNodeContaining(m.start(), m.end());
			
			results.add(new Result(query, article, s));
		}
		
		return results;
	}
	
	public SearchEngine setOption(SearchOption option) {
		options.add(option);
		return this;
	}
}
