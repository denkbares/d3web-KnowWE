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

import org.apache.wiki.InternalWikiException;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.util.FileUtil;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.apache.wiki.providers.GitVersioningUtils.addUserInfo;
import static org.apache.wiki.providers.GitVersioningUtils.gitGc;

/**
 * @author Josua Nürnberger
 * @created 2019-01-02
 */
public class GitVersioningFileProvider extends AbstractFileProvider {

	public static final String JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT = "jspwiki.gitVersioningFileProvider.remoteGit";
	public static final String JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_AUTOUPDATE = "jspwiki.gitVersioningFileProvider.autoUpdate";
	public static final String JSPWIKI_GIT_COMMENT_STRATEGY = "jspwiki.git.commentStrategy";
	protected Repository repository;
	private static final String GIT_DIR = ".git";
	public static final String JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR = "jspwiki.fileSystemProvider.pageDir";
	private static final Logger log = LoggerFactory.getLogger(GitVersioningFileProvider.class);
	private String filesystemPath;
	private final ReadWriteLock pushLock = new ReentrantReadWriteLock();
	private final ReentrantLock commitLock = new ReentrantLock();
	private GitVersionCache cache;
	private final GitAutoUpdateScheduler scheduler;

	private AtomicLong commitCount;

	Map<String, Set<String>> openCommits = new ConcurrentHashMap<>();
	private final Set<String> refreshCacheList = new HashSet<>();
	private boolean windowsGitHackNeeded = false;
	private boolean dontUseHack = false;

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
		this.commitCount = new AtomicLong();
		super.initialize(engine, properties);
		this.filesystemPath = TextUtil.getCanonicalFilePathProperty(properties, JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");
		final File pageDir = new File(this.filesystemPath);
		final File gitDir = new File(pageDir.getAbsolutePath() + File.separator + GIT_DIR);
		autoUpdateEnabled = TextUtil.getBooleanProperty(properties, JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_AUTOUPDATE, false);
		setGitCommentStrategy(properties);
		if (!RepositoryCache.FileKey.isGitRepository(gitDir, FS.DETECTED)) {
			final String remoteURL = TextUtil.getStringProperty(properties, JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT, "");
			if (!"".equals(remoteURL)) {
				try {
					final Git git = Git.cloneRepository()
							.setURI(remoteURL)
							.setDirectory(pageDir)
							.setGitDir(gitDir)
							.setBare(false)
							.call();
					this.repository = git.getRepository();
				}
				catch (final GitAPIException e) {
					throw new IOException(e);
				}
			}
			else {
				this.repository = FileRepositoryBuilder.create(gitDir);
				this.repository.create();
			}
		}
		else {
			this.repository = new FileRepositoryBuilder()
					.setGitDir(gitDir)
					.build();
		}
		final Set<String> remoteNames = this.repository.getRemoteNames();
		if (!remoteNames.isEmpty()) {
			this.remoteRepo = true;
		}

		String os = System.getProperty("os.name").toLowerCase();

		if (os.startsWith("windows") || os.equals("nt")) {
			windowsGitHackNeeded = true;
		}
		if (windowsGitHackNeeded) {
			try {
				Runtime.getRuntime().exec("git");
			}
			catch (IOException e) {
				if (e.getMessage().toLowerCase().contains("command not found")) {
					dontUseHack = true;
					log.warn("Can't find git in PATH");
				}
				else {
					log.error(e.getMessage(), e);
				}
			}
		}
		gitGc(true, windowsGitHackNeeded&&!dontUseHack, repository, true);
		this.repository.autoGC(new TextProgressMonitor());
		IgnoreNode ignoreNode = new IgnoreNode();
		File gitignoreFile = new File(pageDir+"/.gitignore");
		if(gitignoreFile.exists()) {
			ignoreNode.parse(new FileInputStream(gitignoreFile));
		}
		this.cache = new GitVersionCache(engine, this.repository, ignoreNode);
		cache.initializeCache();
		if(autoUpdateEnabled && remoteRepo){
			scheduler.initialize(engine, this);
		}
	}

