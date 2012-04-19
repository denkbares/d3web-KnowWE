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

package connector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.knowwe.core.Environment;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.ConnectorAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

public class DummyConnector implements WikiConnector {

	private static final String DUMMY_USER = "DummyUser";

	private DummyPageProvider dummyPageProvider = null;

	private String knowweExtensionPath = null;

	private final Map<String, String> locks = new HashMap<String, String>();

	public DummyConnector() {
	}

	public DummyConnector(DummyPageProvider dummyPageProvider) {
		this.dummyPageProvider = dummyPageProvider;
	}

	@Override
	public String createArticle(String title, String content, String author) {
		Environment.getInstance().buildAndRegisterArticle(content, title, Environment.DEFAULT_WEB);
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider is null, so additional wiki pages cannot be added");
		}
		dummyPageProvider.setArticleContent(title, content);
		return content;
	}

	@Override
	public boolean doesArticleExist(String title) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider is null, so there are no articles available");
		}
		return dummyPageProvider.getArticle(title) != null;
	}

	@Override
	public String[] getAllActiveUsers() {
		return new String[] { DUMMY_USER };
	}

	@Override
	public Map<String, String> getAllArticles(String web) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider is null, so there are no articles available");
		}
		return dummyPageProvider.getAllArticles();
	}

	@Override
	public String[] getAllUsers() {
		return new String[] { DUMMY_USER };
	}

	@Override
	public String getApplicationRootPath() {
		return new File("").getAbsolutePath();
	}

	@Override
	public ConnectorAttachment getAttachment(String path) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider null, so there are no attachments available");
		}
		return dummyPageProvider.getAttachment(path);
	}

	@Override
	public Collection<ConnectorAttachment> getAttachments() {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider null, so there are no attachments available");
		}
		return new ArrayList<ConnectorAttachment>(dummyPageProvider.getAllAttachments().values());
	}

	@Override
	public List<ConnectorAttachment> getAttachments(String title) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider null, so there are no attachments available");
		}
		Collection<ConnectorAttachment> attachments = getAttachments();
		List<ConnectorAttachment> attachmentsOfPage = new ArrayList<ConnectorAttachment>();
		for (ConnectorAttachment attachment : attachments) {
			if (attachment.getParentName().equals(title)) {
				attachmentsOfPage.add(attachment);
			}
		}
		return attachmentsOfPage;
	}

	@Override
	public String getAuthor(String name, int version) {
		return DUMMY_USER;
	}

	@Override
	public String getBaseUrl() {
		return "http://valid_dummy_base_url/";
	}

	@Override
	public String getKnowWEExtensionPath() {
		if (knowweExtensionPath == null) {
			return getApplicationRootPath() + File.separator + "KnowWEExtension";
		}
		else {
			return knowweExtensionPath;
		}
	}

	@Override
	public Date getLastModifiedDate(String title, int version) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider is null, so there are no articles available");
		}
		String article = dummyPageProvider.getArticle(title);
		if (article == null) return null;
		return dummyPageProvider.getStartUpdate();
	}

	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}

	@Override
	public Locale getLocale(HttpServletRequest request) {
		return Locale.getDefault();
	}

	@Override
	public String getRealPath() {
		return getApplicationRootPath();
	}

	@Override
	public String getSavePath() {
		return getApplicationRootPath() + "\repository";
	}

	@Override
	public ServletContext getServletContext() {
		throw new NullPointerException("Used WikiConnector can not provide a ServletContext");
	}

	@Override
	public int getVersion(String title) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider is null, so there are no articles available");
		}
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page versions");
		return dummyPageProvider.getArticle(title) == null ? 0 : 1;
	}

	@Override
	public String getVersion(String title, int version) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider is null, so there are no articles available");
		}
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector only provides one version per article");
		return dummyPageProvider.getArticle(title);
	}

	@Override
	public int getVersionCount(String title) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider is null, so there are no articles available");
		}
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page versions");
		return dummyPageProvider.getArticle(title) == null ? 0 : 1;
	}

	@Override
	public boolean isArticleLocked(String title) {
		return locks.containsKey(title);
	}

	@Override
	public boolean isArticleLockedCurrentUser(String articlename, String user) {
		String lockingUser = locks.get(articlename);
		return lockingUser != null && lockingUser.equals(user);
	}

	@Override
	public boolean lockArticle(String title, String user) {
		locks.put(title, user);
		return true;
	}

	@Override
	public String normalizeString(String string) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support normalizing of strings");
		return string;
	}

	@Override
	public String renderWikiSyntax(String string, HttpServletRequest request) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support a wiki syntax");
		return string;
	}

	public void setKnowWEExtensionPath(String knowWEExtensionPath) {
		this.knowweExtensionPath = knowWEExtensionPath;
	}

	@Override
	public boolean storeAttachment(String title, String user, File attachmentFile) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider is null, so attachments cannot be stored");
		}
		ConnectorAttachment attachment = new FileSystemConnectorAttachment(
				attachmentFile.getName(), title, attachmentFile);
		dummyPageProvider.storeAttachment(attachment.getPath(), attachment);
		return true;
	}

	@Override
	public boolean storeAttachment(String title, String filename, String user, InputStream stream) {
		if (dummyPageProvider == null) {
			throw new NullPointerException(
					"PageProvider is null, so attachments cannot be stored");
		}
		ConnectorAttachment attachment;
		try {
			attachment = new FileSystemConnectorAttachment(
					filename, title, stream);
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		dummyPageProvider.storeAttachment(attachment.getPath(), attachment);
		return true;
	}

	@Override
	public void unlockArticle(String title) {
		locks.remove(title);
	}

	@Override
	public boolean userCanEditArticle(String articlename, HttpServletRequest r) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support rights managment");
		return true;
	}

	@Override
	public boolean userCanViewArticle(String articlename, HttpServletRequest r) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support rights managment");
		return true;
	}

	@Override
	public boolean userIsMemberOfGroup(String groupname, HttpServletRequest r) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support user groups");
		return false;
	}

	@Override
	public boolean writeArticleToWikiPersistence(String title, String content, UserContext context) {
		Environment.getInstance().buildAndRegisterArticle(content, title, Environment.DEFAULT_WEB);
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so additional wiki pages cannot be added");
			return true;
		}
		dummyPageProvider.setArticleContent(title, content);
		return true;
	}

}
