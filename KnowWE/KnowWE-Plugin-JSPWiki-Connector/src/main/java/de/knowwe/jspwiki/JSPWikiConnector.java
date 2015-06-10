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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.wiki.InternalWikiException;
import org.apache.wiki.PageLock;
import org.apache.wiki.PageManager;
import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.WikiProvider;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.exceptions.WikiException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.SessionMonitor;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.permissions.PagePermission;
import org.apache.wiki.auth.permissions.PermissionFactory;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.providers.CachingAttachmentProvider;
import org.apache.wiki.providers.WikiAttachmentProvider;
import org.apache.wiki.util.TextUtil;

import de.d3web.utils.Log;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiAttachmentInfo;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.core.wikiConnector.WikiPageInfo;

/**
 * For code documentation look at the WikiConnector interface definition
 *
 * @author Jochen
 */
public class JSPWikiConnector implements WikiConnector {

	private ServletContext context = null;
	private WikiEngine engine = null;

	private static final Map<String, List<WikiAttachment>> zipAttachmentCache =
			Collections.synchronizedMap(new HashMap<>());

	private static int skipCount = 0;
	private static final int skipAfter = 10;

	public WikiEngine getEngine() {
		return engine;
	}

	public static final String LINK_PREFIX = "Wiki.jsp?page=";

	public JSPWikiConnector(WikiEngine eng) {
		this.context = eng.getServletContext();
		this.engine = eng;
		initPageLocking();
	}

	/**
	 * We need this method, because there is the possibility of an IllegalThreadStateException while initializing the
	 * lock reaper of the PageManager, if the first call of lockPage is done in parallel by two threads. To avoid this,
	 * we call this directly at startup in a safe way.
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
	public String createArticle(String title, String author, String content) {
		return createArticle(title, author, content, true, true);
	}

	public String createArticle(String title, String author, String content, boolean updateReferences, boolean reindex) {

		WikiPage wp = new WikiPage(this.engine, title);

		try {
			if (updateReferences) {
				updateReferences(title);
			}
			wp.setAuthor(author);
			this.engine.getPageManager().putPageText(wp, content);
			if (reindex) {
				reindex(title);
			}

		}
		catch (ProviderException e) {
			Log.severe(e.getMessage(), e);
			return null;
		}
		catch (NullPointerException e) {
			// should only happen on wiki initialization
			return null;
		}
		Environment.getInstance().buildAndRegisterArticle(Environment.DEFAULT_WEB, title, content);
		return this.engine.getPureText(wp);
	}

	public void reindex(String title) {
		WikiPage wp = this.engine.getPage(title);
		this.engine.getSearchManager().reindexPage(wp);
	}

	@SuppressWarnings("unchecked")
	public void updateReferences(String title) {
		this.engine.getReferenceManager().updateReferences(title,
				this.engine.getReferenceManager().findCreated());
	}

	@Override
	public boolean doesArticleExist(String title) {

		// Check if a Page with the chosen title already exists.
		return this.engine.pageExists(title);
	}

	@Override
	public String[] getAllActiveUsers() {
		String[] activeUsers;

		Principal[] principals = SessionMonitor.getInstance(engine).userPrincipals();
		activeUsers = new String[principals.length];
		for (int i = 0; i < principals.length; i++) {
			activeUsers[i] = principals[i].getName();
		}

		return activeUsers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getAllArticles(String web) {
		Map<String, String> result = new HashMap<>();
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
	public Collection<WikiAttachment> getAttachments() throws IOException {
		cleanZipAttachmentCache();
		try {
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			Collection<?> attachments = attachmentManager.getAllAttachments();
			Collection<WikiAttachment> wikiAttachments = new ArrayList<>(
					attachments.size());
			for (Object o : attachments) {
				if (o instanceof Attachment) {
					Attachment att = (Attachment) o;
					wikiAttachments.add(new JSPWikiAttachment(att,
							attachmentManager));
					wikiAttachments.addAll(getZipEntryAttachments(att));
				}
			}
			return wikiAttachments;
		}
		catch (ProviderException e) {
			String message = "Cannot access attachments due to provider error.";
			throw new IOException(message, e);
		}

	}

	@Override
	public WikiAttachment getAttachment(String path) throws IOException {
		return getAttachment(path, -1);
	}

	@Override
	public WikiAttachment getAttachment(String path, int version) throws IOException {
		try {
			Pattern zipPattern = Pattern.compile("^([^/]+/[^/]+\\.zip)/(.+$)");
			Matcher matcher = zipPattern.matcher(path);
			String entry = null;
			String actualPath = path;
			if (matcher.find()) {
				actualPath = matcher.group(1);
				entry = matcher.group(2);
			}
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			Attachment attachment = attachmentManager.getAttachmentInfo(actualPath, version);

			if (attachment == null) {
				return null;
			}
			else if (entry == null) {
				return new JSPWikiAttachment(attachment, attachmentManager);
			}
			else {
				InputStream attachmentStream = attachmentManager.getAttachmentStream(attachment);
				ZipInputStream zipStream = new ZipInputStream(attachmentStream);
				boolean found = false;
				for (ZipEntry e; (e = zipStream.getNextEntry()) != null; ) {
					if (e.getName().equals(entry)) {
						found = true;
						break;
					}
				}
				if (!found) throw new IOException("ZipEntry '" + entry + "' not found.");
				return new JSPWikiZipAttachment(entry, attachment, attachmentManager);
			}

		}
		catch (ProviderException e) {
			String message = "Cannot access attachments due to provider error.";
			throw new IOException(message, e);
		}
	}

	/**
	 * Removes zip attachments of zip files that are no longer attached themselves.
	 */
	private void cleanZipAttachmentCache() {
		// We call this method quite often, so we skip most of the
		// calls...
		if (skipCount++ < skipAfter) {
			return;
		}
		else {
			skipCount = 0;
		}
		List<String> remove = new ArrayList<>();
		for (String path : zipAttachmentCache.keySet()) {
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			try {
				Attachment attachment = attachmentManager.getAttachmentInfo(path);
				if (attachment == null) {
					remove.add(path);
				}
			}
			catch (ProviderException e) {
				Log.warning("Exception while cleaning zip cache", e);
			}
		}
		zipAttachmentCache.keySet().removeAll(remove);
	}

