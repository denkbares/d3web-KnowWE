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
package de.d3web.we.jspwiki;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.ecyrd.jspwiki.SearchResult;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.auth.AuthorizationManager;
import com.ecyrd.jspwiki.auth.permissions.PagePermission;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.search.GenericSearchResult;
import de.d3web.we.search.KnowWESearchProvider;
import de.d3web.we.search.SearchTerm;

public class JSPWikiSearchConnector extends KnowWESearchProvider{

	public static List<SearchResult> getJSPWikiSearchResults(Collection<SearchTerm> searchTerms , HttpServletRequest request, WikiEngine wiki) {
		
		// assembling query for searchTerm bag
		String query = "";
		for (SearchTerm searchTerm : searchTerms) {
			query += searchTerm.getTerm() + " "; 
		}
		

		// Create wiki context and check for authorization
		WikiContext wikiContext = wiki.createContext(request, WikiContext.FIND);
		
		String pagereq = wikiContext.getName();

		// Get the search results
		Collection list = null;
	//	String query = request.getParameter("query");

		if (query != null) {
			Logger.getLogger(JSPWikiSearchConnector.class).info(
					"Searching for string " + query);

			try {
				list = wiki.findPages(query);

				//
				// Filter down to only those that we actually have a permission
				// to view
				//
				AuthorizationManager mgr = wiki.getAuthorizationManager();

				ArrayList<SearchResult> filteredList = new ArrayList();

				for (Iterator i = list.iterator(); i.hasNext();) {
					SearchResult r = (SearchResult) i.next();

					WikiPage p = r.getPage();

					PagePermission pp = new PagePermission(p,
							PagePermission.VIEW_ACTION);

					try {
						if (mgr.checkPermission(wikiContext.getWikiSession(),
								pp)) {
							filteredList.add(r);
						}
					} catch (Exception e) {
						Logger.getLogger(JSPWikiSearchConnector.class).error(
								"Searching for page " + p, e);
					}
				}
				return filteredList;

			} catch (Exception e) {
				wikiContext.getWikiSession().addMessage(e.getMessage());
			}

		}
		
		return null;
	}

	@Override
	public String getID() {
		return "JSPWiki Search";
	}

	@Override
	public String getVerbalization(Locale local) {
		return "jspwiki search verbalization: "+local.toString();
	}

	@Override
	public Collection<GenericSearchResult> search(Collection<SearchTerm> words, KnowWEParameterMap map) {
		ServletContext context = KnowWEEnvironment.getInstance().getWikiConnector().getServletContext();
		WikiEngine engine = WikiEngine.getInstance(context,null);
		
		List<SearchResult> jspwikiResults = getJSPWikiSearchResults(words, map.getRequest(), engine);
		
		List<GenericSearchResult> knowweResult = new ArrayList<GenericSearchResult>();
		
		// some conversion hack to get rid of jspwiki dependencies
		for (SearchResult result : jspwikiResults) {
			knowweResult.add(new GenericSearchResult(result.getPage().getName(), result.getContexts(), result.getScore()));
		}
		
		return knowweResult;
	}

}
