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
import java.io.IOException;
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
 * This interface defines how KnowWE (and KnowWE-Plugins) can interact with the
 * underlying wiki engine. An implementation provides KnowWE access to
 * attachments file, page sources, user rights, edit locks and much more.
 * <p/>
 * To connect a wiki engine with KnowWE this interface needs to be implemented.
 *
 * @author Jochen Reutelshöfer, Albrecht Striffler (denkbares GmbH)
 */

public interface WikiConnector {

	/**
	 * Returns the list with all available versions of the wiki page with the given title, starting with the most
	 * recent.
	 *
	 * @throws IOException in case of an exception from the underlying wiki
	 */
	List<WikiPageInfo> getArticleHistory(String title) throws IOException;

	/**
	 * Returns the list with all available versions of the attachment page with the given name, starting with the most
	 * recent.
	 *
	 * @param path the path of the attachment
	 * @throws IOException in case of an exception from the underlying wiki
	 */
	List<WikiAttachmentInfo> getAttachmentHistory(String path) throws IOException;

	/**
	 * Creates a new wiki page with given title and content and author in the
	 * connected wiki.
	 *
	 * @param title   the title of the new article
	 * @param author  the author of the new article
	 * @param content the content of the new article
	 */
	String createArticle(String title, String author, String content);

	/**
	 * Tests if a article with the given title exists.
	 *
	 * @param title the title of the article to check
	 */
	boolean doesArticleExist(String title);

	/**
	 * Returns all active users. Active users are users currently having an
	 * active {@link HttpSession} in the wiki.
	 */
	String[] getAllActiveUsers();

	/**
	 * Returns a map of all wiki articles with the titles as key and the article
	 * contents as values.
	 *
	 * @param web the web to check for the articles.
	 */
	Map<String, String> getAllArticles(String web);

	/**
	 * Returns all users registered to the wiki.
	 */
	String[] getAllUsers();

	/**
	 * Returns the path to the root of the application.
	 *
	 * @created 12.04.2012
	 */
	String getApplicationRootPath();

	/**
	 * Returns the {@link WikiAttachment} given by the supplied path. This
	 * method returns null if the attachment does not exists. The method throws
	 * an {@link IOException} if there are any problems to access the attachment
	 * from the underlying wiki architecture.
	 *
	 * @param path the path of the attachment
	 * @return the attachment of the specified path
	 * @throws IOException if the attachment cannot be accessed
	 * @created 30.08.2011
	 */
	WikiAttachment getAttachment(String path) throws IOException;

	/**
	 * Returns the {@link WikiAttachment} given by the supplied path. This
	 * method returns null if the attachment does not exists. The method throws
	 * an {@link IOException} if there are any problems to access the attachment
	 * from the underlying wiki architecture.
	 *
	 * @param path    the path of the attachment
	 * @param version the version of the attachment
	 * @return the attachment of the specified path
	 * @throws IOException if the attachment cannot be accessed
	 * @created 30.08.2011
	 */
	WikiAttachment getAttachment(String path, int version) throws IOException;

	/**
	 * Returns the all attachments of the article with the given title. The
	 * method throws an {@link IOException} if there are any problems to access
	 * the attachments from the underlying wiki architecture.
	 *
	 * @param title the title of the article to get the attachment from
	 * @return the attachments of the specified article
	 * @throws IOException if the attachments cannot be accessed
	 */
	List<WikiAttachment> getAttachments(String title) throws IOException;

	/**
	 * Returns a collection of all attachments known to the wiki. The method
	 * throws an {@link IOException} if there are any problems to access the
	 * attachments from the underlying wiki architecture.
	 *
	 * @return the attachments of the wiki
	 * @throws IOException if the attachments cannot be accessed
	 */
	Collection<WikiAttachment> getAttachments() throws IOException;

	/**
	 * Returns the author of the specified version of the given article (by
	 * name) or null, if the article does not exist.
	 *
	 * @param title   the title of the article which has to be selected
	 * @param version the version number of which the author has to be returned
	 * @created 14.06.2010
	 */
	String getAuthor(String title, int version);

	/**
	 * Returns the URL of the running wiki.
	 */
	String getBaseUrl();

	/**
	 * Returns the path to the KnowWEExtensionFolder of the wiki.
	 *
	 * @created 17.04.2012
	 */
	String getKnowWEExtensionPath();

	/**
	 * Returns the Date when the specified version of the article was last
	 * modified or <tt>null</tt>, if the article does not exist.
	 *
	 * @param title   the name of the article
	 * @param version the version of the article
	 * @created 17.07.2010
	 */
	Date getLastModifiedDate(String title, int version);

	/**
	 * Returns the locale which was configured by the current user.
	 *
	 * @param request is the request from the user
	 */
	Locale getLocale(HttpServletRequest request);

	/**
	 * Returns the absolute path of the web-application
	 */
	String getRealPath();

	/**
	 * Returns a path to safely store files to. The path must be outside the
	 * webapps dir to prevent the files to be deleted during a wiki-redeploy
	 */
	String getSavePath();

	/**
	 * Returns the current servlet-context object.
	 */
	ServletContext getServletContext();

	/**
	 * Returns the most current version number of the article with the given
	 * title
	 *
	 * @param title the title of the article
	 */
	int getVersion(String title);

	/**
	 * Returns the given version of the content of the article with the given
	 * title. If the article does not exist, <tt>null</tt> is returned. To get
	 * the latest version, pass -1 as the version attribute.
	 *
	 * @param title   the title of the article
	 * @param version the version number of the article source to be retrieved
	 */
	String getArticleText(String title, int version);

