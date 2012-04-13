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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.knowwe.core.Environment;
import de.knowwe.core.action.ActionDispatcher;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.ConnectorAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

public class DummyConnector implements WikiConnector {

	private DummyPageProvider dummyPageProvider = null;

	private String knowweExtensionPath = null;

	public DummyConnector() {
	}

	public DummyConnector(DummyPageProvider dummyPageProvider) {
		this.dummyPageProvider = dummyPageProvider;
	}

	@Override
	public String appendContentToPage(String title, String content) {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so wiki pages cannot be changed");
			return content;
		}
		String article = dummyPageProvider.getArticle(title);
		if (article == null) article = "";
		article += content;
		Environment.getInstance().buildAndRegisterArticle(article, title, Environment.DEFAULT_WEB);
		dummyPageProvider.setArticleContent(title, article);
		return content;
	}

	@Override
	public String createWikiPage(String title, String content, String author) {
		Environment.getInstance().buildAndRegisterArticle(content, title, Environment.DEFAULT_WEB);
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so additional wiki pages cannot be added");
			return "";
		}
		dummyPageProvider.setArticleContent(title, content);
		return content;
	}

	@Override
	public boolean doesPageExist(String title) {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so there are no articles available");
			return false;
		}
		return dummyPageProvider.getArticle(title) != null;
	}

	@Override
	public ActionDispatcher getActionDispatcher() {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support users");
		return null;
	}

	@Override
	public String[] getAllActiveUsers() {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support users");
		return null;
	}

	@Override
	public Map<String, String> getAllArticles(String web) {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so there are no articles available");

			return null;
		}
		return dummyPageProvider.getAllArticles();
	}

	@Override
	public String[] getAllUsers() {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support users");
		return null;
	}

	@Override
	public String getApplicationRootPath() {
		return new File("").getAbsolutePath();
	}

	@Override
	public String getArticleSource(String name) {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so there are no articles available");

			return null;
		}
		return dummyPageProvider.getArticle(name);
	}

	@Override
	public String getArticleSource(String name, int version) {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so there are no articles available");

			return null;
		}
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support article versions");
		return dummyPageProvider.getArticle(name);
	}

	@Override
	public ConnectorAttachment getAttachment(String path) {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so there are no attachments available");
			return null;
		}
		return dummyPageProvider.getAttachment(path);
	}

	@Override
	public List<String> getAttachmentFilenamesForPage(String pageName) {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so there are no attachments available");
			return null;
		}
		Collection<ConnectorAttachment> attachments = getAttachments();
		List<String> fileNames = new ArrayList<String>();
		for (ConnectorAttachment attachment : attachments) {
			if (attachment.getParentName().equals(pageName)) {
				fileNames.add(attachment.getFileName());
			}
		}
		return fileNames;
	}

	@Override
	public Collection<ConnectorAttachment> getAttachments() {
		if (dummyPageProvider == null) {
			Logger.getLogger(this.getClass().getName()).warning(
					"No PageProvider given, so there are no attachments available");
			return new ArrayList<ConnectorAttachment>();
		}
		return new ArrayList<ConnectorAttachment>(dummyPageProvider.getAllAttachments().values());
	}

	@Override
	public String getAuthor(String name, int version) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support authors");
		return null;
	}

	@Override
	public String getBaseUrl() {
		return "http://valid_dummy_base_url/";
	}

	@Override
	public String getKnowWEExtensionPath() {
		if (knowweExtensionPath == null) {
			return getApplicationRootPath() + "/KnowWEExtension";
		}
		else {
			return knowweExtensionPath;
		}
	}

	@Override
	public Date getLastModifiedDate(String name, int version) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page versions");
		return null;
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
	public Map<Integer, Date> getModificationHistory(String name) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page versions");
		return null;
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
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page versions");
		return null;
	}

	@Override
	public int getVersion(String name) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page versions");
		return 0;
	}

	@Override
	public Map<String, Integer> getVersionCounts() {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page versions");
		return null;
	}

	@Override
	public boolean isPageLocked(String articlename) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page locks");
		return false;
	}

	@Override
	public boolean isPageLockedCurrentUser(String articlename, String user) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page locks");
		return false;
	}

	@Override
	public String normalizeStringTo(String string) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support normalizing of strings");
		return string;
	}

	@Override
	public String renderWikiSyntax(String pagedata, UserActionContext userContext) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support a wiki syntax");
		return null;
	}

	public void setKnowWEExtensionPath(String knowWEExtensionPath) {
		this.knowweExtensionPath = knowWEExtensionPath;
	}

	@Override
	public boolean setPageLocked(String articlename, String user) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page locks");
		return false;
	}

	@Override
	public boolean storeAttachment(String wikiPage, String user, File attachmentFile) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support to store new attachments");
		return false;
	}

	@Override
	public boolean storeAttachment(String wikiPage, String filename, String user, InputStream stream) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support to store new attachments");
		return false;
	}

	@Override
	public void undoPageLocked(String articlename) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support page locks");
	}

	@Override
	public boolean userCanEditPage(String articlename) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support rights managment");
		return true;
	}

	@Override
	public boolean userCanEditPage(String articlename, HttpServletRequest r) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support rights managment");
		return true;
	}

	@Override
	public boolean userCanViewPage(String articlename) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support rights managment");
		return true;
	}

	@Override
	public boolean userCanViewPage(String articlename, HttpServletRequest r) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support rights managment");
		return true;
	}

	@Override
	public boolean userIsMemberOfGroup(String username, String groupname, HttpServletRequest r) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support user groups");
		return false;
	}

	@Override
	public String wikiSyntaxToHtml(String syntax) {
		Logger.getLogger(this.getClass().getName()).warning(
				"The used WikiConnector does not support a wiki syntax");
		return null;
	}

	@Override
	public boolean writeArticleToWikiEnginePersistence(String title, String content, UserContext context) {
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
