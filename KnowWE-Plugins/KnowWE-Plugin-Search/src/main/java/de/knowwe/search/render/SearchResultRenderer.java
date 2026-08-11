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

package de.knowwe.search.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.preview.PreviewManager;
import de.knowwe.core.preview.PreviewRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.search.index.ArticleChunker;
import de.knowwe.search.index.IndexChunk;
import de.knowwe.search.index.SectionAnchor;

/**
 * Renders the section behind a hit the way the wiki would render it, so a result shows real tables and markup boxes
 * instead of a flattened line of text.
 * <p>
 * This is what a text snippet cannot do and what mkdocs has no equivalent for: the wiki already knows how to display
 * this section, so the result list asks it rather than approximating.
 * <p>
 * Rendering costs a wiki-syntax pass per hit, so only ever call this for the hits actually being shown.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class SearchResultRenderer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchResultRenderer.class);

	/**
	 * How long a joined line may get.
	 * <p>
	 * {@code MarkupParser.PUSHBACK_BUFFER_SIZE} is 10*1024 and not configurable, and {@code peekAheadLine()} does not
	 * wrap a longer line -- it pushes back the first 10239 characters and <b>drops the rest</b>. A preview whose joined
	 * line went over that lost its tail mid-tag, which arrived in the browser as {@code <p></div</p>} and tore the rest
	 * of the section off. Masked characters count here, and each one is a token of a good dozen characters, so the
	 * budget is spent much faster than the visible text suggests.
	 */
	private static final int MAX_LINE_LENGTH = 8000;

	/** The same rule that cut the article into index documents, so a preview shows exactly what was indexed. */
	private final ArticleChunker chunker = new ArticleChunker();

	/**
	 * @return the rendered section, or null when it cannot be rendered — the caller then falls back to the snippet
	 */
	public @Nullable Rendered render(@NotNull SectionAnchor anchor, @NotNull UserContext user) {
		SectionAnchor.Resolution resolution = anchor.resolve(user.getArticleManager());
		Section<?> section = resolution.section();
		if (section == null) return null;
		if (!KnowWEUtils.canView(section, user)) return null;

		// the query is not part of this: the matches are marked in the browser, so the same section renders the same
		// html for every keystroke of the same search
		String cacheKey = section.getID();
		String cached = PreviewCache.getInstance().get(anchor.title(), cacheKey, user.getUserName());
		if (cached != null) return new Rendered(cached, resolution.stale());

		try {
			RenderResult result = new RenderResult(user);
			PreviewRenderer renderer = previewRendererFor(section);
			if (renderer != null) {
				Section<?> previewSection = PreviewManager.getInstance().getPreviewAncestor(section);
				renderer.render(previewSection == null ? section : previewSection, everything(section), user, result);
			}
			else {
				renderChunk(section, user, result);
			}
			String html = toHtml(result, user);
			PreviewCache.getInstance().put(anchor.title(), cacheKey, user.getUserName(), html);
			return new Rendered(html, resolution.stale());
		}
		catch (RuntimeException e) {
			// one badly rendering markup must not take down the whole result list
			LOGGER.warn("Could not render a preview for {}", anchor.title(), e);
			return null;
		}
	}

	/**
	 * The renderer the wiki has for this hit, or null when the hit has to be rendered as a chunk of its own.
	 * <p>
	 * Two cases end up in {@link #renderChunk}. A hit before the first heading has no preview ancestor at all: the only
	 * renderer registered above it is the one for the whole article, and that one only accepts the root section itself
	 * (see {@code ArticlePreviewRenderer.matches}). And a hit that resolved all the way up to the root -- the last step
	 * of the anchor cascade -- would render as the single link "Go to article", which says less than the beginning of
	 * the article does.
	 * <p>
	 * Everything else keeps the wiki's own renderer: a heading brings its section along, a markup block renders as the
	 * box it is on the page.
	 */
	private static @Nullable PreviewRenderer previewRendererFor(Section<?> section) {
		if (section.get() instanceof RootType) return null;
		Section<?> previewSection = PreviewManager.getInstance().getPreviewAncestor(section);
		if (previewSection == null) return null;
		return PreviewManager.getInstance().getPreviewRenderer(previewSection);
	}

	/**
	 * Renders the piece of the article the hit stands for, section by section, the way the article renders it.
	 * <p>
	 * Which sections those are is not decided here: the chunker that cut the article into index documents is asked
	 * again, and the chunk anchored at this section is the one that was indexed. Deriving the range a second time in
	 * this class would let preview and index drift apart -- the preview would show text that never matched, or leave
	 * out the text that did.
	 * <p>
	 * Chunking is a read only walk over the finished KDOM of a single article, and every preview is cached, so asking
	 * again is cheaper than keeping a second copy of the rule.
	 */
	private void renderChunk(Section<?> section, UserContext user, RenderResult result) {
		for (Section<?> content : chunkSections(section)) {
			result.append(content, user);
		}
	}

	private List<Section<?>> chunkSections(Section<?> section) {
		Article article = section.getArticle();
		if (article == null) return List.of(section);
		List<IndexChunk> chunks = chunker.chunk(article);
		for (IndexChunk chunk : chunks) {
			if (chunk.anchor() == section) return chunk.sections();
		}
		// the anchor is the root section, or a section this chunker does not anchor at: the first chunk is the
		// beginning of the article, which is the most useful thing left to show
		return chunks.isEmpty() ? List.of(section) : chunks.get(0).sections();
	}

	/**
	 * Everything inside the hit, so a renderer that shows only the "relevant" parts shows the whole block.
	 * <p>
	 * {@link de.knowwe.kdom.table.TablePreviewRenderer} keeps the header row plus the rows that one of these sections
	 * belongs to, and drops the rest for a placeholder. Handing it the hit alone -- a whole markup block, never a single
	 * row -- left every table as a header over "...". Which rows actually matter cannot be decided here without knowing
	 * the query, and knowing it would make the preview query-dependent and the cache useless; the browser knows where
	 * the matches are and prunes the table there.
	 */
	private static Collection<Section<?>> everything(Section<?> section) {
		List<Section<?>> sections = new ArrayList<>(Sections.successors(section));
		sections.add(section);
		return sections;
	}

	/**
	 * The preview renderers emit wiki markup mixed with HTML, so it has to go through the wiki's own renderer before it
	 * is HTML. Same treatment as {@code RenderPreviewAction}, including its workaround for a heading that directly
	 * follows an annotation.
	 */
	private static String toHtml(RenderResult result, UserContext user) {
		String raw = result.toStringRaw().replaceAll("@!!!", "@\n!!!");
		raw = TABLE_OF_CONTENTS.matcher(raw).replaceAll("");
		String markup = Environment.getInstance().getWikiConnector().renderWikiSyntax(joinRenderedLines(raw, result));
		return RenderResult.unmask(markup, user);
	}

	/**
	 * A table of contents is navigation for a whole page, and the wiki builds it for the page the <i>request</i> is
	 * for, not for the page being previewed. A hit on a page that opens with {@code [{TableOfContents}]} therefore
	 * showed the headings of whatever page the reader happened to be on -- content from somewhere else entirely, inside
	 * a search result. A preview of a passage has no use for it either way.
	 */
	private static final Pattern TABLE_OF_CONTENTS =
			Pattern.compile("\\[\\{\\s*(?:INSERT\\s+)?TableOfContents\\b[^}]*}]\\s*", Pattern.CASE_INSENSITIVE);

	/**
	 * Turns the newlines that sit between two already rendered tags into explicit breaks.
	 * <p>
	 * A markup renderer writes one element per line, and JSPWiki's parser makes a paragraph out of every such line.
	 * A rule block that is one inline element in the article arrives here as twelve paragraphs -- inside a span, where
	 * a paragraph may not be, so the browser tears the block apart while parsing. Wrapping the fragment in a block of
	 * its own does not help; the parser paragraphs inside a div just the same (measured, not assumed).
	 * <p>
	 * The tokens are asked for rather than assumed: {@link RenderResult#mask(String, RenderResult)} uses the result's
	 * own masking key, so nothing here depends on how masking is encoded. A newline in running text has no tag on
	 * either side and is left alone, which is what keeps prose previews their paragraphs.
	 * <p>
	 * Joining has a hard limit, see {@link #MAX_LINE_LENGTH}: the last newline before it is reached stays a newline.
	 */
	static String joinRenderedLines(String maskedHtml, RenderResult result) {
		String tagEnd = RenderResult.mask(">", result);
		String tagStart = RenderResult.mask("<", result);
		String lineBreak = RenderResult.mask("<br/>", result);

		// one pattern for both cases, so the longer one cannot be swallowed by the shorter: group 1 is the blank line
		Pattern between = Pattern.compile(Pattern.quote(tagEnd)
										  + "[ \\t]*\\n[ \\t]*(\\n\\s*)?"
										  + Pattern.quote(tagStart));
		Matcher matcher = between.matcher(maskedHtml);
		StringBuilder joined = new StringBuilder(maskedHtml.length());
		int copiedUpTo = 0;
		int lineLength = 0;

		while (matcher.find()) {
			joined.append(maskedHtml, copiedUpTo, matcher.start());
			lineLength += matcher.start() - copiedUpTo;
			copiedUpTo = matcher.end();

			joined.append(tagEnd);
			// A block element already starts on its own line. Adding a break between two of them -- </div><div>, one
			// table row and the next -- produces a blank line, and since the renderer is handed the whole block there
			// are many such boundaries. Only inline neighbours need the break.
			boolean betweenBlocks = isBlock(lastTagName(maskedHtml, matcher.start(), tagStart))
									|| isBlock(nextTagName(maskedHtml, copiedUpTo, tagStart));
			if (betweenBlocks) {
				lineLength += tagEnd.length() + tagStart.length();
				joined.append(tagStart);
				continue;
			}
			if (lineLength < MAX_LINE_LENGTH) {
				// A blank line separates one rule from the next and has to stay visible, so it becomes two breaks.
				joined.append(lineBreak);
				if (matcher.group(1) != null) joined.append(lineBreak);
				lineLength += tagEnd.length() + lineBreak.length() * (matcher.group(1) == null ? 1 : 2);
			}
			else {
				// Beyond the limit the parser does not wrap the line, it throws the rest of it away. One stray
				// paragraph per ten kilobytes is the cheaper damage.
				joined.append('\n');
				lineLength = 0;
			}
			joined.append(tagStart);
			lineLength += tagStart.length();
		}
		joined.append(maskedHtml, copiedUpTo, maskedHtml.length());
		return joined.toString();
	}

	/** Elements that occupy a line of their own, so a break beside them shows as an empty line. */
	private static final java.util.Set<String> BLOCK_ELEMENTS = java.util.Set.of(
			"div", "p", "table", "thead", "tbody", "tfoot", "tr", "td", "th", "ul", "ol", "li", "dl", "dt", "dd",
			"h1", "h2", "h3", "h4", "h5", "h6", "pre", "blockquote", "form", "fieldset", "section", "article", "hr");

	private static boolean isBlock(@Nullable String tagName) {
		return tagName != null && BLOCK_ELEMENTS.contains(tagName);
	}

	/** The name of the tag that ends right before {@code end}, or null when that is not a tag at all. */
	private static @Nullable String lastTagName(String masked, int end, String tagStart) {
		int open = masked.lastIndexOf(tagStart, end);
		return open < 0 ? null : tagNameAt(masked, open + tagStart.length());
	}

	/** The name of the tag that starts at {@code from}, where {@code from} sits behind a masked "&lt;". */
	private static @Nullable String nextTagName(String masked, int from, String tagStart) {
		int open = masked.indexOf(tagStart, Math.max(0, from - tagStart.length()));
		return open < 0 ? null : tagNameAt(masked, open + tagStart.length());
	}

	private static @Nullable String tagNameAt(String masked, int at) {
		int end = at;
		if (end < masked.length() && masked.charAt(end) == '/') end++;
		int start = end;
		while (end < masked.length() && (Character.isLetterOrDigit(masked.charAt(end)))) end++;
		return end == start ? null : masked.substring(start, end).toLowerCase(java.util.Locale.ROOT);
	}

	/**
	 * @param html  the rendered section
	 * @param stale whether the index is behind the wiki, so this may show something else than what was matched
	 */
	public record Rendered(@NotNull String html, boolean stale) {
	}
}
