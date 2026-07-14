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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wiki.api.providers.WikiProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitFileRevision;

/**
 * Eager per-repository "latest commit per file" index, plus the full per-file commit lists, built from a single
 * {@link GitConnector#log() whole-repo} {@link de.uniwue.d3web.gitConnector.GitConnectorLog#revisionsByFile() history
 * walk}. It is the read model behind {@code getAllPages}, {@code getPageInfo}, {@code getVersionHistory} and
 * {@code getAllChangedSince}.
 * <p>
 * <b>Invalidation.</b> The index is held per branch and validated against {@code HEAD} once per operation.
 * On a read it checks the current branch and {@code HEAD}:
 * <ul>
 *     <li>HEAD unchanged: served from memory, zero further git calls.</li>
 *     <li>Fast-forward (cached HEAD is an ancestor of the new HEAD): the new commits are folded in incrementally.</li>
 *     <li>Non-fast-forward (backward {@code reset --hard}, divergent/force-updated history) or branch switch: the
 *         branch index is rebuilt from a fresh full walk.</li>
 * </ul>
 * A {@code BranchIndex} is immutable once published, so a returned snapshot stays consistent even as the map changes.
 * The per-branch map is an LRU bounded at {@link #MAX_CACHED_BRANCHES}, so an instance that visits many branches (task
 * workflow) does not accumulate a full repo snapshot per branch indefinitely.
 * <p>
 * Per-file lists are <b>newest-first</b> (matching {@code revisionsByFile}); version numbers exposed to callers are
 * 1-based <b>oldest-first</b> ({@code version 1 = oldest}, {@link WikiProvider#LATEST_VERSION} = newest), the same
 * convention {@link GitPageHistory} and the providers use.
 */