	private List<WikiAttachment> getZipEntryAttachments(Attachment attachment) throws IOException, ProviderException {
		if (!attachment.getFileName().endsWith(".zip")) return Collections.emptyList();
		List<WikiAttachment> zipEntryAttachments = zipAttachmentCache.get(attachment.getName());
		AttachmentManager attachmentManager = this.engine.getAttachmentManager();
		if (attachment.getVersion() == WikiProvider.LATEST_VERSION) {
			// little hack for JSPWiki 2.8.4 not always providing a correct
			// version number and we need it here.
			WikiAttachmentProvider currentProvider = attachmentManager.getCurrentProvider();
			// there only are two possible providers, the
			// BasicAttachmentProvider and the CachingAttachmentProvider
			// the BasicAttachmentProvider has a correct version number
			if (currentProvider instanceof CachingAttachmentProvider) {
				currentProvider = ((CachingAttachmentProvider) currentProvider).getRealProvider();
			}
			Attachment attachmentInfo = currentProvider.getAttachmentInfo(
					new WikiPage(engine, attachment.getParentName()),
					attachment.getFileName(), WikiProvider.LATEST_VERSION);
			// this attachmentInfo will have the correct version number, so
			// we set it for the actual attachment
			attachment.setVersion(attachmentInfo.getVersion());
		}
		if (zipEntryAttachments != null) {
			// we check if the attachments are outdated
			int cachedVersion = zipEntryAttachments.get(0).getVersion();
			int currentVersion = attachment.getVersion();
			if (cachedVersion != currentVersion) {
				zipEntryAttachments = null;
			}
		}
		if (zipEntryAttachments == null) {
			zipEntryAttachments = new ArrayList<>();

			InputStream attachmentStream = attachmentManager.getAttachmentStream(attachment);
			ZipInputStream zipStream = new ZipInputStream(attachmentStream);
			for (ZipEntry e; (e = zipStream.getNextEntry()) != null; ) {
				zipEntryAttachments.add(new JSPWikiZipAttachment(e.getName(), attachment,
						attachmentManager));
			}
			zipStream.close();
			if (!zipEntryAttachments.isEmpty()) {
				zipAttachmentCache.put(attachment.getName(), zipEntryAttachments);
			}
		}
		return zipEntryAttachments;
	}

