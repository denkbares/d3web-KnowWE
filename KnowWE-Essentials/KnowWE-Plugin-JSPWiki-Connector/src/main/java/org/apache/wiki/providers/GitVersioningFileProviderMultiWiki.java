/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.commentStrategy.ChangeNoteStrategy;
import org.apache.wiki.providers.commentStrategy.GitCommentStrategy;
import org.apache.wiki.providers.git.GitCommitBatchRegistry;
import org.apache.wiki.providers.git.GitPageHistory;
import org.apache.wiki.providers.git.GitPageVersion;
import org.apache.wiki.structs.WikiPageProxy;
import org.apache.wiki.util.TextUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

/**
 * Git-backed page provider for the multi-wiki setup. Inherits the multi-wiki path axis from
 * {@link AbstractMultiWikiFileProvider} (page resolution per sub-wiki folder, {@code getAllPages} across folders,
 * the {@code MultiWikiPageProvider} contract) and composes the git axis: each sub-wiki folder is its own git
 * repository, served by one {@link GitPageHistory}. The provider is a thin router, it strips the {@code Repo&&} prefix
 * at the boundary, delegates the resulting flat single-repo operation to the right {@code GitPageHistory}, and
 * re-prefixes results. History comes from git ({@code git log}); the {@code OLD/} directory mechanism is not used.
 * <p>
 * Engine-coupled concerns (user-profile -> author/email, comment strategy, JSPWiki {@code Page} construction,
 * wiki-event firing, cache refresh) live here, the per-repo {@code GitPageHistory} stays engine-free. Batched
 * transactions ({@link #openCommit}/{@link #commit}/{@link #rollback}) accumulate paths in the
 * {@link GitCommitBatchRegistry} and produce one commit per touched repo.
 *
 * @see GitPageHistory
 * @see GitCommitBatchRegistry
 */
