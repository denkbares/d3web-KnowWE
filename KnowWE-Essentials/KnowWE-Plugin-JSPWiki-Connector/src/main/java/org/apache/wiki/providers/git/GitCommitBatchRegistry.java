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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.CommitUserData;

/**
 * Holds the open {@link GitCommitBatch}es of the git-backed providers, keyed by wiki user name. The user-name key
 * matches the {@code WikiConnector} transaction API, which is keyed by user name only. Concurrent batches of different
 * users are independent.
 * <p>
 * Replaces the old delegates' shared, externally-mutated {@code openCommits} map: callers stage and close batches only
 * through this API, nobody reaches into another class's mutable state.
 * <p>
 * The registry serves the one wiki repository, represented by the {@link GitWikiRepository} it is constructed with.
 * Closing a batch commits or rolls back through {@link GitWikiRepository#commitBatch} and
 * {@link GitWikiRepository#rollbackPaths}, which take the repository's commit lock, so a batch close is serialized
 * against concurrent immediate commits, sweeps, deletes and moves instead of relying on git's own {@code index.lock}
 * retries.
 */
public class GitCommitBatchRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitCommitBatchRegistry.class);

	private final Map<String, GitCommitBatch> openBatches = new ConcurrentHashMap<>();
	private final GitWikiRepository repository;

	/**
	 * @param repository the wiki repository, whose commit lock serializes the batch close
	 */
	public GitCommitBatchRegistry(GitWikiRepository repository) {
		this.repository = repository;
	}

	/**
	 * Opens a batch for the given user, so that subsequent {@link #stage} calls accumulate instead of committing
	 * immediately. Idempotent: a second open while a batch is already open is a no-op (the existing batch is kept).
	 * A {@code null} user cannot open a batch (its changes are committed immediately by the caller).
	 */
	public void open(String user) {
		if (user == null) {
			LOGGER.warn("Tried to open a commit batch without a user; ignoring.");
			return;
		}
		openBatches.computeIfAbsent(user, GitCommitBatch::new);
	}

	/**
	 * Whether the given user currently has an open batch. Null-tolerant: a {@code null} user (page saved without an
	 * author) never has an open batch.
	 */
	public boolean isOpen(String user) {
		return user != null && openBatches.containsKey(user);
	}

	/**
	 * Stages a changed repo-relative path for the user. If the user has an open batch, the path is added to it (and,
	 * for a new file, staged in the git index, since the closing pathspec commit picks up tracked modifications but
	 * not untracked files) and {@code true} is returned. If the user has no open batch, nothing is recorded and
	 * {@code false} is returned, the caller must commit the change immediately.
	 *
	 * @param newFile whether the file is new to git (untracked); tracked modifications need no index staging
	 * @return {@code true} if the path was added to an open batch, {@code false} if there is no open batch and the
	 * caller should commit immediately
	 */
	public boolean stage(String user, String path, boolean newFile) {
		GitCommitBatch batch = user == null ? null : openBatches.get(user);
		if (batch == null) {
			return false;
		}
		if (newFile) {
			repository.stageInIndex(path);
		}
		batch.stage(path);
		return true;
	}

	/**
	 * Stages several changed paths for the user (e.g. the from/to paths of a move). Semantics match
	 * {@link #stage(String, String, boolean)}.
	 */
	public boolean stage(String user, Collection<String> paths, boolean newFiles) {
		GitCommitBatch batch = user == null ? null : openBatches.get(user);
		if (batch == null) {
			return false;
		}
		if (newFiles) {
			for (String path : paths) {
				repository.stageInIndex(path);
			}
		}
		batch.stage(paths);
		return true;
	}

	/**
	 * Commits all paths staged for the user since {@link #open} as one commit and closes the batch. The
	 * author/email/message are resolved by the caller (the provider, via its user-profile lookup and comment
	 * strategy).
	 *
	 * @return the commit and the paths that went into it, or {@code null} if the user had no open batch, none of the
	 * staged paths had changes left to commit, or the commit failed (logged)
	 */
	@Nullable
	public CommitResult commit(String user, CommitUserData userData) {
		GitCommitBatch batch = user == null ? null : openBatches.remove(user);
		if (batch == null) {
			LOGGER.warn("Tried to commit batch for user '{}' but no batch was open.", user);
			return null;
		}
		SortedSet<String> paths = batch.paths();
		try {
			String commitHash = repository.commitBatch(paths, userData);
			if (commitHash == null) {
				// none of the staged paths had changes left to commit
				LOGGER.info("Batch of user '{}' had no changes to commit.", user);
				return null;
			}
			return new CommitResult(commitHash, paths);
		}
		catch (Exception e) {
			LOGGER.error("Failed to commit batch for user '{}'.", user, e);
			return null;
		}
	}

	/**
	 * Discards all paths staged for the user since {@link #open}, restoring the affected files, and closes the batch.
	 *
	 * @return the restored paths, so the caller can refresh page caches and Lucene for them (the on-disk files were
	 * reverted, so any cached version of the discarded edit must be evicted). Empty if the user had no open batch.
	 */
	public Set<String> rollback(String user) {
		GitCommitBatch batch = user == null ? null : openBatches.remove(user);
		if (batch == null) {
			LOGGER.warn("Tried to roll back batch for user '{}' but no batch was open.", user);
			return Collections.emptySet();
		}
		SortedSet<String> paths = batch.paths();
		try {
			repository.rollbackPaths(paths);
		}
		catch (Exception e) {
			LOGGER.error("Failed to roll back batch for user '{}'.", user, e);
		}
		// reported even if the rollback failed, so the caller still refreshes caches for the affected paths
		return paths;
	}

	/**
	 * Outcome of committing a batch: the resulting commit hash and the paths that went into the commit. The provider
	 * uses this to fire wiki events and refresh caches for the committed paths.
	 */
	public record CommitResult(String commitHash, Set<String> paths) {
	}
}