	@Override
	public List<WikiAttachment> getAttachments(String title) throws IOException {
		cleanZipAttachmentCache();
		try {
			// this list is in fact a Collection<Attachment>,
			// the conversion is type safe!
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			WikiPage page = this.engine.getPage(title);
			if (page == null) {
				// might happen that a page of this title does not exist.
				// return empty list to prevent NullPointer in AttachmentManager
				return Collections.emptyList();
			}
			@SuppressWarnings("unchecked")
			Collection<Attachment> attList = attachmentManager.
					listAttachments(page);

			List<WikiAttachment> attachmentList = new ArrayList<>(attList.size());
			for (Attachment att : attList) {
				attachmentList.add(new JSPWikiAttachment(att, attachmentManager));
				attachmentList.addAll(getZipEntryAttachments(att));
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
		// caused a NullPointer on first KnowWE startup
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
		catch (ProviderException ignored) {
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
		// caused a NullPointer on first KnowWE startup
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
		catch (ProviderException ignored) {
		}
		return null;
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
		return (String) engine.getWikiProperties().get("var.basedir");
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
	public String getArticleText(String title) {
		return getArticleText(title, -1);
	}

	@Override
	public String getArticleText(String title, int version) {
		// Surrounded this because getPage()
		// caused a Nullpointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(title) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		String pageText;
		try {
			pageText = engine.getPageManager().getPageText(title, version);
		}
		catch (ProviderException e) {
			return null;
		}

		return pageText;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WikiPageInfo> getArticleHistory(String title) throws IOException {
		try {
			List<WikiPage> versionHistory = this.engine.getPageManager().getVersionHistory(title);
			if (versionHistory == null) return Collections.emptyList();
			if (versionHistory.isEmpty()) {
				// can happen in JSPWiki, of OLD was cleaned up manually
				WikiPage currentVersion = this.engine.getPage(title);
				versionHistory = Arrays.asList(currentVersion);
			}
			return versionHistory.stream()
					.map(page -> new WikiPageInfo(page.getName(), page.getAuthor(), page.getVersion(),
							page.getLastModified()))
					.collect(Collectors.toList());
		}
		catch (ProviderException e) {
			throw new IOException("Cannot access wiki page history of '" + title + "' due to provider error", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WikiAttachmentInfo> getAttachmentHistory(String name) throws IOException {
		try {
			List<Attachment> versionHistory = this.engine.getAttachmentManager().getVersionHistory(name);
			if (versionHistory == null) return Collections.emptyList();
			return versionHistory.stream()
					.map(page -> new WikiAttachmentInfo(page.getName(), page.getAuthor(), page.getVersion(),
							page.getLastModified()))
					.collect(Collectors.toList());
		}
		catch (ProviderException e) {
			throw new IOException("Cannot access attachment history of '" + name + "' due to provider error", e);
		}
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
		return (String) engine.getWikiProperties().get(property);
	}

	/**
	 * Checks if the current page has an access lock. If TRUE no user other then the lock owner can edit the page. If
	 * FALSE the current page has no lock and can be edited by anyone.
	 *
	 * @param title the title of the article to check
	 */
	@Override
	public boolean isArticleLocked(String title) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, title);
		return mgr.getCurrentLock(page) != null;
	}

	/**
	 * Checks if the current page has been locked by the current user. Returns TRUE if yes, FALSE otherwise.
	 *
	 * @param title the title of the article to check
	 * @param user  the user to check for
	 */
	@Override
	public boolean isArticleLockedCurrentUser(String title, String user) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, title);
		PageLock lock = mgr.getCurrentLock(page);
		return lock != null && lock.getLocker().equals(user);

	}

	@Override
	public boolean lockArticle(String title, String user) {
		PageManager mgr = engine.getPageManager();
		WikiPage page = new WikiPage(engine, title);
		PageLock lock = mgr.lockPage(page, user);
		return lock != null;
	}

	@Override
	public String normalizeString(String string) {
		return TextUtil.normalizePostData(string);
	}

	@Override
	public String renderWikiSyntax(String content, HttpServletRequest request) {
		try {
			WikiContext context = engine.createContext(null, WikiContext.VIEW);
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
		try (FileInputStream in = new FileInputStream(attachmentFile)) {
			return storeAttachment(title, attachmentFile.getName(), user, in);
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
			Log.info("Stored attachment '" + title + "/" + filename + "'");
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
		if (ReadOnlyManager.isReadOnly()) return false;
		WikiPage page = new WikiPage(engine, title);
		WikiContext context = new WikiContext(this.engine, request,
				this.engine.getPage(title));

		AuthorizationManager authmgr = engine.getAuthorizationManager();
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (authmgr) {
			PagePermission pp = PermissionFactory.getPagePermission(page,
					"edit");
			try {
				return authmgr.checkPermission(context.getWikiSession(), pp);
			}
			catch (StackOverflowError e) {
				// happens with very large articles
				Log.severe("StackOverflowError while checking permissions on article '" + title + "': " + e.getMessage());
				return false;
			}
		}
	}

	@Override
	public boolean userCanViewArticle(String title, HttpServletRequest request) {
		WikiPage page = new WikiPage(engine, title);
		WikiContext context = new WikiContext(this.engine, request, this.engine
				.getPage(title));

		AuthorizationManager authmgr = engine.getAuthorizationManager();
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (authmgr) {
			PagePermission pp = PermissionFactory.getPagePermission(page, "view");
			return authmgr.checkPermission(context.getWikiSession(), pp);
		}
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
			WikiPage page = engine.getPage(title);
			WikiContext context = engine.createContext(user.getRequest(), WikiContext.EDIT);
			page.setAuthor(context.getCurrentUser().getName());
			String changeNote = user.getParameter(Attributes.CHANGE_NOTE);
			if (changeNote != null) {
				page.setAttribute(WikiPage.CHANGENOTE, changeNote);
			}
			context.setPage(page);
			context.setRealPage(page);

			engine.saveText(context, content);
			return true;
		}
		catch (WikiException e) {
			Log.severe("Failed to write article changes to wiki persistence", e);
			return false;
		}
	}
}
