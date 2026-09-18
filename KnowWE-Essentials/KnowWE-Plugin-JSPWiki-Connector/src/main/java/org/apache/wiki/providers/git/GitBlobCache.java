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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import de.uniwue.d3web.gitConnector.GitFileAtCommit;

/**
 * Least-recently-used store of file contents read out of git, keyed by the commit the content was read at together
 * with the file's repo-relative path. The content of a path at a given commit never changes, so an entry can never
 * go stale and nothing invalidates this store. It is bounded by the bytes it holds rather than by the number of
 * entries, because it holds page and attachment content, whose sizes differ by orders of magnitude.
 * <p>
 * Content above the per-entry limit is not kept at all. A single large attachment download would otherwise push out
 * everything else for no gain, since the wasteful repeats this store exists to remove are repeated reads of page
 * text within one request.
 */
class GitBlobCache {

	private final long maxBytes;
	private final long maxEntryBytes;

	/**
	 * Access-ordered, so iteration starts at the least recently used entry.
	 */
	private final Map<GitFileAtCommit, byte[]> entries = new LinkedHashMap<>(16, 0.75f, true);

	private long bytes;

	GitBlobCache(long maxBytes, long maxEntryBytes) {
		this.maxBytes = maxBytes;
		this.maxEntryBytes = maxEntryBytes;
	}

	/**
	 * The stored content of the file at the given commit, or {@code null} if it is not stored. The returned array is
	 * the stored one and must not be modified.
	 */
	@Nullable
	synchronized byte[] get(String commitHash, String repoRelativePath) {
		return entries.get(new GitFileAtCommit(commitHash, repoRelativePath));
	}

	/**
	 * Stores the content of the file at the given commit, evicting least recently used entries until the store is
	 * within its byte budget again. Content above the per-entry limit is not stored.
	 */
	synchronized void put(String commitHash, String repoRelativePath, byte[] content) {
		if (content.length > maxEntryBytes) {
			return;
		}
		byte[] replaced = entries.put(new GitFileAtCommit(commitHash, repoRelativePath), content);
		bytes += content.length - (replaced == null ? 0 : replaced.length);
		Iterator<Map.Entry<GitFileAtCommit, byte[]>> eldestFirst = entries.entrySet().iterator();
		while (bytes > maxBytes && eldestFirst.hasNext()) {
			bytes -= eldestFirst.next().getValue().length;
			eldestFirst.remove();
		}
	}

	/**
	 * How many entries are stored. Test seam for the eviction bound.
	 */
	synchronized int size() {
		return entries.size();
	}

	/**
	 * How many bytes of content are stored. Test seam for the eviction bound.
	 */
	synchronized long bytes() {
		return bytes;
	}
}
