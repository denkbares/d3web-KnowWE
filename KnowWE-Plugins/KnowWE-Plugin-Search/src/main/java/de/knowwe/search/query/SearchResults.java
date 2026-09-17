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

import org.jetbrains.annotations.NotNull;

/**
 * The answer to one search.
 *
 * @param hits     the window that was asked for
 * @param total    how many documents matched in total
 * @param exact    whether {@code total} is exact; it is not once permission filtering has dropped hits
 * @param tookMs   how long the search took, shown next to the result count
 * @param relaxed  whether the strict interpretation found nothing and every word was made optional instead. The user
 *                 needs to know: they asked for all their words and are being shown less.
 * @param unmatched words of the query that occur nowhere in the wiki, so a fruitless search can say why
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public record SearchResults(@NotNull List<SearchHit> hits,
							long total,
							boolean exact,
							long tookMs,
							boolean relaxed,
							@NotNull List<String> unmatched) {

	public static @NotNull SearchResults empty(long tookMs) {
		return new SearchResults(List.of(), 0, true, tookMs, false, List.of());
	}

	public boolean isEmpty() {
		return hits.isEmpty();
	}
}
