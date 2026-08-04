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

/**
 * One open page/attachment transaction for a single wiki user. Collects the changed paths the user accumulates between
 * {@code openCommit} and {@code commit}/{@code rollback}, grouped per git repository so that closing the batch produces
 * one commit per touched repository (not one per page). A batch may span repositories, everything a user stages goes
 * into the same batch under its repo key.
 * <p>
 * This replaces the old delegates' shared {@code openCommits} map. It is pure state, only the
 * changed paths per repository, keyed by a stable repo key. It deliberately does not hold a {@code GitConnector}: the
 * connector for a repo is resolved at commit/rollback time by {@link GitCommitBatchRegistry}, so closing the batch
 * always uses the repo's current connector rather than whichever instance happened to be live when a path was staged.
 * Instances are owned by {@link GitCommitBatchRegistry}; callers use them only through the registry.
 *
 * @see GitCommitBatchRegistry
 */
public class GitCommitBatch {

	private final String user;

	// repo key -> staged paths, insertion-ordered for stable commit order
	private final Map<String, SortedSet<String>> pathsByRepo = new LinkedHashMap<>();

	GitCommitBatch(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	/**
	 * Records a changed path for the given repository.
	 *
	 * @param repoKey stable identifier of the repository (e.g. its absolute path)
	 * @param path    the changed path, relative to the repository root
	 */
	synchronized void stage(String repoKey, String path) {
		pathsByRepo.computeIfAbsent(repoKey, k -> new TreeSet<>()).add(path);
	}

	synchronized void stage(String repoKey, Collection<String> paths) {
		pathsByRepo.computeIfAbsent(repoKey, k -> new TreeSet<>()).addAll(paths);
	}

	synchronized Set<String> repoKeys() {
		return new LinkedHashSet<>(pathsByRepo.keySet());
	}

	synchronized SortedSet<String> paths(String repoKey) {
		SortedSet<String> paths = pathsByRepo.get(repoKey);
		return paths == null ? new TreeSet<>() : new TreeSet<>(paths);
	}

	synchronized boolean isEmpty() {
		return pathsByRepo.isEmpty();
	}
}
