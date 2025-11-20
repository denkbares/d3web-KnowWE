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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.WikiSession;
import org.apache.wiki.api.core.Command;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.core.Session;
import org.apache.wiki.api.exceptions.FilterException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.exceptions.WikiException;
import org.apache.wiki.api.providers.AttachmentProvider;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.SessionMonitor;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.authorize.GroupManager;
import org.apache.wiki.auth.permissions.PagePermission;
import org.apache.wiki.auth.permissions.PermissionFactory;
import org.apache.wiki.auth.permissions.WikiPermission;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.cache.CachingManager;
import org.apache.wiki.cache.EhcacheCachingManager;
import org.apache.wiki.content.PageRenamer;
import org.apache.wiki.event.WikiEngineEvent;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.pages.PageLock;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.providers.CachingAttachmentProvider;
import org.apache.wiki.providers.CachingProvider;
import org.apache.wiki.providers.GitVersioningFileProvider;
import org.apache.wiki.providers.KnowWEAttachmentProvider;
import org.apache.wiki.references.ReferenceManager;
import org.apache.wiki.render.RenderingManager;
import org.apache.wiki.search.SearchManager;
import org.apache.wiki.ui.CommandResolver;
import org.apache.wiki.util.MailUtil;
import org.apache.wiki.util.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import com.denkbares.utils.Streams;
import de.knowwe.core.Environment;
import de.knowwe.core.action.ActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.user.AuthenticationManager;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiAttachmentInfo;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.core.wikiConnector.WikiPageInfo;
import de.knowwe.jspwiki.readOnly.ReadOnlyManager;

import static org.apache.wiki.cache.CachingManager.*;

/**
 * For code documentation look at the WikiConnector interface definition
 *
 * @author Jochen
 */
