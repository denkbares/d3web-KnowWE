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

package de.knowwe.jspwiki.administration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import de.knowwe.jspwiki.administration.DeletePagesAction.Entry;
import de.knowwe.jspwiki.administration.DeletePagesAction.Mode;
import de.knowwe.jspwiki.administration.DeletePagesAction.PageLookup;
import de.knowwe.jspwiki.administration.DeletePagesAction.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DeletePagesActionTest {

	/**
	 * Pages by title with their text, S3 is protected, S4 is already cleared
	 */
	private static final Map<String, String> PAGES = Map.of(
			"S1", "%%Identifier\n@type: Compliance Check\n%",
			"S2", "some text",
			"S3", "protected",
			"S4", "Deleted by Test Autor at 2026-09-30 11:41:40",
			"Module", "Deleted by nobody\nbut with more content");

	private static final PageLookup LOOKUP = new PageLookup() {
		@Override
		public @Nullable String findTitle(String name) {
			return PAGES.keySet().stream().filter(title -> title.equalsIgnoreCase(name)).findFirst().orElse(null);
		}

		@Override
		public boolean canDelete(String title) {
			return !Set.of("S3").contains(title);
		}

		@Override
		public @Nullable String getText(String title) {
			return PAGES.get(title);
		}
	};

	@Test
	public void parsePageNames() {
		assertEquals(List.of("S1", "S2", "Some Page"), DeletePagesAction.parsePageNames(" S1\r\n\n\tS2  \n   \nSome Page\n"));
		assertEquals(List.of(), DeletePagesAction.parsePageNames(null));
		assertEquals(List.of(), DeletePagesAction.parsePageNames("\n \n"));
	}

	@Test
	public void parseMode() {
		assertEquals(Mode.CLEAR, DeletePagesAction.parseMode("CLEAR"));
		assertEquals(Mode.DELETE, DeletePagesAction.parseMode(" delete "));
		assertNull(DeletePagesAction.parseMode("everything"));
		assertNull(DeletePagesAction.parseMode(null));
	}

	@Test
	public void planDelete() {
		List<Entry> entries = DeletePagesAction.plan(List.of("S1", "s2", "S9", "S3", "S1", "S2", "S4", "Module"), Mode.DELETE, LOOKUP);
		assertEntries(entries,
				"S1", Status.OK,
				// the actual title of the page is reported
				"S2", Status.OK,
				"S9", Status.NOT_FOUND,
				"S3", Status.NO_PERMISSION,
				"S1", Status.DUPLICATE,
				// duplicates are recognized regardless of the case
				"S2", Status.DUPLICATE,
				// a cleared page can still be deleted
				"S4", Status.OK,
				"Module", Status.OK);
	}

	@Test
	public void planClear() {
		List<Entry> entries = DeletePagesAction.plan(List.of("S1", "S4", "Module", "S9", "s9"), Mode.CLEAR, LOOKUP);
		assertEntries(entries,
				"S1", Status.OK,
				"S4", Status.ALREADY_CLEARED,
				// only a page consisting of the note alone counts as cleared
				"Module", Status.OK,
				"S9", Status.NOT_FOUND,
				"s9", Status.DUPLICATE);
	}

	private static void assertEntries(List<Entry> entries, Object... expected) {
		assertEquals(expected.length / 2, entries.size());
		for (int i = 0; i < entries.size(); i++) {
			assertEquals("name of entry " + i, expected[2 * i], entries.get(i).getName());
			assertEquals("status of " + expected[2 * i], expected[2 * i + 1], entries.get(i).getStatus());
		}
	}
}
