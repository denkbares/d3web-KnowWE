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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

import static de.knowwe.include.InterWikiImportTestWiki.content;
import static org.junit.Assert.*;

/**
 * Checks the "imported by" markers of a source page: how they are grouped by importing wiki and page, how they are
 * rendered and that they disappear when the importing wiki stops asking.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 18.11.2026
 */
public class ImportMarkerTest {

	private static final String TURM = "http://localhost:8091/KnowWE/";
	private static final String OTHER = "http://localhost:9000/KnowWE/";
	private static final Instant NOW = Instant.parse("2026-11-18T12:00:00Z");

	private InterWikiImportTestWiki wiki;

	@Before
	public void clearMarkers() {
		ImportMarker.clearStaticMarkers();
	}

	@After
	public void closeWiki() {
		ImportMarker.clearStaticMarkers();
		if (wiki != null) wiki.close();
	}

	private static ImportMarker marker(String wikiUrl, String wikiLabel, String page, String anchor, String reference, Instant time) {
		String link = wikiUrl + "Wiki.jsp?page=" + page.replace(" ", "+") + "#" + anchor;
		return new ImportMarker(Date.from(time), reference, wikiLabel + ": " + page, link);
	}

	/* ------------------------------------------------ labels ------------------------------------------------ */

	@Test
	public void splitsLabelAndLink() {
		ImportMarker marker = marker(TURM, "Turm", "Fehlercodes Import", "abc", "Fehlercodes", NOW);
		assertEquals("Turm", marker.getWikiLabel());
		assertEquals("Fehlercodes Import", marker.getPageLabel());
		assertEquals(TURM, marker.getWikiUrl());
		assertEquals(TURM + "Wiki.jsp?page=Fehlercodes+Import", marker.getPageLink());
	}

	@Test
	public void unknownWikiAndPage() {
		ImportMarker marker = new ImportMarker(Date.from(NOW), "Fehlercodes", null, null);
		assertEquals("another wiki", marker.getWikiLabel());
		assertNull(marker.getPageLabel());
		assertNull(marker.getWikiUrl());
		assertNull(marker.getPageLink());

		// labels of older wikis without "<wiki>: "
		marker = new ImportMarker(Date.from(NOW), "Fehlercodes", "Some Page", null);
		assertEquals("another wiki", marker.getWikiLabel());
		assertEquals("Some Page", marker.getPageLabel());
	}

	/* ------------------------------------------------ grouping ------------------------------------------------ */

	@Test
	public void groupsByWikiAndPage() {
		List<ImportMarker> markers = List.of(
				marker(TURM, "Turm", "Fehlercodes Import", "a", "Fehlercodes", NOW.minusSeconds(600)),
				marker(TURM, "Turm", "Fehlercodes Import", "b", "Fehlercodes#Codes", NOW.minusSeconds(60)),
				marker(TURM, "Turm", "Literal Import", "c", "Fehlercodes", NOW.minusSeconds(300)),
				marker(OTHER, "Other", "Fehlercodes Import", "a", "Fehlercodes", NOW.minusSeconds(3000)));

		List<ImportMarker.WikiImports> wikis = ImportMarker.groupByWiki(markers);
		assertEquals(2, wikis.size());

		ImportMarker.WikiImports turm = wikis.get(0);
		assertEquals("Turm", turm.label());
		assertEquals(TURM, turm.url());
		assertEquals(3, turm.importCount());
		assertEquals(NOW.minusSeconds(60), turm.latestImport());
		assertEquals(List.of("Fehlercodes Import", "Literal Import"),
				turm.pages().stream().map(ImportMarker.PageImports::label).toList());
		assertEquals(2, turm.pages().get(0).markers().size());

		// the same page title in another wiki is another page
		ImportMarker.WikiImports other = wikis.get(1);
		assertEquals("Other", other.label());
		assertEquals(1, other.pages().size());
		assertEquals(NOW.minusSeconds(3000), other.latestImport());
	}

	@Test
	public void wikisWithoutNameAreKeptApartByUrl() {
		List<ImportMarker> markers = List.of(
				marker(TURM, "another wiki", "A", "a", "Fehlercodes", NOW),
				marker(OTHER, "another wiki", "A", "a", "Fehlercodes", NOW));
		assertEquals(2, ImportMarker.groupByWiki(markers).size());
	}

	/* ------------------------------------------------ rendering ------------------------------------------------ */

