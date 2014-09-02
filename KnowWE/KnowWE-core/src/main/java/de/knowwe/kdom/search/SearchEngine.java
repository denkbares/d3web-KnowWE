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

package de.knowwe.kdom.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Generic KDOM Search Engine
 * 
 * With this class you can search sections with the Type T matching a given
 * query. There are several configuration possibilities
 * 
 * @see SearchOptions
 * 
 * @author Alex Legler, Sebastian Furth (added generics)
 */
public class SearchEngine {

	private final ArticleManager articleManager;

	private final List<SearchOption> options = new LinkedList<SearchOption>();

	public SearchEngine(ArticleManager articleManager) {
		if (articleManager == null) {
			throw new IllegalArgumentException("An article manager is needed.");
		}

		this.articleManager = articleManager;
	}

	/**
	 * Performs a search in all articles belonging to KnowWE's current
	 * ArticleManager. You need to specify the ObjectType of the sections with
	 * the type parameter T.
	 * 
	 * @created Dec 14, 2010
	 * @param <T> Type parameter representing the section's Type
	 * @param query the search query
	 * @param clazz Class necessary for the TypeParameter
	 * @return Map containing the results for each article.
	 */
	public <T extends Type> Map<Article, Collection<Result>> search(String query, Class<T> clazz) {
		Iterator<Article> iter = articleManager.getArticles().iterator();
		Map<Article, Collection<Result>> results = new HashMap<Article, Collection<Result>>();

		while (iter.hasNext()) {
			Article article = iter.next();
			// TODO: This produces a StackOverflowError (We have to wait for a
			// JSPWiki fix...)
			// if
			// (Environment.getInstance().getWikiConnector().userCanViewPage(
			// article.getTitle())) {
			Collection<Result> articleResults = search(query, article, clazz);
			if (articleResults.size() > 0) {
				results.put(article, articleResults);
			}
			// }
		}

		return results;
	}

	/**
	 * Performs a search in the given articles. You need to specify the
	 * ObjectType of the sections with the type parameter T.
	 * 
	 * @created Dec 15, 2010
	 * @param <T> Type parameter representing the section's Type
	 * @param query the search query
	 * @param article the article on which the search is based
	 * @param clazz Class necessary for the TypeParameter
	 * @return Collection containing the found sections
	 */
	public <T extends Type> Collection<Result> search(String query, Article article, Class<T> clazz) {
		if (article == null || query == null || clazz == null) {
			throw new IllegalArgumentException("Need an article, a query and a class");
		}

		String queryString = Pattern.quote(query);

		Pattern p;
		int optionsCode = 0;
		if (options.contains(SearchOption.CASE_INSENSITIVE)) optionsCode += Pattern.CASE_INSENSITIVE;
		if (options.contains(SearchOption.DOTALL)) optionsCode += Pattern.DOTALL;

		p = optionsCode > 0
				? Pattern.compile(queryString, optionsCode)
				: Pattern.compile(queryString);

		return search(p, article.getRootSection(), clazz);

	}

	private <T extends Type> Collection<Result> search(Pattern query, Section<?> section, Class<T> clazz) {
		if (section == null || query == null || clazz == null) {
			throw new IllegalArgumentException("Need an article, a query and a class");
		}

		List<Section<T>> sectionsWithType = new LinkedList<Section<T>>();
		Sections.successors(section, clazz, sectionsWithType);

		Collection<Result> results = new LinkedList<Result>();
		for (Section<T> sectionWithType : sectionsWithType) {
			Matcher m = query.matcher(sectionWithType.getText());
			// We add each occurrence in the current section
			if (options.contains(SearchOption.FUZZY)) {
				while (m.find()) {
					results.add(new Result(query.pattern(), sectionWithType.getArticle(),
							sectionWithType, m.start(), m.end()));
				}
			}
			// The whole section has to match and will be added
			else {
				if (m.matches()) {
					results.add(new Result(query.pattern(), sectionWithType.getArticle(),
							sectionWithType, m.start(), m.end()));
				}
			}

		}

		return results;
	}

	/**
	 * Use this Method to specify your SearchEngine! You can call this method
	 * multiple times. Each time you call this method the option will be added
	 * to the options (if not already present)
	 * 
	 * @see SearchOption
	 * 
	 * @created Dec 15, 2010
	 * @param option SearchOption
	 * @return SearchEngine configured with the specified option(s)
	 */
	public SearchEngine setOption(SearchOption option) {
		options.add(option);
		return this;
	}
}