	private void setGitCommentStrategy(Properties properties) {
		String commentStrategyClassName = TextUtil.getStringProperty(properties, JSPWIKI_GIT_COMMENT_STRATEGY, "org.apache.wiki.providers.ChangeNoteStrategy");
		try {
			Class<?> commentStrategyClass = Class.forName(commentStrategyClassName);
			gitCommentStrategy = (GitCommentStrategy) commentStrategyClass.getConstructor().newInstance(new Object[]{});
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			log.error("Comment strategy not found " +commentStrategyClassName, e);
			gitCommentStrategy = new ChangeNoteStrategy();
		}
	}

	boolean needsWindowsHack(){
		return windowsGitHackNeeded && !dontUseHack;
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
			final File changedFile = findPage(page.getName());
			final boolean addFile = !changedFile.exists();

			super.putPageText(page, text);
			File file = findPage(page.getName());
			page.setSize(file.length());
			final boolean isChanged = false;
			final Git git = new Git(this.repository);
			try {
				if (addFile) {
					retryGitOperation(() -> {
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
					if(addFile){
						cache.addPageVersion(page, comment, null);
					}
					cache.addCacheCommand(page.getAuthor(), new CacheCommand.AddPageVersion(page));

				}
				else {
					final CommitCommand commit = git
							.commit()
							.setOnly(changedFile.getName());
					commit.setMessage(comment);
					addUserInfo(this.m_engine, page.getAuthor(), commit);
					commit.setAllowEmpty(true);
//					try {
					retryGitOperation(() -> {
						final RevCommit revCommit = commit.call();
						WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.UPDATE,
								page.getAuthor(),
								page.getName(),
								revCommit.getId().getName()));
						cache.addPageVersion(page, commit.getMessage(), revCommit.getId());
						return null;
					}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");

					periodicalGitGC();
				}
			}
			catch (final Exception e) {
				log.error(e.getMessage(), e);
				throw new ProviderException("File " + page.getName() + " could not be committed to git");
			}
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	private static final int RETRY = 2;
	private static final int DELAY = 100;

	public static <V> V retryGitOperation(Callable<V> callable, Class<? extends Throwable> t, String message) throws Exception {
		int counter = 0;

		JGitInternalException internalException = null;
		while (counter < RETRY) {
			try {
				return callable.call();
			}
			catch (JGitInternalException e) {
				internalException = e;
				if (t.isAssignableFrom(e.getClass()) ||
						(e.getCause() != null && t.isAssignableFrom(e.getCause().getClass()))) {
					counter++;
					log.warn(String.format("retry %s/%s, %s", counter, RETRY, message));

					try {
						Thread.sleep(DELAY);
					}
					catch (InterruptedException e1) {
						// ignore
					}
				}
				else {
					throw e;
				}
			}
		}
		throw internalException;
	}

	void periodicalGitGC() {
		final long l = this.commitCount.incrementAndGet();
		if (l % 2000 == 0) {
			GitVersioningUtils.gitGc(true, windowsGitHackNeeded && !dontUseHack,repository, true);
		}
		else if (l % 500 == 0) {
			GitVersioningUtils.gitGc(false, windowsGitHackNeeded && !dontUseHack,repository, false);
		}
		else if (l % 100 == 0) {
			this.repository.autoGC(new TextProgressMonitor());
		}
	}

	@Override
	public boolean pageExists(final String page) {
		File pageFile = super.findPage(page);
		try {
			return pageFile.exists() && pageFile.getCanonicalPath().equals(pageFile.getAbsolutePath());
		}
		catch (IOException e) {
			log.warn("Could not evaluate canonical path", e);
		}
		return pageFile.exists();
	}

	@Override
	public boolean pageExists(final String page, final int version) {
		try {
			canWriteFileLock();
			if (pageExists(page)) {
				try {
					if (version == PageProvider.LATEST_VERSION) {
						return true;
					}
					else {
						final List<Page> versionHistory = getVersionHistory(page);
						return (version > 0 && version <= versionHistory.size());
					}
				}
				catch (final ProviderException e) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		finally {
			writeFileUnlock();
		}
	}

	@Override
	public Page getPageInfo(final String pageName, final int version) throws ProviderException {
		try {
			canWriteFileLock();
			if (pageExists(pageName)) {
				// is necessary to get the right version of the current file
				final List<Page> versionHistory = getVersionHistory(pageName);
				// this first block is only needed, if a file was renamed in a larger commit transaction
				// should never be called in normal JSPWiki work
				if (versionHistory.isEmpty() && version == LATEST_VERSION) {
					final WikiPage page = new WikiPage(this.m_engine, pageName);
					page.setVersion(LATEST_VERSION);
					final File file = findPage(pageName);
					page.setSize(file.length());
					page.setLastModified(new Date(file.lastModified()));
					this.refreshCacheList.add(pageName);
					log.info("File not in repo but getPageInfo " + pageName);
					return page;
				}
				else if (version == PageProvider.LATEST_VERSION) {
					return versionHistory.get(0);
				}
				else if (version > 0 && version <= versionHistory.size()) {
					return versionHistory.get(versionHistory.size() - version);
				}
				else {
					throw new ProviderException("Version " + version + " of page " + pageName + " does not exist");
				}
			}
			else {
				return null;
			}
		}
		finally {
			writeFileUnlock();
		}
	}

	@Override
	public Collection<Page> getAllPages() throws ProviderException {
		try {
			canWriteFileLock();
			log.debug("Getting all pages...");

			final Map<String, Page> resultingPages = new HashMap<>();

			final File wikipagedir = new File(this.filesystemPath);
			final File[] wikipages = wikipagedir.listFiles(new WikiFileFilter());

			if (wikipages == null) {
				log.error("Wikipages directory '" + this.filesystemPath + "' does not exist! Please check " + PROP_PAGEDIR + " in jspwiki.properties.");
				throw new InternalWikiException("Page directory does not exist");
			}
			final Map<String, File> files = new HashMap<>();
			for (final File file : wikipages) {
				String fileName = file.getName();
				int cutpoint = fileName.indexOf(FILE_EXT);
				String pageName = fileName.substring(0, cutpoint);
				PageCacheItem pageVersion = cache.getPageVersion(unmangleName(pageName), LATEST_VERSION);
				if (pageVersion != null) {
					WikiPage page = cache.createWikiPage(pageVersion.getPageName(), pageVersion);
					resultingPages.put(fileName, page);
				}
				else {
					log.warn("Datei " + fileName + " wurde nicht im Cache gefunden! Versuche über VersionHistory");
					List<Page> versionHistory = getVersionHistory(pageName);
					if (versionHistory != null && !versionHistory.isEmpty()) {
						resultingPages.put(fileName, versionHistory.get(versionHistory.size() - 1));
						log.info("Datei " + fileName + " wurde gefunden");
					}
					else {
						log.error("Datei " + fileName + " wurde nicht im Repository gefunden!");
					}
				}
			}
			return resultingPages.values();
		}
		finally {
			writeFileUnlock();
		}
	}


	@Override
	public Collection<Page> getAllChangedSince(final Date date) {
		try {
			canWriteFileLock();
			final Iterable<RevCommit> commits = GitVersioningUtils.getRevCommitsSince(date, this.repository);
			final List<Page> pages = new ArrayList<>();

			try {
				ObjectId oldCommit = null;
				ObjectId newCommit;

				for (final RevCommit commit : GitVersioningUtils.reverseToList(commits)) {
					final String fullMessage = commit.getFullMessage();
					final String author = commit.getCommitterIdent().getName();
					final Date modified = new Date(1000L * commit.getCommitTime());

					if (oldCommit != null) {
						newCommit = commit.getTree();
						final List<DiffEntry> diffs = GitVersioningUtils.getDiffEntries(oldCommit, newCommit, this.repository);
						for (final DiffEntry diff : diffs) {
							String path = diff.getOldPath();
							if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
								path = diff.getNewPath();
							}
							if (path.endsWith(FILE_EXT) && !path.contains("/")) {
								final WikiPage page = getWikiPage(fullMessage, author, modified, path);
								pages.add(page);
							}
						}
					}
					else {
						try (final TreeWalk treeWalk = new TreeWalk(this.repository)) {
							treeWalk.reset(commit.getTree());
							treeWalk.setRecursive(true);
							treeWalk.setFilter(TreeFilter.ANY_DIFF);
							while (treeWalk.next()) {
								final String path = treeWalk.getPathString();
								if (path.endsWith(FILE_EXT) && !path.contains("/")) {
									final WikiPage page = getWikiPage(fullMessage, author, modified, path);
									pages.add(page);
								}
							}
						}
					}
					oldCommit = commit.getTree();
				}
			}
			catch (final IOException e) {
				log.error(e.getMessage(), e);
			}
			return pages;
		}
		finally {
			writeFileUnlock();
		}
	}

	@NotNull
	private WikiPage getWikiPage(final String fullMessage, final String author, final Date modified, final String path) {
		final int cutpoint = path.lastIndexOf(FILE_EXT);
		if (cutpoint == -1) {
			log.error("wrong page name " + path);
		}
		final WikiPage page = new WikiPage(this.m_engine, unmangleName(path.substring(0, cutpoint)));
		page.setAttribute(WikiPage.CHANGENOTE, fullMessage);
		page.setAuthor(author);
		page.setLastModified(modified);
		return page;
	}

	@Override
	public int getPageCount() {
		return super.getPageCount();
	}

	@Override
	public List<Page> getVersionHistory(final String pageName) throws ProviderException {
		try {
			canWriteFileLock();
			final File page = findPage(pageName);
			final List<Page> pageVersions = new ArrayList<>();
			if (page.exists()) {
				List<Page> versionHistory = cache.getPageHistory(pageName);
				if (versionHistory != null) {
					Collections.reverse(versionHistory);
					return versionHistory;
				}
				else {
					try {
						final Git git = new Git(this.repository);
						final List<RevCommit> revCommits = GitVersioningUtils.reverseToList(getRevCommits(page, git));
						int versionNr = 1;
						for (final RevCommit revCommit : revCommits) {
							final WikiPage version = getWikiPage(pageName, versionNr, revCommit);
							pageVersions.add(version);
							cache.addPageVersion(version, revCommit.getFullMessage(), revCommit.getId());
							versionNr++;
						}
					}
					catch (final IOException | GitAPIException e) {
						log.error(e.getMessage(), e);
						throw new ProviderException("Can't get version history for page " + pageName + ": " + e.getMessage());
					}
				}
			}
			Collections.reverse(pageVersions);
			return pageVersions;
		}
		finally {
			writeFileUnlock();
		}
	}

	@NotNull
	private WikiPage getWikiPage(final String pageName, final int versionNr, final RevCommit revCommit) {
		final WikiPage version = new WikiPage(this.m_engine, pageName);
		version.setAuthor(revCommit.getCommitterIdent().getName());
		version.setLastModified(new Date(1000L * revCommit.getCommitTime()));
		version.setVersion(versionNr);
		version.setAttribute(WikiPage.CHANGENOTE, revCommit.getFullMessage());
		return version;
	}

	@Override
	public String getPageText(final String pageName, final int version) throws ProviderException {
		try {
			canWriteFileLock();
			final File page = findPage(pageName);
			if (page.exists()) {
				try {
					if (version == LATEST_VERSION) {
						try (final FileInputStream fileInputStream = new FileInputStream(page)) {
							return FileUtil.readContents(fileInputStream, this.m_encoding);
						}
					}
					else {
						PageCacheItem pageCacheItem = cache.getPageVersion(pageName, version);
						if (pageCacheItem != null) {
							try (RevWalk revWalk = new RevWalk(repository)) {
								RevCommit revCommit = revWalk.parseCommit(pageCacheItem.getId());
								return loadObject(page.getName(), version, revCommit);
							}
						}
						else {
							final Git git = new Git(this.repository);
							final List<RevCommit> revCommits = GitVersioningUtils.reverseToList(getRevCommits(page, git));
							if ((version > 0 && version <= revCommits.size())) {
								final RevCommit revCommit = revCommits.get(version - 1);
								return loadObject(page.getName(), version, revCommit);
							}
						}
					}
				}
				catch (final IOException | GitAPIException e) {
					log.error(e.getMessage(), e);
				}
			}
			else {
				log.info("New file " + pageName);
			}
			return null;
		}
		finally {
			writeFileUnlock();
		}
	}

	@NotNull
	private String loadObject(String pageName, int version, RevCommit revCommit) throws IOException, ProviderException {
		final TreeWalk treeWalkDir = new TreeWalk(this.repository);
		treeWalkDir.reset(revCommit.getTree());
		treeWalkDir.setFilter(PathFilter.create(pageName));
		treeWalkDir.setRecursive(false);
		//here we have our file directly
		if (treeWalkDir.next()) {
			final ObjectId fileId = treeWalkDir.getObjectId(0);
			final ObjectLoader loader = this.repository.open(fileId);
			return new String(loader.getBytes(), this.m_encoding);
		}
		else {
			throw new ProviderException("Can't load Git object for " + pageName + " version " + version);
		}
	}

	private Iterable<RevCommit> getRevCommits(final File page, final Git git) throws GitAPIException, IOException {
		return git
				.log()
				.add(git.getRepository().resolve(Constants.HEAD))
				.addPath(page.getName())
				.call();
	}

	@Override
	public void deleteVersion(final Page pageName, final int version) {
		// Can't delete version from git
//		if(version == LATEST_VERSION){
//			this.cache.reset(pageName);
//		}
	}


	@Override
	public void deletePage(Page page) throws ProviderException {
		try {
			canWriteFileLock();
			commitLock();
			final File file = findPage(page.getName());
			file.delete();
			try {
				final Git git = new Git(this.repository);

				retryGitOperation(() -> {
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
					if(comment.isEmpty()) {
						commitCommand.setMessage("removed page");
					} else {
						commitCommand.setMessage(comment);
					}
					addUserInfo(this.m_engine, page.getAuthor(), commitCommand);

					retryGitOperation(() -> {
						final RevCommit revCommit = commitCommand.call();
						WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.DELETE,
								page.getAuthor(),
								page.getName(),
								revCommit.getId().getName()));
						cache.deletePage(page, commitCommand.getMessage(), revCommit.getId());
						return null;
					}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");

					periodicalGitGC();
				}
			}
			catch (final Exception e) {
				log.error(e.getMessage(), e);
				throw new ProviderException("Can't delete page " + page + ": " + e.getMessage());
			}
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	@Override
	public void movePage(final Page from, final String to) throws ProviderException {
		try {
			canWriteFileLock();
			commitLock();
			final File fromFile = findPage(from.getName());
			final File toFile = findPage(to);
			try {
				if (fromFile.getName().equalsIgnoreCase(toFile.getName())) {
					File tmpFile = findPage(to + "_tmp");
					Files.move(fromFile.toPath(), tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					Files.move(tmpFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				else {
					Files.move(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				final Git git = new Git(this.repository);
				retryGitOperation(() -> {
					git.add().addFilepattern(toFile.getName()).call();
					return null;
				}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");

				retryGitOperation(() -> {
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
					if(comment.isEmpty()) {
							commitCommand.setMessage("renamed page " + from + " to " + to);
					} else {
						commitCommand.setMessage(comment);
					}
					addUserInfo(this.m_engine, author, commitCommand);
					retryGitOperation(() -> {
						final RevCommit revCommit = commitCommand.call();
						WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.MOVED,
								author,
								to,
								revCommit.getId().getName()));
						cache.movePage(from, to, commitCommand.getMessage(), revCommit.getId());
						return null;
					}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");

					periodicalGitGC();
				}
			}
			catch (final Exception e) {
				log.error(e.getMessage(), e);
				throw new ProviderException("Can't move page from " + from + " to " + to + ": " + e.getMessage());
			}
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	public void openCommit(final String user) {
		if (!this.openCommits.containsKey(user)) {
			this.openCommits.put(user, Collections.synchronizedSortedSet(new TreeSet<>()));
		}
	}

	public void commit(final String user, final String commitMsg) {
		log.info("start commit");
		try {
			canWriteFileLock();
			commitLock();
			final Git git = new Git(this.repository);
			final CommitCommand commitCommand = git.commit();
			String comment = gitCommentStrategy.getCommentForUser(user);
			if(comment.isEmpty()) {
				commitCommand.setMessage(commitMsg);
			} else {
				commitCommand.setMessage(comment);
			}
			addUserInfo(this.m_engine, user, commitCommand);
			if (this.openCommits.containsKey(user)) {
				final Set<String> paths = this.openCommits.get(user);
				for (final String path : paths) {
					commitCommand.setOnly(path);
				}
				try {
					commitCommand.setAllowEmpty(true);
					retryGitOperation(() -> {
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
					log.info("Start refresh");
					for (final String path : this.refreshCacheList) {
						// decide whether page or attachment
						refreshCache(pm, path);
					}
					this.refreshCacheList.clear();
				}
				catch (final Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		finally {
			commitUnlock();
			writeFileUnlock();
		}
	}

	private PageManager getEnginePageManager() {
		return this.m_engine.getManager(PageManager.class);
	}

	public void rollback(final String user) {
		try {
			canWriteFileLock();
			commitLock();
			final Git git = new Git(this.repository);
			final ResetCommand reset = git.reset();
			final CleanCommand clean = git.clean();
			final CheckoutCommand checkout = git.checkout();
			final Set<String> paths = this.openCommits.get(user);
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
					refreshCache(pm, unmangleName(path).replaceAll(FILE_EXT, ""));
				}
				retryGitOperation(() -> {
					reset.call();
					return null;
				}, LockFailedException.class, "Retry reset repo, because of lock failed exception");

				retryGitOperation(() -> {
					clean.call();
					return null;
				}, LockFailedException.class, "Retry clean repo, because of lock failed exception");

				retryGitOperation(() -> {
					final Status status = git.status().call();
					return null;
				}, LockFailedException.class, "Retry status repo, because of lock failed exception");

				retryGitOperation(() -> {
					checkout.call();
					return null;
				}, LockFailedException.class, "Retry checkout repo, because of lock failed exception");

				this.openCommits.remove(user);
			}
			catch (final Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		finally {
			commitUnlock();
			writeFileUnlock();
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
			log.error(e.getMessage(), e);
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
		if(autoUpdateEnabled && remoteRepo){
			scheduler.shutdown();
		}
		this.repository.close();
	}

	public String getFilesystemPath() {
		return filesystemPath;
	}

	public GitVersionCache getCache() {
		return cache;
	}

	public void pauseAutoUpdate(){
		if(autoUpdateEnabled && remoteRepo)
			scheduler.pauseAutoUpdate();
		else
			log.warn("pauseAutoUpdate was called on a wiki not configured as autoUpdate");
	}

	public void resumeAutoUpdate(){
		if(autoUpdateEnabled && remoteRepo)
			scheduler.resumeAutoUpdate();
		else
			log.warn("resumeAutoUpdate was called on a wiki not configured as autoUpdate");
	}

	GitCommentStrategy getGitCommentStrategy() {
		return gitCommentStrategy;
	}
}
