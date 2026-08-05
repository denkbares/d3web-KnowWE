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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.structs.DefaultPageIdentifier;
import org.apache.wiki.structs.PageIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitFileRevision;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusResultSuccess;

/**
 * The gateway to the wiki's git repository: the one object through which the repository is read and mutated. It owns
 * the repository's {@link GitConnector}, the commit lock that serializes every working-tree mutation (page and
 * attachment commits, deletes, moves, batch closes, the sweep-up reconciliation), and the eager {@link GitRepoIndex}
 * the lock-free history reads are served from.
 * <p>
 * The logic is ported from {@code GitVersioningFileProviderDelegate} but deliberately kept engine-free: it receives an
 * already-resolved {@link CommitUserData} (author, email, message) and returns engine-free {@link GitPageVersion}
 * values. User-profile lookup, comment-strategy resolution, JSPWiki {@code Page} construction, wiki-event firing and
 * cache refresh all stay in the provider (see {@link WikiGitContext}). That keeps this class unit-testable against a
 * bare temp repository, and is a deliberate divergence from the old delegate (which mixed all of those concerns into
 * one class).
 * <p>
 * Batching state is <em>not</em> held here: when a user has an open transaction the provider stages paths into the
 * {@link GitCommitBatchRegistry}, which closes the batch through {@link #commitBatch}/{@link #rollbackPaths} under
 * this repository's commit lock. This class only performs the immediate, single-operation commits.
 */
