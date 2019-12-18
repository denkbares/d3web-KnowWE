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
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.mail.MessagingException;
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
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.SessionMonitor;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.permissions.PagePermission;
import org.apache.wiki.auth.permissions.PermissionFactory;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.providers.CachingAttachmentProvider;
import org.apache.wiki.providers.CachingProvider;
import org.apache.wiki.providers.GitVersioningFileProvider;
import org.apache.wiki.providers.KnowWEAttachmentProvider;
import org.apache.wiki.providers.WikiAttachmentProvider;
import org.apache.wiki.providers.WikiPageProvider;
import org.apache.wiki.util.MailUtil;
import org.apache.wiki.util.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Pair;
import com.denkbares.utils.Streams;
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

	public static final String LINK_PREFIX = "Wiki.jsp?page=";
	private static final Map<String, List<WikiAttachment>> zipAttachmentCache =
			Collections.synchronizedMap(new HashMap<>());
	private static final int skipAfter = 10;
	private static final Pattern ZIP_PATTERN = Pattern.compile("^([^/]+/[^/]+\\.zip)/(.+$)");
	private static int skipCount = 0;
	private final ServletContext context;
	private final WikiEngine engine;

	public JSPWikiConnector(WikiEngine eng) {
		this.context = eng.getServletContext();
		this.engine = eng;
		initPageLocking();
	}

	public static String toPath(String articleTitle, String fileName) {
		return articleTitle + "/" + fileName;
	}

	public WikiEngine getEngine() {
		return engine;
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
	public @Nullable String getTemplate() {
		return getEngine().getTemplateDir();
	}

	@Override
	public String createArticle(String title, String author, String content) {
		return createArticle(title, author, content, true, true);
	}

	public String createArticle(String title, String author, String content, boolean updateReferences, boolean reindex) {

		if (title.contains("+")) {
			throw new IllegalArgumentException("Character + (plus) not allowed in article title: " + title);
		}

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
			Log.severe("Exception while retrieving articles.", e);
		}
		if (pages == null) return null;

		for (WikiPage wikiPage : pages) {
			String pageContent = null;
			try {
				pageContent = pageManager.getPageText(wikiPage.getName(),
						wikiPage.getVersion());
			}
			catch (ProviderException e) {
				Log.severe("Exception while retrieving articles.", e);
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
			Log.severe("Exception while retrieving users.", e);
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
		return collectWikiAttachments(true);
	}

	@Override
	public Collection<WikiAttachment> getRootAttachments() throws IOException {
		return collectWikiAttachments(false);
	}

	@NotNull
	public Collection<WikiAttachment> collectWikiAttachments(boolean includeZipEntries) throws IOException {
		try {
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			Collection<?> attachments = attachmentManager.getAllAttachments();
			Collection<WikiAttachment> wikiAttachments = new ArrayList<>(attachments.size());
			for (Object o : attachments) {
				if (o instanceof Attachment) {
					Attachment att = (Attachment) o;
					wikiAttachments.add(new JSPWikiAttachment(att, attachmentManager));
					if (includeZipEntries) wikiAttachments.addAll(getZipEntryAttachments(att));
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

	private Pair<String, String> getActualPathAndEntry(String path) {
		Matcher matcher = ZIP_PATTERN.matcher(path);
		String entry = null;
		String actualPath = path;
		if (matcher.find()) {
			actualPath = matcher.group(1);
			entry = matcher.group(2);
		}
		return new Pair<>(actualPath, entry);
	}

	@Override
	public WikiAttachment getAttachment(String path, int version) throws IOException {
		try {
			Pair<String, String> actualPathAndEntry = getActualPathAndEntry(path);
			String actualPath = actualPathAndEntry.getA();
			String entry = actualPathAndEntry.getB();

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
		return collectWikiAttachments(title, true);
	}

	@Override
	public List<WikiAttachment> getRootAttachments(String title) throws IOException {
		return collectWikiAttachments(title, false);
	}

	@NotNull
	public List<WikiAttachment> collectWikiAttachments(String title, boolean includeZipEntries) throws IOException {
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

			List<WikiAttachment> attachmentList = new ArrayList<>();
			for (Object attachment : attachmentManager.listAttachments(page)) {
				attachmentList.add(new JSPWikiAttachment((Attachment) attachment, attachmentManager));
				if (includeZipEntries) attachmentList.addAll(getZipEntryAttachments((Attachment) attachment));
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
		return engine.getWikiProperties().getProperty("jspwiki.baseURL", engine.getBaseURL());
	}

	@Override
	public String getApplicationName() {
		return engine.getApplicationName();
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
		// caused a NullPointer on first KnowWE startup
		try {
			if ((this.engine == null) || (this.engine.getPage(title) == null)) return null;
		}
		catch (NullPointerException e) {
			return null;
		}

		String pageText;
		if (title.contains("/")) {
			// we have an attached article
			try {
				WikiAttachment attachment = getAttachment(title, version);
				if (attachment != null) {
					return Streams.getTextAndClose(attachment.getInputStream());
				}
			}
			catch (IOException e) {
				Log.warning("Could not read attachment content from: " + title);
			}
		}
		try {
			pageText = engine.getPageManager().getPageText(title, version);
		}
		catch (ProviderException e) {
			Log.warning("Could not obtain page text from PageManager for: " + title);
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
				// can happen in JSPWiki, if OLD was cleaned up manually
				WikiPage currentVersion = this.engine.getPage(title);
				versionHistory = Collections.singletonList(currentVersion);
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
			Log.severe("Exception while retrieving change notes.", e);
		}

		return null;
	}

	@Override
	@Nullable
	public String getWikiProperty(String property) {
		return (String) engine.getWikiProperties().get(property);
	}

	@Override
	public void openPageTransaction(String user) {
		WikiPageProvider realProvider = getRealPageProvider();
		if (realProvider instanceof GitVersioningFileProvider) {
			((GitVersioningFileProvider) realProvider).openCommit(user);
		}
	}

	@Override
	public void commitPageTransaction(String user, String commitMsg) {
		WikiPageProvider realProvider = getRealPageProvider();
		if (realProvider instanceof GitVersioningFileProvider) {
			((GitVersioningFileProvider) realProvider).commit(user, commitMsg);
		}
	}

	@Override
	public void rollbackPageTransaction(String user) {
		WikiPageProvider realProvider = getRealPageProvider();
		if (realProvider instanceof GitVersioningFileProvider) {
			((GitVersioningFileProvider) realProvider).rollback(user);
		}
	}

	public WikiPageProvider getRealPageProvider() {
		WikiPageProvider realProvider;
		if (this.engine.getPageManager().getProvider() instanceof CachingProvider) {
			realProvider = ((CachingProvider) engine.getPageManager().getProvider()).getRealProvider();
		}
		else {
			realProvider = this.engine.getPageManager().getProvider();
		}
		return realProvider;
	}

	@Override
	public boolean hasRollbackPageProvider() {
		return getRealPageProvider() instanceof GitVersioningFileProvider;
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
			String path = toPath(title, filename);
			Log.info("Stored attachment '" + path + "'");
			if (!wasLocked) unlockArticle(title, user);
//			return getAttachment(toPath(title, filename));
			return new JSPWikiAttachment(attachment, attachmentManager);
		}
		catch (ProviderException e) {
			throw new IOException("could not store attachment", e);
		}
	}

	@Override
	public WikiAttachment storeAttachment(String title, String filename, String user, InputStream stream, boolean versioning) throws IOException {
		if (!versioning) {
			// we just delete the current attachment version and don't fire delete-event
			// there will be a stored-event
			deleteAttachment(title, filename, user, false);
		}
		return storeAttachment(title, filename, user, stream);
	}

	@Override
	public void deleteAttachment(String title, String fileName, String user) throws IOException {
		deleteAttachment(title, fileName, user, true);
	}

	@Override
	public void deleteArticle(String title, String user) throws IOException {
		PageManager pageManager = this.engine.getPageManager();
		WikiPage page = new WikiPage(this.engine, title);
		page.setAuthor(user);
		try {
			boolean wasLocked = isArticleLocked(title);
			if (!wasLocked) lockArticle(title, user);

			pageManager.deletePage(page);

			if (!wasLocked) unlockArticle(title, user);
		}
		catch (ProviderException e) {
			Log.severe("Can't delete article " + title, e);
			throw new IOException(e);
		}
	}

	private void deleteAttachment(String title, String fileName, String user, boolean fireDeleteEvent) throws IOException {
		String path = toPath(title, fileName);
		Pair<String, String> actualPathAndEntry = getActualPathAndEntry(path);
		if (actualPathAndEntry.getB() != null) {
			throw new IOException("Unable to delete zip entry (" + path
					+ ") in zip attachment. Try to delete attachment instead.");
		}
		try {
			boolean wasLocked = isArticleLocked(title);
			if (!wasLocked) lockArticle(title, user);
			AttachmentManager attachmentManager = this.engine.getAttachmentManager();
			Attachment attachment = attachmentManager.getAttachmentInfo(path);

			if (attachment != null && !fireDeleteEvent) {
				// will cause the KnowWEAttachmentProvider to not fire a delete event
				// not pretty, but the JSPWiki API is not on our side here
				attachment.setAttribute(KnowWEAttachmentProvider.FIRE_DELETE_EVENT, "false");
			}

			if (attachment != null) {
				attachmentManager.deleteAttachment(attachment);
				Log.info("Deleted attachment '" + path + "'");
			}
			if (!wasLocked) unlockArticle(title, user);
		}
		catch (ProviderException e) {
			Log.severe("Can't delete attachment " + title, e);
			throw new IOException(e);
		}
	}

	@Override
	public String renamePage(String fromPage, String toPage, HttpServletRequest request) throws IOException {
		WikiContext context = new WikiContext(this.engine, request,
				this.engine.getPage(fromPage));
		try {
			return this.engine.renamePage(context, fromPage, toPage, true);
		}
		catch (WikiException e) {
			throw new IOException(e);
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
		return checkPermission(title, request, "edit");
	}

	@Override
	public boolean userCanViewArticle(String title, HttpServletRequest request) {
		return checkPermission(title, request, "view");
	}

	@Override
	public boolean userCanDeleteArticle(String title, HttpServletRequest request) {
		if (ReadOnlyManager.isReadOnly()) return false;
		return checkPermission(title, request, "delete");
	}

	private boolean checkPermission(String title, HttpServletRequest request, String permission) {
		WikiPage page = new WikiPage(engine, title);
		WikiContext context = new WikiContext(this.engine, request,
				this.engine.getPage(title));

		AuthorizationManager authmgr = engine.getAuthorizationManager();
		PagePermission pp = PermissionFactory.getPagePermission(page,
				permission);
		try {
			return authmgr.checkPermission(context.getWikiSession(), pp);
		}
		catch (StackOverflowError e) {
			// happens with very large articles
			Log.severe("StackOverflowError while checking permissions on article '" + title + "': " + e.getMessage());
			return false;
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
			// if PageProvider throws exception, page will be null and can't be saved
			if (page == null) return false;
			WikiContext context = engine.createContext(user.getRequest(), WikiContext.EDIT);
			if (context.getCurrentUser() != null) {
				page.setAuthor(context.getCurrentUser().getName());
			}
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

	@Override
	public void sendMail(String to, String subject, String content) throws IOException {
		//
		Set<String> resolvedAddrs = resolveRecipients(to);
		if (resolvedAddrs.isEmpty()) {
			Log.info("Aborting to send mail since no recipient was resolved");
			return;
		}

		// perform send
		String resolvedTo = String.join(",", resolvedAddrs);
		try {
			Log.info("Sending mail to '" + resolvedTo + "' with subject '" + subject + "'");
			MailUtil.sendMessage(this.engine.getWikiProperties(), resolvedTo, subject, content);
		}
		catch (MessagingException e) {
			// wrap exception since WikiConnector interface is not aware of JavaMail specific MessagingException
			throw new IOException("Could not send mail", e);
		}
	}

	@Override
	public void sendMultipartMail(String to, String subject, String plainContent, String htmlContent, Map<String, URL> imageUrlsByCid) throws IOException {
		//
		Set<String> resolvedAddrs = resolveRecipients(to);
		if (resolvedAddrs.isEmpty()) {
			Log.info("Aborting to send mail since no recipient was resolved");
			return;
		}

		// perform send
		String resolvedTo = String.join(",", resolvedAddrs);
		try {
			Log.info("Sending multipart-mail to '" + resolvedTo + "' with subject '" + subject + "'");
			MailUtil.sendMultiPartMessage(this.engine.getWikiProperties(), resolvedTo, subject, plainContent, htmlContent, imageUrlsByCid);
		}
		catch (MessagingException e) {
			// wrap exception since WikiConnector interface is not aware of JavaMail specific MessagingException
			throw new IOException("Could not send multipart-mail", e);
		}
	}

	@NotNull
	private Set<String> resolveRecipients(String to) {
		// resolve names of users to their mail addresses since any recipient can be given as a regular email address
		// or by the recipient's full-name, wiki-name or login-name
		UserDatabase userDatabase = this.engine.getUserManager().getUserDatabase();
		String[] addrArr = to.split(",");
		Set<String> resolvedAddrs = new HashSet<>();
		for (String addr : addrArr) {
			// trim whitespace
			addr = Strings.trim(addr);
			if (addr.isEmpty()) continue;
			if (addr.contains("@")) {
				// just an email address (simple or with phrase)
				resolvedAddrs.add(addr);
			}
			else {
				// .. otherwise we need to perform lookup
				try {
					UserProfile userProfile = userDatabase.find(addr);
					if (Strings.isNotBlank(userProfile.getEmail())) {
						resolvedAddrs.add(userProfile.getEmail());
					}
					else {
						Log.warning("Ignoring mail recipient since it's user-profile doesn't contain an email address: " + addr);
					}
				}
				catch (NoSuchPrincipalException e) {
					// we just skip by doing nothing except logging
					Log.warning("Ignoring mail recipient since it's address is neither a mail-address nor a Wiki user with that login-name, full-name or wiki-name: " + addr);
				}
			}
		}
		return resolvedAddrs;
	}
}
