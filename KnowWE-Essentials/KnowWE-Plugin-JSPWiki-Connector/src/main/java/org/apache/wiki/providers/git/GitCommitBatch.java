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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uniwue.d3web.gitConnector.GitConnector;

/**
 * One open page/attachment transaction for a single wiki user. Collects the changed paths the user accumulates between
 * {@code openCommit} and {@code commit}/{@code rollback}, grouped per git repository so that closing the batch produces
 * one commit per touched repository (not one per page). A batch may span repositories, a bulk operation editing pages
 * in several sub-wikis stages into the same batch under different repo keys.
 * <p>
 * This is the multi-wiki replacement for the old delegates' shared {@code openCommits} map: instead of a single set of
 * file names bound to one repository, paths are kept per repository together with the {@link GitConnector} that will
 * commit them. Instances are owned by {@link GitCommitBatchRegistry}, callers use them only through the registry.
 *
 * @see GitCommitBatchRegistry
 */
public class GitCommitBatch {

	private final String user;

	// repo key -> staged paths + the connector that commits them, insertion-ordered for stable commit order
	private final Map<String, RepoStaging> stagingByRepo = new LinkedHashMap<>();

	GitCommitBatch(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	/**
	 * Records a changed path for the given repository. The connector is remembered on first contact
	 * with the repo and used to commit/roll back its staged paths when the batch closes.
	 *
	 * @param repoKey   stable identifier of the repository (e.g. its absolute path)
	 * @param connector the connector that owns this repository
	 * @param path      the changed path, relative to the repository root
	 */
	synchronized void stage(String repoKey, GitConnector connector, String path) {
		stagingByRepo.computeIfAbsent(repoKey, k -> new RepoStaging(connector)).paths.add(path);
	}

	synchronized void stage(String repoKey, GitConnector connector, Collection<String> paths) {
		stagingByRepo.computeIfAbsent(repoKey, k -> new RepoStaging(connector)).paths.addAll(paths);
	}

	synchronized Set<String> repoKeys() {
		return new LinkedHashSet<>(stagingByRepo.keySet());
	}

	synchronized RepoStaging staging(String repoKey) {
		return stagingByRepo.get(repoKey);
	}

	synchronized boolean isEmpty() {
		return stagingByRepo.isEmpty();
	}

	/**
	 * Staged paths of one repository plus the connector that will commit them.
	 */
	static final class RepoStaging {
		final GitConnector connector;
		final SortedSet<String> paths = new TreeSet<>();

		RepoStaging(GitConnector connector) {
			this.connector = connector;
		}
	}
}
