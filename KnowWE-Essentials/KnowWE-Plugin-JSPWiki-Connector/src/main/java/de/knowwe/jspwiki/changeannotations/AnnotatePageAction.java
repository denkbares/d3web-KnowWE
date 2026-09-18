/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki.changeannotations;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.pages.PageManager;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiConnector;

/**
 * HTTP entry point for the page annotation. The Annotate tab fetches its content from here when the user opens it,
 * because computing the annotation reads every version of the page and must not be paid on every page info view.
 *
 * <p>Parameters:
 * <ul>
 *   <li>{@code page} — wiki page name (required).</li>
 * </ul>
 */
public class AnnotatePageAction extends AbstractAction {

	/**
	 * Reads the change annotations of the article it is called for, so it needs read access to that article.
	 */
	@Override
	public Access requiredAccess() {
		return Access.READ;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		String pageName = context.getParameter("page");
		if (Strings.isBlank(pageName)) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "URL parameter 'page' missing.");
			return;
		}

		Engine engine = engineFor();
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

	/**
	 * The wiki engine behind this request, taken from the environment's connector because an action request carries
	 * no wiki context to take it from. This is the same lookup {@link KnowWEUtils#canView} uses above.
	 */
	private static Engine engineFor() {
		if (Environment.isInitialized()
				&& Environment.getInstance().getWikiConnector() instanceof JSPWikiConnector connector) {
			return connector.getEngine();
		}
		return null;
	}
}
