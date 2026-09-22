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

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Pins the bound of {@link GitBlobCache}: what it keeps, what it refuses to keep, and what it gives up first.
 */
public class GitBlobCacheTest {

	private static byte[] content(int size) {
		byte[] content = new byte[size];
		for (int i = 0; i < size; i++) {
			content[i] = (byte) i;
		}
		return content;
	}

	@Test
	public void storedContentIsFoundUnderItsCommitAndPath() {
		GitBlobCache cache = new GitBlobCache(1000, 1000);
		cache.put("commitA", "Page.txt", content(10));

		assertArrayEquals(content(10), cache.get("commitA", "Page.txt"));
		assertNull("another commit of the same path is a different entry", cache.get("commitB", "Page.txt"));
		assertNull("another path at the same commit is a different entry", cache.get("commitA", "Other.txt"));
	}

	@Test
	public void contentAboveThePerEntryLimitIsNotKept() {
		GitBlobCache cache = new GitBlobCache(1000, 100);
		cache.put("commitA", "Attachment.bin", content(101));

		assertNull(cache.get("commitA", "Attachment.bin"));
		assertEquals(0, cache.size());
		assertEquals(0, cache.bytes());
	}

	@Test
	public void theLeastRecentlyReadEntryIsGivenUpFirst() {
		GitBlobCache cache = new GitBlobCache(300, 300);
		cache.put("commit1", "A.txt", content(100));
		cache.put("commit2", "B.txt", content(100));
		cache.put("commit3", "C.txt", content(100));
		// reading A makes B the least recently used one
		assertNotNull(cache.get("commit1", "A.txt"));

		cache.put("commit4", "D.txt", content(100));

		assertNull("B was the least recently used entry", cache.get("commit2", "B.txt"));
		assertNotNull(cache.get("commit1", "A.txt"));
		assertNotNull(cache.get("commit3", "C.txt"));
		assertNotNull(cache.get("commit4", "D.txt"));
		assertEquals(300, cache.bytes());
	}

	@Test
	public void theByteBudgetIsHeldAcrossEntriesOfVeryDifferentSizes() {
		GitBlobCache cache = new GitBlobCache(1000, 1000);
		for (int i = 0; i < 20; i++) {
			cache.put("commit" + i, "Page.txt", content(100));
		}

		assertEquals(10, cache.size());
		assertEquals(1000, cache.bytes());
	}

	@Test
	public void storingTheSameEntryTwiceCountsItOnce() {
		GitBlobCache cache = new GitBlobCache(1000, 1000);
		cache.put("commitA", "Page.txt", content(100));
		cache.put("commitA", "Page.txt", content(100));

		assertEquals(1, cache.size());
		assertEquals(100, cache.bytes());
	}
}