	/**
	 * Returns the content of the article with the given
	 * title from the underlying wiki. If the article does not exist, <tt>null</tt> is returned.
	 *
	 * @param title the title of the article
	 */
	String getArticleText(String title);

	/**
	 * Returns the change note of the given version of the article with the
	 * given title. If the article or the version do not exist, <tt>null</tt> is
	 * returned. To get the latest version, pass -1 as the version attribute.
	 *
	 * @param title   the title of the article
	 * @param version the version number of the article source to be retrieved
	 */
	String getChangeNote(String title, int version);

	/**
	 * Checks whether a article has a editing lock (due to another user who has
	 * started to edit it)
	 *
	 * @param title the title of the article to check the lock for
	 */
	boolean isArticleLocked(String title);

	/**
	 * Checks whether a given article is locked by the given user
	 *
	 * @param title the title of the article to check the lock for
	 * @param user  the user to check the lock for
	 */
	boolean isArticleLockedCurrentUser(String title, String user);

	/**
	 * Sets an editing lock on the article, denoting that the article is
	 * currently edited by the given user.
	 *
	 * @param title the title of the article to be locked
	 * @param user  the user locking the article
	 */
	boolean lockArticle(String title, String user);

	/**
	 * Normalizes a string the same way as the wiki engine normalizes wiki text
	 * before saving it to a file.
	 *
	 * @param string the string to be normalized
	 * @return the normalized string
	 * @created 12.12.2011
	 */
	String normalizeString(String string);

	/**
	 * Renders given wiki mark-up to html.
	 *
	 * @param articleText the current text of the article.
	 * @param request     the request of the user. May be null is not available.
	 * @return the rendered article text
	 */
	String renderWikiSyntax(String articleText, HttpServletRequest request);

	/**
	 * Renders given wiki mark-up to html. Use only if no http request is
	 * available.
	 *
	 * @return the rendered article text
	 */
	String renderWikiSyntax(String articleText);

	/**
	 * Creates a new wiki attachment of the specified path and the specified
	 * file content.
	 * <p/>
	 * If there is already an attachment with this path, a new version of the
	 * attachment will be created. Otherwise a new attachment will be created.
	 * In both cases you can use the returned {@link WikiAttachment} to access
	 * the attachment.
	 *
	 * @param title          the title of the article, for which this attachment should
	 *                       be stored
	 * @param user           is the user wanting to store the attachment
	 * @param attachmentFile the attachment to be stored
	 * @return the newly created attachment
	 * @throws IOException if the stream cannot be read of if the content cannot
	 *                     be stored as an attachment
	 */
	WikiAttachment storeAttachment(String title, String user, File attachmentFile) throws IOException;

	/**
	 * Creates a new wiki attachment of the specified path and the specified
	 * content. Please note that the stream will not be closed by this method.
	 * When the method return the stream has been fully read. It's in the
	 * callers responsibility to close the stream afterwards.
	 * <p/>
	 * If there is already an attachment with this path, a new version of the
	 * attachment will be created. Otherwise a new attachment will be created.
	 * In both cases you can use the returned {@link WikiAttachment} to access
	 * the attachment.
	 *
	 * @param title    the title of the article, for which this attachment should
	 *                 be stored
	 * @param filename the name for which the attachment should be stored
	 * @param user     is the user wanting to store the attachment
	 * @param stream   the stream for the content of the file
	 * @return the newly created attachment
	 * @throws IOException if the stream cannot be read of if the content cannot
	 *                     be stored as an attachment
	 */
	WikiAttachment storeAttachment(String title, String filename, String user, InputStream stream) throws IOException;

	/**
	 * Deletes the attachment <tt>fileName</tt> from the article with the given <tt>title</tt>. If the attachment is
	 * versioned, all versions of the attachment are deleted (as if the attachment never existed).
	 *
	 * @param title    the title of the article the attachment is stored in
	 * @param fileName the file name of the attachment
	 * @param user     the user deleting the attachment
	 * @throws IOException if the attachment cannot be deleted
	 */
	void deleteAttachment(String title, String fileName, String user) throws IOException;

	/**
	 * Removes the lock for the article.
	 *
	 * @param title the title of the article to be unlocked
	 * @param user  the user who's lock shall be removed, if null the most recent
	 *              lock will be removed
	 */
	void unlockArticle(String title, String user);

	/**
	 * Checks whether a user is allowed edit a given article.
	 *
	 * @param title   the title of the article to check
	 * @param request the request of the user to check for
	 */
	boolean userCanEditArticle(String title, HttpServletRequest request);

	/**
	 * Checks whether a user is allowed view a given article
	 *
	 * @param title   the title of the article to check
	 * @param request the request of the user
	 */
	boolean userCanViewArticle(String title, HttpServletRequest request);

	/**
	 * Checks whether the user is member of a given group.
	 *
	 * @param groupname the name of the group to check
	 * @param request   the request of the user to check
	 */
	boolean userIsMemberOfGroup(String groupname, HttpServletRequest request);

	/**
	 * Saves the article (persistently) into the connected wiki
	 *
	 * @param title   the title of the article to save
	 * @param content the content of the article to save
	 * @param context the {@link UserContext} of the user changing the article
	 */
	boolean writeArticleToWikiPersistence(String title, String content, UserContext context);
}