public class JSPWikiConnector implements WikiConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(JSPWikiConnector.class);

	private static final Map<String, List<WikiAttachment>> zipAttachmentCache =
			Collections.synchronizedMap(new HashMap<>());
	private static final int skipAfter = 10;
	private static final Pattern ZIP_PATTERN = Pattern.compile("^([^/]+/[^/]+\\.zip)/(.+$)");
	private static int skipCount = 0;
	private final ServletContext context;
	private final Engine engine;

	public JSPWikiConnector(Engine eng) {
		this.context = eng.getServletContext();
		this.engine = eng;
		initPageLocking();
	}

	public static String toPath(String articleTitle, String fileName) {
		return articleTitle + "/" + fileName;
	}

	public Engine getEngine() {
		return engine;
	}

	public PageManager getPageManager() {
		return getEngine().getManager(PageManager.class);
	}

	/**
	 * We need this method, because there is the possibility of an IllegalThreadStateException while initializing the
	 * lock reaper of the PageManager, if the first call of lockPage is done in parallel by two threads. To avoid this,
	 * we call this directly at startup in a safe way.
	 *
	 * @created 26.10.2012
	 */
	private void initPageLocking() {
		PageManager mgr = engine.getManager(PageManager.class);
		Page page = new WikiPage(getEngine(), "Dummy");
		PageLock lock = mgr.lockPage(page, "Dummy");
		mgr.unlockPage(lock);
	}

	@Override
	public @Nullable String getTemplate() {
		return getEngine().getTemplateDir();
	}

	@Override
	public String createArticle(String title, String author, String content) {
		return createArticle(title, author, content, null);
	}

	@Override
	public String createArticle(String title, String author, String content, @Nullable String changeNote) {

		if (title.contains("+")) {
			throw new IllegalArgumentException("Character + (plus) not allowed in article title: " + title);
		}

		WikiPage wp = new WikiPage(getEngine(), title);
		if (changeNote != null) {
			wp.setAttribute(WikiPage.CHANGENOTE, changeNote);
		}

		try {
			updateReferences(wp, content);
			wp.setAuthor(author);

			/*
			 * This post data normalization is what JSPWiki also does on normal page saves.
			 * Then jspwiki makes an async process initialed via the saveText()-Method.
			 * As we need/must not have the async process step, we just make the normalization here
			 * and call putPageText() directly.
			 */
			final String proposedText = TextUtil.normalizePostData(content);

			getPageManager().putPageText(wp, proposedText);
			reindex(title);
		}
		catch (ProviderException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
		catch (NullPointerException e) {
			// should only happen on wiki initialization
			return null;
		}
		// create article with the new content
		Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB).registerArticle(title, content);
		return getPageManager().getPureText(wp);
	}

	public void reindex(String title) {
		getSearchManager().reindexPage(getWikiPage(title));
	}

	private SearchManager getSearchManager() {
		return getEngine().getManager(SearchManager.class);
	}

	private ReferenceManager getReferenceManager() {
		return getEngine().getManager(ReferenceManager.class);
	}

	private WikiPage getWikiPage(String title) {
		return (WikiPage) getPageManager().getPage(title);
	}

	private void updateReferences(WikiPage page, String content) {
		getReferenceManager().updateReferences(page.getName(), getReferenceManager().scanWikiLinks(page, content));
	}

	@Override
	public boolean doesArticleExist(String title) {
		try {
			return getPageManager().pageExists(title);
		}
		catch (ProviderException e) {
			LOGGER.error("Exception while checking page status", e);
			return false;
		}
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

	@Override
	public Map<String, String> getAllArticles(String web) {
		Map<String, String> result = new HashMap<>();
		Collection<Page> pages;
		PageManager pageManager = getPageManager();
		try {
			pages = pageManager.getProvider().getAllPages();
		}
		catch (ProviderException e) {
			LOGGER.error("Exception while retrieving articles.", e);
			return null;
		}

		for (Page wikiPage : pages) {
			String pageContent = null;
			try {
				pageContent = pageManager.getPageText(wikiPage.getName(),
						wikiPage.getVersion());
			}
			catch (ProviderException e) {
				LOGGER.error("Exception while retrieving articles.", e);
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
			Principal[] princ = getUserManager().listWikiNames();
			users = new String[princ.length];
			for (int i = 0; i < princ.length; i++) {
				users[i] = getUserManager().getUserDatabase().findByWikiName(
						princ[i].getName()).getFullname();
			}
		}
		catch (WikiSecurityException e) {
			LOGGER.error("Exception while retrieving users.", e);
		}

		return users;
	}

	public UserManager getUserManager() {
		return engine.getManager(UserManager.class);
	}

	public GroupManager getGroupManager() {
		return engine.getManager(GroupManager.class);
	}

	@Override
	public String getApplicationRootPath() {
		ServletContext servletContext = getServletContext();
		String realPath = servletContext.getRealPath("");
		String rootPath;
		if (realPath != null) {
			return realPath.replaceAll("/+$", "");
		}
		else {
			return new File("").getAbsolutePath();
		}
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
			AttachmentManager attachmentManager = getAttachmentManager();
			Collection<?> attachments = attachmentManager.getAllAttachments();
			Collection<WikiAttachment> wikiAttachments = new ArrayList<>(attachments.size());
			for (Object o : attachments) {
				if (o instanceof Attachment att) {
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

	public AttachmentManager getAttachmentManager() {
		return this.engine.getManager(AttachmentManager.class);
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

			AttachmentManager attachmentManager = getAttachmentManager();
			Attachment attachment = (Attachment) attachmentManager.getAttachmentInfo(actualPath, version);

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
			AttachmentManager attachmentManager = getAttachmentManager();
			try {
				Attachment attachment = (Attachment) attachmentManager.getAttachmentInfo(path);
				if (attachment == null) {
					remove.add(path);
				}
			}
			catch (ProviderException e) {
				LOGGER.warn("Exception while cleaning zip cache", e);
			}
		}
		remove.forEach(zipAttachmentCache.keySet()::remove);
	}

	private List<WikiAttachment> getZipEntryAttachments(Attachment attachment) throws IOException, ProviderException {
		if (!attachment.getFileName().endsWith(".zip")) return Collections.emptyList();
		List<WikiAttachment> zipEntryAttachments = zipAttachmentCache.get(attachment.getName());
		AttachmentManager attachmentManager = getAttachmentManager();
		if (attachment.getVersion() == PageProvider.LATEST_VERSION) {
			// little hack for JSPWiki 2.8.4 not always providing a correct
			// version number and we need it here.
			AttachmentProvider currentProvider = attachmentManager.getCurrentProvider();
			// there only are two possible providers, the
			// BasicAttachmentProvider and the CachingAttachmentProvider
			// the BasicAttachmentProvider has a correct version number
			if (currentProvider instanceof CachingAttachmentProvider) {
				currentProvider = ((CachingAttachmentProvider) currentProvider).getRealProvider();
			}
			Attachment attachmentInfo = (Attachment) currentProvider.getAttachmentInfo(
					new WikiPage(getEngine(), attachment.getParentName()),
					attachment.getFileName(), PageProvider.LATEST_VERSION);
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
			WikiAttachment att = KnowWEUtils.getAttachment(attachment.getParentName(), attachment.getFileName());
			File attachmentFile = att.asFile();
			if (!attachmentFile.exists()) {
				throw new IllegalStateException("Attachment file not found: " + attachmentFile.getAbsolutePath());
			}
			try (ZipFile zipFile = new ZipFile(attachmentFile.getPath())) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					zipEntryAttachments.add(new JSPWikiZipAttachment(entry.getName(), attachment,
							attachmentManager));
				}
			}
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
			AttachmentManager attachmentManager = getAttachmentManager();
			Page page = getPageManager().getPage(title);
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
		return getPageManager().getPage(title, version).getAuthor();
	}

	@Override
	public String getBaseUrl() {
		return TextUtil.getStringProperty(engine.getWikiProperties(), "jspwiki.baseURL", engine.getBaseURL());
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
		return getPageManager().getPage(title, version).getLastModified();
	}

	@Override
	public Locale getLocale(HttpServletRequest request) {
		WikiContext wikiContext = new WikiContext(getEngine(), request, getWikiPage("Main"));
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
		Page page = getPageManager().getPage(title);
		if (page == null) {
			LOGGER.warn("Trying to get version of page that does not exist: {}", title);
			return 0;
		}
		return page.getVersion();
	}

	@Override
	public String getArticleText(String title) {
		return getArticleText(title, -1);
	}

	@Override
	public String getArticleText(String title, int version) {
		String pageText;
		if (title.contains("/")) {
			// we have an attached article
			try {
				WikiAttachment attachment = getAttachment(title, version);
				if (attachment != null) {
					return Article.cleanupText(Streams.getTextAndClose(attachment.getInputStream()));
				}
			}
			catch (IOException e) {
				LOGGER.warn("Could not read attachment content from: {}", title);
			}
		}
		try {
			pageText = getPageManager().getPageText(title, version);
		}
		catch (ProviderException e) {
			LOGGER.warn("Could not obtain page text from PageManager for: " + title);
			return null;
		}

		return Article.cleanupText(pageText);
	}

	@Override
	public void reinitializeWikiEngine() throws IOException {
		try {
			clearJSPWikiCaches();
			reinitSearchManager();
			reinitReferenceManager();
		}
		catch (Exception e) {
			throw new IOException(e);
		}

		WikiEventManager.fireEvent(engine, new WikiEngineEvent(engine, WikiEngineEvent.INITIALIZED));
	}

	private void reinitReferenceManager() throws WikiException {
		if (engine instanceof WikiEngine wikiEngine) {
			wikiEngine.initReferenceManager(true);
		}
		else {
			String message = "Unknown wiki engine: " + engine;
			LOGGER.error(message);
			throw new IllegalStateException(message);
		}
	}

	private void reinitSearchManager() throws FilterException {
		engine.getManager(SearchManager.class).initialize(engine, engine.getWikiProperties());
	}

	private void clearJSPWikiCaches() throws WikiException {

		// we clear the ehcache
		CachingManager cachingManager = engine.getManager(CachingManager.class);
		if (cachingManager instanceof EhcacheCachingManager ehManager) {
			List<String> cacheNames = List.of(CACHE_ATTACHMENTS, CACHE_ATTACHMENTS_COLLECTION, CACHE_ATTACHMENTS_DYNAMIC, CACHE_PAGES, CACHE_PAGES_TEXT, CACHE_PAGES_HISTORY, CACHE_DOCUMENTS);
			cacheNames.forEach(cache -> cachingManager.shutdown());
			ehManager.initialize(engine, engine.getWikiProperties());
		}

		// we re-initialize the CachingPageProvider
		PageManager pageManager = engine.getManager(PageManager.class);
		PageProvider provider = pageManager.getProvider();
		try {
			provider.initialize(engine, engine.getWikiProperties());
		}
		catch (IOException e) {
			LOGGER.error("Error on re-initializing CachingPageProvider.");
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<WikiPageInfo> getArticleHistory(String title) {
		List<Page> versionHistory = getPageManager().getVersionHistory(title);
		if (versionHistory == null) return Collections.emptyList();
		if (versionHistory.isEmpty()) {
			// can happen in JSPWiki, if OLD was cleaned up manually
			Page currentVersion = getPageManager().getPage(title);
			versionHistory = Collections.singletonList(currentVersion);
		}
		return versionHistory.stream()
				.map(page -> new WikiPageInfo(page.getName(), page.getAuthor(), page.getVersion(), page.getLastModified(), page.getAttribute(WikiPage.CHANGENOTE)))
				.collect(Collectors.toList());
	}

	@Override
	public List<WikiAttachmentInfo> getAttachmentHistory(String name) throws IOException {
		try {
			return getAttachmentManager().getVersionHistory(name).stream()
					.map(page -> new WikiAttachmentInfo(page.getName(), page.getAuthor(), page.getVersion(),
							page.getLastModified()))
					.collect(Collectors.toList());
		}
		catch (ProviderException e) {
			throw new IOException("Cannot access attachment history of '" + name + "' due to provider error", e);
		}
	}

	@Override
	@Nullable
	public String getChangeNote(String title, int version) {
		try {
			Page pageInfo = getPageManager().getPageInfo(title, version);
			if (pageInfo == null) {
				return null;
			}
			return pageInfo.getAttribute(WikiPage.CHANGENOTE);
		}
		catch (ProviderException e) {
			LOGGER.error("Exception while retrieving change notes.", e);
		}
		return null;
	}

	@Override
	@Nullable
	public String getWikiProperty(String property) {
		return (String) engine.getWikiProperties().get(property);
	}

	@Override
	public @NotNull Properties getWikiProperties() {
		return getEngine().getWikiProperties();
	}

	@Override
	public void openPageTransaction(String user) {
		PageProvider realProvider = getRealPageProvider();
		if (realProvider instanceof GitVersioningFileProvider) {
			((GitVersioningFileProvider) realProvider).openCommit(user);
		}
	}

	@Override
	public void commitPageTransaction(String user, String commitMsg) {
		PageProvider realProvider = getRealPageProvider();
		if (realProvider instanceof GitVersioningFileProvider) {
			((GitVersioningFileProvider) realProvider).commit(user, commitMsg);
		}
	}

	@Override
	public void rollbackPageTransaction(String user) {
		PageProvider realProvider = getRealPageProvider();
		if (realProvider instanceof GitVersioningFileProvider) {
			((GitVersioningFileProvider) realProvider).rollback(user);
		}
	}

	public PageProvider getRealPageProvider() {
		PageProvider realProvider;
		if (getPageManager().getProvider() instanceof CachingProvider) {
			realProvider = ((CachingProvider) getPageManager().getProvider()).getRealProvider();
		}
		else {
			realProvider = getPageManager().getProvider();
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
		WikiPage page = new WikiPage(getEngine(), title);
		return getPageManager().getCurrentLock(page) != null;
	}

	/**
	 * Checks if the current page has been locked by the current user. Returns TRUE if yes, FALSE otherwise.
	 *
	 * @param title the title of the article to check
	 * @param user  the user to check for
	 */
	@Override
	public boolean isArticleLockedCurrentUser(String title, String user) {
		WikiPage page = new WikiPage(getEngine(), title);
		PageLock lock = getPageManager().getCurrentLock(page);
		return lock != null && lock.getLocker().equals(user);
	}

	@Override
	public boolean lockArticle(String title, String user) {
		WikiPage page = new WikiPage(getEngine(), title);
		PageLock lock = getPageManager().lockPage(page, user);
		return lock != null;
	}

	@Override
	public String normalizeString(String string) {
		return TextUtil.normalizePostData(string);
	}

	@Override
	public String renderWikiSyntax(String content, HttpServletRequest request) {
		try {
			Command command = getEngine().getManager(CommandResolver.class).findCommand(request, WikiContext.VIEW);
			WikiContext context = new WikiContext(engine, request, command);
			content = getEngine().getManager(RenderingManager.class).textToHTML(context, content);
		}
		catch (Exception e) {
			LOGGER.error("Unable to render wiki syntax", e);
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
			AttachmentManager attachmentManager = getAttachmentManager();

			Attachment attachment = new Attachment(getEngine(), title, filename);
			attachment.setAuthor(user);
			attachmentManager.storeAttachment(attachment, stream);
			String path = toPath(title, filename);
			LOGGER.info("Stored attachment '" + path + "'");
			if (!wasLocked) unlockArticle(title, user);
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
		WikiPage page = new WikiPage(getEngine(), title);
		page.setAuthor(user);
		try {
			boolean wasLocked = isArticleLocked(title);
			if (!wasLocked) lockArticle(title, user);

			getPageManager().deletePage(page);

			if (!wasLocked) unlockArticle(title, user);
		}
		catch (ProviderException e) {
			LOGGER.error("Can't delete article " + title, e);
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
			AttachmentManager attachmentManager = getAttachmentManager();
			Attachment attachment = (Attachment) attachmentManager.getAttachmentInfo(path);

			if (attachment != null && !fireDeleteEvent) {
				// will cause the KnowWEAttachmentProvider to not fire a delete event
				// not pretty, but the JSPWiki API is not on our side here
				attachment.setAttribute(KnowWEAttachmentProvider.FIRE_DELETE_EVENT, "false");
			}

			if (attachment != null) {
				attachmentManager.deleteAttachment(attachment);
				LOGGER.info("Deleted attachment '" + path + "'");
			}
			if (!wasLocked) unlockArticle(title, user);
		}
		catch (ProviderException e) {
			LOGGER.error("Can't delete attachment " + title, e);
			throw new IOException(e);
		}
	}

	@Override
	public String renamePage(String fromPage, String toPage, HttpServletRequest request) throws IOException {
		WikiContext context = new WikiContext(getEngine(), request, getPageManager().getPage(fromPage));
		try {
			return getEngine().getManager(PageRenamer.class).renamePage(context, fromPage, toPage, true);
		}
		catch (WikiException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void unlockArticle(String title, String user) {
		WikiPage page = new WikiPage(getEngine(), title);

		PageManager mgr = getPageManager();
		if (isArticleLocked(title)) {
			if (user == null) {
				PageLock lock = mgr.getCurrentLock(page);
				mgr.unlockPage(lock);
			}
			else {
				for (PageLock other : mgr.getActiveLocks()) {
					if (other.getLocker().equals(user)) {
						mgr.unlockPage(other);
					}
				}
			}
		}
	}

	@Override
	public boolean userCanEditArticle(String title, UserContext context) {
		if (KnowWEUtils.isAttachmentArticle(title)) return false;
		if (ReadOnlyManager.isReadOnly()) return false;
		return checkPagePermission(title, context, "edit");
	}

	@Override
	public boolean userCanUploadAttachment(String title, UserContext context) {
		if (KnowWEUtils.isAttachmentArticle(title)) return false;
		if (ReadOnlyManager.isReadOnly()) return false;
		return checkPagePermission(title, context, "upload");
	}

	@Override
	public boolean userCanViewArticle(String title, UserContext context) {
		return checkPagePermission(title, context, "view");
	}

	@Override
	public boolean userCanDeleteArticle(String title, UserContext context) {
		if (KnowWEUtils.isAttachmentArticle(title)) return false;
		if (ReadOnlyManager.isReadOnly()) return false;
		return checkPagePermission(title, context, "delete");
	}

	@Override
	public boolean userCanCreateArticles(UserContext context) {
		if (ReadOnlyManager.isReadOnly()) return false;
		return checkWikiPermission(context, "createPages");
	}

	private boolean checkPagePermission(String title, UserContext context, String permission) {
		WikiPage page = new WikiPage(getEngine(), title);
		Session wikiSession = getWikiSession(getEngine(), context);

		PagePermission pp = PermissionFactory.getPagePermission(page, permission);
		try {
			return getAuthorizationManager().checkPermission(wikiSession, pp);
		}
		catch (StackOverflowError e) {
			// happens with very large articles
			LOGGER.error("StackOverflowError while checking permissions on article '" + title + "': " + e.getMessage());
			return false;
		}
	}

	public static Session getWikiSession(final Engine engine, final UserContext context) {
		HttpSession session = context.getSession();
		if (session == null) {
			LOGGER.debug("Looking up WikiSession for NULL HttpRequest: returning guestSession()");
			return WikiSession.guestSession(engine);
		}

		// Look for a WikiSession associated with the user's Http Session and create one if it isn't there yet.
		final SessionMonitor monitor = SessionMonitor.getInstance(engine);
		return monitor.find(session);
	}

	public AuthorizationManager getAuthorizationManager() {
		return getEngine().getManager(AuthorizationManager.class);
	}

	private boolean checkWikiPermission(UserContext context, String permissionsCSV) {
		WikiPermission wikiPermission = new WikiPermission(getEngine().getApplicationName(), permissionsCSV);
		Session wikiSession = getWikiSession(getEngine(), context);
		return getAuthorizationManager().checkPermission(wikiSession, wikiPermission);
	}

	@Override
	public boolean userIsMemberOfGroup(String groupname, UserContext userContext) {

		// which article is not relevant
		String articleName = "Main";
		WikiContext context = new WikiContext(getEngine(), userContext.getRequest(), getPageManager().getPage(articleName));

		Principal[] principals = context.getWikiSession().getRoles();

		for (Principal principal : principals) {
			if (principal.getName().equals(groupname)) return true;
		}

		return false;
	}

	@Override
	public List<String> getAllSubWikiFolders() {
		return null;
	}

	@Override
	public String getUserMail(UserContext user) {
		// TODO : find some way to retrieve the mail address from the userdatabase.xml - Seems to be very hard task :(
		if (user instanceof ActionContext actionContext) {
			// not working either
			AuthenticationManager manager = actionContext.getManager();
			return manager.getMailAddress();
		}
		else {
			LOGGER.error("Failed to obtain user profile for user: " + user);
			return null;
		}
	}

	@Override
	public boolean writeArticleToWikiPersistence(String title, String content, UserContext user, String changeNote) {
		try {
			Page page = getPageManager().getPage(title);
			// if PageProvider throws exception, page will be null and can't be saved
			if (page == null) return false;
			WikiContext context = new WikiContext(getEngine(), user.getRequest(), getPageManager().getPage(title));
			if (context.getCurrentUser() != null) {
				page.setAuthor(context.getCurrentUser().getName());
			}
			if (changeNote != null) {
				page.setAttribute(WikiPage.CHANGENOTE, changeNote);
			}
			context.setPage(page);
			context.setRealPage(page);

			getPageManager().saveText(context, content);
			return true;
		}
		catch (WikiException e) {
			LOGGER.error("Failed to write article changes to wiki persistence", e);
			return false;
		}
	}

	@Override
	public void sendMail(String to, String subject, String content) throws IOException {
		Set<String> resolvedAddresses = resolveRecipients(to);
		if (resolvedAddresses.isEmpty()) {
			LOGGER.info("Aborting to send mail since no recipient was resolved");
			return;
		}

		// perform send
		String resolvedTo = String.join(",", resolvedAddresses);
		try {
			LOGGER.info("Sending mail to '" + resolvedTo + "' with subject '" + subject + "'");
			MailUtil.sendMessage(this.engine.getWikiProperties(), resolvedTo, subject, content);
		}
		catch (MessagingException e) {
			// wrap exception since WikiConnector interface is not aware of JavaMail specific MessagingException
			throw new IOException("Could not send mail", e);
		}
	}

	@Override
	public void sendMultipartMail(String to, String subject, String plainContent, String htmlContent, Map<String, URL> imageUrlsByCid) throws IOException {
		Set<String> resolvedAddresses = resolveRecipients(to);
		if (resolvedAddresses.isEmpty()) {
			LOGGER.info("Aborting to send mail since no recipient was resolved");
			return;
		}

		// perform send
		String resolvedTo = String.join(",", resolvedAddresses);
		try {
			LOGGER.info("Sending multipart-mail to '" + resolvedTo + "' with subject '" + subject + "'");
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
		UserDatabase userDatabase = getUserManager().getUserDatabase();
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
						LOGGER.warn("Ignoring mail recipient since it's user-profile doesn't contain an email address: " + addr);
					}
				}
				catch (NoSuchPrincipalException e) {
					// we just skip by doing nothing except logging
					LOGGER.warn("Ignoring mail recipient since it's address is neither a mail-address nor a Wiki user with that login-name, full-name or wiki-name: " + addr);
				}
			}
		}
		return resolvedAddrs;
	}

	@Override
	public String getAntiCsrfToken(UserContext context) {
		return getWikiSession(context).antiCsrfToken();
	}

	public Session getWikiSession(UserContext context) {
		return WikiSession.getWikiSession(getEngine(), context.getRequest());
	}
}
