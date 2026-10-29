/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
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
package de.knowwe.rightpanel.custom;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.rightpanel.RightPanelTabProvider;
import de.knowwe.core.tools.OffMarkup;
import de.knowwe.core.user.UserContext;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * The built-in "Custom" right-panel tab which shows the wiki-editable {@code RightPanel} article. Available only if
 * the article has content: it is hidden if the article is missing, empty, or contains only deactivated markups
 * ({@code %%Off:...}, see {@link OffMarkup}).
 * <p>
 * The body is rendered server-side, inline in the scaffold ({@link #renderContent(UserContext, RenderResult)}), so the
 * panel mounts with no extra AJAX roundtrip. When the {@code RightPanel} article renders blank nevertheless, it renders
 * the create-page link instead.
 */
public class CustomContentTabProvider implements RightPanelTabProvider {

	private static final String ARTICLE_NAME = "RightPanel";

	@Override
	public @NotNull String getTitle(UserContext user) {
		return "Custom";
	}

	@Override
	public @NotNull String getDescription(UserContext user) {
		return "Wiki-editable content from the RightPanel article";
	}

	@Override
	public void renderIcon(UserContext user, RenderResult result) {
		result.appendHtml(Icon.ARTICLE.toHtml());
	}

	@Override
	public boolean isAvailable(UserContext context) {
		Article article = context.getArticleManager().getArticle(ARTICLE_NAME);
		return article != null && hasActiveContent(article);
	}

	/**
	 * Returns true if the article has any text that is not blank and not part of a deactivated markup.
	 */
	static boolean hasActiveContent(Article article) {
		return $(article.getRootSection()).successor(Type.class)
				.filter(section -> section.getChildren().isEmpty())
				.filter(section -> !section.getText().isBlank())
				.anyMatch(section -> !$(section).hasAncestor(OffMarkup.class));
	}

	@Override
	public void renderContent(UserContext context, RenderResult result) {
		Article article = context.getArticleManager().getArticle(ARTICLE_NAME);
		if (article != null) {
			// render into a temporary buffer so we can fall back to the create-page link when the article
			// exists but renders blank (e.g. an empty page)
			RenderResult content = new RenderResult(result);
			DelegateRenderer.getInstance().render(article.getRootSection(), context, content);
			if (!content.toString().isBlank()) {
				result.append(content);
				return;
			}
		}
		result.appendHtml("<a href='Edit.jsp?page=RightPanel' class='createpage'>RightPanel</a>");
	}
}
