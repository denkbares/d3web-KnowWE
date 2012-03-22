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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.PageLock;
import com.ecyrd.jspwiki.PageManager;
import com.ecyrd.jspwiki.TextUtil;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiException;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.attachment.Attachment;
import com.ecyrd.jspwiki.attachment.AttachmentManager;
import com.ecyrd.jspwiki.auth.AuthorizationManager;
import com.ecyrd.jspwiki.auth.SessionMonitor;
import com.ecyrd.jspwiki.auth.WikiSecurityException;
import com.ecyrd.jspwiki.auth.permissions.PagePermission;
import com.ecyrd.jspwiki.auth.permissions.PermissionFactory;
import com.ecyrd.jspwiki.preferences.Preferences;
import com.ecyrd.jspwiki.providers.ProviderException;

import de.knowwe.core.action.ActionDispatcher;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.ConnectorAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

/**
 * For code documentation look at the WikiConnector interface definition
 * 
 * @author Jochen
 * 
 */
public class JSPWikiConnector implements WikiConnector {

	private ServletContext context = null;
	private WikiEngine engine = null;
	public static final String LINK_PREFIX = "Wiki.jsp?page=";

	public JSPWikiConnector(WikiEngine eng) {
		this.context = eng.getServletContext();
		this.engine = eng;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public ActionDispatcher getActionDispatcher() {
		return JSPActionDispatcher.getInstance();
	}

	@Override
	public boolean writeArticleToWikiEnginePersistence(String name, String text, UserContext user) {
		try {
			HttpServletRequest req = user.getRequest();
			WikiContext context = engine.createContext(req, WikiContext.EDIT);
			context.setPage(engine.getPage(name));

			WikiPage page = context.getPage();
			page.setAuthor(context.getCurrentUser().getName());

			engine.saveText(context, text);
			// engine.saveText(map.getContext(), text);
			return true;
		}
		catch (WikiException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Collection<ConnectorAttachment> getAttachments() {
		try {
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			Collection<?> attachments = attachmentManager.getAllAttachments();
			Collection<ConnectorAttachment> ret = new LinkedList<ConnectorAttachment>();
			for (Object o : attachments) {
				if (o instanceof Attachment) {
					ret.add(new JSPWikiConnectorAttachment((Attachment) o,
							attachmentManager));
				}
			}
			return ret;
		}
		catch (ProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public ConnectorAttachment getAttachment(String path) {
		try {
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			Attachment attachment = attachmentManager.getAttachmentInfo(path);

			if (attachment == null) return null;
			else return new JSPWikiConnectorAttachment(attachment, attachmentManager);

		}
		catch (ProviderException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public boolean storeAttachment(String wikiPage, String user, File attachmentFile) {
		try {
			return storeAttachment(wikiPage, attachmentFile.getName(), user, new FileInputStream(
					attachmentFile));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public boolean storeAttachment(String wikiPage, String filename, String user, InputStream stream) {
		try {
			boolean isNotLocked = !isPageLocked(wikiPage);
			if (isPageLockedCurrentUser(wikiPage, user) || isNotLocked) {
				if (isNotLocked) setPageLocked(wikiPage, "WIKI-ENGINE");
				AttachmentManager attachmentManager = this.engine
						.getAttachmentManager();

				Attachment att = new Attachment(engine, wikiPage,
						filename);
				att.setAuthor(user);
				attachmentManager.storeAttachment(att, stream);

				if (isNotLocked) undoPageLocked(wikiPage);
				return true;
			}
			else return false;
		}
		catch (ProviderException e) {
			return false;
		}
		catch (IOException e) {
			return false;
		}

	}

	@Override
	public java.util.Map<String, String> getAllArticles(String web) {
		Map<String, String> result = new HashMap<String, String>();
		Collection<WikiPage> pages = null;
		PageManager pageManager = this.engine.getPageManager();
		try {
			pages = pageManager.getProvider().getAllPages();
		}
		catch (ProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pages == null) return null;

		for (WikiPage wikiPage : pages) {
			String pageContent = null;
			try {
				pageContent = pageManager.getPageText(wikiPage.getName(),
						wikiPage.getVersion());
			}
			catch (ProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (pageContent != null) {
				result.put(wikiPage.getName(), pageContent);
			}

		}

		return result;
	}

	@Override
	public String createWikiPage(String topic, String content, String author) {

		WikiPage wp = new WikiPage(this.engine, topic);

		try {
			// References Updaten.
			this.engine.getReferenceManager().updateReferences(topic,
						this.engine.getReferenceManager().findCreated());
			wp.setAuthor(author);
			this.engine.getPageManager().putPageText(wp, content);
			this.engine.getSearchManager().reindexPage(wp);

		}
		catch (ProviderException e) {
			return null;
		}
		catch (NullPointerException e) {
			// should only happen on wiki initialization
			return null;
		}

		return this.engine.getPureText(wp);
	}

	@Override
	public boolean doesPageExist(String topic) {

		// Check if a Page with the chosen Topic already exists.
		if (this.engine.pageExists(topic)) return true;
		return false;
	}

	@Override
	public String appendContentToPage(String topic, String content) {

		try {
			content = this.engine.getPureText(this.engine.getPage(topic))
					+ content;
			WikiContext context = new WikiContext(this.engine, this.engine
					.getPage(topic));
			this.engine.saveText(context, content);
			this.engine.updateReferences(this.engine.getPage(topic));
			this.engine.getSearchManager().reindexPage(
					this.engine.getPage(topic));

		}
		catch (ProviderException e) {
			return null;
		}
		catch (WikiException e1) {
			return null;
		}

		return this.engine.getPureText(this.engine.getPage(topic));
	}

	@Override
	public String getArticleSource(String name) {

		// Surrounded this because getPage()
		// caused a Nullpointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(name) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(name));

		String pagedata = context.getEngine().getPureText(
				context.getPage().getName(), context.getPage().getVersion());
		return pagedata;
	}

	@Override
	public String getArticleSource(String name, int version) {

		// Surrounded this because getPage()
		// caused a Nullpointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(name) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(name));

		String pagedata = null;
		try {
			if (context.getEngine().pageExists(context.getPage().getName(), version)) {
				pagedata = context.getEngine().getPureText(
						context.getPage().getName(), version);
			}
		}
		catch (ProviderException e) {
			return null;
		}

		return pagedata;
	}

	@Override
	public Map<Integer, Date> getModificationHistory(String name) {
		// Surrounded this because getPage()
		// caused a Nullpointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(name) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		Map<Integer, Date> map = new TreeMap<Integer, Date>();

		for (Object o : this.engine.getVersionHistory(name)) {
			if (o instanceof WikiPage) {
				WikiPage wp = (WikiPage) o;
				map.put(wp.getVersion(), wp.getLastModified());
			}
		}
		return map;
	}

	@Override
	public Date getLastModifiedDate(String name, int version) {
		// Surrounded this because getPage()
		// caused a Nullpointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(name) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(name));

		Date lastModified = null;
		try {
			if (context.getEngine().pageExists(context.getPage().getName(), version)) {
				lastModified = context.getEngine().getPage(context.getPage().getName(),
						version).getLastModified();
			}
		}
		catch (ProviderException e) {
			return null;
		}

		return lastModified;
	}

	@Override
	public String getAuthor(String name, int version) {
		// Surrounded this because getPage()
		// caused a Nullpointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(name) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(name));

		String author = null;
		try {
			if (context.getEngine().pageExists(context.getPage().getName(), version)) {
				author = context.getEngine().getPage(context.getPage().getName(), version).getAuthor();
			}
		}
		catch (ProviderException e) {
			return null;
		}

		return author;
	}

	@Override
	public int getVersion(String name) {
		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(name));
		return context.getPage().getVersion();
	}

	@Override
	public String getBaseUrl() {
		return engine.getBaseURL();
	}

	/**
	 * Checks if the current user has the rights to edit the given page. Returns
	 * TRUE if the user has editing permission, otherwise FALSE and editing of
	 * the page is denied.
	 * 
	 * Not working properly USE userCanEditPage(String, HttpServletRequest)
	 * instead
	 * 
	 * @param articlename
	 */
	@Override
	@Deprecated
	public boolean userCanEditPage(String articlename) {
		WikiPage page = new WikiPage(engine, articlename);
		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(articlename));
		AuthorizationManager authmgr = engine.getAuthorizationManager();
		PagePermission pp = PermissionFactory.getPagePermission(page, "edit");

		return authmgr.checkPermission(context.getWikiSession(), pp);
	}

	/**
	 * Checks if the current user has the rights to edit the given page. Returns
	 * TRUE if the user has editing permission, otherwise FALSE and editing of
	 * the page is denied.
	 * 
	 * @param articlename
	 * @param r HttpRequest
	 */
	@Override
	public boolean userCanEditPage(String articlename, HttpServletRequest r) {
		WikiPage page = new WikiPage(engine, articlename);
		WikiContext context = new WikiContext(this.engine, r, this.engine
				.getPage(articlename));

		AuthorizationManager authmgr = engine.getAuthorizationManager();
		PagePermission pp = PermissionFactory.getPagePermission(page, "edit");

		return authmgr.checkPermission(context.getWikiSession(), pp);
	}

	/**
	 * Checks if the current user has the rights to view the given page. Returns
	 * TRUE if the user has viewing permission, otherwise FALSE and viewing the
	 * page is denied.
	 * 
	 * @param articlename
	 */
	@Override
	public boolean userCanViewPage(String articlename) {
		WikiPage page = new WikiPage(engine, articlename);
		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(articlename));
		AuthorizationManager authmgr = engine.getAuthorizationManager();
		PagePermission pp = PermissionFactory.getPagePermission(page, "view");

		return authmgr.checkPermission(context.getWikiSession(), pp);
	}

	/**
	 * Checks if the current user has the rights to show the given page. Returns
	 * TRUE if the user has showing permission, otherwise FALSE and showing of
	 * the page is denied.
	 * 
	 * @param articlename
	 * @param r HttpRequest
	 */
	@Override
	public boolean userCanViewPage(String articlename, HttpServletRequest r) {
		WikiPage page = new WikiPage(engine, articlename);
		WikiContext context = new WikiContext(this.engine, r, this.engine
				.getPage(articlename));

		AuthorizationManager authmgr = engine.getAuthorizationManager();
		PagePermission pp = PermissionFactory.getPagePermission(page, "view");

		return authmgr.checkPermission(context.getWikiSession(), pp);
	}

	/**
	 * Checks if the current page has an access lock. If TRUE no user other then
	 * the lock owner can edit the page. If FALSE the current page has no lock
	 * and can be edited by anyone.
	 * 
	 * @param articlename
	 */
	@Override
	public boolean isPageLocked(String articlename) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, articlename);
		if (mgr.getCurrentLock(page) == null) return false;
		else return true;
	}

	/**
	 * Checks if the current page has been locked by the current user. Returns
	 * TRUE if yes, FALSE otherwise.
	 * 
	 * @param articlename
	 * @param user
	 * @return
	 */
	@Override
	public boolean isPageLockedCurrentUser(String articlename, String user) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, articlename);
		PageLock lock = mgr.getCurrentLock(page);

		if (lock == null) return false;

		if (lock.getLocker().equals(user)) return true;
		return false;
	}

	/**
	 * Locks a page in the WIKI so no other user can edit the page.
	 * 
	 * @param articlename
	 */
	@Override
	public boolean setPageLocked(String articlename, String user) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, articlename);
		PageLock lock = mgr.lockPage(page, user);

		if (lock != null) return true;
		return false;
	}

	/**
	 * Removes a page lock from a certain page in the WIKI so other users can
	 * edit the page.
	 * 
	 * @param articlename
	 */
	@Override
	public void undoPageLocked(String articlename) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, articlename);

