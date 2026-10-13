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
package de.knowwe.include;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.*;

public class InterWikiImportAnnotationFormatTest {

	private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

	@Test
	public void labels() {
		assertEquals("Acknowledged", InterWikiImportAnnotationFormat.getLabel("trackingAcceptedAt"));
		assertEquals("Last change in source", InterWikiImportAnnotationFormat.getLabel("latestChange"));
		assertEquals("Some new annotation", InterWikiImportAnnotationFormat.getLabel("someNewAnnotation"));
	}

	@Test
	public void dateTime() {
		Instant instant = Instant.parse("2026-10-01T11:18:08.201Z");
		ZoneId berlin = ZoneId.of("Europe/Berlin");
		assertEquals("01.10.2026, 13:18", InterWikiImportAnnotationFormat.formatDateTime(instant, Locale.GERMANY, berlin));
		assertTrue(InterWikiImportAnnotationFormat.formatDateTime(instant, Locale.US, berlin).startsWith("Oct 1, 2026"));
	}

	@Test
	public void relative() {
		assertEquals("less than 1 minute ago", InterWikiImportAnnotationFormat.formatRelative(NOW.minusSeconds(30), NOW));
		assertEquals("1 minute ago", InterWikiImportAnnotationFormat.formatRelative(NOW.minusSeconds(60), NOW));
		assertEquals("59 minutes ago", InterWikiImportAnnotationFormat.formatRelative(NOW.minusSeconds(59 * 60), NOW));
		assertEquals("1 hour ago", InterWikiImportAnnotationFormat.formatRelative(NOW.minusSeconds(3600), NOW));
		assertEquals("47 hours ago", InterWikiImportAnnotationFormat.formatRelative(NOW.minusSeconds(47 * 3600), NOW));
		assertEquals("2 days ago", InterWikiImportAnnotationFormat.formatRelative(NOW.minusSeconds(48 * 3600), NOW));
		assertEquals("in the future", InterWikiImportAnnotationFormat.formatRelative(NOW.plusSeconds(10), NOW));
	}

	@Test
	public void wikiModeAndReplacement() {
		assertEquals("leo2.ilme.dev/KnowWE", InterWikiImportAnnotationFormat.formatWiki(" https://leo2.ilme.dev/KnowWE/ "));
		assertEquals("Tracking", InterWikiImportAnnotationFormat.formatMode("tracking"));
		assertArrayEquals(new String[] { "a", "b" }, InterWikiImportAnnotationFormat.splitReplacement("a->b"));
		assertArrayEquals(new String[] { "[a-z]+", "X" }, InterWikiImportAnnotationFormat.splitReplacement("[a-z]+->X"));
		assertNull(InterWikiImportAnnotationFormat.splitReplacement("no replacement"));
	}
}
