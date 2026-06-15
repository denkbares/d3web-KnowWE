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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnector;

/**
 * Holds the open {@link GitCommitBatch}es of the multi-wiki git provider, keyed by wiki user name. The user-name key
 * matches the {@code WikiConnector} transaction API, which is keyed by user name only. Concurrent batches of different
 * users are independent.
 * <p>
 * Replaces the old delegates' shared, externally-mutated {@code openCommits} map: callers stage and close batches only
 * through this API, nobody reaches into another class's mutable state.
 * <p>
 * Closing a batch produces one commit per touched repository. There is no cross-repo atomicity, since git cannot
 * provide it, so a failure committing one repository is logged and the remaining repositories are still committed.
 */
public class GitCommitBatchRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitCommitBatchRegistry.class);

	private final Map<String, GitCommitBatch> openBatches = new ConcurrentHashMap<>();

	/**
	 * Opens a batch for the given user, so that subsequent {@link #stage} calls accumulate instead of committing
	 * immediately. Idempotent: a second open while a batch is already open is a no-op (the existing batch is kept).
	 */
	public void open(String user) {
		openBatches.computeIfAbsent(user, GitCommitBatch::new);
	}

	/**
	 * Whether the given user currently has an open batch.
	 */
	public boolean isOpen(String user) {
		return openBatches.containsKey(user);
	}

	/**
	 * Stages a changed path for the user. If the user has an open batch, the path is added to it and {@code true} is
	 * returned. If the user has no open batch, nothing is recorded and {@code false} is returned, the caller must
	 * commit the change immediately.
	 *
	 * @return {@code true} if the path was added to an open batch, {@code false} if there is no open batch and the
	 * caller should commit immediately
	 */
	public boolean stage(String user, String repoKey, GitConnector connector, String path) {
		GitCommitBatch batch = openBatches.get(user);
		if (batch == null) {
			return false;
		}
		batch.stage(repoKey, connector, path);
		return true;
	}

	/**
	 * Stages several changed paths of the same repository for the user (e.g. the from/to paths of a move). Semantics
	 * match {@link #stage(String, String, GitConnector, String)}.
	 */
	public boolean stage(String user, String repoKey, GitConnector connector, Collection<String> paths) {
		GitCommitBatch batch = openBatches.get(user);
		if (batch == null) {
			return false;
		}
		batch.stage(repoKey, connector, paths);
		return true;
	}

	/**
	 * Commits all paths staged for the user since {@link #open}, producing one commit per touched repository, and
	 * closes the batch. The author/email/message are resolved by the caller (the provider, via its user-profile lookup
	 * and comment strategy) and applied to every repository's commit.
	 *
	 * @return one {@link RepoCommitResult} per repository that was committed successfully, empty if the user had no
	 * open batch. Repositories whose commit failed are omitted (failure is logged).
	 */
	public List<RepoCommitResult> commit(String user, String message, String author, String email) {
		GitCommitBatch batch = openBatches.remove(user);
		if (batch == null) {
			LOGGER.warn("Tried to commit batch for user '{}' but no batch was open.", user);
			return Collections.emptyList();
		}
		List<RepoCommitResult> results = new ArrayList<>();
		for (String repoKey : batch.repoKeys()) {
			GitCommitBatch.RepoStaging staging = batch.staging(repoKey);
			Set<String> paths = new TreeSet<>(staging.paths);
			try {
				String commitHash = staging.connector.commit().commitPathsForUser(message, author, email, paths);
				results.add(new RepoCommitResult(repoKey, commitHash, paths));
			}
			catch (Exception e) {
				LOGGER.error(
						"Failed to commit batch for user '{}' in repo '{}', continuing with the remaining repos.",
						user, repoKey, e
				);
			}
		}
		return results;
	}

	/**
	 * Discards all paths staged for the user since {@link #open}, restoring the affected files in
	 * each touched repository, and closes the batch. A failure rolling back one repository is logged
	 * and the remaining repositories are still rolled back.
	 *
	 * @return the repo keys whose staged paths were the subject of the rollback (regardless of
	 * per-repo failures), so the caller can refresh caches for them
	 */
	public Set<String> rollback(String user) {
		GitCommitBatch batch = openBatches.remove(user);
		if (batch == null) {
			LOGGER.warn("Tried to roll back batch for user '{}' but no batch was open.", user);
			return Collections.emptySet();
		}
		for (String repoKey : batch.repoKeys()) {
			GitCommitBatch.RepoStaging staging = batch.staging(repoKey);
			try {
				staging.connector.rollback().rollbackPaths(new TreeSet<>(staging.paths));
			}
			catch (Exception e) {
				LOGGER.error(
						"Failed to roll back batch for user '{}' in repo '{}', continuing with the remaining repos.",
						user, repoKey, e
				);
			}
		}
		return batch.repoKeys();
	}

	/**
	 * Outcome of committing one repository's share of a batch: the repo key, the resulting commit hash, and the paths
	 * that went into the commit. The provider uses this to fire wiki events and refresh caches for the committed paths.
	 */
	public record RepoCommitResult(String repoKey, String commitHash, Set<String> paths) {
	}
}