		if (isPageLocked(articlename)) {
			PageLock lock = mgr.getCurrentLock(page);
			mgr.unlockPage(lock);
		}
	}

	@Override
	public String getRealPath() {
		return context.getContextPath();
	}

	@Override
	public Locale getLocale(HttpServletRequest request) {

		WikiContext wikiContext = new WikiContext(this.engine, request,
				this.engine.getPage("Main"));

		return Preferences.getLocale(wikiContext);
	}

	@Override
	public Locale getLocale() {

		WikiContext wikiContext = new WikiContext(this.engine, this.engine
				.getPage("Main"));

		return Preferences.getLocale(wikiContext);
	}

	@Override
	public List<String> getAttachmentFilenamesForPage(String pageName) {
		try {
			List<String> attachmentList = new LinkedList<String>();

			// this list is in fact a Collection<Attachment>,
			// the conversion is typesafe!
			@SuppressWarnings("unchecked")
			Collection<Attachment> attList = this.engine.getAttachmentManager().
					listAttachments(this.engine.getPage(pageName));

			for (Attachment att : attList) {
				attachmentList.add(att.getFileName());
			}

			return attachmentList;
		}
		catch (ProviderException e) {
			return null;
		}
	}

	@Override
	public Map<String, Integer> getVersionCounts() {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		PageManager pm = engine.getPageManager();
		try {
			for (Object pageObj : pm.getAllPages()) {
				String pageName = ((WikiPage) pageObj).getName();
				List<?> versionHistory = pm.getVersionHistory(pageName);
				int versionNumber = versionHistory.size();
				result.put(pageName, versionNumber);
			}
		}
		catch (ProviderException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String getSavePath() {
		String path = (String) engine.getWikiProperties().get("var.basedir");
		return path;
	}
	
	public String getWikiProperty(String property) {
		String path = (String) engine.getWikiProperties().get(property);
		return path;
	}

	@Override
	public String renderWikiSyntax(String pagedata, UserActionContext userContext) {
		HttpServletRequest req = userContext.getRequest();
		WikiContext context = engine.createContext(req, WikiContext.VIEW);
		pagedata = engine.textToHTML(context, pagedata);
		return pagedata;
	}

	@Override
	public boolean userIsMemberOfGroup(String username, String groupname, HttpServletRequest r) {

		// which article is not relevant
		String articleName = "Main";
		WikiContext context = new WikiContext(this.engine, r, this.engine
				.getPage(articleName));

		Principal[] princ = context.getWikiSession().getRoles();

		for (Principal p : princ) {
			if (p.getName().equals(groupname)) return true;
		}

		return false;
	}

	@Override
	public String wikiSyntaxToHtml(String syntax) {
		WikiContext context = this.engine.createContext(null, WikiContext.VIEW);
		String html = engine.textToHTML(context, syntax);
		return html;
	}

	@Override
	public String[] getAllUsers() {
		String[] users = null;

		try {
			Principal[] princ = engine.getUserManager().listWikiNames();
			users = new String[princ.length];
			for (int i = 0; i < princ.length; i++) {
				users[i] = engine.getUserManager().getUserDatabase().findByWikiName(
						princ[i].getName()).getFullname();
			}
		}
		catch (WikiSecurityException e) {
			e.printStackTrace();
		}

		return users;
	}

	@Override
	public String[] getAllActiveUsers() {
		String[] activeUsers = null;

		Principal[] princ = SessionMonitor.getInstance(engine).userPrincipals();
		activeUsers = new String[princ.length];
		for (int i = 0; i < princ.length; i++) {
			activeUsers[i] = princ[i].getName();
		}

		return activeUsers;
	}

	@Override
	public String normalizeStringTo(String string) {
		return TextUtil.normalizePostData(string);
	}

}
