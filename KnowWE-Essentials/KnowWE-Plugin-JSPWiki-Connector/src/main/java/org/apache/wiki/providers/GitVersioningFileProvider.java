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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.wiki.InternalWikiException;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.gitBridge.JspGitBridge;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.autoUpdate.GitAutoUpdateScheduler;
import org.apache.wiki.providers.commentStrategy.ChangeNoteStrategy;
import org.apache.wiki.providers.commentStrategy.GitCommentStrategy;
import org.apache.wiki.providers.gitCache.GitVersionCache;
import org.apache.wiki.providers.gitCache.adhoc.AdhocGitCache;
import org.apache.wiki.providers.gitCache.async.AsyncInitGitVersionCache;
import org.apache.wiki.providers.gitCache.commands.CacheCommand;
import org.apache.wiki.providers.gitCache.complete.CompleteGitVersionCache;
import org.apache.wiki.providers.gitCache.history.BackedJGitHistoryProvider;
import org.apache.wiki.providers.gitCache.history.GitHistoryProvider;
import org.apache.wiki.providers.gitCache.items.PageCacheItem;
import org.apache.wiki.structs.DefaultPageIdentifier;
import org.apache.wiki.structs.PageIdentifier;
import org.apache.wiki.structs.WikiPageProxy;
import org.apache.wiki.util.FileUtil;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CleanCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.wiki.providers.gitCache.GitVersionCache.PROPERTIES_KEY_GIT_VERSION_CACHE;

/**
 * @author Josua Nürnberger
 * @created 2019-01-02
 */
public class GitVersioningFileProvider extends AbstractFileProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitVersioningFileProvider.class);

	public static final String JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT = "jspwiki.gitVersioningFileProvider.remoteGit";
	public static final String JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_SKIP_GC = "jspwiki.gitVersioningFileProvider.skipGC";
	private static final String JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_AUTOUPDATE = "jspwiki.gitVersioningFileProvider.autoUpdate";

	public static final String JSPWIKI_GIT_DEFAULT_BRANCH = "jspwiki.git.defaultBranch";
	private static final String JSPWIKI_GIT_COMMENT_STRATEGY = "jspwiki.git.commentStrategy";
	private static final String JSPWIKI_GIT_REMOTE_USERNAME = "jspwiki.git.remoteUsername";
	private static final String JSPWIKI_GIT_REMOTE_TOKEN = "jspwiki.git.remoteToken";
	public Repository repository;
	public static final String GIT_DIR = ".git";
	public static final String JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR = "jspwiki.fileSystemProvider.pageDir";

	//TODO these 2 should no longer be in here!
	private final ReadWriteLock pushLock = new ReentrantReadWriteLock();
	private final ReentrantLock commitLock = new ReentrantLock();
	private GitVersionCache cache;
	private GitHistoryProvider historyProvider;
	private final GitAutoUpdateScheduler scheduler;

	private String remoteUsername;
	private String remoteToken;

	//user to set of paths
	Map<String, Set<String>> openCommits = new ConcurrentHashMap<>();
	private final Set<String> refreshCacheList = new HashSet<>();

	private Engine engine;
	private JspGitBridge gitBridge;

	public GitVersioningFileProvider() {
		scheduler = new GitAutoUpdateScheduler();
	}

	public boolean isRemoteRepo() {
		return this.remoteRepo;
	}



	private boolean remoteRepo = false;
	private boolean autoUpdateEnabled = false;
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

		this.gitBridge = new JspGitBridge(engine);
		this.gitBridge.init(properties);
		this.gitBridge.initAndVerifyGitRepository(properties);

		this.repository = this.gitBridge.getRepository();

		autoUpdateEnabled = TextUtil.getBooleanProperty(properties, JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_AUTOUPDATE, false);

		this.remoteUsername = TextUtil.getStringProperty(properties, JSPWIKI_GIT_REMOTE_USERNAME, null);
		this.remoteToken = TextUtil.getStringProperty(properties, JSPWIKI_GIT_REMOTE_TOKEN, null);

		setGitCommentStrategy(properties);

		// we run the command to build up the commit graph. It considerably accelerates the reading of the commit history (e. g. git log)
		this.gitBridge.executeGitCommitGraphCommand();

		// initialization of the git history provider
		this.historyProvider = new BackedJGitHistoryProvider(repository, engine, this.gitBridge);

		// initialization of the GitVersionCache
		String versionCacheInitialization = (String) properties.get(PROPERTIES_KEY_GIT_VERSION_CACHE);

		// default we use no cache
		this.cache = new AdhocGitCache(engine, repository, this.gitBridge.getIgnoreNode());

		//use sync if requested
		if (GitVersionCache.SYNC.equalsIgnoreCase(versionCacheInitialization)) {
			this.cache = new CompleteGitVersionCache(engine, this.repository, this.gitBridge.getIgnoreNode());
		}
		else if (GitVersionCache.ASYNC.equalsIgnoreCase(versionCacheInitialization)) {
			this.cache = new AsyncInitGitVersionCache(engine, this.repository, this.gitBridge.getIgnoreNode());
		}
		this.cache = new AdhocGitCache(engine, repository, this.gitBridge.getIgnoreNode());
