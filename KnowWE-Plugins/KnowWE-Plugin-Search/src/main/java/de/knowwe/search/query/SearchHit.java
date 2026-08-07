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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.search.index.SectionAnchor;

/**
 * One hit, with everything the result list needs and nothing that only the index cares about.
 *
 * @param title      the page, for the link
 * @param breadcrumb {@code Page › Heading › Subheading}, the headline of the hit
 * @param snippet    a few lines of the section with the matches wrapped in {@code <mark>}; also the fallback for the
 *                   quick search, where a rendered preview would not fit
 * @param anchor     how to find the live section again in order to render its preview
 * @param score      the Lucene score, for debugging and for the score bar of the old page
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public record SearchHit(@NotNull String title,
						@NotNull String breadcrumb,
						@NotNull String snippet,
						@NotNull SectionAnchor anchor,
						float score) {

	/** The heading part of the breadcrumb, or null on a hit that is the page itself. */
	public @Nullable String heading() {
		return anchor.heading();
	}
}
