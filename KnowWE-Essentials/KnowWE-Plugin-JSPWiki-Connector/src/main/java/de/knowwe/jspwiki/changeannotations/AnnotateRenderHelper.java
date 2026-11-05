/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki.changeannotations;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.pages.PageManager;

import com.denkbares.knowwe.changeannotations.DiffLinkBuilder;
import com.denkbares.knowwe.changeannotations.PageAnnotation;
import com.denkbares.knowwe.changeannotations.PageAnnotationJson;
import com.denkbares.knowwe.changeannotations.PageAnnotationRenderer;
import com.denkbares.knowwe.changeannotations.Theme;
import com.denkbares.strings.Strings;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import de.knowwe.core.kdom.parsing.Section;

/**
 * Glue between the engine, the cache, and the renderer for the JSPWiki side, behind
 * {@link AnnotatePageAction}.
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
	 * Returns the annotation of the latest version of {@code pageName} as JSON, see {@link PageAnnotationJson}, for
	 * at most {@code maxLines} lines starting with the first line of the page, or with the line {@code section}
	 * starts in. The caller is responsible for permission checks and for ensuring the page exists.
	 *
	 * @param section  a section of the article of the page, or null to start with the first line
	 * @param maxLines the maximum number of lines to return
	 * @return the annotation, or null if the compiled article of the section is not the latest version of the page,
	 * so the line of the section cannot be told
	 */
	@Nullable
	public static JSONObject renderJson(Engine engine, String pageName, @Nullable Section<?> section, int maxLines) {
		PageAnnotation annotation = JspWikiPageAnnotationCache.getInstance().getOrCompute(engine, pageName);
		String text = engine.getManager(PageManager.class).getPureText(pageName, PageProvider.LATEST_VERSION);
		if (text == null) text = "";
		int firstLine = 1;
		if (section != null) {
			String articleText = section.getArticle().getText();
			if (!articleText.replace("\r\n", "\n").equals(text.replace("\r\n", "\n"))) return null;
			firstLine = PageAnnotationJson.lineAt(articleText, section.getOffsetInArticle());
		}
		return PageAnnotationJson.toJson(annotation, text, firstLine, maxLines, diffLinkBuilderFor(pageName));
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
