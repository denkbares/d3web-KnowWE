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
package de.knowwe.jspwiki;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.SearchResult;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.auth.AuthorizationManager;
import com.ecyrd.jspwiki.auth.permissions.PagePermission;

import de.knowwe.core.Environment;
import de.knowwe.core.user.UserContext;
import de.knowwe.search.GenericSearchResult;
import de.knowwe.search.SearchProvider;
import de.knowwe.search.SearchTerm;

public class JSPWikiSearchConnector implements SearchProvider {

	private List<GenericSearchResult> getJSPWikiSearchResults(
			Collection<SearchTerm> searchTerms, HttpServletRequest request,
			WikiEngine wiki) {

		// Create wiki context and check for authorization
		WikiContext wikiContext = wiki.createContext(request, WikiContext.FIND);

		String pagereq = wikiContext.getName();

		Map<String, Set<SearchResult>> resultMap = new HashMap<String, Set<SearchResult>>();

		// assembling query for searchTerm bag
		for (SearchTerm searchTerm : searchTerms) {

			Collection list = null;
			String query = searchTerm.getTerm();
			if (query.contains(" ")) {
				query = "\"" + query + "\"";
			}

			if (query != null) {
				// Logger.getLogger(JSPWikiSearchConnector.class).info(
				// "Searching for string " + query);

				try {
					list = wiki.findPages(query);

					//
					// Filter down to only those that we actually have a
					// permission
					// to view
					//
					AuthorizationManager mgr = wiki.getAuthorizationManager();

					ArrayList<SearchResult> filteredList = new ArrayList<SearchResult>();

					for (Iterator<SearchResult> i = list.iterator(); i.hasNext();) {
						SearchResult r = i.next();

						WikiPage p = r.getPage();

						PagePermission pp = new PagePermission(p,
								PagePermission.VIEW_ACTION);

						try {
							if (mgr.checkPermission(wikiContext.getWikiSession(),
									pp)) {
								filteredList.add(r);
							}
						}
						catch (Exception e) {
							Logger.getLogger(JSPWikiSearchConnector.class.getName()).severe(
									"Searching for page " + p + e.getMessage());
						}
					}
					// store all results into map
					for (SearchResult searchResult : filteredList) {
						String article = searchResult.getPage().getName();
						Set<SearchResult> set = resultMap.get(article);
						if (set == null) {
							Set<SearchResult> newSet = new HashSet<SearchResult>();
							newSet.add(searchResult);
							resultMap.put(article, newSet);
						}
						else {
							set.add(searchResult);
						}
					}
					;

				}
				catch (Exception e) {
					wikiContext.getWikiSession().addMessage(e.getMessage());
				}

			}
		}

		return aggregateResultMap(resultMap);
	}

	private List<GenericSearchResult> aggregateResultMap(
			Map<String, Set<SearchResult>> resultMap) {

		List<GenericSearchResult> resultList = new ArrayList<GenericSearchResult>();

		for (Entry<String, Set<SearchResult>> entry : resultMap.entrySet()) {
			String article = entry.getKey();
			Set<SearchResult> results = entry.getValue();
			List<String> contexts = new ArrayList<String>();
			int matchingValue = 0;
			for (SearchResult searchResult : results) {
				matchingValue += searchResult.getScore();
				contexts.addAll(Arrays.asList(searchResult.getContexts()));
			}

			resultList.add(new GenericSearchResult(article,
					contexts.toArray(new String[contexts.size()]), matchingValue));
		}
		return resultList;
	}

	@Override
	public String getID() {
		return "JSPWiki search";
	}

	@Override
	public String getVerbalization(Locale local) {
		return "Wiki-Seiten";
	}

	@Override
	public Collection<GenericSearchResult> search(Collection<SearchTerm> words,
			UserContext user) {
		ServletContext context = Environment.getInstance()
				.getWikiConnector().getServletContext();
		WikiEngine engine = WikiEngine.getInstance(context, null);

		List<GenericSearchResult> jspwikiResults = getJSPWikiSearchResults(words,
				user.getRequest(), engine);

		// List<GenericSearchResult> knowweResult = new
		// ArrayList<GenericSearchResult>();
		//
		// if (jspwikiResults != null) {
		//
		// // some conversion hack to get rid of jspwiki dependencies
		// for (SearchResult result : jspwikiResults) {
		// if(result.getPage().getName().contains("/")) continue;
		// knowweResult.add(new GenericSearchResult(result.getPage()
		// .getName(), result.getContexts(), result.getScore()));
		// }
		// }

		return jspwikiResults;
	}

	@Override
	public String renderResults(Collection<GenericSearchResult> results, String queryString) {
		StringBuffer resultBuffy = new StringBuffer();

		if (results.size() == 0) {
			return null;
		}

		resultBuffy.append("<div class=\"graphBars\">");
		resultBuffy.append("<div class=\"zebra-table\">");

		resultBuffy.append(" <table class=\"wikitable\" >");

		resultBuffy.append(" <tr>  <th align=\"left\">Page</th>    ");
		resultBuffy.append(" <th align=\"left\">Context</th>  </tr>");

		for (GenericSearchResult genericSearchResult : results) {

			String score = "" + genericSearchResult.getScore();

			String url = "";

			resultBuffy.append(" <tr>");
			resultBuffy
					.append("<td><a title='" + score
							+ "' class=\"wikipage\" target='_blank' href=\"Wiki.jsp?page="
							+ genericSearchResult.getPagename() + "" + url
							+ "\">" + genericSearchResult.getPagename()
							+ "</a> </td>");
			// resultBuffy.append("<td><span class=\"gbar\">"
			// + genericSearchResult.getScore() + "</span> </td>");

			resultBuffy.append("<td>");
			if (genericSearchResult.getContexts().length != 0) resultBuffy.append(genericSearchResult.getContexts()[0]);
			resultBuffy.append("</td>");

			resultBuffy.append("</tr>");
		}

		resultBuffy.append("</table>");
		resultBuffy.append("</div>");
		resultBuffy.append("</div>");

		return resultBuffy.toString();
	}

	@Override
	public Collection<SearchTerm> getAllTerms() {
		// Plaintext-search does not generate terms...
		return new ArrayList<SearchTerm>();
	}

	@Override
	public Collection<SearchTerm> expandTermForRecommendation(SearchTerm t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SearchTerm> expandTermForSearch(SearchTerm t) {
		// TODO Auto-generated method stub
		return null;
	}

}
