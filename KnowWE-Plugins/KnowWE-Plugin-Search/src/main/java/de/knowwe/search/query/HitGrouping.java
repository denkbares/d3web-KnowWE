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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

/**
 * Collects the hits of one page into one entry, so a single large page cannot fill the whole result list.
 * <p>
 * A page appears exactly once, at the rank of its best section, with its further sections listed underneath it. They
 * belong together and have to stay together: sorting them into the list by their own score would put a page's second
 * section pages away from its first, while a line above it claims to count it among the ones not shown.
 * <p>
 * Only sections that rank <em>clearly</em> worse are folded away behind that line. One that scores nearly as well as
 * the page's best is a find in its own right and stays visible -- collapsing it would hide a good result and make the
 * list look emptier than it is.
 * <p>
 * The threshold is a fraction of the page's best score, never an absolute distance: Lucene scores are comparable only
 * within one query, so "0.3 lower" means something different for every search, while "below 60% of the best" does not.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class HitGrouping {

	/** A hit below this fraction of its page's best score is folded away. */
	public static final double FOLD_BELOW = 0.6;

	/**
	 * How many sections of one page get a row of their own. Three is enough to show that a page covers a topic in
	 * several places without letting it take over the list; everything beyond goes behind the expander.
	 */
	public static final int MAX_SHOWN_PER_PAGE = 3;

	/**
	 * @param primary the page's best section, the entry itself
	 * @param shown   its further sections that rank close enough to stay visible, in score order
	 * @param folded  the clearly weaker ones, hidden behind a "n more on this page" line
	 */
	public record Group(@NotNull SearchHit primary, @NotNull List<SearchHit> shown, @NotNull List<SearchHit> folded) {

		/** All sections below the entry itself, in the order they are shown once everything is unfolded. */
		public @NotNull List<SearchHit> rest() {
			List<SearchHit> rest = new ArrayList<>(shown);
			rest.addAll(folded);
			return rest;
		}
	}

	/**
	 * @param hits hits in descending score order
	 * @return one entry per page, ranked by that page's best section
	 */
	public static @NotNull List<Group> group(@NotNull List<SearchHit> hits) {
		Map<String, Group> byPage = new LinkedHashMap<>();

		for (SearchHit hit : hits) {
			String key = hit.title().toLowerCase(Locale.ROOT);
			Group group = byPage.get(key);
			if (group == null) {
				// the first hit of a page is by definition its best, everything else is measured against it
				byPage.put(key, new Group(hit, new ArrayList<>(), new ArrayList<>()));
			}
			else if (group.shown().size() + 1 < MAX_SHOWN_PER_PAGE
					 && hit.score() >= group.primary().score() * FOLD_BELOW) {
				group.shown().add(hit);
			}
			else {
				group.folded().add(hit);
			}
		}
		return new ArrayList<>(byPage.values());
	}

	private HitGrouping() {
	}
}
