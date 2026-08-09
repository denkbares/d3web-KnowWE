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

package de.knowwe.search.query;

import java.util.List;

import org.junit.Test;

import de.knowwe.search.index.SectionAnchor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Guards which hits of a page get a row of their own and which disappear behind the expander.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class HitGroupingTest {

	@Test
	public void hitsOfDifferentPagesAllStayVisible() {
		List<HitGrouping.Group> groups = HitGrouping.group(List.of(
				hit("Montage", "Schritt 1", 1.0f), hit("Pruefung", "Sichtpruefung", 0.2f)));
		assertEquals(2, groups.size());
		assertTrue(groups.stream().allMatch(group -> group.rest().isEmpty()));
	}

	@Test
	public void aPageAppearsOnceHoweverManySectionsItHits() {
		// otherwise a page's second section ranks somewhere else entirely while a line above it claims to count it
		List<HitGrouping.Group> groups = HitGrouping.group(List.of(
				hit("Montage", "A", 1.0f), hit("Pruefung", "X", 0.95f), hit("Montage", "B", 0.9f)));
		assertEquals(2, groups.size());
		assertEquals("Montage", groups.get(0).primary().title());
		assertEquals(List.of("B"), groups.get(0).shown().stream().map(h -> h.anchor().heading()).toList());
		assertEquals("Pruefung", groups.get(1).primary().title());
	}

	@Test
	public void aClearlyWeakerHitOfTheSamePageIsFolded() {
		List<HitGrouping.Group> groups = HitGrouping.group(List.of(
				hit("Montage", "Schritt 1", 1.0f), hit("Montage", "Anhang", 0.3f)));
		assertEquals(1, groups.size());
		assertEquals(1, groups.get(0).folded().size());
		assertTrue(groups.get(0).shown().isEmpty());
		assertEquals("Anhang", groups.get(0).folded().get(0).anchor().heading());
	}

	@Test
	public void aHitThatRanksNearlyAsWellKeepsItsOwnRow() {
		// the point of the threshold: a second good section is a find, not a footnote to the first
		List<HitGrouping.Group> groups = HitGrouping.group(List.of(
				hit("Montage", "Schritt 1", 1.0f), hit("Montage", "Schritt 2", 0.9f)));
		assertEquals(1, groups.size());
		assertEquals(1, groups.get(0).shown().size());
		assertTrue(groups.get(0).folded().isEmpty());
	}

	@Test
	public void atMostThreeSectionsOfOnePageAreShown() {
		List<HitGrouping.Group> groups = HitGrouping.group(List.of(
				hit("Montage", "A", 1.0f), hit("Montage", "B", 0.99f),
				hit("Montage", "C", 0.98f), hit("Montage", "D", 0.97f)));
		assertEquals(1, groups.size());
		assertEquals(2, groups.get(0).shown().size());
		assertEquals(1, groups.get(0).folded().size());
		assertEquals("D", groups.get(0).folded().get(0).anchor().heading());
	}

	@Test
	public void whatIsShownAndWhatIsFoldedKeepsTheScoreOrder() {
		List<HitGrouping.Group> groups = HitGrouping.group(List.of(
				hit("Montage", "A", 1.0f), hit("Montage", "B", 0.9f),
				hit("Montage", "C", 0.1f), hit("Montage", "D", 0.05f)));
		assertEquals(1, groups.size());
		assertEquals(List.of("B", "C", "D"),
				groups.get(0).rest().stream().map(h -> h.anchor().heading()).toList());
		assertEquals(1, groups.get(0).shown().size());
		assertEquals(2, groups.get(0).folded().size());
	}

	@Test
	public void theSamePageInDifferentSpellingIsOnePage() {
		// the article manager is case insensitive, so the grouping key has to be as well
		List<HitGrouping.Group> groups = HitGrouping.group(List.of(
				hit("Montage", "A", 1.0f), hit("montage", "B", 0.1f)));
		assertEquals(1, groups.size());
		assertEquals(1, groups.get(0).folded().size());
	}

	@Test
	public void anEmptyResultStaysEmpty() {
		assertTrue(HitGrouping.group(List.of()).isEmpty());
	}

	private static SearchHit hit(String title, String heading, float score) {
		return new SearchHit(title, title + " › " + heading, "",
				new SectionAnchor(title, null, null, heading), score);
	}
}
