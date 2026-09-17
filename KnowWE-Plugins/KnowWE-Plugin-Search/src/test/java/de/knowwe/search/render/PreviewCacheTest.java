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

package de.knowwe.search.render;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Guards that a cached preview never outlives the text it was rendered from, and never reaches the wrong user.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class PreviewCacheTest {

	private PreviewCache cache;

	@Before
	public void setUp() {
		cache = PreviewCache.getInstance();
		cache.clear();
	}

	@Test
	public void whatWentInComesBackOut() {
		cache.put("Montage", "s1", "albrecht", "<p>Schritt 1</p>");
		assertEquals("<p>Schritt 1</p>", cache.get("Montage", "s1", "albrecht"));
	}

	@Test
	public void anotherUserDoesNotSeeIt() {
		// a preview can carry tools and links that not every user is allowed to see
		cache.put("Montage", "s1", "albrecht", "<p>mit Werkzeugen</p>");
		assertNull(cache.get("Montage", "s1", "gast"));
	}

	@Test
	public void reindexingAPageForgetsItsPreviews() {
		cache.put("Montage", "s1", "albrecht", "<p>alt</p>");
		cache.put("Pruefung", "s2", "albrecht", "<p>bleibt</p>");
		cache.invalidate("Montage");
		assertNull(cache.get("Montage", "s1", "albrecht"));
		assertEquals("<p>bleibt</p>", cache.get("Pruefung", "s2", "albrecht"));
	}

	@Test
	public void thePageIsMatchedTheWayTheArticleManagerMatchesIt() {
		// case insensitive, otherwise an edit under a different spelling leaves a stale preview behind
		cache.put("Montage", "s1", "albrecht", "<p>alt</p>");
		cache.invalidate("montage");
		assertNull(cache.get("Montage", "s1", "albrecht"));
	}

	@Test
	public void aPageIsNotForgottenBecauseAnotherStartsWithItsName() {
		cache.put("Montage Anhang", "s1", "albrecht", "<p>bleibt</p>");
		cache.invalidate("Montage");
		assertEquals("<p>bleibt</p>", cache.get("Montage Anhang", "s1", "albrecht"));
	}

	@Test
	public void itStaysBounded() {
		for (int i = 0; i < 2500; i++) {
			cache.put("Seite " + i, "s" + i, "albrecht", "<p>" + i + "</p>");
		}
		assertEquals(2000, cache.size());
		// the oldest went, the newest stayed
		assertNull(cache.get("Seite 0", "s0", "albrecht"));
		assertEquals("<p>2499</p>", cache.get("Seite 2499", "s2499", "albrecht"));
	}
}
