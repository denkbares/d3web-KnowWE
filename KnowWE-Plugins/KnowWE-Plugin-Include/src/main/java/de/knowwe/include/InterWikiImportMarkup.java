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
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.tools.HelpToolProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsyncPreviewRenderer;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.tools.Tool;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Allows including pages a from other wikis by specifying domain, page and optionally title
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 06.12.21
 */
public class InterWikiImportMarkup extends AttachmentUpdateMarkup implements AttachmentCompileType {

	private static final String WIKI_ANNOTATION = "wiki";
	private static final String PAGE_ANNOTATION = "page";
	private static final String SECTION_ANNOTATION = "section";
	private static final String COMPILE_ANNOTATION = "compile";

	private static final DefaultMarkup MARKUP = new DefaultMarkup("InterWikiImport");

	static {
		MARKUP.addAnnotation(INTERVAL_ANNOTATION, false);
		MARKUP.addAnnotationContentType(INTERVAL_ANNOTATION, new TimeStampType());
		MARKUP.addAnnotation(COMPILE_ANNOTATION, false, "true", "false");
		MARKUP.addAnnotation(REPLACEMENT, false, Pattern.compile(".+->(.|[\r\n])*"));
		MARKUP.addAnnotation(REGEX_REPLACEMENT, false, Pattern.compile(".+->(.|[\r\n])*"));
		MARKUP.addAnnotation(WIKI_ANNOTATION, true);
		MARKUP.addAnnotation(PAGE_ANNOTATION, true);
		MARKUP.addAnnotation(SECTION_ANNOTATION, false);
	}

	public InterWikiImportMarkup() {
		super(MARKUP);
		setRenderer(new AsynchronousRenderer(new InterWikiImportRenderer()));
	}

	@Nullable
	@Override
	public WikiAttachment getWikiAttachment(Section<? extends AttachmentUpdateMarkup> section) throws IOException {
		if (section == null) return null;
		Section<InterWikiImportMarkup> interWikiSection = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (interWikiSection == null) return null;
		String path = getWikiAttachmentPath(interWikiSection);
		return Environment.getInstance().getWikiConnector().getAttachment(path);
	}

	@Override
	public String getWikiAttachmentPath(Section<? extends AttachmentUpdateMarkup> section) {
		Section<InterWikiImportMarkup> interWikiSection = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (interWikiSection == null) return null;
		String pageName = "-" + getPageName(interWikiSection);
		String sectionName = getSectionName(interWikiSection);
		sectionName = sectionName == null ? "" : "-" + sectionName;
		return section.getTitle() + PATH_SEPARATOR + "WikiImport" + pageName + sectionName + ".txt";
	}

	private String getPageName(Section<InterWikiImportMarkup> section) {
		return DefaultMarkupType.getAnnotation(section, PAGE_ANNOTATION);
	}

	private String getSectionName(Section<InterWikiImportMarkup> section) {
		return DefaultMarkupType.getAnnotation(section, SECTION_ANNOTATION);
	}

	@Override
	public @Nullable WikiAttachment getCompiledAttachment(Section<? extends AttachmentCompileType> section) throws IOException {
		if (isCompilingTheAttachment(section)) {
			return getWikiAttachment($(section).closest(AttachmentUpdateMarkup.class).getFirst());
		}
		return null;
	}

	@Override
	public String getCompiledAttachmentPath(Section<? extends AttachmentCompileType> section) {
		return $(section).closest(AttachmentUpdateMarkup.class).mapFirst(this::getWikiAttachmentPath);
	}

	@Override
	public boolean isCompilingTheAttachment(Section<? extends AttachmentCompileType> section) {
		return !"false".equals(DefaultMarkupType.getAnnotation(section, COMPILE_ANNOTATION));
	}

	@Override
	public @Nullable URL getUrl(Section<? extends AttachmentUpdateMarkup> section) {
		return getUrl(section, "_action/GetWikiSectionTextAction?reference=", true);
	}