public class GitRepoIndex {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitRepoIndex.class);

	/**
	 * Upper bound on how many per-branch snapshots are kept in memory at once.
	 */
	private static final int MAX_CACHED_BRANCHES = 4;

	private final GitConnector connector;

	/**
	 * Branch name to its immutable snapshot, an access-ordered LRU bounded at {@link #MAX_CACHED_BRANCHES}.
	 */
	private final Map<String, BranchIndex> byBranch = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, BranchIndex> eldest) {
			return size() > MAX_CACHED_BRANCHES;
		}
	};

	/**
	 * Serializes the build/rebuild/forward-update of a branch index (so two concurrent readers that both observe a HEAD
	 * change do the work once) and guards all access to {@link #byBranch}.
	 */
	private final Object rebuildLock = new Object();

	public GitRepoIndex(GitConnector connector) {
		this.connector = connector;
	}

	/**
	 * Immutable snapshot of one branch's history at a known {@code HEAD}: every reachable repo-relative file path to
	 * its newest-first revisions.
	 */
	private record BranchIndex(String head, Map<String, List<GitFileRevision>> byFile) {

		static final BranchIndex EMPTY = new BranchIndex(null, Collections.emptyMap());
	}

	// --- public read API (consumed by GitPageHistory) ------------------------

	/**
	 * Newest-first revisions of the given repo-relative file, or an empty list if the file has no committed history.
	 */
	@NotNull
	public List<GitFileRevision> revisionsNewestFirst(String repoRelativePath) {
		List<GitFileRevision> revisions = fresh().byFile().get(repoRelativePath);
		return revisions == null ? List.of() : revisions;
	}

	/**
	 * The latest (newest) committed revision of the file, or {@code null} if it has no committed history (e.g. staged
	 * but not yet committed).
	 */
	@Nullable
	public GitFileRevision latest(String repoRelativePath) {
		List<GitFileRevision> revisions = fresh().byFile().get(repoRelativePath);
		return revisions == null || revisions.isEmpty() ? null : revisions.get(0);
	}

	/**
	 * Number of committed versions of the file, 0 if none.
	 */
	public int versionCount(String repoRelativePath) {
		List<GitFileRevision> revisions = fresh().byFile().get(repoRelativePath);
		return revisions == null ? 0 : revisions.size();
	}

	/**
	 * The revision at a 1-based oldest-first {@code version} ({@link WikiProvider#LATEST_VERSION} = latest), or
	 * {@code null} if the file has no such committed version.
	 */
	@Nullable
	public GitFileRevision atVersion(String repoRelativePath, int version) {
		List<GitFileRevision> revisions = fresh().byFile().get(repoRelativePath);
		if (revisions == null || revisions.isEmpty()) {
			return null;
		}
		if (version == WikiProvider.LATEST_VERSION) {
			return revisions.get(0);
		}
		if (version < 1 || version > revisions.size()) {
			return null;
		}
		// list is newest-first; oldest-first version v maps to index size - v
		return revisions.get(revisions.size() - version);
	}

	/**
	 * The whole current-branch snapshot, repo-relative file path to newest-first revisions. Used by
	 * {@code getAllPages} (enrich the filesystem listing with author/date) and {@code getAllChangedSince}. The returned
	 * map is unmodifiable and reflects a single consistent {@code HEAD}.
	 */
	@NotNull
	public Map<String, List<GitFileRevision>> revisionsByFile() {
		return Collections.unmodifiableMap(fresh().byFile());
	}

	/**
	 * Number of per-branch snapshots currently held in memory (bounded by {@link #MAX_CACHED_BRANCHES}). Test seam for
	 * the eviction bound.
	 */
	int cachedBranchCount() {
		synchronized (rebuildLock) {
			return byBranch.size();
		}
	}

	// --- freshness / invalidation --------------------------------------------

	/**
	 * Returns the up-to-date index for the current branch, rebuilding or incrementally updating it if {@code HEAD}
	 * moved. One {@code currentBranch} + one {@code currentHEAD} git call per invocation. The heavy walk runs only when
	 * the branch index is missing or {@code HEAD} actually changed.
	 */
	private BranchIndex fresh() {
		String branch = connector.branches().currentBranch();
		String gitHead;
		try {
			gitHead = connector.log().currentHEAD();
		}
		catch (IllegalStateException e) {
			// repository without an initial commit: no history to index yet
			return BranchIndex.EMPTY;
		}
		if (gitHead == null) {
			return BranchIndex.EMPTY;
		}

		synchronized (rebuildLock) {
			// get() also marks the branch most-recently-used, so the access-order LRU never evicts the current branch
			BranchIndex existing = byBranch.get(branch);
			if (existing != null && gitHead.equals(existing.head())) {
				return existing;
			}
			BranchIndex updated = (existing == null || existing.head() == null)
					? buildFull(gitHead)
					: update(existing, gitHead);
			// put() bounds the map via removeEldestEntry (evicts the least-recently-used branch, never this one)
			byBranch.put(branch, updated);
			return updated;
		}
	}

	private BranchIndex buildFull(String gitHead) {
		// walk from the pinned hash, not the symbolic HEAD: a branch switch between reading HEAD and walking can no
		// longer bind this snapshot to a different branch's history. A stale branch-name binding (branch read raced a
		// switch) is harmless, it is never served because serving requires the stored head to match the current HEAD.
		Map<String, List<GitFileRevision>> byFile = connector.log().revisionsByFile(gitHead);
		LOGGER.debug("Built full git index ({} file(s)) at HEAD {}.", byFile.size(), gitHead);
		return new BranchIndex(gitHead, byFile);
	}

	/**
	 * Folds a {@code HEAD} move into an existing branch index. Applies an incremental forward update only for a true
	 * fast-forward (cached HEAD is an ancestor of the new HEAD). For any non-fast-forward move (backward reset or
	 * divergent history) it rebuilds from scratch, which is what keeps stale post-reset history from surviving.
	 */
	private BranchIndex update(BranchIndex existing, String gitHead) {
		String cachedHead = existing.head();
		// commits reachable from the new HEAD but not the cached one ...
		List<String> forward = connector.log().commitsBetween(cachedHead, gitHead);
		// ... and the reverse: commits reachable from the cached HEAD but not the new one. Empty reverse == the cached
		// HEAD is an ancestor of the new one == fast-forward. Non-empty reverse == backward/divergent == must rebuild.
		List<String> backward = connector.log().commitsBetween(gitHead, cachedHead);
		if (!backward.isEmpty() || forward.isEmpty()) {
			LOGGER.debug("Non-fast-forward HEAD move {} -> {} ({} behind), rebuilding git index.",
					cachedHead, gitHead, backward.size());
			return buildFull(gitHead);
		}
		return applyForward(existing, forward, gitHead);
	}

	/**
	 * Returns a new branch index with the given (oldest-first) new commits folded into a copy of the existing per-file
	 * lists. The existing index is left untouched so concurrent readers keep a consistent view.
	 */
	private BranchIndex applyForward(BranchIndex existing, List<String> newCommitsOldestFirst, String gitHead) {
		// shallow copy: untouched files keep sharing their (immutable) lists, touched files get fresh lists below
		Map<String, List<GitFileRevision>> byFile = new LinkedHashMap<>(existing.byFile());
		// oldest-first: inserting each commit's revision at the front of its file lists leaves the newest at the front
		for (String commitHash : newCommitsOldestFirst) {
			GitFileRevision revision = revisionFor(commitHash);
			for (String path : connector.log().listChangedFilesForHash(commitHash)) {
				List<GitFileRevision> updated = new ArrayList<>(byFile.getOrDefault(path, List.of()));
				updated.add(0, revision);
				byFile.put(path, updated);
			}
		}
		LOGGER.debug("Fast-forwarded git index by {} commit(s) to HEAD {}.", newCommitsOldestFirst.size(), gitHead);
		return new BranchIndex(gitHead, byFile);
	}

	private GitFileRevision revisionFor(String commitHash) {
		// commitUserDataFor / commitTimeFor are cached per (immutable) hash by the connector, so this is a one-time cost
		CommitUserData userData = connector.log().commitUserDataFor(commitHash);
		long time = connector.log().commitTimeFor(commitHash);
		return new GitFileRevision(commitHash, userData.user, userData.email, time, userData.message);
	}
}
