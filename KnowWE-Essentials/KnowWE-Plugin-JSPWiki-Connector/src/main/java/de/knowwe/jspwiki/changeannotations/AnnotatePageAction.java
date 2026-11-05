/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki.changeannotations;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.pages.PageManager;

import com.denkbares.strings.Strings;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiConnector;

/**
 * HTTP entry point for the page annotation. The Annotate tab fetches its content from here when the user opens it,
 * because computing the annotation reads every version of the page and must not be paid on every page info view.
 *
 * <p>The page editors fetch the same annotation as JSON, to show it in a gutter next to the text.
 *
 * <p>Parameters:
 * <ul>
 *   <li>{@code page} — wiki page name (required).</li>
 *   <li>{@code format} — {@code json} for the annotation as JSON, see {@link
 *   com.denkbares.knowwe.changeannotations.PageAnnotationJson}, otherwise the rendered HTML (optional).</li>
 *   <li>{@code SectionID} — with {@code format=json}, start with the line this section starts in (optional).</li>
 *   <li>{@code lines} — with {@code format=json}, the maximum number of lines to return (optional).</li>
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
		Article article = context.getArticleManager().getArticle(pageName);
		if (article != null && !KnowWEUtils.canView(article, context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to view: " + pageName);
			return;
		}

		if ("json".equals(context.getParameter("format"))) {
			writeJson(context, engine, pageName);
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

	private static void writeJson(UserActionContext context, Engine engine, String pageName) throws IOException {
		Section<?> section = null;
		if (context.getParameter(Attributes.SECTION_ID) != null) {
			section = getSection(context);
			if (!pageName.equals(section.getTitle())) {
				context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Section is not part of page: " + pageName);
				return;
			}
		}
		JSONObject json;
		try {
			json = AnnotateRenderHelper.renderJson(engine, pageName, section, maxLines(context));
		}
		catch (IllegalArgumentException e) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "No version history for: " + pageName);
			return;
		}
		if (json == null) {
			context.sendError(HttpServletResponse.SC_CONFLICT, "The page was changed, please reload: " + pageName);
			return;
		}
		context.setContentType("application/json; charset=UTF-8");
		context.getWriter().write(json.toString());
	}

	private static int maxLines(UserActionContext context) {
		String lines = context.getParameter("lines");
		if (Strings.isBlank(lines)) return Integer.MAX_VALUE;
		try {
			return Math.max(0, Integer.parseInt(lines.trim()));
		}
		catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * The wiki engine behind this request, taken from the connector of the request's environment because an action
	 * request carries no wiki context to take it from. This is the same lookup {@link KnowWEUtils#canView} uses above.
	 */
	private static Engine engineFor(@NotNull UserActionContext context) {
		Environment environment = context.findEnvironment();
		if (environment != null && environment.getWikiConnector() instanceof JSPWikiConnector connector) {
			return connector.getEngine();
		}
		return null;
	}
}
