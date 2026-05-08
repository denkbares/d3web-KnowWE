/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki.changeannotations;

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

/**
 * Glue between the engine, the cache, and the renderer for the JSPWiki side. Used by both
 * {@link AnnotatePageAction} (HTTP entry point) and {@code ChangeAnnotationsTab.jsp}
 * (server-side direct call) so the two paths stay in lock-step.
 */
public final class AnnotateRenderHelper {

	private AnnotateRenderHelper() {
	}

	/**
	 * Returns the server-rendered {@code <knowwe-page-annotate>} HTML for the latest
	 * version of {@code pageName}. The caller is responsible for permission checks and
	 * for ensuring the page exists.
	 */
	public static String renderHtml(Engine engine, String pageName, HttpSession session) {
		PageAnnotation annotation = JspWikiPageAnnotationCache.getInstance().getOrCompute(engine, pageName);
		String text = engine.getManager(PageManager.class).getPureText(pageName, PageProvider.LATEST_VERSION);
		return PageAnnotationRenderer.render(
				annotation,
				text == null ? "" : text,
				diffLinkBuilderFor(pageName),
				themeFromSession(session));
	}

	/**
	 * Diff URL format follows JSPWiki's {@code Diff.jsp}: {@code r1} is the newer version,
	 * {@code r2} the older one (see {@code DiffLinkTag} / {@code InfoContent.jsp} —
	 * {@code <wiki:DiffLink version="current" newVersion="previous">} maps to r1=current,
	 * r2=previous). Version 1 has no predecessor — return null so no link is rendered.
	 */
	private static DiffLinkBuilder diffLinkBuilderFor(String pageName) {
		String encodedPage = Strings.encodeURL(pageName);
		return version -> version <= 1 ? null
				: "Diff.jsp?page=" + encodedPage + "&r1=" + version + "&r2=" + (version - 1);
	}

	/**
	 * Pin {@link Theme#DARK} when the user's {@code DisplayMode} preference is
	 * {@code dark-mode}, otherwise pin {@link Theme#LIGHT}. Mirrors {@code KnowWETextDiffProvider}:
	 * we defer to the wiki preference, not to {@code prefers-color-scheme}, so a light wiki
	 * doesn't get a dark annotate panel just because the OS reports dark mode.
	 */
	private static Theme themeFromSession(HttpSession session) {
		if (session == null) return Theme.LIGHT;
		Object prefs = session.getAttribute("prefs");
		if (prefs instanceof Map<?, ?> map) {
			Object mode = map.get("DisplayMode");
			if (mode != null && "dark-mode".equals(mode.toString())) return Theme.DARK;
		}
		return Theme.LIGHT;
	}
}