public class GitVersioningFileProviderMultiWiki extends AbstractMultiWikiFileProvider implements GitVersioningProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitVersioningFileProviderMultiWiki.class);

	/**
	 * repo key (the repo's working-tree path, == GitPageHistory.repoKey()) -> history component
	 */
	private final Map<String, GitPageHistory> historyByRepoKey = new ConcurrentHashMap<>();
	/**
	 * sub-wiki folder name (incl. "" / main folder) -> history component, for routing by page prefix
	 */
	private final Map<String, GitPageHistory> historyByFolder = new ConcurrentHashMap<>();

	private GitCommitBatchRegistry batchRegistry;
	private GitCommentStrategy gitCommentStrategy;

	@Override
	public void initialize(Engine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);
		setGitCommentStrategy(properties);
		this.batchRegistry = new GitCommitBatchRegistry(this::connectorForRepoKey);

		// one GitPageHistory per sub-wiki repo, including the main wiki folder (V1)
		Set<String> folders = new LinkedHashSet<>();
		folders.add(SubWikiUtils.getMainWikiFolder(properties));
		folders.addAll(getAllSubWikiFolders(true));
		for (String folder : folders) {
			GitPageHistory history = historyByFolder.computeIfAbsent(folder, this::createHistory);
			if (history != null) {
				history.sweepUp("provider-startup");
			}
		}
		LOGGER.info("Initialized {} with {} sub-wiki repo(s).", getClass().getSimpleName(), historyByRepoKey.size());
	}

	@Nullable
	private GitConnector connectorForRepoKey(String repoKey) {
		GitPageHistory history = historyByRepoKey.get(repoKey);
		return history == null ? null : history.connector();
	}

	private void setGitCommentStrategy(Properties properties) {
		String className = TextUtil.getStringProperty(
				properties,
				GitProviderProperties.JSPWIKI_GIT_COMMENT_STRATEGY,
				ChangeNoteStrategy.class.getName()
		);
		try {
			this.gitCommentStrategy = (GitCommentStrategy) Class.forName(className).getConstructor().newInstance();
		}
		catch (ClassNotFoundException
		       | InstantiationException
		       | IllegalAccessException
		       | NoSuchMethodException
		       | InvocationTargetException
				e
		) {
			LOGGER.error(
					"Comment strategy not found: {}, falling back to {}.",
					className, ChangeNoteStrategy.class.getName(), e
			);
			this.gitCommentStrategy = new ChangeNoteStrategy();
		}
	}

	/**
	 * Creates (and registers) the {@link GitPageHistory} for a sub-wiki folder, initializing the git repository if it
	 * does not exist yet. Returns {@code null} if the folder does not exist on disk.
	 * <p>
	 * The provider owns its connectors. The batch registry resolves connectors from these via
	 * {@link #connectorForRepoKey}, so a shared pool would be a one-line swap if a concrete need ever arises.
	 */
	@Nullable
	private GitPageHistory createHistory(String folder) {
		File repoDir = new File(m_pageDirectory, folder);
		if (!repoDir.isDirectory()) {
			LOGGER.warn("Sub-wiki folder '{}' does not exist; skipping.", repoDir.getAbsolutePath());
			return null;
		}
		String repoPath = repoDir.getAbsolutePath();
		if (!new File(repoDir, ".git").isDirectory()) {
			// ensure a repo exists
			RawGitExecutor.executeGitCommand("git init", repoPath);
			LOGGER.info("Initialized new git repository at '{}'.", repoPath);
		}
		GitConnector connector = new CachingGitConnector(JGitBackedGitConnector.fromPath(repoPath));
		// build the commit-graph to accelerate git-log reads (matches the single-wiki provider)
		connector.repo().executeCommitGraph();
		GitPageHistory history = new GitPageHistory(connector);
		// the folder -> history mapping is established by the computeIfAbsent caller (initialize / historyFor), so a
		// concurrent first access to the same runtime-added folder builds exactly one connector
		historyByRepoKey.put(history.repoKey(), history);
		return history;
	}

	/**
	 * Routes a (possibly prefixed) page name to the {@link GitPageHistory} of its sub-wiki repo. Folders added at
	 * runtime (e.g. by {@code CloneRepositoryAction}) are picked up lazily on first access.
	 */
	private GitPageHistory historyFor(String fullPageName) throws ProviderException {
		String folder = SubWikiUtils.getSubFolderNameOfPage(fullPageName, m_engine.getWikiProperties());
		// atomic per folder: concurrent first accesses to a runtime-added sub-wiki build exactly one connector
		GitPageHistory history = historyByFolder.computeIfAbsent(folder, this::createHistory);
		if (history == null) {
			throw new ProviderException("No git repository for page '" + fullPageName + "' (sub-wiki folder '" + folder + "').");
		}
		return history;
	}

	private String localName(String fullPageName) {
		return SubWikiUtils.getLocalPageName(fullPageName);
	}

	// --- write path ----------------------------------------------------------

	@Override
	public void putPageText(Page page, String text) throws ProviderException {
		String fullName = page.getName();
		File file = findPage(fullName);
		boolean addFile = !file.exists();

		if (sameTextContent(text, file)) {
			return;
		}
		// writes the file into the correct sub-wiki folder (inherited routing)
		super.putPageText(page, text);
		page.setSize(file.length());

		GitPageHistory history = historyFor(fullName);
		String user = page.getAuthor();
		if (batchRegistry.isOpen(user)) {
			// part of an open transaction: stage, the batch commits one commit per repo on commit()
			if (addFile) {
				history.stageForBatch(file.getName());
			}
			batchRegistry.stage(user, history.repoKey(), file.getName());
		}
		else {
			CommitUserData userData = getUserData(user, resolveComment(page, addFile));
			String commitHash = history.commitPut(file, userData);
			if (commitHash != null) {
				fireWikiEvent(GitVersioningWikiEvent.UPDATE, user, List.of(fullName), commitHash);
			}
		}
	}

	private boolean sameTextContent(String text, File file) {
		if (file.exists() && text != null) {
			try {
				return text.equals(Files.readString(file.toPath()));
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	private String resolveComment(Page page, boolean addFile) {
		String comment = gitCommentStrategy.getComment(page, "");
		if (comment.isEmpty()) {
			return addFile ? "Added page" : "-";
		}
		return comment;
	}

	// --- read path -----------------------------------------------------------

	@Override
	public List<Page> getVersionHistory(String pageName) throws ProviderException {
		GitPageHistory history = historyFor(pageName);
		List<GitPageVersion> versions = history.history(localName(pageName));
		List<Page> result = new ArrayList<>(versions.size());
		for (GitPageVersion v : versions) {
			result.add(WikiPageProxy.fromUserData(pageName, v.version(), v.userData(), v.size(), v.date(), m_engine));
		}
		return result;
	}

	@Override
	public String getPageText(String pageName, int version) throws ProviderException {
		try {
			return historyFor(pageName).textAtVersion(localName(pageName), version);
		}
		catch (IOException e) {
			throw new ProviderException("Could not read text of '" + pageName + "' v" + version + ": " + e.getMessage());
		}
	}

	@Override
	public Page getPageInfo(String pageName, int version) throws ProviderException {
		GitPageHistory history = historyFor(pageName);
		GitPageVersion v = history.infoAt(localName(pageName), version);
		if (v != null) {
			return WikiPageProxy.fromUserData(pageName, v.version(), v.userData(), v.size(), v.date(), m_engine);
		}
		// not in git (yet): a file staged in an open batch but not committed, serve it from the filesystem
		File file = findPage(pageName);
		if (file.exists()) {
			WikiPageProxy page = new WikiPageProxy(m_engine, pageName);
			page.setHistoryProvider(history.connector());
			page.setVersion(WikiProvider.LATEST_VERSION);
			page.setSize(file.length());
			page.setLastModified(new java.util.Date(file.lastModified()));
			return page;
		}
		return null;
	}

	@Override
	public boolean pageExists(String page, int version) {
		if (!pageExists(page)) {
			return false;
		}
		if (version == WikiProvider.LATEST_VERSION) {
			return true;
		}
		try {
			int count = historyFor(page).versionCount(localName(page));
			return version > 0 && version <= count;
		}
		catch (ProviderException e) {
			LOGGER.error("Could not check existence of '{}' v{}.", page, version, e);
			return false;
		}
	}

	// --- delete / move -------------------------------------------------------

	@Override
	public void deletePage(Page page) throws ProviderException {
		String fullName = page.getName();
		File file = findPage(fullName);
		if (!file.exists()) {
			LOGGER.info("Delete requested for non-existing page '{}'; nothing to do.", fullName);
			return;
		}
		GitPageHistory history = historyFor(fullName);
		String author = authorOrLatest(page, history);
		String comment = gitCommentStrategy.getComment(page, "removed page");
		// Deletes commit immediately, even inside an open transaction (documented divergence from batching). A page
		// delete within a bulk transaction is rare, keeping it immediate avoids staging a removal into the batch.
		String commitHash = history.commitDelete(file, getUserData(author, comment));
		if (commitHash != null) {
			fireWikiEvent(GitVersioningWikiEvent.DELETE, author, List.of(fullName), commitHash);
		}
	}

	@Override
	public void deleteVersion(Page page, int version) throws ProviderException {
		throw new ProviderException(
				"Deleting a single version is not supported by git-backed history. Use revert (RevertLocalFileChangeAction) to undo a change."
		);
	}

	@Override
	public void movePage(Page from, String to) throws ProviderException {
		String fromName = from.getName();
		Properties props = m_engine.getWikiProperties();
		String fromFolder = SubWikiUtils.getSubFolderNameOfPage(fromName, props);
		String toFolder = SubWikiUtils.getSubFolderNameOfPage(to, props);
		if (!fromFolder.equals(toFolder)) {
			throw new ProviderException(
					"Moving a page across sub-wikis is not supported (each sub-wiki is its own git repository): '" + fromName + "' -> '" + to + "'."
			);
		}
		GitPageHistory history = historyFor(fromName);
		File fromFile = findPage(fromName);
		File toFile = findPage(to);
		String author = authorOrLatest(from, history);
		String comment = gitCommentStrategy.getComment(from, "renamed page " + fromName + " to " + to);
		try {
			String commitHash = history.commitMove(fromFile, toFile, getUserData(author, comment));
			if (commitHash != null) {
				fireWikiEvent(GitVersioningWikiEvent.MOVED, author, List.of(to), commitHash);
			}
		}
		catch (IOException e) {
			throw new ProviderException("Can't move page '" + fromName + "' to '" + to + "': " + e.getMessage());
		}
	}

	/**
	 * Returns the page's author, or, if unset, the author of the page's latest commit.
	 */
	private String authorOrLatest(Page page, GitPageHistory history) {
		String author = page.getAuthor();
		if (author == null) {
			GitPageVersion latest = history.infoAt(localName(page.getName()), WikiProvider.LATEST_VERSION);
			if (latest != null) {
				author = latest.userData().user;
				page.setAuthor(author);
			}
		}
		return author;
	}

	// --- transaction batching (GitVersioningProvider) ------------------------

	@Override
	public void openCommit(String user) {
		batchRegistry.open(user);
	}

	@Override
	public void commit(String user, String commitMessage) {
		String comment = gitCommentStrategy.getCommentForUser(user);
		if (comment.isEmpty()) {
			comment = commitMessage;
		}
		CommitUserData userData = getUserData(user, comment);
		List<GitCommitBatchRegistry.RepoCommitResult> results = batchRegistry.commit(user, userData.message, userData.user, userData.email);
		for (GitCommitBatchRegistry.RepoCommitResult result : results) {
			Collection<String> pages = globalPageNames(result.repoKey(), result.paths());
			fireWikiEvent(GitVersioningWikiEvent.UPDATE, user, pages, result.commitHash());
			refreshCache(pages);
		}
	}

	@Override
	public void rollback(String user) {
		List<GitCommitBatchRegistry.RepoRollbackResult> results = batchRegistry.rollback(user);
		// the staged files were restored on disk, so the JSPWiki page cache and Lucene must drop the discarded edits
		for (GitCommitBatchRegistry.RepoRollbackResult result : results) {
			Collection<String> pages = globalPageNames(result.repoKey(), result.paths());
			refreshCache(pages);
			LOGGER.info("Rolled back batch of user '{}' in repo '{}' ({} path(s)).",
					user, result.repoKey(), result.paths().size());
		}
	}

	// --- accessors shared with the attachment provider -----------------------
	// The git attachment provider routes to the SAME GitPageHistory instances and the SAME batch registry, so that
	// attachment changes share the page repo, the per-repo commit lock, and (within a transaction) the same commit.

	/**
	 * Routes a (possibly prefixed) page name to its sub-wiki {@link GitPageHistory}. Public for the attachment
	 * provider.
	 */
	GitPageHistory gitHistoryForPage(String fullPageName) throws ProviderException {
		return historyFor(fullPageName);
	}

	/**
	 * The shared batch registry, so attachment changes join the same per-repo transaction commit.
	 */
	GitCommitBatchRegistry getBatchRegistry() {
		return batchRegistry;
	}

	GitCommentStrategy getGitCommentStrategy() {
		return gitCommentStrategy;
	}

	/**
	 * Resolves author/email/message exactly as the page path does (user-profile lookup, SSO-email parsing).
	 */
	CommitUserData resolveUserData(String author, String comment) {
		return getUserData(author, comment);
	}

	/**
	 * Maps a repo key + repo-relative file paths back to global (prefixed) page names.
	 */
	private Collection<String> globalPageNames(String repoKey, Set<String> paths) {
		String folder = folderForRepoKey(repoKey);
		List<String> names = new ArrayList<>(paths.size());
		for (String path : paths) {
			String fileName = new File(path).getName();
			int cut = fileName.lastIndexOf(AbstractFileProvider.FILE_EXT);
			String localName = unmangleName(cut >= 0 ? fileName.substring(0, cut) : fileName);
			names.add(SubWikiUtils.concatSubWikiAndLocalPageName(folder, localName, m_engine.getWikiProperties()));
		}
		return names;
	}

	@Nullable
	private String folderForRepoKey(String repoKey) {
		for (Map.Entry<String, GitPageHistory> entry : historyByFolder.entrySet()) {
			if (entry.getValue().repoKey().equals(repoKey)) {
				return entry.getKey();
			}
		}
		return null;
	}

	private void refreshCache(Collection<String> pageNames) {
		PageManager pm = m_engine.getManager(PageManager.class);
		for (String pageName : pageNames) {
			Page page = getRefreshPage(pageName);
			try {
				// only evicts the page from the cache, the underlying provider delete is a no-op here
				pm.deleteVersion(page);
			}
			catch (ProviderException e) {
				LOGGER.error("Could not refresh cache for '{}'.", pageName, e);
			}
		}
	}

	private Page getRefreshPage(String pageName) {
		Page page = new org.apache.wiki.WikiPage(m_engine, pageName);
		page.setVersion(WikiProvider.LATEST_VERSION);
		return page;
	}

	// --- user data (ported from the single-wiki delegate) --------------------

	private CommitUserData getUserData(String author, String comment) {
		UserProfile userProfile = getUserProfile(author);
		String userName = author;
		String email = "";
		if (userProfile != null) {
			userName = userProfile.getFullname();
			email = userProfile.getEmail();
		}
		// SSO often makes the author an email address, derive a readable name and use the address as the email
		if (userName != null && userName.contains("@")) {
			String local = author.split("@")[0];
			if (local.contains(".")) {
				userName = capitalize(local.split("\\.")[0]) + " " + capitalize(local.split("\\.")[1]);
			}
			else {
				userName = local;
			}
			email = author;
		}
		return new CommitUserData(userName, email, comment);
	}

	@Nullable
	private UserProfile getUserProfile(String user) {
		try {
			return m_engine.getManager(UserManager.class).getUserDatabase().findByFullName(user);
		}
		catch (NoSuchPrincipalException e) {
			LOGGER.debug("No user profile for '{}'; using raw author for the commit.", user);
			return null;
		}
	}

	private String capitalize(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
	}

	private void fireWikiEvent(int event, String author, Collection<String> pages, String commitHash) {
		WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, event, author, pages, commitHash));
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningFileProviderMultiWiki.class.getSimpleName();
	}
}
