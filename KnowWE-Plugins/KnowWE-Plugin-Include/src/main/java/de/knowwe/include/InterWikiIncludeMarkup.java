/*
 * Copyright (C) 2021 denkbares GmbH, Germany
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

package de.knowwe.include;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Allows including pages a from other wikis by specifying domain, page and optionally title
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 06.12.21
 */
public class InterWikiIncludeMarkup extends AttachmentUpdateMarkup implements AttachmentCompileType {

	private static final String WIKI_ANNOTATION = "wiki";
	private static final String PAGE_ANNOTATION = "page";
	private static final String SECTION_ANNOTATION = "title";
	private static final String COMPILE_ANNOTATION = "compile";

	private static final DefaultMarkup MARKUP = new DefaultMarkup("InterWikiInclude");

	static {
		MARKUP.addAnnotation(INTERVAL_ANNOTATION, false);
		MARKUP.addAnnotationContentType(INTERVAL_ANNOTATION, new TimeStampType());
		MARKUP.addAnnotation(COMPILE_ANNOTATION, false, "true", "false");
		MARKUP.addAnnotation(REPLACEMENT, false, Pattern.compile(".+->.*"));
		MARKUP.addAnnotation(REGEX_REPLACEMENT, false, Pattern.compile(".+->.*"));
		MARKUP.addAnnotation(WIKI_ANNOTATION, true);
		MARKUP.addAnnotation(PAGE_ANNOTATION, true);
		MARKUP.addAnnotation(SECTION_ANNOTATION, false);
	}

	public InterWikiIncludeMarkup() {
		super(MARKUP);
		setRenderer(new InterWikiIncludeRenderer());
	}

	@Nullable
	@Override
	public WikiAttachment getWikiAttachment(Section<? extends AttachmentUpdateMarkup> section) throws IOException {
		if (section == null) return null;
		Section<InterWikiIncludeMarkup> interWikiSection = $(section).closest(InterWikiIncludeMarkup.class).getFirst();
		if (interWikiSection == null) return null;
		String path = getWikiAttachmentPath(interWikiSection);
		return Environment.getInstance().getWikiConnector().getAttachment(path);
	}

	@Override
	public String getWikiAttachmentPath(Section<? extends AttachmentUpdateMarkup> section) {
		Section<InterWikiIncludeMarkup> interWikiSection = $(section).closest(InterWikiIncludeMarkup.class).getFirst();
		if (interWikiSection == null) return null;
		String pageName = "-" + getPageName(interWikiSection);
		String sectionName = getSectionName(interWikiSection);
		sectionName = sectionName == null ? "" : "-" + sectionName;
		return section.getTitle() + PATH_SEPARATOR + "WikiImport" + pageName + sectionName;
	}

	private String getPageName(Section<InterWikiIncludeMarkup> section) {
		return DefaultMarkupType.getAnnotation(section, PAGE_ANNOTATION);
	}

	private String getSectionName(Section<InterWikiIncludeMarkup> section) {
		return DefaultMarkupType.getAnnotation(section, SECTION_ANNOTATION);
	}

	@Override
	public @Nullable WikiAttachment getCompiledAttachment(Section<? extends AttachmentCompileType> section) throws IOException {
		if (isCompiling(section)) {
			return getWikiAttachment($(section).closest(AttachmentUpdateMarkup.class).getFirst());
		}
		return null;
	}

	private boolean isCompiling(Section<? extends AttachmentCompileType> section) {
		return !"false".equals(DefaultMarkupType.getAnnotation(section, COMPILE_ANNOTATION));
	}

	@Override
	public @Nullable URL getUrl(Section<? extends AttachmentUpdateMarkup> sec) {
		Section<InterWikiIncludeMarkup> section = $(sec).closest(InterWikiIncludeMarkup.class).getFirst();
		if (section == null) return null;
		String wikiAnnotation = Strings.trim(DefaultMarkupType.getAnnotation(section, WIKI_ANNOTATION));
		if (wikiAnnotation == null) return null;
		if (!wikiAnnotation.matches("^https?://.*")) {
			wikiAnnotation = "https://" + wikiAnnotation;
		}
		if (!wikiAnnotation.endsWith("/")) {
			wikiAnnotation += "/";
		}

		String pageName = section.get().getPageName(section);
		String reference = Strings.encodeURL(pageName);
		String sectionName = section.get().getSectionName(section);
		if (sectionName != null) {
			reference += Strings.encodeURL("#" + sectionName);
		}

		String url = wikiAnnotation + "_action/GetWikiSectionTextAction?reference=" + reference;

		try {
			return new URL(url);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	protected long getIntervalMillis(Section<? extends AttachmentUpdateMarkup> section) {
		long interval = super.getIntervalMillis(section);
		if (interval == Long.MAX_VALUE) {
			interval = 1000 * 60 * 60; // 1 h default interval
		}
		return interval;
	}

	private static class InterWikiIncludeRenderer extends DefaultMarkupRenderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			Collection<String> errors = getMessageStrings(section, Message.Type.ERROR, user);
			Collection<String> warnings = getMessageStrings(section, Message.Type.ERROR, user);
			setFramed(!errors.isEmpty() || !warnings.isEmpty());
			super.render(section, user, result);
		}

		@Override
		public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult result) {
			Section<InterWikiIncludeMarkup> markup = $(section).closest(InterWikiIncludeMarkup.class).getFirst();
			if (markup == null) return;
			String path = markup.get().getWikiAttachmentPath(markup);
			Article article = user.getArticleManager().getArticle(path);
			if (article == null) {
				result.appendHtmlElement("span", "Included article not (yet) available", "class", "warning");
			}
			else {
				new FramedIncludedSectionRenderer(true).render(article.getRootSection(), user, result);
			}

			long lastRun = markup.get().timeSinceLastRun(markup);
			if (lastRun < Long.MAX_VALUE) {
				String lastRunDisplay = getDisplay(lastRun);
				String message = "Last check was " + lastRunDisplay + " ago";
				long lastChange = markup.get().timeSinceLastChange(markup);
				if (lastChange < Long.MAX_VALUE) {
					String lastChangeDisplay = getDisplay(lastChange);
					message += ", last change was " + lastChangeDisplay + " ago";
				}
				result.appendHtmlElement("span", message);
			}
			if (isFramed()) {
				renderAnnotations(markup, $(markup).successor(AnnotationType.class).asList(), user, result);
			}
		}
	}
}
