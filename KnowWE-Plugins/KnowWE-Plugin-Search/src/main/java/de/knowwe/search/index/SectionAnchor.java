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

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.jspwiki.types.HeaderType;

/**
 * Finds the live KDOM section a hit refers to, so its preview can be rendered.
 * <p>
 * A stored section id is not a durable key: it is a hash over the section's position and text, registered lazily in a
 * JVM global map, and collisions are resolved by counting up. After a restart it may resolve to nothing, and after a
 * reparse it may resolve to a <i>different</i> section. Treating it as authoritative would show the wrong preview,
 * which is worse than showing none.
 * <p>
 * Hence a cascade with a verification step, ending at the article itself rather than at a failure. Everything below the
 * first step means the index is behind the wiki, which the result list says out loud instead of quietly showing stale
 * content as current.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public record SectionAnchor(@NotNull String title, @Nullable String sectionId, @Nullable String sectionPath,
							@Nullable String heading) {

	/**
	 * @param section the section to render, never null unless the article itself is gone
	 * @param stale   whether the index no longer matches the article, so the preview may show something else than what
	 *                was indexed
	 */
	public record Resolution(@Nullable Section<?> section, boolean stale) {

		public boolean isResolved() {
			return section != null;
		}
	}

	public @NotNull Resolution resolve(@NotNull ArticleManager articleManager) {
		Article article = articleManager.getArticle(title);
		if (article == null) return new Resolution(null, true);

		Section<?> byId = byId(article);
		if (byId != null) return new Resolution(byId, false);

		Section<?> byPosition = byPosition(article);
		if (byPosition != null) return new Resolution(byPosition, true);

		Section<?> byHeading = byHeading(article);
		if (byHeading != null) return new Resolution(byHeading, true);

		return new Resolution(article.getRootSection(), true);
	}

	/** The fast path, but only when the section it finds really is the one that was indexed. */
	private @Nullable Section<?> byId(Article article) {
		if (sectionId == null || sectionId.isBlank()) return null;
		Section<?> section;
		try {
			section = Sections.get(sectionId);
		}
		catch (RuntimeException e) {
			// an id is read as a hexadecimal number, so anything else in the index -- a value from an older schema,
			// a truncated field -- throws rather than answering null. One such id must not fail the whole search.
			return null;
		}
		if (section == null || !Sections.isLive(section)) return null;
		if (!section.getTitle().equalsIgnoreCase(title)) return null;
		return section;
	}

	/**
	 * The position in the KDOM survives a reparse as long as the page structure did not change, which is the common
	 * case for an edit somewhere else on the page. KnowWE uses the same idiom internally to carry a section over into a
	 * new article version.
	 */
	private @Nullable Section<?> byPosition(Article article) {
		List<Integer> position = parsePosition(sectionPath);
		if (position.isEmpty()) return null;
		try {
			return Sections.get(article, position);
		}
		catch (RuntimeException e) {
			// the path may point past the end of a shortened article
			return null;
		}
	}

	/** Last resort before giving up on the section: a heading that still reads the same. */
	private @Nullable Section<?> byHeading(Article article) {
		if (heading == null || heading.isBlank()) return null;
		for (Section<HeaderType> header : Sections.successors(article, HeaderType.class)) {
			if (heading.equals(header.get().getHeaderText(header))) return header;
		}
		return null;
	}

	static @NotNull List<Integer> parsePosition(@Nullable String path) {
		if (path == null || path.isBlank()) return List.of();
		List<Integer> position = new ArrayList<>();
		for (String part : path.split("\\.")) {
			try {
				position.add(Integer.parseInt(part.trim()));
			}
			catch (NumberFormatException e) {
				return List.of();
			}
		}
		return position;
	}
}