public class GitWikiRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitWikiRepository.class);

	private final GitConnector connector;
	private final String repoPath;
	private final ReentrantLock commitLock = new ReentrantLock();
	private final GitRepoIndex index;

	public GitWikiRepository(GitConnector connector) {
		this.connector = connector;
		this.repoPath = connector.repo().getGitDirectory();
		this.index = new GitRepoIndex(connector);
	}

	/**
	 * The eager index this repository reads history and page info from. The provider uses it for the bulk
	 * {@code getAllPages} / {@code getAllChangedSince} paths so those cost one git walk, not one per page.
	 */
	public GitRepoIndex index() {
		return index;
	}

	/**
	 * The working-tree path of this repository.
	 */
	public String path() {
		return repoPath;
	}

	/**
	 * The connector this repository owns. This is the sanctioned external access point (exposed by the provider as
	 * {@code getGitConnector()}); internal callers should prefer the dedicated methods of this class.
	 */
	public GitConnector connector() {
		return connector;
	}

	/**
	 * Runs the given action while holding this repository's commit lock, so no commit, sweep, delete or move can
	 * interleave with it. This is the sanctioned "I need the repo quiescent" entry point. Providers bracket a page's
	 * file write and its commit with it, so a concurrent sweep cannot commit a half-finished save under the wrong
	 * author. A future pull/fetch+merge after a rejected push belongs here too, combined with {@link #sweepUp} before
	 * the branch operation. The lock is reentrant, actions may call the commit methods of this class.
	 */
	public <T> T withCommitLock(Callable<T> action) throws Exception {
		commitLock.lock();
		try {
			return action.call();
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Commits the given already-staged/tracked paths as one commit, returning the commit hash, or {@code null} if none
	 * of the paths had changes left to commit. This is the closing commit of a transaction batch, taken under the same
	 * commit lock as the immediate commits, so a batch close cannot interleave with a concurrent save, delete, move or
	 * sweep of this repository.
	 *
	 * @param paths    the repo-relative paths staged for the batch
	 * @param userData resolved author, email and commit message
	 */
	@Nullable
	public String commitBatch(Set<String> paths, CommitUserData userData) {
		commitLock.lock();
		try {
			return connector.commit().commitPathsForUser(userData.message, userData.user, userData.email, paths);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Restores the working-tree state of the given repo-relative paths, discarding their uncommitted changes. This is
	 * the rollback of a transaction batch, taken under the commit lock for the same reason as {@link #commitBatch}.
	 */
	public void rollbackPaths(Set<String> paths) {
		commitLock.lock();
		try {
			connector.rollback().rollbackPaths(paths);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Commits a single just-written file immediately (page or attachment), returning the commit hash, or {@code null}
	 * if there was nothing to commit (unchanged content) or the path is git-ignored. Callers that are batching must
	 * not use this; they stage into the registry instead.
	 *
	 * @param file             the file on disk (already written by the caller)
	 * @param repoRelativePath the file's path relative to the repository root, the flat file name for pages and
	 *                         {@code <page>-att/<file>} for attachments
	 * @param userData         resolved author, email and commit message
	 */
	@Nullable
	public String commitFile(File file, String repoRelativePath, CommitUserData userData) {
		commitLock.lock();
		try {
			if (connector.isIgnored(repoRelativePath)) {
				// guard against a file added to .gitignore but never untracked (ported life-saver)
				untrackIgnoredFile(repoRelativePath);
				return null;
			}
			// changePath stages and commits, on unchanged content git finds nothing to commit and returns null
			return connector.commit().changePath(file.toPath(), userData);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Stages a newly created file in the git index so a subsequent batch commit ({@code commitPathsForUser}) picks it
	 * up. For files already tracked by git this is unnecessary, a batch commit by path includes tracked modifications.
	 * Package-private: only {@link GitCommitBatchRegistry#stage} calls this, as part of staging a new file.
	 */
	void stageInIndex(String repoRelativePath) {
		commitLock.lock();
		try {
			connector.commit().addPath(repoRelativePath);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Whether the given repo-relative path is git-ignored. Ignored files exist on disk but have no git history, the
	 * providers serve them from the filesystem.
	 */
	public boolean isIgnored(String repoRelativePath) {
		return connector.isIgnored(repoRelativePath);
	}

	/**
	 * The raw content of the file at the given version, read from git. Used for attachment data, which unlike page
	 * text has no text encoding.
	 */
	public InputStream bytesAtVersion(String repoRelativePath, int version) {
		return new ByteArrayInputStream(connector.log().getBytesForPath(repoRelativePath, version));
	}

	/**
	 * The size in bytes of the file as of the given commit ({@code git cat-file -s}, cached per commit+path by the
	 * connector). Fetched lazily because the {@code git log --name-status} index walk does not report sizes.
	 */
	public long fileSizeAt(String commitHash, String repoRelativePath) {
		return connector.log().getFilesizeForCommit(commitHash, repoRelativePath);
	}

	/**
	 * Full version history of the page, newest version first. Version numbers are branch-relative positions in the
	 * file's git log, 1 = oldest. Returns an empty list if the file does not exist.
	 */
	public List<GitPageVersion> history(String pageName) {
		DefaultPageIdentifier id = PageIdentifier.fromPagename(repoPath, pageName, -1);
		File file = id.accordingFile();
		if (file == null || !file.exists()) {
			return Collections.emptyList();
		}
		// The index serves the per-file revisions (author/email/time/message) from one whole-repo walk. The list is
		// newest-first and version numbers are oldest-first (1 = oldest), so the newest commit gets version = count.
		List<GitFileRevision> revisions = index.revisionsNewestFirst(id.fileName());
		int count = revisions.size();
		List<GitPageVersion> versions = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			versions.add(buildVersion(id.fileName(), revisions.get(i), count - i));
		}
		if (versions.isEmpty()) {
			LOGGER.error("File '{}' exists but git reports no version of it.", id.fileName());
		}
		// already newest-first (revisions were newest-first)
		return versions;
	}

	/**
	 * Maps an index revision to a {@link GitPageVersion}. Author/email/time/message come from the index (free after the
	 * one walk); only the file size is fetched lazily via {@link #fileSizeAt}.
	 */
	private GitPageVersion buildVersion(String fileName, GitFileRevision revision, int version) {
		return new GitPageVersion(
				version,
				revision.commitHash(),
				revision.userData(),
				fileSizeAt(revision.commitHash(), fileName),
				Date.from(Instant.ofEpochSecond(revision.timeSeconds()))
		);
	}

	/**
	 * Info about a single version of the page without computing the full history, one commit lookup. Returns
	 * {@code null} if the page does not exist on disk or git has no commit for the requested version (e.g. the file is
	 * staged in an open batch but not yet committed, the caller decides how to fall back).
	 *
	 * @param version a 1-based version number, or {@link WikiProvider#LATEST_VERSION} for the latest
	 */
	@Nullable
	public GitPageVersion infoAt(String pageName, int version) {
		DefaultPageIdentifier id = PageIdentifier.fromPagename(repoPath, pageName, version);
		if (!id.exists()) {
			return null;
		}
		String fileName = id.fileName();
		// single index read (one HEAD check): the per-file list yields both the requested revision and the version
		// count, so getPageInfo(LATEST) no longer does two HEAD checks (and can't observe two different HEADs).
		List<GitFileRevision> revisions = index.revisionsNewestFirst(fileName);
		if (revisions.isEmpty()) {
			return null;
		}
		int count = revisions.size();
		GitFileRevision revision;
		int realVersion;
		if (version == WikiProvider.LATEST_VERSION) {
			revision = revisions.get(0);
			realVersion = count;
		}
		else if (version >= 1 && version <= count) {
			// newest-first list: oldest-first version v is at index count - v
			revision = revisions.get(count - version);
			realVersion = version;
		}
		else {
			return null;
		}
		return buildVersion(fileName, revision, realVersion);
	}

	/**
	 * Number of committed versions of the page in git, 0 if none / not tracked.
	 */
	public int versionCount(String pageName) {
		DefaultPageIdentifier id = PageIdentifier.fromPagename(repoPath, pageName, -1);
		if (!id.exists()) {
			return 0;
		}
		return index.versionCount(id.fileName());
	}

	/**
	 * Whole-repo revisionsByFile, repo-relative file path to its newest-first revisions, from one git walk (cached
	 * with HEAD-diff invalidation). The provider drives both {@code getAllPages} (latest = first of each list, version
	 * count = list size) and {@code getAllChangedSince} (revisions newer than a date) off this, so neither costs a
	 * per-page git call.
	 */
	public Map<String, List<GitFileRevision>> revisionsByFile() {
		return index.revisionsByFile();
	}

	/**
	 * Text of the page at the given version. The latest version is read from disk (fast), older versions are read from
	 * git. Returns {@code null} if the page file does not exist. Text is decoded as UTF-8 on both paths (the git side
	 * has no other choice), so providers on top of this class must ensure {@code jspwiki.encoding} is UTF-8.
	 *
	 * @param version a 1-based version number, or {@link WikiProvider#LATEST_VERSION} for the working-tree content
	 */
	@Nullable
	public String textAtVersion(String pageName, int version) throws IOException {
		DefaultPageIdentifier id = PageIdentifier.fromPagename(repoPath, pageName, version);
		File pageFile = id.accordingFile();
		if (pageFile == null || !pageFile.exists()) {
			LOGGER.info("Page text requested for non-existing file '{}'.", pageName);
			return null;
		}
		if (version == WikiProvider.LATEST_VERSION) {
			return Files.readString(pageFile.toPath());
		}
		return connector.log().getTextForPath(id.fileName(), version);
	}

	/**
	 * Deletes a file (working-tree file and from git history going forward) and commits the removal, returning the
	 * commit hash. Disk deletion and commit happen under the commit lock, so no sweep can interleave and commit the
	 * half-done delete under the wrong author. History of the deleted file is preserved in git.
	 */
	@Nullable
	public String commitDelete(File file, String repoRelativePath, CommitUserData userData) {
		commitLock.lock();
		try {
			if (!file.delete()) {
				LOGGER.warn("Failed to delete file on disk: {}", repoRelativePath);
			}
			return removeFile(repoRelativePath, userData);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Removes an already-deleted path from git and commits, returning the commit hash (or {@code null} if the path is
	 * git-ignored). The caller is responsible for having deleted the working-tree file, inside a
	 * {@link #withCommitLock} bracket that spans both steps.
	 */
	@Nullable
	public String removeFile(String repoRelativePath, CommitUserData userData) {
		commitLock.lock();
		try {
			if (connector.isIgnored(repoRelativePath)) {
				return null;
			}
			return connector.commit().deletePath(repoRelativePath, userData, false);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Commits a set of moved paths as one commit: the (already-moved-on-disk) old paths are removed from the index and
	 * the new paths added. Used by the attachment provider's {@code moveAttachmentsForPage}, which moves the directory
	 * and commits inside one {@link #withCommitLock} bracket. Returns the commit hash.
	 */
	@Nullable
	public String commitMovedPaths(List<String> removedRelPaths, List<String> addedRelPaths, CommitUserData userData) {
		commitLock.lock();
		try {
			if (!removedRelPaths.isEmpty()) {
				// cached = index only; the working-tree files were already moved by the caller
				connector.commit().deletePaths(removedRelPaths, userData, true);
			}
			if (!addedRelPaths.isEmpty()) {
				connector.commit().addPaths(addedRelPaths);
			}
			return connector.commit().commitForUser(userData);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Commits the removal of paths whose working-tree files the caller has already deleted (inside the same
	 * {@link #withCommitLock} bracket that deleted them), as one commit. Returns the commit hash, or {@code null} if
	 * none of the paths was tracked.
	 */
	@Nullable
	public String commitRemovedPaths(List<String> removedRelPaths, CommitUserData userData) {
		commitLock.lock();
		try {
			// deletePaths stages the removals and commits them itself (cached = the files are already gone on disk)
			return connector.commit().deletePaths(removedRelPaths, userData, true);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Moves/renames a page on disk and commits the move, returning the commit hash. History restarts at the rename
	 * commit (no {@code --follow}), the accepted, pinned behavior of the git providers.
	 */
	public String commitMove(File fromFile, File toFile, CommitUserData userData) throws IOException {
		commitLock.lock();
		try {
			moveCaseSafe(fromFile, toFile);
			return connector.commit().moveFile(fromFile.toPath(), toFile.toPath(),
					userData.user, userData.email, userData.message);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Moves a file or directory, bouncing case-only renames through a temp name so case-insensitive filesystems do not
	 * no-op the move. Callers that commit the move afterwards must run both steps under {@link #withCommitLock}.
	 */
	public static void moveCaseSafe(File from, File to) throws IOException {
		if (from.getName().equalsIgnoreCase(to.getName())) {
			File tmp = new File(to.getParentFile(), to.getName() + "_tmp");
			Files.move(from.toPath(), tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.move(tmp.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		else {
			Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Self-heals a dirty working tree: stages everything currently changed/untracked/removed and commits it as a single
	 * reconciliation commit. A no-op when the tree is clean. Run at provider startup and before branch operations
	 * (switch/pull/merge), which require a clean tree.
	 *
	 * @param reason short description of why the sweep runs, used in the commit message
	 * @return the reconciliation commit and the swept paths, or {@code null} if the tree was already clean
	 */
	@Nullable
	public SweepUp sweepUp(String reason) {
		commitLock.lock();
		try {
			GitStatusCommandResult status = connector.status().get();
			if (!(status instanceof GitStatusResultSuccess result)) {
				LOGGER.warn("Could not read git status of '{}' for sweep-up ({}); skipping.", repoPath, reason);
				return null;
			}
			List<String> affected = result.getAffectedFiles();
			if (affected.isEmpty()) {
				return null;
			}
			LOGGER.info("Sweep-up of '{}' ({}): reconciling {} dirty path(s).", repoPath, reason, affected.size());
			connector.commit().addPaths(affected);
			String commitHash = connector.commit().commitForUser(
					new CommitUserData("system", "system@denkbares.com", "[reconciliation] " + reason));
			return commitHash == null ? null : new SweepUp(commitHash, List.copyOf(affected));
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Outcome of a sweep-up reconciliation: the commit and the repo-relative paths it swept up, so the caller can fire
	 * complete commit notifications for it.
	 */
	public record SweepUp(String commitHash, List<String> paths) {
	}

	private void untrackIgnoredFile(String path) {
		GitStatusCommandResult status = connector.status().get();
		if (!(status instanceof GitStatusResultSuccess result) || !result.getChangedFiles().contains(path)) {
			return;
		}
		if (!connector.commit().untrackPath(path)) {
			return;
		}
		// untracking left a staged deletion, commit it on its own if it is the only change
		status = connector.status().get();
		if (status instanceof GitStatusResultSuccess innerResult
				&& innerResult.getRemovedFiles().contains(path)
				&& innerResult.getAffectedFiles().size() == 1) {
			connector.commit().commitForUser(
					new CommitUserData("system", "system@denkbares.com", "Untrack: " + path));
			LOGGER.info("Untracked already-ignored file: {}", path);
		}
	}

	@NotNull
	@Override
	public String toString() {
		return "GitWikiRepository[" + repoPath + "]";
	}
}