	@Test
	public void rendersOneLineForOneWiki() {
		List<ImportMarker> markers = List.of(
				marker(TURM, "Turm", "Fehlercodes Import", "a", "Fehlercodes", NOW.minusSeconds(600)),
				marker(TURM, "Turm", "Fehlercodes Import", "b", "Fehlercodes#Codes", NOW.minusSeconds(300)),
				marker(TURM, "Turm", "Literal Import", "c", "Fehlercodes", NOW.minusSeconds(300)));
		String html = ImportMarkerRenderer.createNote(markers, "page", NOW, Locale.ENGLISH, ZoneId.of("UTC")).toString();

		assertTrue(html, html.contains("This page is imported by "));
		assertEquals(html, 1, count(html, "Fehlercodes Import</a>"));
		assertTrue(html, html.contains("(2 imports)"));
		assertTrue(html, html.contains("Literal Import</a>"));
		assertTrue(html, html.contains("5 minutes ago"));
		assertFalse(html, html.contains("<ul"));
		assertFalse(html, html.contains("<details"));
	}

	@Test
	public void rendersOneItemPerWikiAndCollapsesManyPages() {
		List<ImportMarker> markers = List.of(
				marker(TURM, "Turm", "P1", "a", "Fehlercodes", NOW),
				marker(TURM, "Turm", "P2", "a", "Fehlercodes", NOW),
				marker(TURM, "Turm", "P3", "a", "Fehlercodes", NOW),
				marker(TURM, "Turm", "P4", "a", "Fehlercodes", NOW),
				marker(OTHER, "Other", "Q1", "a", "Fehlercodes", NOW));
		String html = ImportMarkerRenderer.createNote(markers, "page", NOW, Locale.ENGLISH, ZoneId.of("UTC")).toString();

		assertTrue(html, html.contains("This page is imported by 2 wikis:"));
		assertEquals(html, 6, count(html, "<li")); // 2 wikis, 4 pages of the collapsed one
		assertEquals(html, 1, count(html, "<details"));
		assertTrue(html, html.contains(" on 4 pages"));
	}

	@Test
	public void escapesLabels() {
		List<ImportMarker> markers = List.of(marker(TURM, "<b>Turm</b>", "<script>", "a", "Fehlercodes", NOW));
		String html = ImportMarkerRenderer.createNote(markers, "page", NOW, Locale.ENGLISH, ZoneId.of("UTC")).toString();
		assertFalse(html, html.contains("<script>"));
		assertFalse(html, html.contains("<b>Turm"));
	}

	private static int count(String text, String part) {
		int count = 0;
		for (int index = text.indexOf(part); index >= 0; index = text.indexOf(part, index + 1)) count++;
		return count;
	}

	/* ------------------------------------------------ expiry ------------------------------------------------ */

	@Test
	public void expiresAfterTimeToLive() {
		ImportMarker marker = marker(TURM, "Turm", "A", "a", "Fehlercodes", NOW);
		assertFalse(marker.isExpired(NOW.plus(ImportMarker.TIME_TO_LIVE)));
		assertTrue(marker.isExpired(NOW.plus(ImportMarker.TIME_TO_LIVE).plusSeconds(1)));
	}

	@Test
	public void markersOfPageDisappearWhenNoLongerRefreshed() throws Exception {
		wiki = new InterWikiImportTestWiki();
		wiki.start(List.of(content("Fehlercodes", "codes", null)));
		Section<?> root = wiki.manager.getArticle("Fehlercodes").getRootSection();
		Section<?> content = Sections.successor(root, InterWikiImportTestWiki.TestContentMarkup.class);
		assertNotNull(content);

		Instant now = Instant.now();
		Instant expired = now.minus(ImportMarker.TIME_TO_LIVE).minus(Duration.ofMinutes(1));
		ImportMarker.markAsImported(root, marker(TURM, "Turm", "Fresh", "a", "Fehlercodes", now));
		ImportMarker.markAsImported(root, marker(TURM, "Turm", "Old", "a", "Fehlercodes", expired));
		ImportMarker.markAsImported(content, marker(OTHER, "Other", "Section", "a", "Fehlercodes#x", now));
		ImportMarker.markAsImported(content, marker(OTHER, "Other", "OldSection", "a", "Fehlercodes#x", expired));

		// the page shows the markers of all its sections, but none that is expired
		assertEquals(List.of("Fresh", "Section"),
				ImportMarker.getImportMarkers(root).stream().map(ImportMarker::getPageLabel).sorted().toList());
		assertEquals(List.of("Section"),
				ImportMarker.getImportMarkers(content).stream().map(ImportMarker::getPageLabel).toList());

		// a new request refreshes an expired marker
		ImportMarker.markAsImported(root, marker(TURM, "Turm", "Old", "a", "Fehlercodes", now));
		assertEquals(List.of("Fresh", "Old", "Section"),
				ImportMarker.getImportMarkers(root).stream().map(ImportMarker::getPageLabel).sorted().toList());
	}
}
