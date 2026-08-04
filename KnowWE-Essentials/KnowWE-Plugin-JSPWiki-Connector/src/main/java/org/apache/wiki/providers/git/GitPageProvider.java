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

package org.apache.wiki.providers.git;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.providers.AbstractFileProvider;
import org.apache.wiki.providers.BasicAttachmentProvider;
import org.apache.wiki.providers.GitVersioningProvider;
import org.apache.wiki.structs.WikiPageProxy;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.event.GitCommitEvent;
import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitFileRevision;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

/**
 * Git-backed page provider for a single-wiki instance: the page directory is one flat git repository, every save is
 * one commit authored by the saving user, and history is served from git log instead of the {@code OLD/} directory
 * mechanism. The provider composes the per-repository components of this package, one {@link GitPageHistory} for the
 * git side of every operation, one {@link GitCommitBatchRegistry} for transaction batching, and one
 * {@link WikiGitContext} for everything engine facing (commit author resolution, comment strategy, event firing,
 * cache eviction). It commits but never pushes; push policy belongs to the async push listener driven by the
 * {@link GitCommitEvent}s fired here.
 * <p>
 * All repository mutations run under the repository's commit lock. A page save brackets the file write and its commit
 * in {@link GitPageHistory#withCommitLock}, so no sweep, delete or move can interleave between the two, and closing a
 * transaction batch commits through the same lock. A dirty working tree (crash during a save) is self-healed at
 * startup by a sweep-up reconciliation commit.
 * <p>
 * Deliberate behavioral divergences from the older {@code GitVersioningFileProvider}: {@code deleteVersion} throws
 * (revert is the undo path), {@code getPageInfo(LATEST)} carries no change note (a previous commit message must not
 * round-trip into the next commit), delete and move commit immediately even inside an open transaction, and
 * {@code getAllChangedSince} works.
 *
 * @see GitPageHistory
 * @see GitCommitBatchRegistry
 * @see GitAttachmentProvider
 */
