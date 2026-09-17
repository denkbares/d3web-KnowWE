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
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * One open page/attachment transaction for a single wiki user. Collects the repo-relative paths the user accumulates
 * between {@code openCommit} and {@code commit}/{@code rollback}, so that closing the batch produces one commit (not
 * one per page).
 * <p>
 * This replaces the old delegates' shared {@code openCommits} map. It is pure state, only the changed paths of the
 * wiki repository. Instances are owned by {@link GitCommitBatchRegistry}; callers use them only through the registry.
 *
 * @see GitCommitBatchRegistry
 */
public class GitCommitBatch {

	private final String user;

	private final SortedSet<String> paths = new TreeSet<>();

	GitCommitBatch(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	/**
	 * Records a changed path, relative to the repository root.
	 */
	synchronized void stage(String path) {
		paths.add(path);
	}

	synchronized void stage(Collection<String> paths) {
		this.paths.addAll(paths);
	}

	synchronized SortedSet<String> paths() {
		return new TreeSet<>(paths);
	}

	synchronized boolean isEmpty() {
		return paths.isEmpty();
	}
}
