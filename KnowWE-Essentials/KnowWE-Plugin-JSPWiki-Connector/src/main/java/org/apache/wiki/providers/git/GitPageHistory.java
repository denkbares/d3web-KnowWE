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
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;
import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusResultSuccess;

/**
 * Per-repository git history component which owns one sub-wiki repository's {@link GitConnector} and performs the git
 * side of page operations on it: commit-on-save, version to commit mapping, text-at-version, delete and move, and the
 * sweep-up reconciliation. One instance lives per sub-wiki repo, the multi-wiki provider is a thin router that
 * strips the {@code Repo&&} prefix and delegates the resulting flat, single-repo operation here.
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

	public GitPageHistory(GitConnector connector) {
		this.connector = connector;
		this.repoPath = connector.repo().getGitDirectory();
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
	 * Commits a single just-written page file immediately, returning the resulting commit hash, or {@code null} if
	 * there was nothing to commit (the content was unchanged, the same-content no-op). Callers that are batching must
	 * not use this; they stage into the registry instead.
	 *
	 * @param pageFile the page file on disk (already written by the provider)
	 * @param userData resolved author, email and commit message
	 */
	@Nullable
	public String commitPut(File pageFile, CommitUserData userData) {
		// in a flat per-sub-wiki page repo the file name is the repo-relative path
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
		// commitHashesForFile is oldest-first, so the natural index gives version 1 = oldest
		List<String> commitHashes = connector.log().commitHashesForFile(id.fileName());
		List<GitPageVersion> versions = new ArrayList<>(commitHashes.size());
		int version = 1;
		for (String commitHash : commitHashes) {
			versions.add(buildVersion(id.fileName(), commitHash, version));
			version++;
		}
		if (versions.isEmpty()) {
			LOGGER.error("File '{}' exists but git reports no version of it.", id.fileName());
		}
		Collections.reverse(versions);
		return versions;
	}

	private GitPageVersion buildVersion(String fileName, String commitHash, int version) {
		CommitUserData userData = connector.log().commitUserDataFor(commitHash);
		long size = connector.log().getFilesizeForCommit(commitHash, fileName);
		long timeInSeconds = connector.log().commitTimeFor(commitHash);
		return new GitPageVersion(version, commitHash, userData, size, Date.from(Instant.ofEpochSecond(timeInSeconds)));
	}

	/**
	 * Info about a single version of the page without computing the full history, one commit lookup. Returns
	 * {@code null} if the page does not exist on disk or git has no commit for the requested version (e.g. the file is
	 * staged in an open batch but not yet committed; the caller decides how to fall back).
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
		String commitHash = connector.log().commitHashForFileAndVersion(fileName, version);
		if (commitHash == null) {
			return null;
		}
		int realVersion = version == WikiProvider.LATEST_VERSION
				? connector.log().numberOfCommitsForFile(fileName)
				: version;
		return buildVersion(fileName, commitHash, realVersion);
	}

	/**
	 * Number of committed versions of the page in git, 0 if none / not tracked.
	 */
	public int versionCount(String localName) {
		DefaultPageIdentifier id = PageIdentifier.fromPagename(repoPath, localName, -1);
		if (!id.exists()) {
			return 0;
		}
		return connector.log().numberOfCommitsForFile(id.fileName());
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
	 * git-ignored). The caller is responsible for having deleted the working-tree file. Used by the attachment provider,
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
