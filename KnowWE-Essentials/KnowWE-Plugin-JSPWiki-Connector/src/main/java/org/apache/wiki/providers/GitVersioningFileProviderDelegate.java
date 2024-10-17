/*
 * Copyright (C) 2019 denkbares GmbH, Germany
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

package org.apache.wiki.providers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wiki.InternalWikiException;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.gitBridge.JspGitBridge;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.commentStrategy.ChangeNoteStrategy;
import org.apache.wiki.providers.commentStrategy.GitCommentStrategy;
import org.apache.wiki.structs.DefaultPageIdentifier;
import org.apache.wiki.structs.PageIdentifier;
import org.apache.wiki.structs.WikiPageProxy;
import org.apache.wiki.util.FileUtil;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.UserData;
import de.uniwue.d3web.gitConnector.impl.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.JGitBackedGitConnector;

/**
 * This Git delegate does the actual work on the underlying Git repository, albeit it does not check any file lock
 * mechanisms!
 * Do never use this delegate without the actual GitVersioningFileProvider, consider it as a 'cleaner' version of its
 * implementation.
 */
public class GitVersioningFileProviderDelegate extends AbstractFileProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitVersioningFileProviderDelegate.class);

	//TODO has to go
	private Repository repository;
	//user to set of paths
	Map<String, Set<String>> openCommits = new ConcurrentHashMap<>();
	private final Set<String> refreshCacheList = new HashSet<>();

	private Engine engine;
	//TODO has to go
	private JspGitBridge gitBridge;
	private GitConnector gitConnector;

	GitVersioningFileProviderDelegate() {
	}

	private GitCommentStrategy gitCommentStrategy;

	/**
	 * {@inheritDoc}
	 *
	 * @throws IOException
	 */
	@Override
	public void initialize(final Engine engine, final Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);
		this.engine = engine;

		String pageDir = properties.getProperty(GitProviderProperties.JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR, null);
		if (pageDir != null) {
			this.gitConnector = new CachingGitConnector(JGitBackedGitConnector.fromPath(pageDir));
		}

		this.gitBridge = new JspGitBridge(engine);
		this.gitBridge.init(properties);
		this.gitBridge.initAndVerifyGitRepository(properties);

		this.repository = this.gitBridge.getRepository();

		setGitCommentStrategy(properties);

		// we run the command to build up the commit graph. It considerably accelerates the reading of the commit history (e. g. git log)
		this.gitConnector.executeCommitGraph();
	}

	private void setGitCommentStrategy(Properties properties) {
		String commentStrategyClassName = TextUtil.getStringProperty(properties, GitProviderProperties.JSPWIKI_GIT_COMMENT_STRATEGY, "org.apache.wiki.providers.commentStrategy.ChangeNoteStrategy");
		GitCommentStrategy gitCommentStrategy;
		try {
			Class<?> commentStrategyClass = Class.forName(commentStrategyClassName);
			gitCommentStrategy = (GitCommentStrategy) commentStrategyClass.getConstructor()
					.newInstance(new Object[] {});
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
			   InvocationTargetException e) {
			LOGGER.error("Comment strategy not found " + commentStrategyClassName, e);
			gitCommentStrategy = new ChangeNoteStrategy();
		}
		 this.gitCommentStrategy=gitCommentStrategy;
	}

	public Repository getRepository() {
		return this.repository;
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningFileProviderDelegate.class.getSimpleName();
	}

	/**
	 * Does the actual work, can assume that all read/write locks are already existing
	 *
	 * @param page
	 * @param text
	 * @throws ProviderException
	 */
	@Override
	public void putPageText(Page page, String text) throws ProviderException {
		LOGGER.info("put page text for page: " + page.getName() + " and version: " + page.getVersion());
		final File changedFile = super.findPage(page.getName());
		final boolean addFile = !changedFile.exists();

		//check if there is no change! (aka nothing to cache)
		if (sameTextContent(text, changedFile)) return;
		//perform action on wiki (this also changes the filesystem)
		super.putPageText(page, text);

		putPageTextGit(page, addFile, changedFile);
	}

	private boolean sameTextContent(String text, File changedFile) {
		if (changedFile.exists() && text != null) {
			try {
				String currentText = Files.readString(changedFile.toPath());
				if (text.equals(currentText)) {
					return true;
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	private void putPageTextGit(Page page, boolean addFile, File changedFile) throws ProviderException {
		File file = super.findPage(page.getName());
		page.setSize(file.length());

		String comment = gitCommentStrategy.getComment(page, "");
		if (comment.isEmpty()) {
			if (addFile) {
				comment = "Added page";
			}
			else {
				comment = "-";
			}
		}

		if (this.openCommits.containsKey(page.getAuthor())) {
			if (addFile) {
				this.gitConnector.addPath(changedFile.getName());
			}
			this.openCommits.get(page.getAuthor()).add(changedFile.getName());
		}
		else {
			UserData userdata = getUserData(page.getAuthor(), comment);
			String commitHash = this.gitConnector.changePath(changedFile.toPath(), userdata);
			fireWikiEvent(GitVersioningWikiEvent.UPDATE, page.getAuthor(), List.of(page.getName()), commitHash);
		}
	}

	@Override
	public boolean pageExists(final String page, final int version) {
		//TODO i think there are no tests for this one...
		if (!pageExists(page)) {
			return false;
		}

		if (version == PageProvider.LATEST_VERSION) {
			return true;
		}

		final List<Page> versionHistory = getVersionHistory(page);
		return (version > 0 && version <= versionHistory.size());
	}

	@Override
	public Page getPageInfo(final String pageName, final int version) {
		PageIdentifier pageIdentifier = PageIdentifier.fromPagename(this.getFilesystemPath(), pageName, version);

		LOGGER.info("Get page for : " + pageIdentifier + " and version: " + pageIdentifier.version());

		if (!pageIdentifier.exists()) {
			return null;
		}

		String commitHash = this.gitConnector.commitHashForFileAndVersion(pageIdentifier.fileName(), pageIdentifier.version());
		if (commitHash == null) {
			//check if there is an open commit with that page => if so we can safely fetch from disk
			if(this.openCommits.values().stream().flatMap(it -> it.stream()).anyMatch(it -> it.equals(pageIdentifier.fileName()))){
				return getWikiPageFromFilesystem(pageIdentifier);
			}

			//not untracked not in git => no idea
			return null;
		}
		int realVersion = pageIdentifier.version();
		if (pageIdentifier.version() == LATEST_VERSION) {
			realVersion = this.gitConnector.numberOfCommitsForFile(pageIdentifier.fileName());
		}
		return createWikiPageFromCommitHash(pageIdentifier, commitHash, realVersion);
	}

	@Override
	public Collection<Page> getAllPages() {
		LOGGER.debug("Getting all pages...");

		final Map<String, Page> resultingPages = new HashMap<>();

		final File wikipagedir = new File(this.gitConnector.getGitDirectory());
		final File[] wikipages = wikipagedir.listFiles(new WikiFileFilter());

		if (wikipages == null) {
			LOGGER.error("Wikipages directory '" + this.gitConnector.getGitDirectory() + "' does not exist! Please check " + PROP_PAGEDIR + " in jspwiki.properties.");
			throw new InternalWikiException("Page directory does not exist");
		}
		for (final File file : wikipages) {
			PageIdentifier pageIdentifier = PageIdentifier.fromFile(this.getFilesystemPath(), file);

			//we obtain the pages from the filesystem and not from git!
			WikiPage page = getWikiPageFromFilesystem(pageIdentifier);
			resultingPages.put(pageIdentifier.accordingFile().getName(), page);
		}
		return resultingPages.values();
	}

	@NotNull
	private WikiPage getWikiPageFromFilesystem(PageIdentifier pageIdentifier) {

		WikiPageProxy page = new WikiPageProxy(engine, pageIdentifier.pageName());
		page.setHistoryProvider(this.gitConnector);

		File file = pageIdentifier.accordingFile();
		if (file != null && file.exists()) {
			page.setLastModified(new Date(file.lastModified()));
			page.setVersion(LATEST_VERSION);
			page.setSize(file.length());
		}

		return page;
	}

	@Override
	public Collection<Page> getAllChangedSince(final Date date) {
		//TODO Dirty we dont need the bridge
		return this.gitBridge.getAllChangedSinceSafe(date, this.repository, engine);
	}

	@Override
	public int getPageCount() {
		return super.getPageCount();
	}

	@Override
	public List<Page> getVersionHistory(final String pageName) {
		DefaultPageIdentifier pageIdentifier = PageIdentifier.fromPagename(this.getFilesystemPath(), pageName, -1);
		final File page = pageIdentifier.accordingFile();
		List<Page> pageVersions = new ArrayList<>();
		if (page != null && page.exists()) {

			List<String> commitHashes = this.gitConnector.commitHashesForFile(pageIdentifier.fileName());
			int version = 1;
			for (String commitHash : commitHashes) {
				WikiPage wikiPage = createWikiPageFromCommitHash(pageIdentifier, commitHash, version);
				pageVersions.add(wikiPage);
				version++;
			}

			if (pageVersions.isEmpty()) {
				LOGGER.error("File: " + pageIdentifier.pageName() + "is existing but there is no single version of it...");
			}
		}
		Collections.reverse(pageVersions);
		return pageVersions;
	}

	private @NotNull WikiPage createWikiPageFromCommitHash(PageIdentifier pageIdentifier, String commitHash, int version) {
		//mark page as to be refreshed TODO why???
		this.refreshCacheList.add(pageIdentifier.pageName());
		UserData userData = this.gitConnector.userDataFor(commitHash);
		long filesize = this.gitConnector.getFilesizeForCommit(commitHash, pageIdentifier.fileName());
		long timeInSeconds = this.gitConnector.commitTimeFor(commitHash);
		Date date = Date.from(Instant.ofEpochSecond(timeInSeconds));
		return WikiPageProxy.fromUserData(pageIdentifier.pageName(), version, userData, filesize, date, this.engine);
	}

	@Override
	public String getPageText(final String pageName, final int version) throws ProviderException {
		PageIdentifier pageIdentifier = PageIdentifier.fromPagename(this.getFilesystemPath(), pageName, version);
		File pageFile = pageIdentifier.accordingFile();
		//weird jspwiki-behavior
		pageFile = applyPageFileFix(pageIdentifier, pageFile);

		if (!pageFile.exists()) {
			LOGGER.info("Page text requested for new file " + pageIdentifier.pageName());
			return null;
		}

		//read from disk if latest version is requested
		if (pageIdentifier.version() == LATEST_VERSION) {
			try (final FileInputStream fileInputStream = new FileInputStream(pageFile)) {
				return FileUtil.readContents(fileInputStream, this.m_encoding);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		//else we read from git, slower but what can we do
		else {
			return this.gitConnector.getTextForPath(pageIdentifier.fileName(), pageIdentifier.version());
		}
	}

	private @NotNull File applyPageFileFix(PageIdentifier pageIdentifier, File pageFile) {
		if (!pageFile.exists()) {
			// fix because for some reason, the findPage-Method in AbstractFileProvider creates a File that does not exist
			File pageFileFix = new File(this.engine.getWikiProperties()
					.getProperty("var.basedir"), pageIdentifier.pageName() + ".txt");
			if (pageFile.exists()) {
				pageFile = pageFileFix;
			}
		}
		return pageFile;
	}

	@Override
	public void deleteVersion(final Page pageName, final int version) {
		/*
		 * NOTHING TO DO HERE
		 */
	}

	@Override
	public void deletePage(Page page) throws ProviderException {

		final File file = findPage(page.getName());

		if (page.getAuthor() == null) {
			String commitHash = this.gitConnector.commitHashForFileAndVersion(file.getName(), page.getVersion());
			UserData userData = this.gitConnector.userDataFor(commitHash);
			page.setAuthor(userData.user);
		}

		boolean delete = file.delete();
		if (!delete) {
			LOGGER.warn("Failed to delete page (on filesystem): " + page.getName());
		}

		String comment = gitCommentStrategy.getComment(page, "removed page");
		String commitHash = this.gitConnector.deletePath(file.getName(), getUserData(page.getAuthor(), comment), false);

		String author = page.getAuthor();
		if (this.openCommits.containsKey(author)) {
			this.openCommits.get(author).add(file.getName());
		}
		fireWikiEvent(GitVersioningWikiEvent.DELETE, page.getAuthor(), List.of(page.getName()), commitHash);
	}

	public UserData getUserData(String author, String comment) {
		UserProfile userProfile = getUserProfile(author);
		String userName = author;
		String email = "";
		if (userProfile != null) {
			userName = userProfile.getFullname();
			email = userProfile.getEmail();
		}
		return new UserData(userName, email, comment);
	}

	@Override
	public void movePage(final Page from, final String to) throws ProviderException {
		final File fromFile = findPage(from.getName());
		final File toFile = findPage(to);
		try {

			if (from.getAuthor() == null) {
				this.gitBridge.assignAuthor(from);
			}
			movePageOnFilesystem(to, fromFile, toFile);

			movePageGit(from, to, toFile, fromFile);
		}
		catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ProviderException("Can't move page from " + from + " to " + to + ": " + e.getMessage());
		}
	}

	private void movePageGit(Page from, String to, File toFile, File fromFile) {

		String author = from.getAuthor();
		if (this.openCommits.containsKey(author)) {
			this.openCommits.get(author).add(fromFile.getName());
			this.openCommits.get(author).add(toFile.getName());
		}
		else {
			String comment = gitCommentStrategy.getComment(from, "renamed page " + from + " to " + to);
			UserData userData = getUserData(author, comment);
			String commitHash = this.gitConnector.moveFile(fromFile.toPath(), toFile.toPath(), userData.user, userData.email, userData.message);
			fireWikiEvent(GitVersioningWikiEvent.MOVED, author, List.of(to), commitHash);
		}
	}

	private void fireWikiEvent(int event, String author, Collection<String> pages, String commitHash) {
		WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, event,
				author, pages, commitHash));
	}

	private void movePageOnFilesystem(String to, File fromFile, File toFile) throws IOException {
		if (fromFile.getName().equalsIgnoreCase(toFile.getName())) {
			File tmpFile = findPage(to + "_tmp");
			Files.move(fromFile.toPath(), tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.move(tmpFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		else {
			Files.move(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public void openCommit(final String user) {
		if (!this.openCommits.containsKey(user)) {
			this.openCommits.put(user, Collections.synchronizedSortedSet(new TreeSet<>()));
		}
	}

	private UserProfile getUserProfile(String user) {
		try {
			return engine.getManager(UserManager.class)
					.getUserDatabase()
					.findByFullName(user);
		}
		catch (NoSuchPrincipalException e) {
			LOGGER.error("Tried to access user: " + user + " but that user does not exist!");
			return null;
		}
	}

	public void commit(final String user, final String commitMsg) {
		//if there are no commits for this user, we do nothing
		if (!this.openCommits.containsKey(user)) {
			LOGGER.warn("Tried to commit for user: " + user, " but there were no paths for her!");
			return;
		}

		String comment = gitCommentStrategy.getCommentForUser(user);
		if (comment.isEmpty()) {
			comment = commitMsg;
		}
		UserProfile userProfile = getUserProfile(user);
		String username = user;
		String email = "";
		if (userProfile != null) {
			username = userProfile.getFullname();
			email = userProfile.getEmail();
		}

		String commitHash = this.gitConnector.commitPathsForUser(comment, username, email, this.openCommits.get(user));
		fireWikiEvent(GitVersioningWikiEvent.MOVED, user, this.openCommits.get(user), commitHash);

		this.openCommits.remove(user);
		final PageManager pm = getEnginePageManager();
		LOGGER.info("Start refresh");
		for (final String path : this.refreshCacheList) {
			// decide whether page or attachment
			refreshCache(pm, path);
		}
		this.refreshCacheList.clear();
	}

	private PageManager getEnginePageManager() {
		return this.m_engine.getManager(PageManager.class);
	}

	public void rollback(final String user) {
		final Set<String> paths = this.openCommits.get(user);

		final PageManager pm = getEnginePageManager();
		// this could be done, because we only remove the page from the cache and the method of
		// GitVersioningFileProvider does nothing here
		// But we have to inform KnowWE also and Lucene
		for (final String path : paths) {
			// decide whether page or attachment
			refreshCache(pm, JSPUtils.unmangleName(path).replaceAll(FILE_EXT, ""));
		}

		this.gitConnector.rollbackPaths(paths);
		this.openCommits.remove(user);
	}

	private void refreshCache(final PageManager pm, final String pageName) {
		final WikiPage page = new WikiPage(this.m_engine, pageName);
		page.setVersion(PageProvider.LATEST_VERSION);
		try {
			// this can be done, because we only remove the page from the cache and the method of
			// GitVersioningFileProvider does nothing here
			// But we have to inform KnowWE also and Lucene
			pm.deleteVersion(page);
		}
		catch (final ProviderException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void shutdown() {
		this.repository.close();
	}

	public String getFilesystemPath() {
		return this.gitConnector.getGitDirectory();
	}

	GitCommentStrategy getGitCommentStrategy() {
		return gitCommentStrategy;
	}

	public JspGitBridge gitBridge() {
		return this.gitBridge;
	}

	public GitConnector getGitConnector() {
		return this.gitConnector;
	}
}