public class GitPageProvider extends AbstractFileProvider implements GitVersioningProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitPageProvider.class);

	private GitPageHistory history;
	private GitCommitBatchRegistry batchRegistry;
	private WikiGitContext context;

	@Override
	public void initialize(Engine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		// the base class resolves and creates the page directory, which is the repository's working tree
		super.initialize(engine, properties);
		this.context = new WikiGitContext(engine, properties);
		if (!StandardCharsets.UTF_8.equals(Charset.forName(m_encoding))) {
			// the git side (text-at-version, the mangled attachment paths) can only decode UTF-8, a different page
			// encoding would corrupt every non-ASCII page name and history read in ways that look like git problems
			LOGGER.error("The git-backed providers require '{}'=UTF-8, but it is '{}'. Fix the wiki properties.",
					Engine.PROP_ENCODING, m_encoding);
		}

		String repoPath = new File(m_pageDirectory).getAbsolutePath();
		if (!new File(repoPath, ".git").isDirectory()) {
			RawGitExecutor.executeGitCommand(new String[] { "git", "init" }, repoPath);
			LOGGER.info("Initialized new git repository at '{}'.", repoPath);
		}
		GitConnector connector = new CachingGitConnector(JGitBackedGitConnector.fromPath(repoPath));
		// build the commit-graph to accelerate git-log reads
		connector.repo().executeCommitGraph();
		this.history = new GitPageHistory(connector);
		this.batchRegistry = new GitCommitBatchRegistry(history);

		// self-heal a dirty working tree (crash during a save) with one reconciliation commit
		GitPageHistory.SweepUp sweepUp = history.sweepUp("provider-startup");
		if (sweepUp != null) {
			context.fireCommitted(this, GitVersioningWikiEvent.UPDATE, "system", wikiNames(sweepUp.paths()),
					sweepUp.commitHash(), history.repoKey(), GitCommitEvent.Origin.RECONCILIATION);
		}
		LOGGER.info("Initialized {} on repository '{}'.", getClass().getSimpleName(), repoPath);
	}

	// --- write path ----------------------------------------------------------

	@Override
	public void putPageText(Page page, String text) throws ProviderException {
		File file = findPage(page.getName());
		// bracket the file write and its commit, so no concurrent sweep, delete or move can interleave in between
		// (a sweep would otherwise commit the half-finished save as a reconciliation commit with the wrong author)
		try {
			history.withCommitLock(() -> {
				putPageTextLocked(page, text, file);
				return null;
			});
		}
		catch (ProviderException e) {
			throw e;
		}
		catch (IOException e) {
			throw new ProviderException("Could not save page '" + page.getName() + "': " + e.getMessage(), e);
		}
		catch (Exception e) {
			throw new ProviderException("Could not save page '" + page.getName() + "': " + e);
		}
	}

	private void putPageTextLocked(Page page, String text, File file) throws ProviderException, IOException {
		boolean addFile = !file.exists();
		if (sameTextContent(text, file)) {
			return;
		}
		super.putPageText(page, text);
		page.setSize(file.length());

		String user = page.getAuthor();
		if (batchRegistry.isOpen(user)) {
			// part of an open transaction: stage, the batch commits once on commit()
			if (addFile) {
				history.stageForBatch(file.getName());
			}
			batchRegistry.stage(user, file.getName());
		}
		else {
			// "use the page's change note, else this operation's default", same seam as delete/move. The default is
			// a meaningful message (not the connector's catch-all "Added page" for every put), a custom
			// GitCommentStrategy can override it (e.g. to prefix the branch).
			String comment = context.commentStrategy()
					.getComment(page, addFile ? "Added page" : "Edited " + page.getName());
			CommitUserData userData = context.userData(user, comment);
			String commitHash = history.commitPut(file, userData);
			if (commitHash != null) {
				context.fireCommitted(this, GitVersioningWikiEvent.UPDATE, user, List.of(page.getName()),
						commitHash, history.repoKey());
			}
		}
	}

	private boolean sameTextContent(String text, File file) throws IOException {
		// compare bytes, not decoded strings: the on-disk encoding is m_encoding, and a decode with the wrong charset
		// would fail the save instead of just re-writing the file
		return file.exists() && text != null && Arrays.equals(text.getBytes(m_encoding), Files.readAllBytes(file.toPath()));
	}

	// --- read path -----------------------------------------------------------

	@Override
	public List<Page> getVersionHistory(String pageName) throws ProviderException {
		List<GitPageVersion> versions = history.history(pageName);
		List<Page> result = new ArrayList<>(versions.size());
		for (GitPageVersion v : versions) {
			result.add(WikiPageProxy.fromUserData(pageName, v.version(), v.userData(), v.size(), v.date(), m_engine));
		}
		return result;
	}

	@Override
	public String getPageText(String pageName, int version) throws ProviderException {
		try {
			return history.textAtVersion(pageName, version);
		}
		catch (IOException e) {
			throw new ProviderException("Could not read text of '" + pageName + "' v" + version + ": " + e.getMessage());
		}
	}

	@Override
	public Page getPageInfo(String pageName, int version) throws ProviderException {
		GitPageVersion gitVersion = history.infoAt(pageName, version);
		if (gitVersion != null) {
			Page page = WikiPageProxy.fromUserData(pageName, gitVersion.version(), gitVersion.userData(),
					gitVersion.size(), gitVersion.date(), m_engine);
			if (version == WikiProvider.LATEST_VERSION) {
				// The current/live page is the basis for the next save or delete. It must NOT carry a change note:
				// ChangeNoteStrategy would otherwise hand that note straight back as the next commit's message, so the
				// previous commit's message would silently round-trip into the new commit. Per-version history entries
				// (getVersionHistory) keep their commit message as the change note for display.
				page.removeAttribute(Page.CHANGENOTE);
			}
			return page;
		}
		// not in git (yet): a file staged in an open batch but not committed, serve it from the filesystem
		File file = findPage(pageName);
		if (file.exists()) {
			WikiPageProxy page = new WikiPageProxy(m_engine, pageName);
			page.setHistoryProvider(history.connector());
			page.setVersion(WikiProvider.LATEST_VERSION);
			page.setSize(file.length());
			page.setLastModified(new Date(file.lastModified()));
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
		int count = history.versionCount(page);
		return version > 0 && version <= count;
	}

	// --- bulk read path (eager index, one git walk) ---------------------------

	/**
	 * Lists all pages with their latest author/date/version, enriched from the eager index instead of the inherited
	 * base which calls the git-backed {@link #getPageInfo} once per page (an O(pages) git-process storm at startup).
	 * Pages are enumerated from the filesystem (so deleted pages are correctly absent) and the repository's history is
	 * read once via {@link GitPageHistory#revisionsByFile()}, so the cost is O(1) git walks, not O(pages).
	 */
	@Override
	public Collection<Page> getAllPages() throws ProviderException {
		Map<String, List<GitFileRevision>> revisionsByFile = history.revisionsByFile();
		File[] wikiFiles = new File(m_pageDirectory).listFiles(new WikiFileFilter());
		if (wikiFiles == null) {
			throw new ProviderException("Page directory does not exist: " + m_pageDirectory);
		}
		Collection<Page> pages = new ArrayList<>(wikiFiles.length);
		for (File wikiFile : wikiFiles) {
			String fileName = wikiFile.getName();
			pages.add(buildListingPage(pageNameOfFile(fileName), wikiFile, revisionsByFile.get(fileName)));
		}
		return pages;
	}

	/**
	 * Builds the page-listing entry: size from disk (the on-disk file is the latest version), author/commit-time/
	 * change-note and version count from the index. Falls back to a filesystem-only page when the file is not yet in
	 * git (staged in an open batch, or untracked).
	 */
	private Page buildListingPage(String pageName, File wikiFile, @Nullable List<GitFileRevision> revisions) {
		if (revisions != null && !revisions.isEmpty()) {
			GitFileRevision latest = revisions.get(0);
			return WikiPageProxy.fromUserData(pageName, revisions.size(), latest.userData(), wikiFile.length(),
					Date.from(Instant.ofEpochSecond(latest.timeSeconds())), m_engine);
		}
		WikiPageProxy page = new WikiPageProxy(m_engine, pageName);
		page.setHistoryProvider(history.connector());
		page.setVersion(WikiProvider.LATEST_VERSION);
		page.setSize(wikiFile.length());
		page.setLastModified(new Date(wikiFile.lastModified()));
		return page;
	}

	/**
	 * All page changes since the given date, one entry per (page, commit) with that commit's author, change-note and
	 * date, served from the index (one git walk). Replaces the inherited base, which returns an empty list, so Recent
	 * Changes works on this provider. Attachment paths (those in a {@code <page>-att/} subdirectory) are skipped, only
	 * page files are reported. Deleted pages are deliberately included (the delete commit is a change and should show
	 * up in Recent Changes), which diverges from the filesystem-only enumeration of {@link #getAllPages()}.
	 */
	@Override
	public Collection<Page> getAllChangedSince(Date date) {
		long sinceSeconds = date.getTime() / 1000L;
		List<Page> changed = new ArrayList<>();
		for (Map.Entry<String, List<GitFileRevision>> fileEntry : history.revisionsByFile().entrySet()) {
			String path = fileEntry.getKey();
			if (path.contains("/") || !path.endsWith(FILE_EXT)) {
				// only top-level page files; attachments live in a <page>-att/ subdirectory
				continue;
			}
			String pageName = pageNameOfFile(path);
			List<GitFileRevision> revisions = fileEntry.getValue();
			for (int i = 0; i < revisions.size(); i++) {
				GitFileRevision revision = revisions.get(i);
				if (revision.timeSeconds() < sinceSeconds) {
					// newest-first: once we pass the cut-off, all remaining revisions of this file are older
					break;
				}
				// version is oldest-first (1 = oldest); newest-first index i maps to size - i
				changed.add(buildChangePage(pageName, revisions.size() - i, revision));
			}
		}
		return changed;
	}

	private Page buildChangePage(String pageName, int version, GitFileRevision revision) {
		org.apache.wiki.WikiPage page = new org.apache.wiki.WikiPage(m_engine, pageName);
		page.setVersion(version);
		page.setAuthor(revision.author());
		page.setLastModified(Date.from(Instant.ofEpochSecond(revision.timeSeconds())));
		page.setAttribute(Page.CHANGENOTE, revision.message());
		return page;
	}

	// --- delete / move -------------------------------------------------------

	@Override
	public void deletePage(Page page) throws ProviderException {
		String pageName = page.getName();
		File file = findPage(pageName);
		if (!file.exists()) {
			LOGGER.info("Delete requested for non-existing page '{}'; nothing to do.", pageName);
			return;
		}
		String author = authorOrLatest(page);
		String comment = context.commentStrategy().getComment(page, "Removed page");
		// Deletes commit immediately, even inside an open transaction (documented divergence from batching). A page
		// delete within a bulk transaction is rare, keeping it immediate avoids staging a removal into the batch.
		String commitHash = history.commitDelete(file, context.userData(author, comment));
		if (commitHash != null) {
			context.fireCommitted(this, GitVersioningWikiEvent.DELETE, author, List.of(pageName),
					commitHash, history.repoKey());
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
		File fromFile = findPage(fromName);
		File toFile = findPage(to);
		String author = authorOrLatest(from);
		String comment = context.commentStrategy().getComment(from, "Renamed page " + fromName + " to " + to);
		try {
			String commitHash = history.commitMove(fromFile, toFile, context.userData(author, comment));
			if (commitHash != null) {
				context.fireCommitted(this, GitVersioningWikiEvent.MOVED, author, List.of(to),
						commitHash, history.repoKey());
			}
		}
		catch (IOException e) {
			throw new ProviderException("Can't move page '" + fromName + "' to '" + to + "': " + e.getMessage());
		}
	}

	/**
	 * Returns the page's author, or, if unset, the author of the page's latest commit.
	 */
	private String authorOrLatest(Page page) {
		String author = page.getAuthor();
		if (author == null) {
			GitPageVersion latest = history.infoAt(page.getName(), WikiProvider.LATEST_VERSION);
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
		String comment = context.commentStrategy().getCommentForUser(user);
		if (comment.isEmpty()) {
			comment = commitMessage;
		}
		GitCommitBatchRegistry.CommitResult result = batchRegistry.commit(user, context.userData(user, comment));
		if (result != null) {
			Collection<String> pages = wikiNames(result.paths());
			context.fireCommitted(this, GitVersioningWikiEvent.UPDATE, user, pages, result.commitHash(), history.repoKey());
			context.evictPages(pages);
		}
	}

	@Override
	public void rollback(String user) {
		Set<String> restored = batchRegistry.rollback(user);
		if (!restored.isEmpty()) {
			// the staged files were restored on disk, so the JSPWiki page cache and Lucene must drop the discarded edits
			context.evictPages(wikiNames(restored));
			LOGGER.info("Rolled back batch of user '{}' ({} path(s)).", user, restored.size());
		}
	}

	// --- accessors shared with the attachment provider -----------------------
	// The git attachment provider routes to the SAME GitPageHistory, batch registry and context, so that attachment
	// changes share the repository, the commit lock, and (within a transaction) the same commit.

	GitPageHistory history() {
		return history;
	}

	GitCommitBatchRegistry batchRegistry() {
		return batchRegistry;
	}

	WikiGitContext context() {
		return context;
	}

	/**
	 * The git connector of the wiki repository, the sanctioned entry point for external git access (there is no raw
	 * JGit repository accessor by design).
	 */
	public GitConnector getGitConnector() {
		return history.connector();
	}

	/**
	 * Maps repo-relative file paths back to wiki names: a flat {@code .txt} file to its page name, an attachment path
	 * ({@code <page>-att/<file>}) to the JSPWiki attachment name {@code <page>/<file>}. The attachment layout
	 * knowledge is deliberate: batch and sweep results genuinely contain attachment paths, and their names must be
	 * mapped for the commit events and the cache eviction.
	 */
	private Collection<String> wikiNames(Collection<String> paths) {
		List<String> names = new ArrayList<>(paths.size());
		for (String path : paths) {
			File asFile = new File(path);
			String dir = asFile.getParent();
			if (dir != null && dir.endsWith(BasicAttachmentProvider.DIR_EXTENSION)) {
				// attachment paths were mangled by JSPUtils (hard UTF-8), so unmangle them the same way
				String parent = JSPUtils.unmangleName(
						dir.substring(0, dir.length() - BasicAttachmentProvider.DIR_EXTENSION.length()));
				names.add(parent + "/" + JSPUtils.unmangleName(asFile.getName()));
			}
			else {
				names.add(pageNameOfFile(asFile.getName()));
			}
		}
		return names;
	}

	private String pageNameOfFile(String fileName) {
		// strip the extension only as a suffix, a page may legitimately be called "Foo.txt"
		String base = fileName.endsWith(FILE_EXT)
				? fileName.substring(0, fileName.length() - FILE_EXT.length())
				: fileName;
		return unmangleName(base);
	}

	@Override
	public String getProviderInfo() {
		return GitPageProvider.class.getSimpleName();
	}
}
