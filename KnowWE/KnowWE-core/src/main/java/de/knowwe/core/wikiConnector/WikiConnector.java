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

package de.knowwe.core.wikiConnector;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.knowwe.core.user.UserContext;

/**
 * 
 * This interface defines how KnowWE (and KnowWE-Plugins) can interact with the
 * underlying wiki engine. An implementation provides KnowWE access to
 * attachments file, page sources, user rights, edit locks and much more.
 * 
 * To connect a wiki engine with KnowWE this interface needs to be implemented.
 * 
 * @author Jochen Reutelshöfer, Albrecht Striffler (denkbares GmbH)
 * 
 */

public interface WikiConnector {

	/**
	 * Creates a new wiki page with given title and content and author in the
	 * connected wiki.
	 * 
	 * @param title the title of the new article
	 * @param content the content of the new article
	 * @param author the author of the new article
	 */
	public String createArticle(String title, String content, String author);

	/**
	 * Tests if a article with the given title exists.
	 * 
	 * @param title the title of the article to check
	 */
	public boolean doesArticleExist(String title);

	/**
	 * Returns all active users. Active users are users currently having an
	 * active {@link HttpSession} in the wiki.
	 */
	public String[] getAllActiveUsers();

	/**
	 * Returns a map of all wiki articles with the titles as key and the article
	 * contents as values.
	 * 
	 * @param web the web to check for the articles.
	 */
	public Map<String, String> getAllArticles(String web);

	/**
	 * Returns all users registered to the wiki.
	 */
	public String[] getAllUsers();

	/**
	 * Returns the path to the root of the application.
	 * 
	 * @created 12.04.2012
	 */
	public String getApplicationRootPath();

	/**
	 * Returns the {@link ConnectorAttachment} given by the supplied path.
	 * 
	 * @created 30.08.2011
	 * @param path the path of the attachment
	 */
	public ConnectorAttachment getAttachment(String path);

	/**
	 * Returns all attachments known to the wiki.
	 */
	public Collection<ConnectorAttachment> getAttachments();

	/**
	 * Returns the filenames of the attachments of the article with the given
	 * title
	 * 
	 * @param title the title of the article to get the attachment filenames
	 *        from
	 */
	public List<ConnectorAttachment> getAttachments(String title);

	/**
	 * Returns the author of the specified version of the given article (by
	 * name) or null, if the article does not exist.
	 * 
	 * @created 14.06.2010
	 * @param title the title of the article which has to be selected
	 * @param version the version number of which the author has to be returned
	 */
	public String getAuthor(String title, int version);

	/**
	 * Returns the URL of the running wiki.
	 */
	public String getBaseUrl();

	/**
	 * Returns the path to the KnowWEExtensionFolder of the wiki.
	 * 
	 * @created 17.04.2012
	 */
	public String getKnowWEExtensionPath();

	/**
	 * Returns the Date when the specified version of the article was last
	 * modified or <tt>null</tt>, if the article does not exist.
	 * 
	 * @created 17.07.2010
	 * @param title the name of the article
	 * @param version the version of the article
	 */
	public Date getLastModifiedDate(String title, int version);

	/**
	 * Returns the default locale of the connected wiki
	 */
	public Locale getLocale();

	/**
	 * Returns the locale which was configured by the current user.
	 * 
	 * @param request is the request from the user
	 */
	public Locale getLocale(HttpServletRequest request);

	/**
	 * Returns the absolute path of the web-application
	 */
	public String getRealPath();

	/**
	 * Returns a path to safely store files to. The path must be outside the
	 * webapps dir to prevent the files to be deleted during a wiki-redeploy
	 */
	public String getSavePath();

	/**
	 * Returns the current servlet-context object.
	 */
	public ServletContext getServletContext();

	/**
	 * Returns the most current version number of the article with the given
	 * title
	 * 
	 * @param title the title of the article
	 */
	public int getVersion(String title);

	/**
	 * Returns the given version of the content of the article with the given
	 * title. If the article does not exist, <tt>null</tt> is returned. To get
	 * the latest version, pass -1 as the version attribute.
	 * 
	 * @param title the title of the article
	 * @param version the version number of the article source to be retrieved
	 */
	public String getVersion(String title, int version);

	/**
	 * Returns the total amount of versions for the article with the given
	 * title.
	 * 
	 * @created 17.04.2012
	 * @param title the title of the article you want to check the version count
	 *        for
	 */
	public int getVersionCount(String title);

	/**
	 * Checks whether a article has a editing lock (due to another user who has
	 * started to edit it)
	 * 
	 * @param title the title of the article to check the lock for
	 */
	public boolean isArticleLocked(String title);

	/**
	 * Checks whether a given article is locked by the given user
	 * 
	 * @param title the title of the article to check the lock for
	 * @param user the user to check the lock for
	 */
	public boolean isArticleLockedCurrentUser(String title, String user);

	/**
	 * Sets an editing lock on the article, denoting that the article is
	 * currently edited by the given user.
	 * 
	 * @param title the title of the article to be locked
	 * @param user the user locking the article
	 */
	public boolean lockArticle(String title, String user);

	/**
	 * Normalizes a string the same way as the wiki engine normalizes wiki text
	 * before saving it to a file.
	 * 
	 * @created 12.12.2011
	 * @param string the string to be normalized
	 * @return the normalized string
	 */
	public String normalizeString(String string);

	/**
	 * Renders given wiki mark-up.
	 * 
	 * @param articleText the current text of the article.
	 * @param request the request of the user
	 * @return the rendered article text
	 */
	public String renderWikiSyntax(String pagedata, HttpServletRequest request);

	/**
	 * Stores a file as an attachment to the given article.
	 * 
	 * @param title the title of the article, for which this attachment should
	 *        be stored
	 * @param user is the user wanting to store the attachment (important for
	 *        lock of the page)
	 * @param attachmentFile the attachment to be stored
	 * @return whether the operation was successful
	 */
	boolean storeAttachment(String title, String user, File attachmentFile);

	/**
	 * Stores a file as an attachment to the given article.
	 * 
	 * @param title the title of the article, for which this attachment should
	 *        be stored
	 * @param filename the name for which the attachment should be stored
	 * @param user is the user wanting to store the attachment (important for
	 *        lock of the page)
	 * @param stream the stream for the content of the file
	 * @return whether the operation was successful
	 */
	boolean storeAttachment(String title, String filename, String user, InputStream stream);

	/**
	 * Removes the lock for the article.
	 * 
	 * @param title the title of the article to be unlocked
	 */
	public void unlockArticle(String title);

	/**
	 * Checks whether a user is allowed edit a given article.
	 * 
	 * @param title the title of the article to check
	 * @param request the request of the user to check for
	 */
	public boolean userCanEditArticle(String title, HttpServletRequest request);

	/**
	 * Checks whether a user is allowed view a given article
	 * 
	 * @param title the title of the article to check
	 * @param request the request of the user
	 */
	public boolean userCanViewArticle(String title, HttpServletRequest request);

	/**
	 * Checks whether the user is member of a given group.
	 * 
	 * @param groupname the name of the group to check
	 * @param request the request of the user to check
	 */
	public boolean userIsMemberOfGroup(String groupname, HttpServletRequest request);

	/**
	 * Saves the article (persistently) into the connected wiki
	 * 
	 * @param title the title of the article to save
	 * @param content the content of the article to save
	 * @param context the {@link UserContext} of the user changing the article
	 */
	public boolean writeArticleToWikiPersistence(String title, String content, UserContext context);
}
