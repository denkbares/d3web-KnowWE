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
 * Folds the weaker hits of a page under its best one, so a single large page cannot fill the whole result list.
 * <p>
 * Only hits that rank <em>clearly</em> worse are folded away. A page's second section that scores nearly as well as
 * its first is a find in its own right and keeps its own row -- collapsing it would hide a good result behind a
 * "3 more" line and make the list look emptier than it is.
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
	 * @param primary the hit shown as the entry itself
	 * @param folded  the same page's clearly weaker hits, hidden behind a "n more on this page" line
	 */
	public record Group(@NotNull SearchHit primary, @NotNull List<SearchHit> folded) {
	}

	/**
	 * @param hits hits in descending score order
	 * @return one entry per shown hit, in the same order
	 */
	public static @NotNull List<Group> group(@NotNull List<SearchHit> hits) {
		Map<String, List<SearchHit>> foldedByPage = new LinkedHashMap<>();
		Map<String, Double> bestByPage = new LinkedHashMap<>();
		Map<String, Integer> shownByPage = new LinkedHashMap<>();
		List<Group> groups = new ArrayList<>();

		for (SearchHit hit : hits) {
			String key = hit.title().toLowerCase(Locale.ROOT);
			Double best = bestByPage.get(key);
			if (best == null) {
				// the first hit of a page is by definition its best, everything else is measured against it
				bestByPage.put(key, (double) hit.score());
				shownByPage.put(key, 1);
				List<SearchHit> folded = new ArrayList<>();
				foldedByPage.put(key, folded);
				groups.add(new Group(hit, folded));
			}
			else if (shownByPage.get(key) < MAX_SHOWN_PER_PAGE && hit.score() >= best * FOLD_BELOW) {
				shownByPage.put(key, shownByPage.get(key) + 1);
				groups.add(new Group(hit, List.of()));
			}
			else {
				// folded under the page's best entry, not under whichever entry happens to be the latest
				foldedByPage.get(key).add(hit);
			}
		}
		return groups;
	}

	private HitGrouping() {
	}
}
