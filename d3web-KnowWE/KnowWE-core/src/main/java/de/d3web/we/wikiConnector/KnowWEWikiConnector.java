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

package de.d3web.we.wikiConnector;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.core.KnowWEParameterMap;

/**
 * 
 * This interface defines how KnowWE (and KnowWE-Plugins) 
 * can interact with the underlying wiki engine. 
 * An implementation provides KnowWE access to attachments file, page sources,
 * user rights, edit locks and much more.
 * 
 * To connect a wiki engine with KnowWE this interface needs to be implemented.
 * 
 * @author Jochen
 *
 */

public interface KnowWEWikiConnector {
	
	/**
	 * Return the absolute path of the web-application
	 * 
	 * @return path of the web-application
	 */
	public String getRealPath();
	
	/**
	 * Returns the current servlet-context object
	 * 
	 * @return
	 */
	public ServletContext getServletContext();
	

	/**
	 * Return the ActionDispatcher which is responsible to receive 
	 * the http-request for the KnowWE-actions
	 * 
	 * @return
	 */
	public KnowWEActionDispatcher getActionDispatcher();
	
	
	/**
	 * Saves the article (persistently) into the connected wiki
	 * 
	 * @param name
	 * @param text
	 * @param map
	 * @return
	 */
	public boolean saveArticle(String name, String text, KnowWEParameterMap map);
	
	/**
	 * Returns a list of all attachment files of the wiki
	 * 
	 * @return
	 */
	public List<String> getAttachments();
	
	/**
	 * Returns the filenames of the attachments of the given wiki page
	 * 
	 * @param pageName
	 * @return
	 */
	public List<String> getAttachmentFilenamesForPage(String pageName);
	
	
	
	/**
	 * Returns the path of the folder where the attachments are stored
	 * 
	 * @param JarName
	 * @return
	 */
	public String getAttachmentPath(String JarName);
	
	/**
	 * Returns the URL of the running wiki
	 * 
	 * @return
	 */
	public String getBaseUrl();
	
	
	
	/**
	 * Returns the source text of the wiki page with 
	 * the given name as one string
	 * 
	 * @param name
	 * @return
	 */
	public String getArticleSource(String name);
	
	/**
	 * Returns a map of all wiki pages with page names as key and 
	 * page sources as values
	 * 
	 * @param web
	 * @return
	 */
	public Map<String,String> getAllArticles(String web);
	
	/**
	 * tests if a page of the given name exists
	 * 
	 * @param Topic
	 * @return
	 */
	public boolean doesPageExist(String Topic);
	
	/**
	 * Creates a new Wiki page with given name and content and author
	 * in the connected wiki
	 * 
	 * @param topic
	 * @param newContent
	 * @param author
	 * @return
	 */
	public String createWikiPage(String topic, String newContent, String author);

	/**
	 * Appends some content to the wiki page with the given name
	 * 
	 * @param topic
	 * @param pageContent
	 * @return
	 */
	public String appendContentToPage(String topic, String pageContent);
	
	
	
	/**
	 * Checks whether a user can edit a given page
	 * 
	 * @param articlename
	 * @return
	 */
	public boolean userCanEditPage( String articlename );
	
	/**
	 * Checks whether a user can edit a given page
	 * 
	 * @param articlename
	 * @return
	 */
	public boolean userCanEditPage( String articlename, HttpServletRequest r );
	
	
	/**
	 * Checks whether a page has a editing lock (due to another user who 
	 * has startet to edit it)
	 * 
	 * @param articlename
	 * @return
	 */
	public boolean isPageLocked( String articlename );
	
	/**
	 * Sets an editing lock on the page, denoting that the 
	 * page is currently editing by the given user.
	 * 
	 * @param articlename
	 * @param user
	 * @return
	 */
	public boolean setPageLocked( String articlename, String user );
	
	/**
	 * Removes a page editing lock
	 * 
	 * @param articlename
	 */
	public void undoPageLocked( String articlename );
	
	/**
	 * Checks whether a given page is locked by the given user
	 * 
	 * @param articlename
	 * @param user
	 * @return
	 */
	public boolean isPageLockedCurrentUser( String articlename, String user );
	
	
	/**
	 * reads the default locale of the connected wiki
	 * 
	 * @return
	 */
	public Locale getLocale();
	
	
	/**
	 * reads the locale which was configured by the current user
	 * 
	 * @param request
	 * @return
	 */
	public Locale getLocale( HttpServletRequest request );
	
	@SuppressWarnings("unchecked")
	public Collection findPages(String query);

	
	/**
	 * Creates a link to an article with the given text to display
	 * as link text in the syntax of the specific wiki.
	 * If the link text is null or empty it is omitted.
	 * 
	 * 
	 * @param articleName name of the article to link to
	 * @param linkText the text to show for the link. can be empty String or null 
	 * @return s a string representing a link to the given article in wiki syntax
	 */
	String createWikiLink(String articleName, String linkText);
	
	
}
