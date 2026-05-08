/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki.changeannotations;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.pages.PageManager;

import com.denkbares.strings.Strings;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiUserContext;

/**
 * HTTP entry point for the page annotation. The Annotate tab in {@code InfoContent.jsp}
 * renders the same HTML server-side via {@link AnnotateRenderHelper}, so this action only
 * remains for programmatic / external callers — the standard UI does not depend on it.
 *
 * <p>Parameters:
 * <ul>
 *   <li>{@code page} — wiki page name (required).</li>
 * </ul>
 */
public class AnnotatePageAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String pageName = context.getParameter("page");
		if (Strings.isBlank(pageName)) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "URL parameter 'page' missing.");
			return;
		}

		Engine engine = engineFor(context);
		if (engine == null) {
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Wiki engine not available.");
			return;
		}

		PageManager pageManager = engine.getManager(PageManager.class);
		if (pageManager.getPage(pageName) == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found: " + pageName);
			return;
		}

		// Per-page view permission via the same path KnowWE uses for article rendering.
		Article article = KnowWEUtils.getDefaultArticleManager().getArticle(pageName);
		if (article != null && !KnowWEUtils.canView(article, context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to view: " + pageName);
			return;
		}

		try {
			String html = AnnotateRenderHelper.renderHtml(engine, pageName, context.getSession());
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(html);
		}
		catch (IllegalArgumentException e) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "No version history for: " + pageName);
		}
	}

	private static Engine engineFor(UserActionContext context) {
		if (context instanceof JSPWikiUserContext jsp) {
			return jsp.getWikiContext().getEngine();
		}
		return null;
	}
}
