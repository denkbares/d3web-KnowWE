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
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.HeaderType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Cuts an article into the units that become Lucene documents.
 * <p>
 * This is a read only walk over the finished KDOM: it looks at
 * {@code article.getRootSection().getChildren()} and groups them. Nothing is parsed, no type is registered and nothing
 * is written back — sectioning is entirely unaffected.
 * <p>
 * A new chunk starts at
 * <ul>
 * <li>every heading, regardless of level, so chunks stay disjoint. Using
 * {@code JSPWikiMarkupUtils.getContent(header)} instead would nest a heading's sub headings inside it and index their
 * text twice.</li>
 * <li>every top level markup block such as {@code %%Question}. Many pages carry no headings at all, and there a markup
 * block and the running text around it are the units a reader recognises. Each also has its own
 * {@code PreviewRenderer}, so a hit renders exactly the block it matched.</li>
 * </ul>
 * The heading hierarchy is carried by {@link IndexChunk#headingPath()}, not by nesting.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class ArticleChunker {

	/** JSPWiki spells the biggest heading {@code !!!}, so more markers mean a higher level. */
	private static final int MAX_HEADING_LEVEL = 3;

	public @NotNull List<IndexChunk> chunk(@NotNull Article article) {
		List<IndexChunk> chunks = new ArrayList<>();
		String[] headingStack = new String[MAX_HEADING_LEVEL];

		List<Section<?>> run = new ArrayList<>();
		Section<?> runAnchor = null;
		String runHeading = null;
		IndexChunk.Kind runKind = IndexChunk.Kind.INTRO;

		for (Section<?> child : article.getRootSection().getChildren()) {
			if (child.get() instanceof HeaderType headerType) {
				addChunk(chunks, runKind, runAnchor, runHeading, headingStack, run, article);
				@SuppressWarnings("unchecked") Section<HeaderType> headerSection = (Section<HeaderType>) child;
				runHeading = headerType.getHeaderText(headerSection);
				push(headingStack, level(headerType), runHeading);
				runAnchor = child;
				runKind = IndexChunk.Kind.HEADING;
				run = new ArrayList<>();
			}
			else if (isBlockMarkup(child)) {
				addChunk(chunks, runKind, runAnchor, runHeading, headingStack, run, article);
				addChunk(chunks, IndexChunk.Kind.MARKUP, child, null, headingStack, List.of(child), article);
				// text following the markup becomes its own block again
				runAnchor = null;
				runHeading = null;
				runKind = IndexChunk.Kind.TEXT;
				run = new ArrayList<>();
			}
			else {
				if (runAnchor == null) runAnchor = child;
				run.add(child);
			}
		}
		addChunk(chunks, runKind, runAnchor, runHeading, headingStack, run, article);
		return chunks;
	}

	/**
	 * Whether the section is a block that deserves its own document. Kept as a single method so further block types can
	 * be added in one place.
	 */
	protected boolean isBlockMarkup(@NotNull Section<?> section) {
		return section.get() instanceof DefaultMarkupType;
	}

	private void addChunk(List<IndexChunk> chunks, IndexChunk.Kind kind, Section<?> anchor, String heading,
						  String[] headingStack, List<Section<?>> sections, Article article) {
		// a heading is worth indexing even without body text, anything else is not
		boolean empty = sections.isEmpty() || sections.stream().allMatch(s -> s.getText().isBlank());
		if (anchor == null) return;
		if (empty && kind != IndexChunk.Kind.HEADING && kind != IndexChunk.Kind.MARKUP) return;

		List<Section<?>> content = kind == IndexChunk.Kind.HEADING
				? prepend(anchor, sections)
				: List.copyOf(sections);
		chunks.add(new IndexChunk(kind, anchor, heading, headingPath(headingStack), content, chunks.size()));
	}

	private static List<Section<?>> prepend(Section<?> first, List<Section<?>> rest) {
		List<Section<?>> all = new ArrayList<>(rest.size() + 1);
		all.add(first);
		all.addAll(rest);
		return List.copyOf(all);
	}

	private static int level(HeaderType headerType) {
		return MAX_HEADING_LEVEL + 1 - headerType.getMarkerCount();
	}

	private static void push(String[] stack, int level, String heading) {
		stack[level - 1] = heading;
		Arrays.fill(stack, level, stack.length, null);
	}

	/**
	 * Skips unused levels instead of stopping at them: many pages start at {@code !!} rather than {@code !!!}, and such
	 * a page must still get a breadcrumb.
	 */
	private static List<String> headingPath(String[] stack) {
		List<String> path = new ArrayList<>(stack.length);
		for (String heading : stack) {
			if (heading != null) path.add(heading);
		}
		return List.copyOf(path);
	}
}
