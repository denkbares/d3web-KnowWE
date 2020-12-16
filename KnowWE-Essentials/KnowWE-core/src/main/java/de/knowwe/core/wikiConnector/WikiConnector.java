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
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.user.UserContext;

/**
 * This interface defines how KnowWE (and KnowWE-Plugins) can interact with the underlying wiki engine. An
 * implementation provides KnowWE access to attachments file, page sources, user rights, edit locks and much more.
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
	 * Creates a new wiki page with given title and content and author in the connected wiki.
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
	 * Returns all active users. Active users are users currently having an active {@link HttpSession} in the wiki.
	 */
	String[] getAllActiveUsers();

	/**
	 * Returns a map of all wiki articles with the titles as key and the article contents as values.
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
	 * Returns the {@link WikiAttachment} given by the supplied path. This method returns null if the attachment does
	 * not exists. The method throws an {@link IOException} if there are any problems to access the attachment from the
	 * underlying wiki architecture.
	 *
	 * @param path the path of the attachment
	 * @return the attachment of the specified path or null, if there is no such attachment
	 * @throws IOException if the attachment cannot be accessed
	 * @created 30.08.2011
	 */
	@Nullable
	WikiAttachment getAttachment(String path) throws IOException;

	/**
	 * Returns the {@link WikiAttachment} given by the supplied path. This method returns null if the attachment does
	 * not exists. The method throws an {@link IOException} if there are any problems to access the attachment from the
	 * underlying wiki architecture.
	 *
	 * @param path    the path of the attachment
	 * @param version the version of the attachment
	 * @return the attachment of the specified path or null, if there is no such attachment
	 * @throws IOException if the attachment cannot be accessed
	 * @created 30.08.2011
	 */
	@Nullable
	WikiAttachment getAttachment(String path, int version) throws IOException;

	/**
	 * Returns the all attachments of the article with the given title. The method throws an {@link IOException} if
	 * there are any problems to access the attachments from the underlying wiki architecture.
	 *
	 * @param title the title of the article to get the attachment from
	 * @return the attachments of the specified article
	 * @throws IOException if the attachments cannot be accessed
	 */
	List<WikiAttachment> getAttachments(String title) throws IOException;

	/**
	 * Returns a collection of all attachments known to the wiki. The method throws an {@link IOException} if there are
	 * any problems to access the attachments from the underlying wiki architecture.
	 *
	 * @return the attachments of the wiki
	 * @throws IOException if the attachments cannot be accessed
	 */
	Collection<WikiAttachment> getAttachments() throws IOException;

	/**
	 * Returns the all attachments of the article with the given title. The method throws an {@link IOException} if
	 * there are any problems to access the attachments from the underlying wiki architecture.
	 * <p>
	 * In contrast to {@link #getAttachment(String)}, this method doe not resolve attached archives files to particular
	 * attachments, it only returns the plain files as they are attached.
	 *
	 * @param title the title of the article to get the attachment from
	 * @return the attachments of the specified article
	 * @throws IOException if the attachments cannot be accessed
	 */
	List<WikiAttachment> getRootAttachments(String title) throws IOException;

	/**
	 * Returns a collection of all attachments known to the wiki. The method throws an {@link IOException} if there are
	 * any problems to access the attachments from the underlying wiki architecture.
	 * <p>
	 * In contrast to {@link #getAttachments()}, this method doe not resolve attached archives files to particular
	 * attachments, it only returns the plain files as they are attached.
	 *
	 * @return the attachments of the wiki
	 * @throws IOException if the attachments cannot be accessed
	 */
	Collection<WikiAttachment> getRootAttachments() throws IOException;

	/**
	 * Returns the author of the specified version of the given article (by name) or null, if the article does not
	 * exist.
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
	 * Returns the name of the currently running wiki.
	 */
	String getApplicationName();

	/**
	 * Returns the path to the KnowWEExtensionFolder of the wiki.
	 *
	 * @created 17.04.2012
	 */
	String getKnowWEExtensionPath();

	/**
	 * Returns the Date when the specified version of the article was last modified or <tt>null</tt>, if the article
	 * does not exist.
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
	 * Returns a path to safely store files to. The path must be outside the webapps dir to prevent the files to be
	 * deleted during a wiki-redeploy
	 */
	String getSavePath();

	/**
	 * Returns the current servlet-context object.
	 */
	ServletContext getServletContext();

	/**
	 * Returns the most current version number of the article with the given title
	 *
	 * @param title the title of the article
	 */
	int getVersion(String title);

	/**
	 * Returns the given version of the content of the article with the given title. If the article does not exist,
	 * <tt>null</tt> is returned. To get the latest version, pass -1 as the version attribute.
	 *
	 * @param title   the title of the article
	 * @param version the version number of the article source to be retrieved
	 */
	String getArticleText(String title, int version);

	/**
	 * Returns the content of the article with the given title from the underlying wiki. If the article does not exist,
	 * <tt>null</tt> is returned.
	 *
	 * @param title the title of the article
	 */
	String getArticleText(String title);

	/**
	 * Returns the change note of the given version of the article with the given title. If the article or the version
	 * do not exist, <tt>null</tt> is returned. To get the latest version, pass -1 as the version attribute.
	 *
	 * @param title   the title of the article
	 * @param version the version number of the article source to be retrieved
	 */
	String getChangeNote(String title, int version);

	/**
	 * Checks whether a article has a editing lock (due to another user who has started to edit it)
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
	 * Sets an editing lock on the article, denoting that the article is currently edited by the given user.
	 *
	 * @param title the title of the article to be locked
	 * @param user  the user locking the article
	 */
	boolean lockArticle(String title, String user);

	/**
	 * Normalizes a string the same way as the wiki engine normalizes wiki text before saving it to a file.
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
	 * Renders given wiki mark-up to html. Use only if no http request is available.
	 *
	 * @return the rendered article text
	 */
	String renderWikiSyntax(String articleText);

	/**
	 * Creates a new wiki attachment of the specified path and the specified file content.
	 * <p/>
	 * If there is already an attachment with this path, a new version of the attachment will be created. Otherwise a
	 * new attachment will be created. In both cases you can use the returned {@link WikiAttachment} to access the
	 * attachment.
	 *
	 * @param title          the title of the article, for which this attachment should be stored
	 * @param user           is the user wanting to store the attachment
	 * @param attachmentFile the attachment to be stored
	 * @return the newly created attachment
	 * @throws IOException if the stream cannot be read of if the content cannot be stored as an attachment
	 */
	WikiAttachment storeAttachment(String title, String user, File attachmentFile) throws IOException;

	/**
	 * Creates a new wiki attachment of the specified path and the specified content. Please note that the stream will
	 * not be closed by this method. When the method return the stream has been fully read. It's in the callers
	 * responsibility to close the stream afterwards.
	 * <p/>
	 * If there is already an attachment with this path, a new version of the attachment will be created. Otherwise a
	 * new attachment will be created. In both cases you can use the returned {@link WikiAttachment} to access the
	 * attachment.
	 *
	 * @param title    the title of the article, for which this attachment should be stored
	 * @param filename the name for which the attachment should be stored
	 * @param user     is the user wanting to store the attachment
	 * @param stream   the stream for the content of the file
	 * @return the newly created attachment
	 * @throws IOException if the stream cannot be read of if the content cannot be stored as an attachment
	 */
	WikiAttachment storeAttachment(String title, String filename, String user, InputStream stream) throws IOException;

	/**
	 * Creates a new wiki attachment of the specified path and the specified content. Please note that the stream will
	 * not be closed by this method. When the method return the stream has been fully read. It's in the callers
	 * responsibility to close the stream afterwards.
	 * <p/>
	 * If there is already an attachment with this path and <tt>versioning</tt> is set to true, a new version of the
	 * attachment will be created. If
	 * <tt>versioning</tt> is set to false, any previous versions (if there are
	 * any) will be removed. In all cases you can use the returned {@link WikiAttachment} to access the attachment.
	 *
	 * @param title      the title of the article, for which this attachment should be stored
	 * @param filename   the name for which the attachment should be stored
	 * @param user       is the user wanting to store the attachment
	 * @param stream     the stream for the content of the file
	 * @param versioning decides whether the attachment should be stored with versioning
	 * @return the newly created attachment
	 * @throws IOException if the stream cannot be read of if the content cannot be stored as an attachment
	 */
	WikiAttachment storeAttachment(String title, String filename, String user, InputStream stream, boolean versioning) throws IOException;

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
	 * Deletes the article with the given <tt>title</tt>. If the article is versioned, all versions of the article are
	 * deleted.
	 *
	 * @param title the title of the article the attachment is stored in
	 * @param user  the user deleting the attachment
	 * @throws IOException if the attachment cannot be deleted
	 */
	void deleteArticle(String title, String user) throws IOException;

	/**
	 * Renames the article page name from <tt>fromPage</tt> to a new name <tt>toPage</tt>. If the new page name already
	 * exists it throws an exception. Also triggers the renaming of the KnowWE article name.
	 *
	 * @param fromPage actual name of the page
	 * @param toPage   new name of the page
	 * @param request  the request of the user
	 * @return the new page name
	 * @throws IOException if the actual page not exists, the new page name already exists or can not be written
	 */
	String renamePage(String fromPage, String toPage, HttpServletRequest request) throws IOException;

	/**
	 * Removes the lock for the article.
	 *
	 * @param title the title of the article to be unlocked
	 * @param user  the user who's lock shall be removed, if null the most recent lock will be removed
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
	 * Checks whether a user is allowed delete a given article
	 *
	 * @param title   the title of the article to check
	 * @param request the request of the user
	 */
	boolean userCanDeleteArticle(String title, HttpServletRequest request);

	/**
	 * Checks whether a user is allowed delete a given article
	 *
	 * @param request the request of the user
	 */
	boolean userCanCreateArticles(HttpServletRequest request);

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

	/**
	 * Send an email to one or more recipients.
	 *
	 * @param to One or more comma-separated addresses. Note that an address is either a simple email address
	 *           or an RFC822 'name & addr-spec' or a user's full-name or a user's login-name or a user's
	 *           wiki-name
	 * @throws IOException Thrown if mail transport identifies an error situation during preparing and dispatching
	 *                     the mail. Note that subsequent errors (such as unknown recipients) are not covered but must
	 *                     be handled by managing the sender's inbox.
	 */
	void sendMail(String to, String subject, String content) throws IOException;

	void sendMultipartMail(String toAddresses, String subject, String plainTextContent, String htmlContent, Map<String, URL> imageMapping) throws IOException;

	/**
	 * Returns the current (rendering) template of the wiki. If the wiki does not support templates, the method will
	 * return null.
	 *
	 * @return the current template of the wiki.
	 */
	@Nullable
	String getTemplate();

	/**
	 * Returns the property value of the specified propery, as definied in the wiki connector (for JSPWiki this are the
	 * property value as defined in "jspwiki-custom.properties".
	 *
	 * @param property the property key to get the value for
	 * @return the property value
	 */
	@Nullable
	String getWikiProperty(String property);

	/**
	 * Opens a page transaction for a Wiki user, in which multiple file operations are put in one Git commit together,
	 * if GitVersioningFileProvider or equivalent is active
	 *
	 * @param user the user which executes a big change
	 */
	void openPageTransaction(String user);

	/**
	 * Do the commit of page transaction which was opened by openPageTransaction. If successful the page
	 * transaction will be closed.
	 * If no page transaction was opened before, nothing happens.
	 *
	 * @param user      the user which executes a big change
	 * @param commitMsg commitPageTransaction message
	 */
	void commitPageTransaction(String user, String commitMsg);

	/**
	 * This will rollback the changes of a user to the file system.
	 * This can be done, if an error occurs or if some defined states were not reached.
	 * But only if commitPageTransaction was not successfully executed. It also closes
	 * the page transaction which was opened with openPageTransaction
	 *
	 * @param user the user which executes a big change
	 */
	void rollbackPageTransaction(String user);

	boolean hasRollbackPageProvider();
}
