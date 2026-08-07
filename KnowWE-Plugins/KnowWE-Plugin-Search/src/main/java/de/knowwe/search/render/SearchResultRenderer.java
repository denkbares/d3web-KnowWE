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

		try {
			Section<?> previewSection = PreviewManager.getInstance().getPreviewAncestor(section);
			if (previewSection == null) previewSection = section;
			PreviewRenderer renderer = PreviewManager.getInstance().getPreviewRenderer(previewSection);
			if (renderer == null) return null;

			RenderResult result = new RenderResult(user);
			renderer.render(previewSection, List.of(section), user, result);
			return new Rendered(toHtml(result, user), resolution.stale());
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
		String markup = Environment.getInstance().getWikiConnector()
				.renderWikiSyntax(result.toStringRaw().replaceAll("@!!!", "@\n!!!"));
		return RenderResult.unmask(markup, user);
	}

	/**
	 * @param html  the rendered section
	 * @param stale whether the index is behind the wiki, so this may show something else than what was matched
	 */
	public record Rendered(@NotNull String html, boolean stale) {
	}
}