//		this.cache = new CompleteGitVersionCache(engine, this.repository, this.gitBridge.getIgnoreNode());
		cache.clearAndReinitialize();

		this.remoteRepo = this.gitBridge.isRemoteRepo();

		if (autoUpdateEnabled && remoteRepo) {
			scheduler.initialize(engine, this);
		}
	}

	private void setGitCommentStrategy(Properties properties) {
		String commentStrategyClassName = TextUtil.getStringProperty(properties, JSPWIKI_GIT_COMMENT_STRATEGY, "org.apache.wiki.providers.commentStrategy.ChangeNoteStrategy");
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
	}

	public boolean needsWindowsHack() {
		return this.gitBridge.windowsGitHackNeeded && !this.gitBridge.dontUseHack;
	}

	public Repository getRepository() {
		return this.repository;
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningFileProvider.class.getSimpleName();
	}

	@Override
	public void putPageText(final Page page, final String text) throws ProviderException {
		try {
			canWriteFileLock();
			commitLock();
			//delegate
			putPageTextSafe(page, text);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	/**
	 * Does the actual work, can assume that all read/write locks are already existing
	 *
	 * @param page
	 * @param text
	 * @throws ProviderException
	 */
	private void putPageTextSafe(Page page, String text) throws ProviderException {
		LOGGER.info("put page text for page: " + page.getName() + " and version: " + page.getVersion());
		final File changedFile = super.findPage(page.getName());
		final boolean addFile = !changedFile.exists();

		//check if there is no change! (aka nothing to cache)
		if (sameTextContent(text, changedFile)) return;
		//perform action on wiki
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
		final Git git = new Git(this.repository);

		if (page.getAuthor() == null) {
			this.gitBridge.assignAuthor(page);
		}
		try {
			if (addFile) {
				JspGitBridge.retryGitOperation(() -> {
					git.add().addFilepattern(changedFile.getName()).call();
					return null;
				}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");
			}

			String comment = gitCommentStrategy.getComment(page);
			if (comment.isEmpty()) {
				if (addFile) {
					comment = "Added page";
				}
				else {
					comment = "-";
				}
			}

			if (this.openCommits.containsKey(page.getAuthor())) {
				this.openCommits.get(page.getAuthor()).add(changedFile.getName());
				if (addFile) {
					cache.addPageVersion(page, comment, null);
				}
				cache.addCacheCommand(page.getAuthor(), new CacheCommand.AddPageVersion(page));
			}
			else {
				final CommitCommand commit = git
						.commit()
						.setOnly(changedFile.getName());
				commit.setMessage(comment);
				String author = page.getAuthor();
				this.gitBridge.addUserInfo(this.m_engine, author, commit);
				commit.setAllowEmpty(true);

				JspGitBridge.retryGitOperation(() -> {
					final RevCommit revCommit = commit.call();
					WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.UPDATE,
							page.getAuthor(),
							page.getName(),
							revCommit.getId().getName()));
					cache.addPageVersion(page, commit.getMessage(), revCommit.getId());
					return null;
				}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");

				this.gitBridge.periodicalGitGC();
			}
		}
		catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ProviderException("File " + page.getName() + " could not be committed to git");
		}
	}

	@Override
	public boolean pageExists(final String page, final int version) {
		try {
			canWriteFileLock();
			return pageExistsSafe(page, version);
		}
		finally {
			writeFileUnlock();
		}
	}

	private boolean pageExistsSafe(String page, int version) {
		if (!pageExists(page)) {
			return false;
		}

		if (version == PageProvider.LATEST_VERSION) {
			return true;
		}

		try {
			final List<Page> versionHistory = getVersionHistory(page);
			return (version > 0 && version <= versionHistory.size());
		}
		catch (final ProviderException e) {
			return false;
		}
	}

	@Override
	public Page getPageInfo(final String pageName, final int version) throws ProviderException {

//		if(this.pageIdentifierCache!=null  ){
//			Page pageFor = this.pageIdentifierCache.getPageFor(pageName, version);
//			if(pageFor!=null){
//				return pageFor;
//			}
//		}
		try {
			canWriteFileLock();
			PageIdentifier pageIdentifier = PageIdentifier.fromPagename(this.getFilesystemPath(), pageName, version);
			Page page = getPageInfoSafe(pageIdentifier);

//			if(this.pageIdentifierCache!=null){
//				this.pageIdentifierCache.addToCache(pageName,version,page);
//			}
			return page;
		}
		finally {
			writeFileUnlock();
		}
	}

	@Nullable
	private Page getPageInfoSafe(PageIdentifier pageIdentifier) throws ProviderException {

		LOGGER.info("Get page for : " + pageIdentifier + " and version: " + pageIdentifier.version());

		if (!pageIdentifier.exists()) {
			return null;
		}

		if (!cache.isInitialized()) {
			// right now version -1 should be fine
			return createWikiPageLatest(pageIdentifier);
		}

		// history is necessary to get the right version of the current file
		final List<Page> versionHistory = getVersionHistory(pageIdentifier.pageName());
		// this first block is only needed, if a file was renamed in a larger commit transaction
		// should never be called in normal JSPWiki work
		if (versionHistory.isEmpty() && pageIdentifier.version() == LATEST_VERSION) {
			return createWikiPageLatest(pageIdentifier);
		}
		else if (pageIdentifier.version() == PageProvider.LATEST_VERSION) {
			return versionHistory.get(0);
		}
		else if (pageIdentifier.version() > 0 && pageIdentifier.version() <= versionHistory.size()) {
			return versionHistory.get(versionHistory.size() - pageIdentifier.version());
		}
		else {
			throw new ProviderException("Version " + pageIdentifier.version() + " of page " + pageIdentifier.pageName() + " does not exist");
		}
	}

	@NotNull
	private WikiPage createWikiPageLatest(PageIdentifier pageIdentifier) {
		WikiPage page = cache.createWikiPageLatest(pageIdentifier.pageName());
		this.refreshCacheList.add(pageIdentifier.pageName());
		LOGGER.info("File not in repo but getPageInfo " + pageIdentifier.pageName());
		if (page == null) {
			final WikiPage version = new WikiPage(engine, pageIdentifier.pageName());
			File file = Paths.get(this.getFilesystemPath(), pageIdentifier.pageName()).toFile();
			version.setLastModified(new Date(file.lastModified()));
			//if this method gets called the version is always the first version
			version.setVersion(1);
			version.setSize(file.length());
			return version;
		}
		return page;
	}

	@Override
	public Collection<Page> getAllPages() {
		try {
			canWriteFileLock();
			return getAllPagesSafe();
		}
		finally {
			writeFileUnlock();
		}
	}

	@NotNull
	private Collection<Page> getAllPagesSafe() {
		LOGGER.debug("Getting all pages...");

		final Map<String, Page> resultingPages = new HashMap<>();

		final File wikipagedir = new File(this.gitBridge.getFilesystemPath());
		final File[] wikipages = wikipagedir.listFiles(new WikiFileFilter());

		if (wikipages == null) {
			LOGGER.error("Wikipages directory '" + this.gitBridge.getFilesystemPath() + "' does not exist! Please check " + PROP_PAGEDIR + " in jspwiki.properties.");
			throw new InternalWikiException("Page directory does not exist");
		}
		for (final File file : wikipages) {
			PageIdentifier pageIdentifier = PageIdentifier.fromFile(this.getFilesystemPath(), file);

			WikiPage page = cache.createWikiPageLatest(pageIdentifier.pageName());
			if (page != null) {
				resultingPages.put(pageIdentifier.accordingFile().getName(), page);
			}
			else {
				try {
					//we obtain the pages from the filesystem and not from git!
					page = getWikiPageFromFilesystem(pageIdentifier);
					//page = getWikiPageFromGit(fileName, pageName);
					resultingPages.put(pageIdentifier.accordingFile().getName(), page);
				}
				catch (GitAPIException | IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return resultingPages.values();
	}

	@NotNull
	private WikiPage getWikiPageFromFilesystem(PageIdentifier pageIdentifier) throws GitAPIException, IOException {

		WikiPageProxy page = new WikiPageProxy(engine, pageIdentifier.pageName());
		page.setHistoryProvider(this.historyProvider);

		File file = pageIdentifier.accordingFile();
		if (file != null && file.exists()) {
			page.setLastModified(new Date(file.lastModified()));
			page.setVersion(LATEST_VERSION);
			page.setSize(file.length());
		}

		return page;
	}

	//Note: this would provide the correct versions but is super slow
	@NotNull
	private WikiPage getWikiPageFromGit(String fileName, String pageName) throws GitAPIException, IOException {
		WikiPage page;
		long time = System.currentTimeMillis();
		List<RevCommit> revCommitList = this.gitBridge.getRevCommitList(fileName, new Git(repository), -1);

		System.out.println("Time to get commits for page: " + fileName + " was: " + (System.currentTimeMillis() - time));
		RevCommit revCommit = revCommitList.get(revCommitList.size() - 1);

		page = new WikiPage(engine, pageName);
		page.setVersion(revCommitList.size());
		//TODO why do i need the size even?
//					page.setSize(getObjectSize(pageName));
		Date date = revCommit.getAuthorIdent().getWhen();
		page.setLastModified(date);
		page.setAuthor(revCommit.getAuthorIdent().getName());
		return page;
	}

	@Override
	public Collection<Page> getAllChangedSince(final Date date) {
		try {
			canWriteFileLock();
			return this.gitBridge.getAllChangedSinceSafe(date, this.repository, engine);
		}
		finally {
			writeFileUnlock();
		}
	}

	@Override
	public int getPageCount() {
		return super.getPageCount();
	}

	@Override
	public List<Page> getVersionHistory(final String pageName) throws ProviderException {

		try {
			canWriteFileLock();
			DefaultPageIdentifier pageIdentifier = PageIdentifier.fromPagename(this.getFilesystemPath(), pageName, -1);
			return getVersionHistorySafe(pageIdentifier);
		}
		finally {
			writeFileUnlock();
		}
	}

	@NotNull
	private List<Page> getVersionHistorySafe(PageIdentifier pageIdentifier) throws ProviderException {
		final File page = pageIdentifier.accordingFile();
		List<Page> pageVersions = new ArrayList<>();
		if (page != null && page.exists()) {
			//attempt cache
			List<Page> versionHistory = getPageHistoryFromCache(pageIdentifier);

			if (versionHistory == null) {
				pageVersions = historyProvider.getPageHistory(pageIdentifier);
				cache.setPageHistory(pageIdentifier.pageName(), pageVersions);
			}
			if (pageVersions.isEmpty()) {
				LOGGER.error("File: " + pageIdentifier.pageName() + "is existing but there is no single version of it...");
			}
		}
		Collections.reverse(pageVersions);
		return pageVersions;
	}

	//TODO delete this if cache is no longer required in the code
	private List<Page> getPageHistoryFromCache(PageIdentifier pageIdentifier) {
		List<Page> versionHistory = cache.getPageHistory(pageIdentifier.pageName());
		if (versionHistory == null) {
			return null;
		}
		Collections.reverse(versionHistory);
		return versionHistory;
	}

	@Override
	public String getPageText(final String pageName, final int version) throws ProviderException {

		try {
			canWriteFileLock();
			PageIdentifier pageIdentifier = PageIdentifier.fromPagename(this.getFilesystemPath(), pageName, version);

			String pageText = getPageTextSafe(pageIdentifier);

			return pageText;
		}
		finally {
			writeFileUnlock();
		}
	}

	@Nullable
	private String getPageTextSafe(PageIdentifier pageIdentifier) throws ProviderException {
		final int version = pageIdentifier.version();
		File pageFile = pageIdentifier.accordingFile();
		if (!pageFile.exists()) {
			// fix because for some reason, the findPage-Method in AbstractFileProvider creates a File that does not exist
			File pageFileFix = new File(this.engine.getWikiProperties()
					.getProperty("var.basedir"), pageIdentifier.pageName() + ".txt");
			if (pageFile.exists()) {
				pageFile = pageFileFix;
			}
		}

		if (pageFile.exists()) {
			try {
				if (pageIdentifier.version() == LATEST_VERSION) {
					try (final FileInputStream fileInputStream = new FileInputStream(pageFile)) {
						return FileUtil.readContents(fileInputStream, this.m_encoding);
					}
				}
				else {

					PageCacheItem pageCacheItem = cache.getPageVersion(pageIdentifier.pageName(), version);
					if (pageCacheItem != null) {
						try (RevWalk revWalk = new RevWalk(repository)) {
							RevCommit revCommit = revWalk.parseCommit(pageCacheItem.getId());
							return getPageTextFromCommit(pageIdentifier, version, revCommit);
						}
					}
					else {
						final Git git = new Git(this.repository);
						LOGGER.info("Access commits for page: " + pageFile.getName());
						try {
							List<String> commitHashesForPageRaw = this.gitBridge.getCommitHashesForPageRaw(pageFile.getName(), false, -1);
							//obtain the according commit
							String accordingCommit = null;
							if (version == -1) {
								accordingCommit = commitHashesForPageRaw.get(0);
							}
							if (version - 1 < commitHashesForPageRaw.size()) {
								accordingCommit = commitHashesForPageRaw.get(commitHashesForPageRaw.size() - pageIdentifier.version());
							}
							if (accordingCommit != null) {
								RevCommit revCommit = GitVersioningUtils.getRevCommit(new Git(this.repository), accordingCommit);
								return getPageTextFromCommit(pageIdentifier, version, revCommit);
							}
						}
						catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
						final List<RevCommit> revCommits = GitVersioningUtils.reverseToList(this.gitBridge.getRevCommits(pageFile.getName(), git));
						if ((version > 0 && version <= revCommits.size())) {
							final RevCommit revCommit = revCommits.get(version - 1);
							return getPageTextFromCommit(pageIdentifier, version, revCommit);
						}
					}
				}
			}
			catch (final IOException | GitAPIException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		else {
			LOGGER.info("New file " + pageIdentifier.pageName());
		}
		return null;
	}

	@NotNull
	private String getPageTextFromCommit(PageIdentifier pageIdentifier, int version, RevCommit revCommit) throws IOException, ProviderException {
		final TreeWalk treeWalkDir = new TreeWalk(this.repository);
		treeWalkDir.reset(revCommit.getTree());
		treeWalkDir.setFilter(PathFilter.create(pageIdentifier.fileName()));
		treeWalkDir.setRecursive(false);
		//here we have our file directly
		if (treeWalkDir.next()) {
			final ObjectId fileId = treeWalkDir.getObjectId(0);
			final ObjectLoader loader = this.repository.open(fileId);
			return new String(loader.getBytes(), this.m_encoding);
		}
		else {
			throw new ProviderException("Can't load Git object for " + pageIdentifier.pageName() + " version " + version);
		}
	}

	@Override
	public void deleteVersion(final Page pageName, final int version) {
		/*
		 * NOTHING TO DO HERE
		 */
	}

	@Override
	public void deletePage(Page page) throws ProviderException {
		try {
			canWriteFileLock();
			commitLock();
			deletePageSafe(page);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	private void deletePageSafe(Page page) throws ProviderException {

		if (page.getAuthor() == null) {
			this.gitBridge.assignAuthor(page);
		}
		final File file = findPage(page.getName());
		file.delete();
		try {
			final Git git = new Git(this.repository);

			JspGitBridge.retryGitOperation(() -> {
				git.rm().addFilepattern(file.getName()).call();
				return null;
			}, LockFailedException.class, "Retry removing from repo, because of lock failed exception");

			String author = page.getAuthor();
			if (this.openCommits.containsKey(author)) {
				this.openCommits.get(author).add(file.getName());
				cache.addCacheCommand(author, new CacheCommand.DeletePageVersion(page));
			}
			else {
				final CommitCommand commitCommand = git.commit()
						.setOnly(file.getName());
				String comment = gitCommentStrategy.getComment(page);
				if (comment.isEmpty()) {
					commitCommand.setMessage("removed page");
				}
				else {
					commitCommand.setMessage(comment);
				}
				this.gitBridge.addUserInfo(this.m_engine, page.getAuthor(), commitCommand);

				JspGitBridge.retryGitOperation(() -> {
					final RevCommit revCommit = commitCommand.call();
					WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.DELETE,
							page.getAuthor(),
							page.getName(),
							revCommit.getId().getName()));
					cache.deletePage(page, commitCommand.getMessage(), revCommit.getId());
					return null;
				}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");

				this.gitBridge.periodicalGitGC();
			}
		}
		catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ProviderException("Can't delete page " + page + ": " + e.getMessage());
		}
	}

	@Override
	public void movePage(final Page from, final String to) throws ProviderException {
		try {
			canWriteFileLock();
			commitLock();
			movePageSafe(from, to);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	private void movePageSafe(Page from, String to) throws ProviderException {
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

	private void movePageGit(Page from, String to, File toFile, File fromFile) throws Exception {
		final Git git = new Git(this.repository);

		JspGitBridge.retryGitOperation(() -> {
			git.add().addFilepattern(toFile.getName()).call();
			return null;
		}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");

		JspGitBridge.retryGitOperation(() -> {
			git.rm().setCached(true).addFilepattern(fromFile.getName()).call();
			return null;
		}, LockFailedException.class, "Retry removing from repo, because of lock failed exception");

		String author = from.getAuthor();
		if (this.openCommits.containsKey(author)) {
			this.openCommits.get(author).add(fromFile.getName());
			this.openCommits.get(author).add(toFile.getName());
			cache.addCacheCommand(author, new CacheCommand.MovePage(from, to));
		}
		else {
			final CommitCommand commitCommand = git.commit()
					.setOnly(toFile.getName())
					.setOnly(fromFile.getName());
			String comment = gitCommentStrategy.getComment(from);
			if (comment.isEmpty()) {
				commitCommand.setMessage("renamed page " + from + " to " + to);
			}
			else {
				commitCommand.setMessage(comment);
			}
			this.gitBridge.addUserInfo(this.m_engine, author, commitCommand);
			JspGitBridge.retryGitOperation(() -> {
				final RevCommit revCommit = commitCommand.call();
				WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.MOVED,
						author,
						to,
						revCommit.getId().getName()));
				cache.movePage(from, to, commitCommand.getMessage(), revCommit.getId());
				return null;
			}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");

			this.gitBridge.periodicalGitGC();
		}
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

	public void commit(final String user, final String commitMsg) {
		LOGGER.info("start commit");
		try {
			canWriteFileLock();
			commitLock();
			commitSafe(user, commitMsg);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	private void commitSafe(String user, String commitMsg) {
		final Git git = new Git(this.repository);
		final CommitCommand commitCommand = git.commit();
		String comment = gitCommentStrategy.getCommentForUser(user);
		if (comment.isEmpty()) {
			commitCommand.setMessage(commitMsg);
		}
		else {
			commitCommand.setMessage(comment);
		}
		this.gitBridge.addUserInfo(this.m_engine, user, commitCommand);
		if (this.openCommits.containsKey(user)) {
			final Set<String> paths = this.openCommits.get(user);
			for (final String path : paths) {
				commitCommand.setOnly(path);
			}
			try {
				commitCommand.setAllowEmpty(true);
				JspGitBridge.retryGitOperation(() -> {
					final RevCommit revCommit = commitCommand.call();
					WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.MOVED,
							user,
							this.openCommits.get(user),
							revCommit.getId().getName()));
					cache.executeCacheCommands(user, commitCommand.getMessage(), revCommit.getId());
					return null;
				}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");

				this.openCommits.remove(user);
				final PageManager pm = getEnginePageManager();
				LOGGER.info("Start refresh");
				for (final String path : this.refreshCacheList) {
					// decide whether page or attachment
					refreshCache(pm, path);
				}
				this.refreshCacheList.clear();
			}
			catch (final Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public Engine getEngine() {
		return engine;
	}

	private PageManager getEnginePageManager() {
		return this.m_engine.getManager(PageManager.class);
	}

	public void rollback(final String user) {
		try {
			canWriteFileLock();
			commitLock();
			//delegate
			rollbackSafe(user);
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	private void rollbackSafe(String user) {
		final Git git = new Git(this.repository);
		final Set<String> paths = this.openCommits.get(user);

		final ResetCommand reset = git.reset();
		final CleanCommand clean = git.clean();
		final CheckoutCommand checkout = git.checkout().setForced(true);
		clean.setPaths(paths);
		for (final String path : paths) {
			reset.addPath(path);
			checkout.addPath(path);
		}
		clean.setCleanDirectories(true);
		try {
			final PageManager pm = getEnginePageManager();
			// this could be done, because we only remove the page from the cache and the method of
			// GitVersioningFileProvider does nothing here
			// But we have to inform KnowWE also and Lucene
			for (final String path : paths) {
				// decide whether page or attachment
				refreshCache(pm, JSPUtils.unmangleName(path).replaceAll(FILE_EXT, ""));
			}
			JspGitBridge.retryGitOperation(() -> {
				reset.call();
				return null;
			}, LockFailedException.class, "Retry reset repo, because of lock failed exception");

			JspGitBridge.retryGitOperation(() -> {
				clean.call();
				return null;
			}, LockFailedException.class, "Retry clean repo, because of lock failed exception");

			JspGitBridge.retryGitOperation(() -> {
				final Status status = git.status().call();
				return null;
			}, LockFailedException.class, "Retry status repo, because of lock failed exception");

			JspGitBridge.retryGitOperation(() -> {
				checkout.call();
				return null;
			}, LockFailedException.class, "Retry checkout repo, because of lock failed exception");

			this.openCommits.remove(user);
		}
		catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void refreshCache(final PageManager pm, final String pageName) {
		final WikiPage page = new WikiPage(this.m_engine, pageName);
		page.setVersion(PageProvider.LATEST_VERSION);
		try {
			// this could be done, because we only remove the page from the cache and the method of
			// GitVersioningFileProvider does nothing here
			// But we have to inform KnowWE also and Lucene
			pm.deleteVersion(page);
		}
		catch (final ProviderException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	void commitLock() {
		//noinspection LockAcquiredButNotSafelyReleased
		this.commitLock.lock();
	}

	void commitUnlock() {
		this.commitLock.unlock();
	}

	public void pushLock() {
		//noinspection LockAcquiredButNotSafelyReleased
		this.pushLock.writeLock().lock();
	}

	public void pushUnlock() {
		this.pushLock.writeLock().unlock();
	}

	public void canWriteFileLock() {
		//noinspection LockAcquiredButNotSafelyReleased
		this.pushLock.readLock().lock();
	}

	public void writeFileUnlock() {
		this.pushLock.readLock().unlock();
	}

	public void shutdown() {
		if (autoUpdateEnabled && remoteRepo) {
			scheduler.shutdown();
		}
		this.cache.shutdown();
		this.repository.close();
	}

	public String getFilesystemPath() {
		return this.gitBridge.getFilesystemPath();
	}

	public GitVersionCache getCache() {
		return cache;
	}

	public void pauseAutoUpdate() {
		if (autoUpdateEnabled && remoteRepo) {
			scheduler.pauseAutoUpdate();
		}
		else {
			LOGGER.warn("pauseAutoUpdate was called on a wiki not configured as autoUpdate");
		}
	}

	public void resumeAutoUpdate() {
		if (autoUpdateEnabled && remoteRepo) {
			scheduler.resumeAutoUpdate();
		}
		else {
			LOGGER.warn("resumeAutoUpdate was called on a wiki not configured as autoUpdate");
		}
	}

	GitCommentStrategy getGitCommentStrategy() {
		return gitCommentStrategy;
	}

	public JspGitBridge gitBridge() {
		return this.gitBridge;
	}

	public boolean isClean() {
		return this.gitBridge.isClean();
	}

	public String getRemoteToken() {
		return remoteToken;
	}

	public String getRemoteUsername() {
		return remoteUsername;
	}
}
