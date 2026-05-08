/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki.changeannotations;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.pages.PageManager;

import com.denkbares.knowwe.changeannotations.DiffLinkBuilder;
import com.denkbares.knowwe.changeannotations.PageAnnotation;
import com.denkbares.knowwe.changeannotations.PageAnnotationRenderer;
import com.denkbares.knowwe.changeannotations.Theme;
import com.denkbares.strings.Strings;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiUserContext;

/**
 * Returns the server-rendered {@code <knowwe-page-annotate>} HTML for a wiki page —
 * consumed by the Annotate tab in {@code InfoContent.jsp}.
 *
 * <p>Parameters:
 * <ul>
 *   <li>{@code page} — wiki page name (required).</li>
 * </ul>
 *
 * <p>Always annotates the latest version. Annotating a specific historical version would
 * require extending {@link com.denkbares.knowwe.changeannotations.PageAnnotator} to stop
 * at a given step — left for a future iteration.
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

		PageAnnotation annotation;
		try {
			annotation = JspWikiPageAnnotationCache.getInstance().getOrCompute(engine, pageName);
		}
		catch (IllegalArgumentException e) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "No version history for: " + pageName);
			return;
		}

		String pureText = pageManager.getPureText(pageName, PageProvider.LATEST_VERSION);
		if (pureText == null) pureText = "";

		Theme theme = themeFor(context);
		String html = PageAnnotationRenderer.render(annotation, pureText, diffLinkBuilderFor(pageName), theme);
		context.setContentType("text/html; charset=UTF-8");
		context.getWriter().write(html);
	}

	private static Engine engineFor(UserActionContext context) {
		if (context instanceof JSPWikiUserContext jsp) {
			return jsp.getWikiContext().getEngine();
		}
		return null;
	}

	/**
	 * Mirrors the diff provider: pin {@link Theme#DARK} when the user's {@code DisplayMode}
	 * preference is {@code dark-mode}, otherwise leave it on {@link Theme#AUTO}. Reads the
	 * preferences map straight from the session — keeps this file off jsp-api types.
	 */
	private static Theme themeFor(UserActionContext context) {
		HttpSession session = context.getSession();
		if (session == null) return Theme.AUTO;
		Object prefs = session.getAttribute("prefs");
		if (prefs instanceof Map<?, ?> map) {
			Object mode = map.get("DisplayMode");
			if (mode != null && "dark-mode".equals(mode.toString())) return Theme.DARK;
		}
		return Theme.AUTO;
	}

	/**
	 * Diff-URL format follows JSPWiki's {@code Diff.jsp}: {@code Diff.jsp?page=...&r1=N-1&r2=N}.
	 * Version 1 has no predecessor — return {@code null} so the renderer omits the link.
	 */
	private static DiffLinkBuilder diffLinkBuilderFor(String pageName) {
		String encodedPage = Strings.encodeURL(pageName);
		return version -> {
			if (version <= 1) return null;
			return "Diff.jsp?page=" + encodedPage + "&r1=" + (version - 1) + "&r2=" + version;
		};
	}
}
