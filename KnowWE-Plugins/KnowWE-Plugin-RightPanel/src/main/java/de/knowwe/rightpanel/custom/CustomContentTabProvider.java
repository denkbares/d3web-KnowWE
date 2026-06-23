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

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.rightpanel.RightPanelTabProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.util.Icon;

/**
 * The built-in "Custom" right-panel tab which shows the wiki-editable {@code RightPanel} article. Always available, so
 * a wiki always has at least this one tab.
 * <p>
 * The body is rendered server-side, inline in the scaffold ({@link #renderContent(UserContext, RenderResult)}), so the
 * panel mounts with no extra AJAX roundtrip. When the {@code RightPanel} article is missing <em>or</em> renders blank
 * it renders the create-page link instead.
 */
public class CustomContentTabProvider implements RightPanelTabProvider {

	private static final String ARTICLE_NAME = "RightPanel";

	@Override
	public String getTitle(UserContext user) {
		return "Custom";
	}

	@Override
	public void renderIcon(UserContext user, RenderResult result) {
		result.appendHtml(Icon.ARTICLE.toHtml());
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
