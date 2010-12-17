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

package de.d3web.we.wikiConnector;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.d3web.we.action.KnowWEActionDispatcher;
import de.d3web.we.core.KnowWEParameterMap;

/**
 *
 * This interface defines how KnowWE (and KnowWE-Plugins) can interact with the
 * underlying wiki engine. An implementation provides KnowWE access to
 * attachments file, page sources, user rights, edit locks and much more.
 *
 * To connect a wiki engine with KnowWE this interface needs to be implemented.
 *
 * @author Jochen
 *
 */

public interface KnowWEWikiConnector {

	/**
	 * Checks whether the user is member of a given group
	 *
	 * @param username
	 * @param groupname
	 * @param r
	 * @return
	 */
	public boolean userIsMemberOfGroup(String username, String groupname, HttpServletRequest r);

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
	 * Returns a path to savely store owl files to. The path must be outside the
	 * webapps dir to prevent the files to be deleted during a wiki-redeploy
	 *
	 */
	public String getSavePath();

	/**
	 * Return the ActionDispatcher which is responsible to receive the
	 * http-request for the KnowWE-actions
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
	public boolean writeArticleToWikiEnginePersistence(String name, String text, KnowWEParameterMap map);

	/**
	 * Saves the article (persistently) into the connected wiki
	 *
	 * @param name
	 * @param text
	 * @param map
	 * @param fullParse possibility to force the full parsing of the page (only
	 *        for KnowWETestWikiConnector working)
	 * @return
	 */
	// public boolean writeArticleToWikiEnginePersistence(String name, String
	// text, KnowWEParameterMap map, boolean fullParse);

	/**
	 * Returns a list of all jar attachment files of the wiki
	 *
	 * @return
	 */
	public List<String> getJarAttachments();

	/**
	 * @return a List of all ConnectorAttachments
	 */
	public Collection<ConnectorAttachment> getAttachments();

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
	 * Returns the most current version of the source text of the wiki page with
	 * the given name as one string
	 *
	 * @param name
	 * @return
	 */
	public String getArticleSource(String name);

	/**
	 * Gets the most current version number of the article with the given name
	 *
	 * @param name
	 * @return
	 */
	public int getVersion(String name);

	/**
	 * Returns the given version of the source text of the wiki page
	 *
	 * @param name the name of the article
	 * @param version the version number of the article source to be retrieved
	 * @return the source of the article, or null if the given version does not
	 *         exist
	 */
	public String getArticleSource(String name, int version);

	/**
	 * Gets the author of the specified version of the given article ( by name )
	 *
	 * @created 14.06.2010
	 * @param name the name of the article which has to be selected
	 * @param version the version number of which the author has to be returned
	 * @return the author of the specified version or null
	 */
	public String getAuthor(String name, int version);

	public Map<Integer, Date> getModificationHistory(String name);

	/**
	 * Gets the Date when the specified version of the article was last modified
	 *
	 * @created 17.07.2010
	 * @param name the name of the article
	 * @param version the version of the article
	 * @return the Date when the selected articleversion was modified
	 */
	public Date getLastModifiedDate(String name, int version);

	/**
	 * Returns a map of all wiki pages with page names as key and page sources
	 * as values
	 *
	 * @param web
	 * @return
	 */
	public Map<String, String> getAllArticles(String web);

	/**
	 * tests if a page of the given name exists
	 *
	 * @param Topic
	 * @return
	 */
	public boolean doesPageExist(String Topic);

	/**
	 * Creates a new Wiki page with given name and content and author in the
	 * connected wiki
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
	public boolean userCanEditPage(String articlename);

	/**
	 * Checks whether a user can edit a given page
	 *
	 * @param articlename
	 * @param r
	 * @return
	 */
	public boolean userCanEditPage(String articlename, HttpServletRequest r);

	/**
	 * Checks whether a user can view a given page
	 * 
	 * @param articlename
	 * @return
	 */
	public boolean userCanViewPage(String articlename);

	/**
	 * Checks whether a user can view a given page
	 * 
	 * @param articlename
	 * @param r
	 * @return
	 */
	public boolean userCanViewPage(String articlename, HttpServletRequest r);

	/**
	 * Checks whether a page has a editing lock (due to another user who has
	 * startet to edit it)
	 *
	 * @param articlename
	 * @return
	 */
	public boolean isPageLocked(String articlename);

	/**
	 * Sets an editing lock on the page, denoting that the page is currently
	 * editing by the given user.
	 *
	 * @param articlename
	 * @param user
	 * @return
	 */
	public boolean setPageLocked(String articlename, String user);

	/**
	 * Removes a page editing lock
	 *
	 * @param articlename
	 */
	public void undoPageLocked(String articlename);

	/**
	 * Checks whether a given page is locked by the given user
	 *
	 * @param articlename
	 * @param user
	 * @return
	 */
	public boolean isPageLockedCurrentUser(String articlename, String user);

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
	public Locale getLocale(HttpServletRequest request);

	/**
	 * Creates a link to an article with the given text to display as link text
	 * in the syntax of the specific wiki. If the link text is null or empty it
	 * is omitted.
	 *
	 *
	 * @param articleName name of the article to link to
	 * @param linkText the text to show for the link. can be empty String or
	 *        null
	 * @return s a string representing a link to the given article in wiki
	 *         syntax
	 */
	String createWikiLink(String articleName, String linkText);

	/**
	 * Return a Map from pageNames to the number of (edited) versions of this
	 * page
	 *
	 * @return
	 */
	public Map<String, Integer> getVersionCounts();

	/**
	 * Stores an File as an attachment to the given page. Returns whether the
	 * operation was successful or not.
	 *
	 * @param wikiPage the name of the page, to which this attachment should be
	 *        stored
	 * @param attachmentFile the attachment to be stored
	 * @return
	 */
	boolean storeAttachment(String wikiPage, File attachmentFile);

	/**
	 * Renders given WIKI mark-up in the pagedata.
	 *
	 * @param pagedata The current data of the page.
	 * @param map The parameters of the request.
	 * @return The masked pagedata.
	 */
	public String renderWikiSyntax(String pagedata, KnowWEParameterMap map);

}