	@Nullable
	private URL getUrl(Section<? extends AttachmentUpdateMarkup> sec, String command, boolean params) {
		Section<InterWikiImportMarkup> section = $(sec).closest(InterWikiImportMarkup.class).getFirst();
		if (section == null) return null;
		String wikiAnnotation = getWiki(section);
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

		String linkLabel = Environment.getInstance().getWikiConnector().getApplicationName();
		if (Strings.isBlank(linkLabel) || "knowwe".equalsIgnoreCase(linkLabel)) {
			linkLabel = "another wiki";
		}
		linkLabel += ": " + section.getTitle();

		String fromParam = "&" + ImportMarker.REQUEST_FROM + "=" + Strings.encodeURL(linkLabel);

		String link = KnowWEUtils.getAsAbsoluteLink(KnowWEUtils.getURLLink(section));
		String linkParam = "&" + ImportMarker.REQUEST_LINK + "=" + Strings.encodeURL(link);

		String url = wikiAnnotation + command + reference + (params ? fromParam + linkParam : "");

		try {
			return new URL(url);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	@Nullable
	private String getWiki(Section<InterWikiImportMarkup> section) {
		return Strings.trim(DefaultMarkupType.getAnnotation(section, WIKI_ANNOTATION));
	}

	@Override
	protected long getIntervalMillis(Section<? extends AttachmentUpdateMarkup> section) {
		long interval = super.getIntervalMillis(section);
		if (interval == Long.MAX_VALUE) {
			interval = 1000 * 60 * 10; // 10 min default interval
		}
		return interval;
	}

	private class InterWikiImportRenderer extends DefaultMarkupRenderer implements AsyncPreviewRenderer {

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
			render(section, user, result, true);
		}

		@Override
		public void renderAsyncPreview(Section<?> section, UserContext user, RenderResult result) {
			render(section, user, result, false);
		}

		private void render(Section<?> section, UserContext user, RenderResult result, boolean waitForUpdate) {
			waitForUpdate(section, user, waitForUpdate);
			Collection<String> errors = getMessageStrings(section, Message.Type.ERROR, user);
			Collection<String> warnings = getMessageStrings(section, Message.Type.ERROR, user);
			setFramed(!errors.isEmpty() || !warnings.isEmpty());
			super.render(section, user, result);
		}

		private void waitForUpdate(Section<?> section, UserContext user, boolean waitForUpdate) {
			if (waitForUpdate) {
				Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
				if (markup != null) {
					String path = markup.get().getWikiAttachmentPath(markup);
					ArticleManager articleManager = user.getArticleManager();
					try {
						Stopwatch stopwatch = new Stopwatch();
						while (articleManager.getArticle(path) == null && stopwatch.getTime() < 20000) {
							// busy wait... not great, but ok in this case, since it is just a renderer
							// the attachment compilation is triggered asynchronous, so it would
							// be some hassle to do it event based
							Thread.sleep(50);
						}
					}
					catch (InterruptedException ignore) {
					}
				}
			}
		}

		@Override
		public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult result) {
			Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
			if (markup == null) return;

			renderHeader(markup, user, result);
			renderLastChangesMessage(markup, result);
			renderImport(markup, user, result);

			if (isFramed()) {
				renderAnnotations(markup, $(markup).successor(AnnotationType.class).asList(), user, result);
			}
		}

		@Override
		public boolean shouldRenderAsynchronous(Section<?> section, UserContext user) {
			Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
			if (markup == null) return true;
			try {
				if (markup.get().getWikiAttachment(markup) == null) {
					return true;
				}
			}
			catch (IOException e) {
				return true;
			}
			Article article = user.getArticleManager().getArticle(markup.get().getWikiAttachmentPath(markup));
			if (article == null) return true;
			return getLock(markup).isLocked();
		}

		private void renderImport(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			String path = markup.get().getWikiAttachmentPath(markup);
			Article article = user.getArticleManager().getArticle(path);
			if (article == null) {
				result.appendHtmlElement("span", "Included article not (yet) available", "class", "warning");
			}
			else {
				if (isFramed()) {
					// used the framing renderer
					new FramedIncludedSectionRenderer(true).render(
							article.getRootSection(), user, result);
				}
				else {
					// or simply render the sections belonging to the header
					FramedIncludedSectionRenderer.renderTargetSections(
							article.getRootSection(), true, user, result);
				}
			}
		}

		private void renderLastChangesMessage(Section<InterWikiImportMarkup> markup, RenderResult result) {
			if (getLock(markup).isLocked()) {
				result.appendHtmlElement("p", "Update currently ongoing...", "style", "color:green");
				return;
			}

			long lastRun = markup.get().timeSinceLastRun(markup);
			String message;
			if (lastRun < Long.MAX_VALUE) {
				String lastRunDisplay = getDisplay(lastRun);
				message = "Last check for changes was " + lastRunDisplay + " ago";
				long lastChange = markup.get().timeSinceLastChange(markup);
				if (lastChange < Long.MAX_VALUE) {
					String lastChangeDisplay = getDisplay(lastChange);
					message += ", last change was " + lastChangeDisplay + " ago";
				}
			}
			else {
				message = "No check yet, click here to check now: ";
			}
			result.appendHtmlTag("p");
			result.appendHtmlElement("span", message, "class", "include-message");
			result.appendHtmlTag("a", "onclick", "KNOWWE.core.plugin.attachment.update('" + markup.getID() + "')",
					"class", "include-refresh tooltipster", "title", "Check for changes");
			result.appendHtml(Icon.REFRESH.toHtml());
			result.appendHtmlTag("/a");
			result.appendHtmlTag("/p");
		}

		private void renderHeader(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			URL url = markup.get().getUrl(markup, "Wiki.jsp?page=", false);
			String wiki = markup.get().getWiki(markup);
			if (url != null && wiki != null) {
				String wikiName = wiki.replaceAll("^https?://", "").replaceAll("/$", "");
				String pageName = markup.get().getPageName(markup);
				String sectionName = markup.get().getSectionName(markup);
				String linkLabel = wikiName + ": " + pageName;
				if (sectionName != null) {
					linkLabel += " - " + sectionName;
				}

				result.appendHtmlTag("h2");
				result.append("Import from wiki ");
				String shortenedUrl = url.toString().replaceAll("%23.+$", "");
				result.appendHtmlElement("a", linkLabel, "href", shortenedUrl);

				HelpToolProvider helpToolProvider = new HelpToolProvider();
				if (helpToolProvider.hasTools(markup, user)) {
					Tool[] tools = helpToolProvider.getTools(markup, user);
					for (Tool tool : tools) {
						result.appendHtmlTag("a", "title", tool.getDescription(), "class", "tooltipster help-tool", tool.getActionType() == Tool.ActionType.ONCLICK ? "onclick" : "href", tool.getAction());
						result.appendHtml(tool.getIcon().toHtml());
						result.appendHtmlTag("/a");
					}
				}

				String action = "KNOWWE.core.plugin.setMarkupSectionActivationStatus('" + markup.getID() + "', 'off')";
				result.appendHtmlTag("a", "onclick", action, "class", "include-deactivate tooltipster",
						"title", "Deactivate import");
				result.appendHtml(Icon.TOGGLE_OFF.toHtml());
				result.appendHtmlTag("/a");
				result.appendHtmlTag("/h2");
			}
		}
	}
}
