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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.ecyrd.jspwiki.InternalWikiException;
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

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
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
		initPageLocking();
	}

	/**
	 * We need this method, because there is the possibility of an
	 * IllegalThreadStateException while initializing the lock reaper of the
	 * PageManager, if the first call of lockPage is done in parallel by two
	 * threads. To avoid this, we call this directly at startup in a safe way.
	 * 
	 * @created 26.10.2012
	 */
	private void initPageLocking() {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, "Dummy");
		PageLock lock = mgr.lockPage(page, "Dummy");
		mgr.unlockPage(lock);
	}

	@Override
	public String createArticle(String title, String content, String author) {

		WikiPage wp = new WikiPage(this.engine, title);

		try {
			// References Updaten.
			this.engine.getReferenceManager().updateReferences(title,
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
		Environment.getInstance().buildAndRegisterArticle(content, title, Environment.DEFAULT_WEB);
		return this.engine.getPureText(wp);
	}

	@Override
	public boolean doesArticleExist(String title) {

		// Check if a Page with the chosen title already exists.
		if (this.engine.pageExists(title)) return true;
		return false;
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

	@SuppressWarnings("unchecked")
	@Override
	public java.util.Map<String, String> getAllArticles(String web) {
		Map<String, String> result = new HashMap<String, String>();
		Collection<WikiPage> pages = null;
		PageManager pageManager = this.engine.getPageManager();
		try {
			pages = pageManager.getProvider().getAllPages();
		}
		catch (ProviderException e) {
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
				e.printStackTrace();
			}
			if (pageContent != null) {
				result.put(wikiPage.getName(), pageContent);
			}

		}

		return result;
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
	public String getApplicationRootPath() {
		return getServletContext().getRealPath("").replaceAll("/+$", "");
	}

	@Override
	public WikiAttachment getAttachment(String path) throws IOException {
		try {
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			Attachment attachment = attachmentManager.getAttachmentInfo(path);

			if (attachment == null) return null;
			else return new JSPWikiAttachment(attachment, attachmentManager);

		}
		catch (ProviderException e) {
			String message = "cannot access attachments due to provider error";
			throw new IOException(message, e);
		}
	}

	@Override
	public Collection<WikiAttachment> getAttachments() throws IOException {
		try {
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			Collection<?> attachments = attachmentManager.getAllAttachments();
			Collection<WikiAttachment> ret = new LinkedList<WikiAttachment>();
			for (Object o : attachments) {
				if (o instanceof Attachment) {
					ret.add(new JSPWikiAttachment((Attachment) o,
							attachmentManager));
				}
			}
			return ret;
		}
		catch (ProviderException e) {
			String message = "cannot access attachments due to provider error";
			throw new IOException(message, e);
		}

	}

	@Override
	public List<WikiAttachment> getAttachments(String title) throws IOException {
		try {
			List<WikiAttachment> attachmentList = new LinkedList<WikiAttachment>();
			// this list is in fact a Collection<Attachment>,
			// the conversion is type safe!
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			WikiPage page = this.engine.getPage(title);
			if (page == null) {
				// might happen that a page of this title does not exist.
				// return empty list to prevent NullPointer in AttachmentManager
				return attachmentList;
			}
			@SuppressWarnings("unchecked")
			Collection<Attachment> attList = attachmentManager.
					listAttachments(page);

			for (Attachment att : attList) {
				attachmentList.add(new JSPWikiAttachment(att,
						attachmentManager));
			}

			return attachmentList;
		}
		catch (ProviderException e) {
			String message = "cannot access attachments due to provider error";
			throw new IOException(message, e);
		}
	}

	@Override
	public String getAuthor(String title, int version) {
		// Surrounded this because getPage()
		// caused a Nullpointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(title) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		try {
			WikiContext context = new WikiContext(this.engine, this.engine.getPage(title));
			if (context.getEngine().pageExists(context.getPage().getName(), version)) {
				return context.getEngine().getPage(context.getPage().getName(), version).getAuthor();
			}
		}
		catch (ProviderException e) {
		}
		return null;
	}

	@Override
	public String getBaseUrl() {
		return engine.getBaseURL();
	}

	@Override
	public String getKnowWEExtensionPath() {
		return KnowWEUtils.getRealPath(KnowWEUtils.getConfigBundle()
				.getString("path_to_knowweextension"));
	}

	@Override
	public Date getLastModifiedDate(String title, int version) {
		// Surrounded this because getPage()
		// caused a Nullpointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(title) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		WikiContext context = new WikiContext(this.engine, this.engine.getPage(title));
		try {
			if (context.getEngine().pageExists(context.getPage().getName(), version)) {
				return context.getEngine().getPage(context.getPage().getName(),
						version).getLastModified();
			}
		}
		catch (ProviderException e) {
		}
		return null;
	}

	@Override
	public Locale getLocale() {
		WikiContext wikiContext = new WikiContext(this.engine, this.engine.getPage("Main"));
		return Preferences.getLocale(wikiContext);
	}

	@Override
	public Locale getLocale(HttpServletRequest request) {
		WikiContext wikiContext = new WikiContext(this.engine, request, this.engine.getPage("Main"));
		return Preferences.getLocale(wikiContext);
	}

	@Override
	public String getRealPath() {
		return context.getContextPath();
	}

	@Override
	public String getSavePath() {
		String path = (String) engine.getWikiProperties().get("var.basedir");
		return path;
	}

	@Override
	public ServletContext getServletContext() {
		return context;
	}

	@Override
	public int getVersion(String title) {
		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(title));
		return context.getPage().getVersion();
	}

	@Override
	public String getVersion(String title, int version) {

		// Surrounded this because getPage()
		// caused a Nullpointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(title) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(title));

		String pagedata = null;
		try {
			if (version == -1) {
				pagedata = context.getEngine().getPureText(
						context.getPage().getName(), context.getPage().getVersion());
			}
			else if (context.getEngine().pageExists(context.getPage().getName(), version)) {
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
	public int getVersionAtDate(String title, Date d) throws IOException {
		WikiContext context = new WikiContext(this.engine, this.engine
				.getPage(title));

		int versionMax = context.getPage().getVersion();

		int versionForDate = -1; // -1 represents the current/newest version of
									// article
		for (int v = 1; v <= versionMax; v++) {
			WikiPage wikiPage = null;
			try {
				wikiPage = engine.getPageManager().getPageInfo(title, v);
				Date lastModified = wikiPage.getLastModified();

				// fix for invalid dates d
				if (v == 1 && lastModified.after(d)) {
					// version 1 is the creation of the page
					// in the ridiculous case (corrupted persistence file) that
					// the build date is before the date of page creation, we
					// return the current version
					versionForDate = -2;
					break;
				}

				if (lastModified.after(d)) {
					versionForDate = v - 1;
					break;
				}
			}
			catch (ProviderException e) {
				String message = "cannot access wiki page info due to provider error";
				throw new IOException(message, e);
			}
		}

		return versionForDate;

	}

	@Override
	public int getVersionCount(String title) {
		PageManager pm = engine.getPageManager();
		try {
			return pm.getVersionHistory(title).size();
		}
		catch (ProviderException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String getChangeNote(String title, int version) {
		PageManager pm = engine.getPageManager();
		try {
			WikiPage pageInfo = pm.getPageInfo(title, version);
			if (pageInfo == null) {
				return null;
			}
			String note = (String) pageInfo.getAttribute(WikiPage.CHANGENOTE);
			return note == null ? "" : note;
		}
		catch (ProviderException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String getWikiProperty(String property) {
		String path = (String) engine.getWikiProperties().get(property);
		return path;
	}

	/**
	 * Checks if the current page has an access lock. If TRUE no user other then
	 * the lock owner can edit the page. If FALSE the current page has no lock
	 * and can be edited by anyone.
	 * 
	 * @param title the title of the article to check
	 */
	@Override
	public boolean isArticleLocked(String title) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, title);
		if (mgr.getCurrentLock(page) == null) return false;
		else return true;
	}

	/**
	 * Checks if the current page has been locked by the current user. Returns
	 * TRUE if yes, FALSE otherwise.
	 * 
	 * @param title the title of the article to check
	 * @param user the user to check for
	 */
	@Override
	public boolean isArticleLockedCurrentUser(String title, String user) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, title);
		PageLock lock = mgr.getCurrentLock(page);

		if (lock == null) return false;

		if (lock.getLocker().equals(user)) return true;
		return false;
	}

	@Override
	public boolean lockArticle(String title, String user) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, title);
		PageLock lock = mgr.lockPage(page, user);

		if (lock != null) return true;
		return false;
	}

	@Override
	public String normalizeString(String string) {
		return TextUtil.normalizePostData(string);
	}

	@Override
	public String renderWikiSyntax(String content, HttpServletRequest request) {
		try {
			WikiContext context = engine.createContext(request, WikiContext.VIEW);
			content = engine.textToHTML(context, content);
		}
		catch (InternalWikiException e) {
			// happens only during KnowWE's startup and can thus be ignored...
		}
		return content;
	}

	@Override
	public String renderWikiSyntax(String string) {
		return renderWikiSyntax(string, null);
	}

	@Override
	public WikiAttachment storeAttachment(String title, String user, File attachmentFile) throws IOException {
		FileInputStream in = new FileInputStream(attachmentFile);
		try {
			return storeAttachment(title, attachmentFile.getName(), user, in);
		}
		finally {
			in.close();
		}
	}

	@Override
	public WikiAttachment storeAttachment(String title, String filename, String user, InputStream stream) throws IOException {
		try {
			boolean wasLocked = isArticleLocked(title);
			if (!wasLocked) lockArticle(title, user);
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();

			Attachment attachment = new Attachment(engine, title, filename);
			attachment.setAuthor(user);
			attachmentManager.storeAttachment(attachment, stream);

			if (!wasLocked) unlockArticle(title, user);
			return getAttachment(title + "/" + filename);
		}
		catch (ProviderException e) {
			throw new IOException("could not store attachment");
		}
	}

	@Override
	public void unlockArticle(String title, String user) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, title);

		if (isArticleLocked(title)) {
			if (user == null) {
				PageLock lock = mgr.getCurrentLock(page);
				mgr.unlockPage(lock);
			}
			else {
				for (Object other : mgr.getActiveLocks()) {
					PageLock otherLock = (PageLock) other;
					if (otherLock.getLocker().equals(user)) {
						mgr.unlockPage(otherLock);
					}
				}
			}
		}
	}

	@Override
	public boolean userCanEditArticle(String title, HttpServletRequest request) {
		WikiPage page = new WikiPage(engine, title);
		WikiContext context = new WikiContext(this.engine, request, this.engine
				.getPage(title));

		AuthorizationManager authmgr = engine.getAuthorizationManager();
		PagePermission pp = PermissionFactory.getPagePermission(page, "edit");

		return authmgr.checkPermission(context.getWikiSession(), pp);
	}

	@Override
	public boolean userCanViewArticle(String title, HttpServletRequest request) {
		WikiPage page = new WikiPage(engine, title);
		WikiContext context = new WikiContext(this.engine, request, this.engine
				.getPage(title));

		AuthorizationManager authmgr = engine.getAuthorizationManager();
		PagePermission pp = PermissionFactory.getPagePermission(page, "view");

		return authmgr.checkPermission(context.getWikiSession(), pp);
	}

	@Override
	public boolean userIsMemberOfGroup(String groupname, HttpServletRequest request) {

		// which article is not relevant
		String articleName = "Main";
		WikiContext context = new WikiContext(this.engine, request, this.engine
				.getPage(articleName));

		Principal[] princ = context.getWikiSession().getRoles();

		for (Principal p : princ) {
			if (p.getName().equals(groupname)) return true;
		}

		return false;
	}

	@Override
	public boolean writeArticleToWikiPersistence(String title, String content, UserContext user) {
		try {
			HttpServletRequest req = user.getRequest();
			WikiContext context = engine.createContext(req, WikiContext.EDIT);
			context.setPage(engine.getPage(title));

			WikiPage page = context.getPage();
			page.setAuthor(context.getCurrentUser().getName());
			String changeNote = user.getParameter(Attributes.CHANGE_NOTE);
			if (changeNote != null) {
				page.setAttribute(WikiPage.CHANGENOTE, changeNote);
			}

			engine.saveText(context, content);
			// engine.saveText(map.getContext(), text);
			return true;
		}
		catch (WikiException e) {
			e.printStackTrace();
			return false;
		}
	}
}
