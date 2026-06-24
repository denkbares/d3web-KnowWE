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
package de.d3web.we.watches;

import org.jetbrains.annotations.NotNull;

import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.rightpanel.RightPanelTabProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.util.Icon;

/**
 * The "Watches" right-panel tab: a d3web debugging tool that resolves term identifiers to their current knowledge-base
 * value (via {@link GetExpressionValueAction} -> the {@link GetValueObject} resolver).
 * <p>
 * Client-rendered: {@link #renderContent(UserContext, RenderResult)} leaves the body empty and the watches JS
 * (KNOWWE.plugin.d3webbasic.watches) hydrates it on the {@code rightPanelTabInitialized} event for id {@code watches}.
 */
public class WatchesTabProvider implements RightPanelTabProvider {

	@Override
	public @NotNull String getTitle(UserContext user) {
		return "Watches";
	}

	@Override
	public @NotNull String getDescription(UserContext user) {
		return "Live values of watched knowledge-base terms";
	}

	@Override
	public void renderIcon(UserContext user, RenderResult result) {
		result.appendHtml(Icon.DEBUG.toHtml());
	}

	@Override
	public void renderContent(UserContext context, RenderResult result) {
		// client-rendered: the watches JS fills the body on the rightPanelTabInitialized event for id "watches"
	}

	@Override
	public boolean isAvailable(UserContext context) {
		// only on pages that compile a d3web knowledge base (same notion of "d3web page" the watch
		// resolver relies on), re-evaluated on every panel load
//		return true;
		Article article = context.getArticle();
		if (article == null) return false;
		return !Compilers.getCompilers(article.getRootSection(), D3webCompiler.class).isEmpty();
	}
}
