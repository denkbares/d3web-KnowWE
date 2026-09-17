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

package de.knowwe.search.index;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.kdom.parsing.Section;

/**
 * One unit of the search index: the piece of an article that becomes a single Lucene document, and that a hit links to
 * and renders a preview of.
 * <p>
 * Chunks of an article are disjoint and together cover it completely, so no text is indexed twice. The heading
 * hierarchy is not expressed by nesting but by {@link #headingPath()}.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public record IndexChunk(@NotNull Kind kind,
						 @NotNull Section<?> anchor,
						 @Nullable String heading,
						 @NotNull List<String> headingPath,
						 @NotNull List<Section<?>> sections,
						 int ordinal) {

	public enum Kind {
		/** Everything before the first heading or block of an article. */
		INTRO,
		/** A heading and the running text directly below it. */
		HEADING,
		/** A top level markup block such as {@code %%Question}, indexed and previewed as its own unit. */
		MARKUP,
		/** Running text between two blocks, on a page that may have no headings at all. */
		TEXT
	}

	/**
	 * The breadcrumb shown for a hit, for example {@code Body-Mass-Index › The Knowledge Base › Terminology}. The
	 * article title is always the first element.
	 */
	public @NotNull String breadcrumb(@NotNull String title) {
		if (headingPath.isEmpty()) return title;
		return title + " › " + String.join(" › ", headingPath);
	}

	/**
	 * Position of the anchor section within the article, used as a stable tie breaker when two chunks of the same page
	 * score equally.
	 */
	public int offset() {
		return anchor.getOffsetInArticle();
	}
}
