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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.preview.PreviewManager;
import de.knowwe.core.preview.PreviewRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
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
			Section<?> previewSection = PreviewManager.getInstance().getPreviewAncestor(section);
			if (previewSection == null) previewSection = section;
			PreviewRenderer renderer = PreviewManager.getInstance().getPreviewRenderer(previewSection);
			if (renderer == null) return null;

			RenderResult result = new RenderResult(user);
			renderer.render(previewSection, List.of(section), user, result);
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
	 * The preview renderers emit wiki markup mixed with HTML, so it has to go through the wiki's own renderer before it
	 * is HTML. Same treatment as {@code RenderPreviewAction}, including its workaround for a heading that directly
	 * follows an annotation.
	 */
	private static String toHtml(RenderResult result, UserContext user) {
		String raw = result.toStringRaw().replaceAll("@!!!", "@\n!!!");
		String markup = Environment.getInstance().getWikiConnector().renderWikiSyntax(joinRenderedLines(raw, result));
		return RenderResult.unmask(markup, user);
	}

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
	 */
	static String joinRenderedLines(String maskedHtml, RenderResult result) {
		String tagEnd = RenderResult.mask(">", result);
		String tagStart = RenderResult.mask("<", result);
		String lineBreak = RenderResult.mask("<br/>", result);
		String end = Pattern.quote(tagEnd);
		String start = Pattern.quote(tagStart);

		// A blank line separates one rule from the next and has to stay visible, so it becomes two breaks. Matching
		// it first matters: the single line pattern would otherwise swallow it and every rule would look alike.
		String joined = maskedHtml.replaceAll(
				end + "[ \\t]*\\n[ \\t]*\\n\\s*" + start,
				Matcher.quoteReplacement(tagEnd + lineBreak + lineBreak + tagStart));
		return joined.replaceAll(
				end + "[ \\t]*\\n[ \\t]*" + start,
				Matcher.quoteReplacement(tagEnd + lineBreak + tagStart));
	}

	/**
	 * @param html  the rendered section
	 * @param stale whether the index is behind the wiki, so this may show something else than what was matched
	 */
	public record Rendered(@NotNull String html, boolean stale) {
	}
}
