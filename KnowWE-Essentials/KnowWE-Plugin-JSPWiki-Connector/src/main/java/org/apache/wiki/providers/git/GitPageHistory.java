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
 * Per-repository git history component which owns one repository's {@link GitConnector} and performs the git side of
 * page operations on it: commit-on-save, version to commit mapping, text-at-version, delete and move, and the
 * sweep-up reconciliation. One instance lives per repository, and it is provider agnostic, a provider delegates the
 * flat, single-repository operation here.
 * <p>
 * The logic is ported from {@code GitVersioningFileProviderDelegate} but deliberately kept engine-free: it receives an
 * already-resolved {@link CommitUserData} (author, email, message) and returns engine-free {@link GitPageVersion}
 * values. User-profile lookup, comment-strategy resolution, JSPWiki {@code Page} construction, wiki-event firing and
 * cache refresh all stay in the provider. That keeps this class unit-testable against a bare temp repository, and is a
 * deliberate divergence from the old delegate (which mixed all of those concerns into one class).
 * <p>
 * Batching is <em>not</em> handled here: when a user has an open transaction the provider stages paths into the
 * {@link GitCommitBatchRegistry} (using {@link #stageForBatch} for new files) and the registry commits per repo. This
 * class only performs the immediate, single-operation commits.
 */
public class GitPageHistory {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitPageHistory.class);

	private final GitConnector connector;
	private final String repoPath;
	private final ReentrantLock commitLock = new ReentrantLock();
	private final GitRepoIndex index;

	public GitPageHistory(GitConnector connector) {
		this.connector = connector;
		this.repoPath = connector.repo().getGitDirectory();
		this.index = new GitRepoIndex(connector);
	}

	/**
	 * The eager per-repo index this component reads history and page info from. The provider uses it for the bulk
	 * {@code getAllPages} / {@code getAllChangedSince} paths so those cost one git walk per repo, not one per page.
	 */
	public GitRepoIndex index() {
		return index;
	}

	/**
	 * Stable identifier of this repository (its working-tree path), used as the batch registry's repo key.
	 */
	public String repoKey() {
		return repoPath;
	}

	/**
	 * The connector this component owns; the provider exposes it to the batch registry's connector resolver.
	 */
	public GitConnector connector() {
		return connector;
	}

	/**
	 * Runs the given action while holding this repository's commit lock, so no commit, sweep, delete or move of this
	 * component can interleave with it. This is the sanctioned "I need the repo quiescent" entry point. Providers
	 * bracket a page's file write and its commit with it, so a concurrent sweep cannot commit a half-finished save
	 * under the wrong author. A future pull/fetch+merge after a rejected push belongs here too, combined with
	 * {@link #sweepUp} before the branch operation. The lock is reentrant, actions may call the commit methods of this
	 * class.
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
	 * Commits a single just-written page file immediately, returning the resulting commit hash, or {@code null} if
	 * there was nothing to commit (the content was unchanged, the same-content no-op). Callers that are batching must
	 * not use this; they stage into the registry instead.
	 *
	 * @param pageFile the page file on disk (already written by the provider)
	 * @param userData resolved author, email and commit message
	 */
	@Nullable
	public String commitPut(File pageFile, CommitUserData userData) {
		// in a flat page repository the file name is the repo-relative path
		return commitFile(pageFile, pageFile.getName(), userData);
	}

	/**
	 * Commits a single just-written file immediately (page or attachment), returning the commit hash, or {@code null}
	 * if there was nothing to commit (unchanged content) or the path is git-ignored. The repo-relative path is given
	 * explicitly because attachments live in a {@code <page>-att/} subdirectory of the repo, unlike flat page files.
	 *
	 * @param file             the file on disk (already written by the caller)
	 * @param repoRelativePath the file's path relative to the repository root (used for the ignore check)
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
	 * Stages a newly created page file so a subsequent batch commit (one {@code commitPathsForUser} per repo) picks it
	 * up. For files already tracked by git this is unnecessary, a batch commit by path includes tracked modifications,
	 * so the provider calls this only when the file is new.
	 */
	public void stageForBatch(String fileName) {
		commitLock.lock();
		try {
			connector.commit().addPath(fileName);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Full version history of the page, newest version first. Version numbers are branch-relative positions in the
	 * file's git log, 1 = oldest. Returns an empty list if the file does not exist.
	 */
	public List<GitPageVersion> history(String localName) {
		DefaultPageIdentifier id = PageIdentifier.fromPagename(repoPath, localName, -1);
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
	 * one walk); only the file size is fetched lazily here ({@code git cat-file -s}, cached per commit+path by the
	 * connector) because the {@code git log --name-status} walk does not report sizes.
	 */
	private GitPageVersion buildVersion(String fileName, GitFileRevision revision, int version) {
		long size = connector.log().getFilesizeForCommit(revision.commitHash(), fileName);
		return new GitPageVersion(
				version,
				revision.commitHash(),
				revision.userData(),
				size,
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
	public GitPageVersion infoAt(String localName, int version) {
		DefaultPageIdentifier id = PageIdentifier.fromPagename(repoPath, localName, version);
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
	public int versionCount(String localName) {
		DefaultPageIdentifier id = PageIdentifier.fromPagename(repoPath, localName, -1);
		if (!id.exists()) {
			return 0;
		}
		return index.versionCount(id.fileName());
	}

	/**
	 * Whole-repo revisionsByFile, repo-relative file path to its newest-first revisions, from one git walk per repo
	 * (cached
	 * with HEAD-diff invalidation). The provider drives both {@code getAllPages} (latest = first of each list, version
	 * count = list size) and {@code getAllChangedSince} (revisions newer than a date) off this, so neither costs a
	 * per-page git call.
	 */
	public Map<String, List<GitFileRevision>> revisionsByFile() {
		return index.revisionsByFile();
	}

	/**
	 * Text of the page at the given version. The latest version is read from disk (fast), older versions are read from
	 * git. Returns {@code null} if the page file does not exist.
	 *
	 * @param version a 1-based version number, or {@link WikiProvider#LATEST_VERSION} for the working-tree content
	 */
	@Nullable
	public String textAtVersion(String localName, int version) throws IOException {
		DefaultPageIdentifier id = PageIdentifier.fromPagename(repoPath, localName, version);
		File pageFile = id.accordingFile();
		if (pageFile == null || !pageFile.exists()) {
			LOGGER.info("Page text requested for non-existing file '{}'.", localName);
			return null;
		}
		if (version == WikiProvider.LATEST_VERSION) {
			return Files.readString(pageFile.toPath());
		}
		return connector.log().getTextForPath(id.fileName(), version);
	}

	/**
	 * Deletes the page (working-tree file and from git history going forward), returning the commit hash. Removes the
	 * file on disk, then {@code git rm} + {@code commit}. History of the deleted page is preserved in git.
	 */
	@Nullable
	public String commitDelete(File pageFile, CommitUserData userData) {
		commitLock.lock();
		try {
			if (!pageFile.delete()) {
				LOGGER.warn("Failed to delete page file on disk: {}", pageFile.getName());
			}
			return removeFile(pageFile.getName(), userData);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Removes an already-deleted path from git and commits, returning the commit hash (or {@code null} if the path is
	 * git-ignored). The caller is responsible for having deleted the working-tree file. Used by the attachment
	 * provider,
	 * whose base class deletes the file on disk before the git side runs.
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
	 * the new paths added. Used by the attachment provider's {@code moveAttachmentsForPage}. Returns the commit hash.
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
	 * Moves/renames a page on disk and commits the move, returning the commit hash. History restarts at the rename
	 * commit (no {@code --follow}), the accepted, pinned behavior of the single-wiki git provider.
	 */
	public String commitMove(File fromFile, File toFile, CommitUserData userData) throws IOException {
		commitLock.lock();
		try {
			movePageOnFilesystem(fromFile, toFile);
			return connector.commit().moveFile(fromFile.toPath(), toFile.toPath(),
					userData.user, userData.email, userData.message);
		}
		finally {
			commitLock.unlock();
		}
	}

	/**
	 * Self-heals a dirty working tree: stages everything currently changed/untracked/removed and commits it as a single
	 * reconciliation commit. A no-op when the tree is clean. Run at provider startup and before branch operations
	 * (switch/pull/merge), which require a clean tree.
	 *
	 * @param reason short description of why the sweep runs, used in the commit message
	 * @return the reconciliation commit hash, or {@code null} if the tree was already clean
	 */
	@Nullable
	public String sweepUp(String reason) {
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
			return connector.commit().commitForUser(
					new CommitUserData("system", "system@denkbares.com", "[reconciliation] " + reason));
		}
		finally {
			commitLock.unlock();
		}
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

	private void movePageOnFilesystem(File fromFile, File toFile) throws IOException {
		if (fromFile.getName().equalsIgnoreCase(toFile.getName())) {
			// case-only rename: bounce through a temp file so case-insensitive filesystems don't no-op the move
			File tmpFile = new File(toFile.getParentFile(), toFile.getName() + "_tmp");
			Files.move(fromFile.toPath(), tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.move(tmpFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		else {
			Files.move(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	@NotNull
	@Override
	public String toString() {
		return "GitPageHistory[" + repoPath + "]";
	}
}
