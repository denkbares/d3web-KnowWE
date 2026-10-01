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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.jetbrains.annotations.Nullable;

import com.denkbares.knowwe.textdiff.DiffHtmlRenderer;
import com.denkbares.knowwe.textdiff.TextDiff;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import com.denkbares.utils.Streams;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.elements.A;
import de.knowwe.core.kdom.rendering.elements.Div;
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlNode;
import de.knowwe.core.kdom.rendering.elements.HtmlProvider;
import de.knowwe.core.kdom.rendering.elements.P;
import de.knowwe.core.kdom.rendering.elements.PlainTextNode;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.kdom.rendering.elements.TextNode;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.tools.HelpToolProvider;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsyncPreviewRenderer;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.tools.Tool;
import de.knowwe.util.Color;
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
	private static final String MODE_ANNOTATION = "mode";
	private static final String VALIDATION_MODE_ANNOTATION = "validationMode";
	private static final String LATEST_CHANGE_ANNOTATION = "latestChange";
	private static final String TRACKING_ACCEPTED_AT_ANNOTATION = "trackingAcceptedAt";

	/** Maximum number of entries of the "Compare with source" selection, including the complete comparison. */
	private static final int MAX_DIFF_OPTIONS = 10;

	private static final InterWikiImportUpdateService UPDATE_SERVICE = new InterWikiImportUpdateService();
	private static final DefaultMarkup MARKUP = new DefaultMarkup("InterWikiImport");

	/**
	 * Last sync-failure message per wiki attachment path, so the warning survives article
	 * recompiles (which discard section messages and rerun the compile scripts). Keyed by the
	 * stable attachment path, analogous to {@link AttachmentUpdateMarkup}'s last-run tracking.
	 */
	private static final Map<String, String> LAST_SYNC_ERRORS = new ConcurrentHashMap<>();

	static {
		MARKUP.addAnnotation(INTERVAL_ANNOTATION, false);
		MARKUP.addAnnotationContentType(INTERVAL_ANNOTATION, new TimeStampType());
		MARKUP.addAnnotation(COMPILE_ANNOTATION, false, "true", "false");
		MARKUP.addAnnotation(MODE_ANNOTATION, false, "import", "tracking");
		MARKUP.addAnnotation(REPLACEMENT, false, Pattern.compile(".+->(.|[\r\n])*"));
		MARKUP.addAnnotation(REGEX_REPLACEMENT, false, Pattern.compile(".+->(.|[\r\n])*"));
		MARKUP.addAnnotation(WIKI_ANNOTATION, true);
		MARKUP.addAnnotation(PAGE_ANNOTATION, true);
		MARKUP.addAnnotation(SECTION_ANNOTATION, false);
		MARKUP.addAnnotation(VALIDATION_MODE_ANNOTATION, false, TermCompiler.ReferenceValidationMode.class);
		MARKUP.addAnnotation(LATEST_CHANGE_ANNOTATION, false);
		MARKUP.addAnnotation(TRACKING_ACCEPTED_AT_ANNOTATION, false);
		PackageManager.addPackageAnnotation(MARKUP);
		UPDATE_SERVICE.initialize();
	}

	public InterWikiImportMarkup() {
		super(MARKUP);
		addCompileScript(new RegistrationScript());
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

	@Override
	public TermCompiler.ReferenceValidationMode getReferenceValidationMode(Section<? extends AttachmentCompileType> section) {
		return $(section).closest(DefaultMarkupType.class)
				.map(s -> DefaultMarkupType.getAnnotation(s, VALIDATION_MODE_ANNOTATION))
				.filter(Objects::nonNull)
				.findFirst().map(TermCompiler.ReferenceValidationMode::valueOf)
				.orElse(TermCompiler.ReferenceValidationMode.error);
	}

	String getPageName(Section<InterWikiImportMarkup> section) {
		return DefaultMarkupType.getAnnotation(section, PAGE_ANNOTATION);
	}

	String getSectionName(Section<InterWikiImportMarkup> section) {
		return DefaultMarkupType.getAnnotation(section, SECTION_ANNOTATION);
	}

	public Mode getMode(Section<InterWikiImportMarkup> section) {
		String mode = Strings.trim(DefaultMarkupType.getAnnotation(section, MODE_ANNOTATION));
		if ("tracking".equalsIgnoreCase(mode)) {
			return Mode.TRACKING;
		}
		return Mode.IMPORT;
	}

	public boolean isTrackingMode(Section<?> markupOrSuccessor) {
		Section<InterWikiImportMarkup> markup = $(markupOrSuccessor).closest(InterWikiImportMarkup.class).getFirst();
		return markup != null && getMode(markup) == Mode.TRACKING;
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
		if (isTrackingMode(section)) return null;
		return $(section).closest(AttachmentUpdateMarkup.class).mapFirst(this::getWikiAttachmentPath);
	}

	@Override
	public boolean isCompilingTheAttachment(Section<? extends AttachmentCompileType> section) {
		if (isTrackingMode(section)) return false;
		return !"false".equals(DefaultMarkupType.getAnnotation(section, COMPILE_ANNOTATION));
	}

	@Override
	public @Nullable URL getUrl(Section<? extends AttachmentUpdateMarkup> section) {
		return getUrl(section, getActionFragment() + "/GetWikiSectionTextAction?reference=", true);
	}

	@Nullable
	private URL getUrl(Section<? extends AttachmentUpdateMarkup> sec, String command, boolean params) {
		Section<InterWikiImportMarkup> section = $(sec).closest(InterWikiImportMarkup.class).getFirst();
		if (section == null) return null;
		String wikiAnnotation = normalizeWiki(getWiki(section));

		String pageName = section.get().getPageName(section);
		String reference = Strings.encodeURL(pageName);
		String sectionName = section.get().getSectionName(section);
		if (sectionName != null) {
			reference += Strings.encodeURL("#" + sectionName);
		}

		String fromParam = "&" + ImportMarker.REQUEST_FROM + "=" + Strings.encodeURL(getImportSourceLabel(section));
		String linkParam = "&" + ImportMarker.REQUEST_LINK + "=" + Strings.encodeURL(getImportSourceLink(section));

		String url = wikiAnnotation + command + reference + (params ? fromParam + linkParam : "");

		try {
			return new URL(url);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Label identifying this importing wiki and article, shown on the source page's "imported by" marker.
	 * Sent both as the {@code requestFrom} URL parameter (single fetch) and in the bulk poll request body.
	 */
	String getImportSourceLabel(Section<InterWikiImportMarkup> section) {
		String linkLabel = Environment.getInstance().getWikiConnector().getApplicationName();
		if (Strings.isBlank(linkLabel) || "knowwe".equalsIgnoreCase(linkLabel)) {
			linkLabel = "another wiki";
		}
		return linkLabel + ": " + section.getTitle();
	}

	/** Absolute link back to this importing section, shown on the source page's "imported by" marker. */
	String getImportSourceLink(Section<InterWikiImportMarkup> section) {
		return KnowWEUtils.getAsAbsoluteLink(KnowWEUtils.getURLLink(section));
	}

	@Nullable
	String getWiki(Section<InterWikiImportMarkup> section) {
		return Strings.trim(DefaultMarkupType.getAnnotation(section, WIKI_ANNOTATION));
	}

	static String normalizeWiki(String wiki) {
		if (wiki == null) return "";
		String normalized = Strings.trim(wiki);
		if (normalized.isBlank()) return "";
		if (!normalized.matches("^https?://.*")) {
			normalized = "https://" + normalized;
		}
		if (!normalized.endsWith("/")) {
			normalized += "/";
		}
		return normalized;
	}

	@Nullable
	Instant getLatestChange(Section<InterWikiImportMarkup> section) {
		String latestChangeText = DefaultMarkupType.getAnnotation(section, LATEST_CHANGE_ANNOTATION);
		return InterWikiChanges.parseInstant(latestChangeText);
	}

	void collectLatestChangeReplacement(Section<InterWikiImportMarkup> section, Instant latestChange, Map<String, String> replacements) {
		Section<?> latestChangeContent = DefaultMarkupType.getAnnotationContentSection(section, LATEST_CHANGE_ANNOTATION);
		if (latestChangeContent != null) {
			replacements.put(latestChangeContent.getID(), latestChange.toString());
		}
		else {
			Section<?> closingTag = $(section).children().getLast();
			if (closingTag == null || !"%".equals(Strings.trim(closingTag.getText()))) return;
			replacements.put(closingTag.getID(),
					"\n@" + LATEST_CHANGE_ANNOTATION + ": " + latestChange + "\n" + Strings.trimLeft(closingTag.getText()));
		}
	}

	boolean collectTrackingAcceptedAtReplacement(Section<InterWikiImportMarkup> section, Instant acceptedAt, Map<String, String> replacements) {
		SectionReplacement replacement = getTrackingAcceptedAtReplacement(section, acceptedAt);
		if (replacement == null) return false;
		replacements.put(replacement.section().getID(), replacement.text());
		return true;
	}

	/**
	 * Returns the replacement that sets {@code @trackingAcceptedAt}: either the content of the existing
	 * annotation, or the closing tag of the markup with the annotation inserted in front of it.
	 */
	@Nullable
	private SectionReplacement getTrackingAcceptedAtReplacement(Section<InterWikiImportMarkup> section, Instant acceptedAt) {
		Section<?> acceptedAtContent = DefaultMarkupType.getAnnotationContentSection(section, TRACKING_ACCEPTED_AT_ANNOTATION);
		if (acceptedAtContent != null) {
			return new SectionReplacement(acceptedAtContent, acceptedAt.toString());
		}

		Section<?> closingTag = $(section).children().getLast();
		if (closingTag == null || !"%".equals(Strings.trim(closingTag.getText()))) return null;
		return new SectionReplacement(closingTag,
				"\n@" + TRACKING_ACCEPTED_AT_ANNOTATION + ": " + acceptedAt
						+ "\n" + Strings.trimLeft(closingTag.getText()));
	}

	private record SectionReplacement(Section<?> section, String text) {
	}

	boolean shouldUpdateLatestChange(Section<InterWikiImportMarkup> section, boolean attachmentChanged) {
		return getSectionName(section) == null || attachmentChanged;
	}

	boolean updateAttachmentWithSourceText(Section<InterWikiImportMarkup> section, String sourceText) {
		if (section.getArticleManager() == null) return false;

		logLastRun(section);
		Messages.clearMessages(section, getClass());

		String path = getWikiAttachmentPath(section);
		if (path == null) return false;
		if (path.split("/").length > 2) {
			Messages.storeMessage(section, getClass(), Messages.error("Unable to update entries in zipped attachments!"));
			return false;
		}

		ReentrantLock lock = getLock(section);
		if (!lock.tryLock()) {
			return false;
		}

		try {
			byte[] sourceBytes = Streams.getBytesAndClose(applyReplacements(section,
					new ByteArrayInputStream(sourceText.getBytes(StandardCharsets.UTF_8))));
			WikiAttachment attachment = getWikiAttachment(section);
			if (attachment != null) {
				byte[] attachmentBytes = Streams.getBytesAndClose(attachment.getInputStream());
				if (java.util.Arrays.equals(sourceBytes, attachmentBytes)) {
					return false;
				}
				// tracking compares trimmed texts, so e.g. a different number of trailing line breaks (the
				// section is no longer the last one of the source page) must not create a new version
				if (isTrackingMode(section) && InterWikiTrackingService.normalizeForComparison(new String(sourceBytes, StandardCharsets.UTF_8))
						.equals(InterWikiTrackingService.normalizeForComparison(new String(attachmentBytes, StandardCharsets.UTF_8)))) {
					return false;
				}
			}

			String parentName = path.substring(0, path.indexOf(PATH_SEPARATOR));
			String fileName = path.substring(path.indexOf(PATH_SEPARATOR) + 1);
			Environment.getInstance().getWikiConnector()
					.storeAttachment(parentName, fileName, "SYSTEM", new ByteArrayInputStream(sourceBytes), isVersioning(section));
			return true;
		}
		catch (IOException e) {
			String message = e.getClass().getSimpleName() + " while trying to update attachment " + path;
			Messages.storeMessage(section, getClass(), Messages.error(message + ": " + e.getMessage()));
			throw new RuntimeException(message, e);
		}
		finally {
			lock.unlock();
		}
	}

	@Nullable
	String getTrackingReferenceText(Section<InterWikiImportMarkup> section) throws IOException {
		WikiAttachment attachment = getWikiAttachment(section);
		if (attachment == null) return null;
		return Streams.getTextAndClose(attachment.getInputStream());
	}

	@Nullable
	Instant getTrackingReferenceLastModified(Section<InterWikiImportMarkup> section) throws IOException {
		WikiAttachment attachment = getWikiAttachment(section);
		if (attachment == null || attachment.getDate() == null) return null;
		return attachment.getDate().toInstant();
	}

	/**
	 * Returns the reference text as it was when the tracking differences were acknowledged, i.e. the
	 * newest attachment version stored not after {@code acceptedAt}. Returns {@code null} if that
	 * version is not available (anymore), e.g. because the attachment was stored without versioning
	 * at that time.
	 */
	@Nullable
	String getTrackingAcceptedReferenceText(Section<InterWikiImportMarkup> section, Instant acceptedAt) throws IOException {
		return InterWikiTrackingService.findReferenceTextAt(getWikiAttachment(section), acceptedAt);
	}

	@Nullable
	Instant getTrackingAcceptedAt(Section<InterWikiImportMarkup> section) {
		String acceptedAt = DefaultMarkupType.getAnnotation(section, TRACKING_ACCEPTED_AT_ANNOTATION);
		return InterWikiChanges.parseInstant(acceptedAt);
	}

	@Nullable
	String getTrackingLocalComparisonText(Section<InterWikiImportMarkup> section) {
		int[] range = getLocalComparisonRange(section);
		if (range == null) return null;
		return section.getArticle().getText().substring(range[0], range[1]);
	}

	private int[] getLocalComparisonRange(Section<InterWikiImportMarkup> section) {
		Article article = section.getArticle();
		if (article == null) return null;
		String articleText = article.getText();
		if (articleText == null) return null;

		int sectionStart = section.getOffsetInArticle();
		int sectionEnd = sectionStart + section.getTextLength();

		OptionalInt nextInterWikiImportStart = $(article).successor(InterWikiImportMarkup.class)
				.stream()
				.mapToInt(Section::getOffsetInArticle)
				.filter(offset -> offset > sectionStart)
				.min();

		int compareStart = Math.max(0, Math.min(sectionEnd, articleText.length()));
		int compareEnd = Math.max(compareStart, Math.min(nextInterWikiImportStart.orElse(articleText.length()), articleText.length()));
		return new int[] { compareStart, compareEnd };
	}

	/**
	 * Replaces the local content below the markup with the given text and, if {@code acceptedAt} is
	 * given, sets {@code @trackingAcceptedAt} (if possible). Both changes are done in one replacement of
	 * the article text, as they cannot be combined with section replacements.
	 */
	boolean collectLocalContentReplacement(Section<InterWikiImportMarkup> section, String localText, @Nullable Instant acceptedAt, Map<String, String> replacements) {
		int[] range = getLocalComparisonRange(section);
		if (range == null) return false;

		Article article = section.getArticle();
		String articleText = article.getText();
		String textUpToLocalContent = articleText.substring(0, range[0]);
		// without a closing tag the annotation cannot be inserted, then replace without acknowledging
		SectionReplacement acceptedAtReplacement = acceptedAt == null ? null : getTrackingAcceptedAtReplacement(section, acceptedAt);
		if (acceptedAtReplacement != null) {
			int acceptedAtStart = acceptedAtReplacement.section().getOffsetInArticle();
			int acceptedAtEnd = acceptedAtStart + acceptedAtReplacement.section().getTextLength();
			textUpToLocalContent = articleText.substring(0, acceptedAtStart)
					+ acceptedAtReplacement.text()
					+ articleText.substring(acceptedAtEnd, range[0]);
		}
		// keep the line breaks around the local content, e.g. the empty line before a following markup
		String oldLocalText = articleText.substring(range[0], range[1]);
		String leading = "\n\n";
		String trailing = "\n";
		if (!Strings.isBlank(oldLocalText)) {
			leading = oldLocalText.substring(0, oldLocalText.length() - Strings.trimLeft(oldLocalText).length());
			trailing = oldLocalText.substring(Strings.trimRight(oldLocalText).length());
		}
		String newArticleText = textUpToLocalContent
				+ leading + Strings.trim(localText) + trailing
				+ articleText.substring(range[1]);
		replacements.put(article.getRootSection().getID(), newArticleText);
		return true;
	}

	void refreshNow(Section<InterWikiImportMarkup> section, boolean force) {
		UPDATE_SERVICE.pollSingleMarkup(section, force);
	}

	static String buildRefreshScript(String sectionId, boolean force) {
		return "(function(){"
				+ "jq$.ajax({"
				+ "url: KNOWWE.core.util.getURL({action:'RefreshInterWikiImportAction',"
				+ Attributes.SECTION_ID + ":'" + sectionId + "',"
				+ "force:'" + force + "'}),"
				+ "type:'post',"
				+ "cache:false"
				+ "}).done(function(){window.location.reload();})"
				+ ".fail(function(xhr){"
				+ "KNOWWE.notification.error(null,"
				+ "KNOWWE.plugin.include.errorMessage(xhr, 'Unable to refresh InterWikiImport.'),"
				+ "'iwii-refresh',5000);"
				+ "});"
				+ "})();";
	}

	void refreshTrackingMessages(Section<InterWikiImportMarkup> section) {
		Messages.clearMessages(section, TrackingMessages.class);
		if (getMode(section) != Mode.TRACKING) return;
		try {
			InterWikiTrackingService.TrackingStatus status = InterWikiTrackingService.getTrackingStatus(section);
			if (status.warningActive()) {
				Messages.storeMessage(section, TrackingMessages.class,
						Messages.warning(status.sourceChanges() == null
								? "InterWikiImport tracking differences are not acknowledged yet."
								: "InterWikiImport source changed since the last acknowledgement."));
			}
		}
		catch (IOException e) {
			Messages.storeMessage(section, TrackingMessages.class,
					Messages.warning("Unable to evaluate InterWikiImport tracking status: " + e.getMessage()));
		}
	}

	/** Marker source so tracking messages can be cleared independently from other markup messages. */
	private static final class TrackingMessages {
	}

	/** Marker source so sync-failure warnings can be cleared independently from other markup messages. */
	private static final class SyncMessages {
	}

	/**
	 * Records the outcome of a remote-change poll on this markup, called by
	 * {@link InterWikiImportUpdateService} after every attempt (whether or not anything changed).
	 * Always refreshes the "last check" timestamp; on failure ({@code errorMessage != null}) it
	 * stores a warning so the failing sync is visible in the markup instead of silently showing
	 * "No check yet", on success it clears any previous warning. The failure is also remembered
	 * per attachment path so {@link RegistrationScript} can re-apply it after a recompile.
	 */
	void recordSyncOutcome(Section<InterWikiImportMarkup> section, @Nullable String errorMessage) {
		logLastRun(section);
		String path = getWikiAttachmentPath(section);
		if (errorMessage == null) {
			if (path != null) LAST_SYNC_ERRORS.remove(path);
			Messages.clearMessages(section, SyncMessages.class);
		}
		else {
			if (path != null) LAST_SYNC_ERRORS.put(path, errorMessage);
			Messages.storeMessage(section, SyncMessages.class, Messages.warning(errorMessage));
		}
	}

	/** Re-applies a remembered sync-failure warning after a recompile cleared the section messages. */
	private void restoreSyncMessage(Section<InterWikiImportMarkup> section) {
		String path = getWikiAttachmentPath(section);
		String error = path == null ? null : LAST_SYNC_ERRORS.get(path);
		if (error != null) {
			Messages.storeMessage(section, SyncMessages.class, Messages.warning(error));
		}
	}

	void collectTrackingInitializationReplacement(Section<InterWikiImportMarkup> section, Map<String, String> replacements) throws IOException {
		if (getMode(section) != Mode.TRACKING) return;

		String referenceText = getTrackingReferenceText(section);
		if (Strings.isBlank(referenceText)) return;

		String localComparisonText = getTrackingLocalComparisonText(section);
		// Initialize only when the local area is still empty to protect user-maintained local content.
		if (localComparisonText == null || Strings.isNotBlank(localComparisonText)) return;

		Section<?> closingTag = $(section).children().getLast();
		if (closingTag == null || !"%".equals(Strings.trim(closingTag.getText()))) return;

		// Merge with a possibly existing replacement for this closing tag (e.g. @latestChange update).
		String existingReplacement = replacements.getOrDefault(closingTag.getID(), closingTag.getText());
		String initializedText = Strings.trimRight(existingReplacement)
				+ "\n\n"
				+ Strings.trimRight(referenceText)
				+ "\n";
		replacements.put(closingTag.getID(), initializedText);
	}

	/**
	 * Polls all given markups together, so their updates change each page only once (see
	 * {@link InterWikiImportUpdateService#pollMarkups(Collection, boolean)}).
	 */
	@Override
	public void performUpdates(Collection<? extends Section<? extends AttachmentUpdateMarkup>> sections, boolean force, boolean allowWaitForOtherDownloads) {
		List<Section<InterWikiImportMarkup>> markups = sections.stream()
				.map(section -> $(section).closest(InterWikiImportMarkup.class).getFirst())
				.filter(Objects::nonNull)
				.toList();
		UPDATE_SERVICE.pollMarkups(markups, force);
	}

	/**
	 * Routes generic update requests (e.g. from {@link de.knowwe.core.action.RecompileAction} or the
	 * global "Update imports" admin tool) through the central {@code @latestChange}-aware poller
	 * instead of the header/timestamp-based pipeline of {@link AttachmentUpdateMarkup}. The generic
	 * pipeline stores a new attachment version even for unchanged content once the page is newer than
	 * the attachment ({@code IMPORT_SECTION_CHANGED}) — that timestamp bump would re-activate already
	 * acknowledged tracking diffs ({@code @trackingAcceptedAt}) on every forced recompile.
	 */
	@Override
	public void performUpdate(Section<? extends AttachmentUpdateMarkup> section, boolean force, boolean allowWaitForOtherDownloads) {
		Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (markup == null) return;
		UPDATE_SERVICE.pollSingleMarkup(markup, force);
	}

	/**
	 * Tracking mode keeps the history of the reference attachment, so the reference text at the time
	 * of the last acknowledgement can be restored and only the source changes since then have to be
	 * reviewed.
	 */
	@Override
	protected boolean isVersioning(Section<? extends AttachmentUpdateMarkup> section) {
		return isTrackingMode(section);
	}

	@Override
	protected long getIntervalMillis(Section<? extends AttachmentUpdateMarkup> section) {
		return Long.MAX_VALUE;
	}

	@Override
	protected boolean usesOwnScheduling(Section<? extends AttachmentUpdateMarkup> section) {
		return false;
	}

	/**
	 * Renders the diff as HTML in the light or dark theme of the user.
	 */
	static String renderTrackingDiff(TextDiff diff, UserContext user) {
		String theme = getDiffTheme(user);
		return DiffHtmlRenderer.renderTextDiff(diff)
				.replaceFirst("<knowwe-text-diff ", "<knowwe-text-diff data-theme=\"" + theme + "\" ");
	}

	/**
	 * Renders the diff of the given entry: what applying it changes in the local content, on a conflict
	 * with the source winning. The element tells by {@code data-applicable} whether applying changes
	 * anything and by {@code data-conflict} whether local changes are overridden.
	 */
	static HtmlElement renderDiffOption(InterWikiTrackingService.DiffOption option, String localText, UserContext user) {
		String local = InterWikiTrackingService.normalizeForComparison(localText);
		HtmlElement element = new Div().attributes(
				"data-applicable", String.valueOf(isApplicable(option, localText)),
				"data-conflict", String.valueOf(option.conflict()));
		if (local.equals(option.resultText())) {
			element.children(new P().clazz("note").plainText("No changes to apply, the local content already contains them."));
		}
		else {
			element.children(new HtmlNode(renderTrackingDiff(new TextDiff(local, option.resultText()), user)));
		}
		return element;
	}

	/**
	 * Whether applying the entry changes the local content (on a conflict the source winning).
	 */
	static boolean isApplicable(InterWikiTrackingService.DiffOption option, String localText) {
		return !InterWikiTrackingService.normalizeForComparison(localText).equals(option.resultText());
	}

	// Mirrors DefaultLogoAction#getLogoPath: derive light/dark from the user's "DisplayMode"
	// preference, defaulting to light when no preference is available.
	private static String getDiffTheme(UserContext user) {
		HttpSession session = user.getSession();
		if (session == null) return "light";
		Object prefs = session.getAttribute("prefs");
		if (prefs instanceof Map<?, ?> map) {
			Object mode = map.get("DisplayMode");
			if (mode != null && "dark-mode".equals(mode.toString())) return "dark";
		}
		return "light";
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
			Collection<String> warnings = getMessageStrings(section, Message.Type.WARNING, user);
			setFramed(!errors.isEmpty() || !warnings.isEmpty());
			super.render(section, user, result);
		}

		private void waitForUpdate(Section<?> section, UserContext user, boolean waitForUpdate) {
			if (waitForUpdate) {
				Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
				if (markup != null) {
					if (markup.get().isTrackingMode(markup)) return;
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
			if (markup.get().isTrackingMode(markup)) {
				renderTracking(markup, user, result);
			}
			else {
				renderImport(markup, user, result);
			}

			// always available, but collapsed (see renderAnnotations)
			renderAnnotations(markup, $(markup).successor(AnnotationType.class).asList(), user, result);
		}

		/**
		 * Renders the annotations as label/value list, with formatted dates, links and replacements.
		 * Values that cannot be formatted (or carry errors) are rendered as usual.
		 */
		@Override
		protected void renderAnnotations(List<Section<AnnotationType>> annotations, UserContext user, RenderResult result, String parentTag, String elementTag) {
			HtmlElement list = new Div().clazz("markupAnnotations iwi-annotations");
			for (Section<AnnotationType> annotation : annotations) {
				String name = annotation.get().getName();
				list.children(new Div()
						.clazz("markupAnnotation iwi-annotation")
						.attributes("data-name", name)
						.children(
								new Span().clazz("iwi-annotation-label").plainText(InterWikiImportAnnotationFormat.getLabel(name)),
								new Span().clazz("iwi-annotation-value").children(getAnnotationValue(annotation, name, user))));
			}
			// the configuration is rarely of interest, so it is collapsed
			result.append(new HtmlElement("details").clazz("iwi-configuration").children(
					new HtmlElement("summary").plainText("Configuration"),
					list));
		}

		private HtmlProvider[] getAnnotationValue(Section<AnnotationType> annotation, String name, UserContext user) {
			Section<AnnotationContentType> content = $(annotation).successor(AnnotationContentType.class).getFirst();
			Section<InterWikiImportMarkup> markup = $(annotation).closest(InterWikiImportMarkup.class).getFirst();
			HtmlProvider plainValue = result -> {
				if (content != null) result.append(content, user);
			};
			if (content == null || markup == null
					|| !Messages.getMessages(content, Message.Type.ERROR, Message.Type.WARNING).isEmpty()) {
				return new HtmlProvider[] { plainValue };
			}
			String text = Strings.trim(content.getText());
			switch (name) {
				case WIKI_ANNOTATION -> {
					return new HtmlProvider[] {
							new A().attributes("href", normalizeWiki(text)).plainText(InterWikiImportAnnotationFormat.formatWiki(text)) };
				}
				case PAGE_ANNOTATION -> {
					URL url = markup.get().getUrl(markup, "Wiki.jsp?page=", false);
					if (url == null) return new HtmlProvider[] { new PlainTextNode(text) };
					return new HtmlProvider[] {
							new A().attributes("href", url.toString().replaceAll("%23.+$", "")).plainText(text) };
				}
				case MODE_ANNOTATION -> {
					return new HtmlProvider[] { new PlainTextNode(InterWikiImportAnnotationFormat.formatMode(text)) };
				}
				case COMPILE_ANNOTATION -> {
					return new HtmlProvider[] { new PlainTextNode("false".equalsIgnoreCase(text) ? "No" : "Yes") };
				}
				case LATEST_CHANGE_ANNOTATION, TRACKING_ACCEPTED_AT_ANNOTATION -> {
					Instant instant = InterWikiChanges.parseInstant(text);
					if (instant == null) return new HtmlProvider[] { plainValue };
					HtmlElement date = new Span().title(instant.toString())
							.plainText(InterWikiImportAnnotationFormat.formatDateTime(instant, getLocale(user), ZoneId.systemDefault()));
					return new HtmlProvider[] { date, new Span().clazz("iwi-annotation-hint")
							.plainText(InterWikiImportAnnotationFormat.formatRelative(instant, Instant.now())) };
				}
				case REPLACEMENT, REGEX_REPLACEMENT -> {
					String[] parts = InterWikiImportAnnotationFormat.splitReplacement(text);
					if (parts == null) return new HtmlProvider[] { plainValue };
					return new HtmlProvider[] {
							new HtmlElement("code").plainText(parts[0]),
							new Span().clazz("iwi-annotation-arrow").plainText("\u2192"),
							new HtmlElement("code").plainText(parts[1]) };
				}
				default -> {
					return new HtmlProvider[] { plainValue };
				}
			}
		}

		private static Locale getLocale(UserContext user) {
			try {
				return user.getLocale();
			}
			catch (RuntimeException e) {
				// no request available, e.g. for asynchronous rendering
				return Locale.ENGLISH;
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

		private void renderTracking(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			InterWikiTrackingService.TrackingStatus trackingStatus;
			try {
				trackingStatus = InterWikiTrackingService.getTrackingStatus(markup);
			}
			catch (IOException e) {
				result.append(new HtmlElement("p").clazz("warning").content("Unable to read the source content: " + e.getMessage()));
				return;
			}

			if (trackingStatus.state() == InterWikiTrackingService.State.MISSING_REFERENCE) {
				result.append(new HtmlElement("p").clazz("note").content("Source content not (yet) available"));
				return;
			}

			if (trackingStatus.canInitializeFromReference() && KnowWEUtils.canWrite(markup, user)) {
				renderInitializationButton(markup, result);
				return;
			}

			if (trackingStatus.localComparisonAvailable()) {
				renderTrackingComparisonStatus(markup, trackingStatus, user, result);
			}
			else {
				result.append(new HtmlElement("p").clazz("warning")
						.content("Tracking mode: local comparison range is currently not available."));
			}
		}

		private void renderInitializationButton(Section<InterWikiImportMarkup> markup, RenderResult result) {
			result.append(new HtmlElement("p").clazz("note")
					.content("Tracking mode: local copy is empty. Insert the current text of the source below the markup as a starting point."));
			result.append(new HtmlElement("button")
					.attributes("type", "button",
							"class", "tracking-action-button",
							"onclick", buildTrackingActionScript(
									"InitInterWikiTrackingLocalCopyAction", markup.getID(),
									"tracking-init", "Unable to initialize local copy.", null))
					.content("Insert source text below"));
		}

		/**
		 * Renders the differences of a tracking markup: if not acknowledged yet, the changes to review
		 * (what applying them changes in the local content) with the buttons to apply and to acknowledge
		 * them, and in any case the collapsed "Compare with source" to look at the complete comparison or
		 * the changes since a previous version.
		 */
		private void renderTrackingDifferences(Section<InterWikiImportMarkup> markup, InterWikiTrackingService.TrackingStatus trackingStatus,
				UserContext user, RenderResult result) {
			String localText = markup.get().getTrackingLocalComparisonText(markup);
			String referenceText;
			List<InterWikiTrackingService.DiffOption> options;
			try {
				WikiAttachment attachment = markup.get().getWikiAttachment(markup);
				referenceText = markup.get().getTrackingReferenceText(markup);
				if (attachment == null || referenceText == null || localText == null) return;
				Instant acceptedAt = trackingStatus.trackingAcceptedAt();
				int acceptedVersion = acceptedAt == null ? -1 : InterWikiTrackingService.findVersionAt(attachment, acceptedAt);
				options = InterWikiTrackingService.getDiffOptions(attachment, referenceText, localText, acceptedVersion, MAX_DIFF_OPTIONS);
			}
			catch (IOException e) {
				result.append(new P().clazz("warning").plainText("Unable to read the versions of the source: " + e.getMessage()));
				return;
			}
			Locale locale = getLocale(user);

			if (trackingStatus.state() == InterWikiTrackingService.State.ACCEPTED_DIFF) {
				result.append(new P().clazz("note").children(
						new PlainTextNode("Tracking mode: local content differs from the source, but changes were acknowledged"),
						new HtmlNode(Icon.CHECKED.addColor(Color.OK).addClasses("tracking-check").toHtml())));
			}
			else if (trackingStatus.state() == InterWikiTrackingService.State.CONTAINED_DIFF) {
				result.append(new P().clazz("note").children(
						new PlainTextNode("Tracking mode: local content differs from the source, but all changes of the source since the last acknowledgement are already contained locally"),
						new HtmlNode(Icon.CHECKED.addColor(Color.OK).addClasses("tracking-check").toHtml())));
			}
			else {
				// the changes since the last acknowledgement, or all differences if that is unknown
				InterWikiTrackingService.DiffOption review = trackingStatus.sourceChanges() == null ? null : options.stream()
						.filter(InterWikiTrackingService.DiffOption::acknowledged)
						.findFirst()
						.orElse(null);
				if (review == null) {
					review = options.get(options.size() - 1);
					result.append(new P().clazz("note").plainText("Tracking mode: local content differs from the source and was not acknowledged yet."));
				}
				else {
					result.append(new P().clazz("note").plainText("Tracking mode: the source changed since the last acknowledgement ("
							+ InterWikiImportAnnotationFormat.formatDateTime(Objects.requireNonNull(trackingStatus.trackingAcceptedAt()), locale, ZoneId.systemDefault())
							+ ")."));
				}
				renderReview(markup, review, localText, user, result);
			}
			renderComparison(markup, options, trackingStatus.trackingAcceptedAt(), locale, user, result);
		}

		private void renderReview(Section<InterWikiImportMarkup> markup, InterWikiTrackingService.DiffOption review,
				String localText, UserContext user, RenderResult result) {
			result.append(new Div().clazz("tracking-diff").children(renderDiffOption(review, localText, user)));
			if (!KnowWEUtils.canWrite(markup, user)) return;
			HtmlElement buttons = new Div().clazz("tracking-action-buttons");
			String acknowledge = buildTrackingActionScript("AcceptInterWikiTrackingDiffAction", markup.getID(),
					"tracking-accept", "Unable to acknowledge tracking differences.", null);
			if (isApplicable(review, localText)) {
				String label = review.conflict()
						? "Override local changes by applying shown changes from source and acknowledge"
						: "Apply shown changes from source and acknowledge";
				buttons.children(createButton(label, "KNOWWE.plugin.include.applyTrackingChanges('" + markup.getID() + "', "
						+ review.version() + ", true, " + review.conflict() + ")"));
			}
			buttons.children(createButton("Acknowledge differences", acknowledge));
			result.append(buttons);
		}

		/**
		 * Renders the collapsed "Compare with source": the complete comparison with the source or the
		 * changes since a previous version (loaded when expanded), and the button to apply them without
		 * acknowledging.
		 */
		private void renderComparison(Section<InterWikiImportMarkup> markup, List<InterWikiTrackingService.DiffOption> options,
				@Nullable Instant acceptedAt, Locale locale, UserContext user, RenderResult result) {
			HtmlElement select = new HtmlElement("select").attributes(
					"id", "tracking-compare-select-" + markup.getID(),
					"class", "tracking-diff-select",
					"onchange", "KNOWWE.plugin.include.showTrackingComparison('" + markup.getID() + "')");
			// the complete comparison first, then the changes since the previous versions, newest first
			InterWikiTrackingService.DiffOption complete = options.get(options.size() - 1);
			select.children(createComparisonOption(complete, acceptedAt, locale));
			for (InterWikiTrackingService.DiffOption option : options.subList(0, options.size() - 1)) {
				select.children(createComparisonOption(option, acceptedAt, locale));
			}
			HtmlElement controls = new Div().clazz("tracking-action-buttons").children(
					new Span().clazz("tracking-diff-selection").children(new Span().plainText("Show:"), select));
			if (KnowWEUtils.canWrite(markup, user)) {
				controls.children(createButton("Apply shown changes",
						"KNOWWE.plugin.include.applyTrackingComparison('" + markup.getID() + "')")
						.attributes("id", "tracking-compare-apply-" + markup.getID()));
			}
			result.append(new HtmlElement("details")
					.clazz("iwi-comparison")
					.attributes("ontoggle", "KNOWWE.plugin.include.showTrackingComparison('" + markup.getID() + "')")
					.children(
							new HtmlElement("summary").plainText("Compare with source"),
							controls,
							new Div().id("tracking-compare-" + markup.getID()).clazz("tracking-diff")));
		}

		private static HtmlElement createComparisonOption(InterWikiTrackingService.DiffOption option, @Nullable Instant acceptedAt, Locale locale) {
			return new HtmlElement("option")
					.attributes("value", String.valueOf(option.version()))
					.plainText(getComparisonLabel(option, acceptedAt, locale));
		}

		/**
		 * Labels the entry by the date of the version, the one of the last acknowledgement by the date of the
		 * acknowledgement (as in the status message).
		 */
		private static String getComparisonLabel(InterWikiTrackingService.DiffOption option, @Nullable Instant acceptedAt, Locale locale) {
			if (option.version() == InterWikiTrackingService.ALL_CHANGES) return "Complete comparison with source";
			List<String> details = new ArrayList<>();
			String label;
			if (option.acknowledged() && acceptedAt != null) {
				label = "Changes since last acknowledgement";
				details.add(InterWikiImportAnnotationFormat.formatDateTime(acceptedAt, locale, ZoneId.systemDefault()));
			}
			else {
				label = "Changes since " + InterWikiImportAnnotationFormat.formatDateTime(Objects.requireNonNull(option.date()), locale, ZoneId.systemDefault());
			}
			if (option.conflict()) details.add("conflict");
			return details.isEmpty() ? label : label + " (" + String.join(", ", details) + ")";
		}

		private static HtmlElement createButton(String label, String onclick) {
			return new HtmlElement("button")
					.attributes("type", "button", "class", "tracking-action-button", "onclick", onclick)
					.plainText(label);
		}

		private static String buildTrackingActionScript(String action, String sectionId, String notificationKey, String fallbackError, @Nullable String confirmMessage) {
			String prefix = confirmMessage == null
					? ""
					: "if(!confirm('" + confirmMessage.replace("\\", "\\\\").replace("'", "\\'") + "'))return;";
			return "(function(){"
					+ prefix
					+ "jq$.ajax({"
					+ "url: KNOWWE.core.util.getURL({action:'" + action + "',"
					+ Attributes.SECTION_ID + ":'" + sectionId + "'}),"
					+ "type:'post',"
					+ "cache:false"
					+ "}).done(function(){window.location.reload();})"
					+ ".fail(function(xhr){"
					+ "KNOWWE.notification.error(null,"
					+ "KNOWWE.plugin.include.errorMessage(xhr, '" + fallbackError + "'),"
					+ "'" + notificationKey + "',10000);"
					+ "});"
					+ "})();";
		}

		private void renderTrackingComparisonStatus(
				Section<InterWikiImportMarkup> markup,
				InterWikiTrackingService.TrackingStatus trackingStatus,
				UserContext user,
				RenderResult result) {
			switch (trackingStatus.state()) {
				case EQUAL -> result.append(new HtmlElement("p").clazz("success")
						.content("Tracking mode: local content matches the source."));
				case UNACCEPTED_DIFF, ACCEPTED_DIFF, CONTAINED_DIFF -> renderTrackingDifferences(markup, trackingStatus, user, result);
				default -> result.append(new HtmlElement("p").clazz("note")
						.content("Tracking mode: status currently unavailable."));
			}
		}

		private void renderImport(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			String path = markup.get().getWikiAttachmentPath(markup);
			Article article = user.getArticleManager().getArticle(path);
			if (article == null) {
				result.append(new Span("Included article not (yet) available").clazz("warning"));
			}
			else {
				boolean framed = isFramed();
				// the imported content is set off from the import markup itself
				result.append(new Div().clazz("iwi-imported-content").children(content -> {
					if (framed) {
						// used the framing renderer
						new FramedIncludedSectionRenderer(true).render(article.getRootSection(), user, content);
					}
					else {
						// or simply render the sections belonging to the header
						FramedIncludedSectionRenderer.renderTargetSections(article.getRootSection(), true, user, content);
					}
				}));
			}
		}

		private void renderLastChangesMessage(Section<InterWikiImportMarkup> markup, RenderResult result) {
			if (getLock(markup).isLocked()) {
				result.append(new HtmlElement("p").attributes("style", "color:green").content("Update currently ongoing..."));
			}
			else {
				long lastRun = markup.get().timeSinceLastRun(markup);
				String message;
				if (lastRun < Long.MAX_VALUE) {
					Instant now = Instant.now();
					message = "Last check for changes: "
							+ InterWikiImportAnnotationFormat.formatRelative(now.minusMillis(lastRun), now);
					// the change in the source (@latestChange), not the local update of the attachment
					Instant latestChange = markup.get().getLatestChange(markup);
					if (latestChange != null) {
						message += ", last change in source: " + InterWikiImportAnnotationFormat.formatRelative(latestChange, now);
					}
				}
				else {
					message = "No check yet, click here to check now: ";
				}
				result.append(new HtmlElement("p").children(
						new Span(message).clazz("include-message"),
						new A().attributes(
										"onclick", buildRefreshScript(markup.getID(), false),
										"class", "include-refresh tooltipster",
										"title", "Check for changes")
								.children(new HtmlNode(Icon.REFRESH.toHtml()))
				));
			}

			if (DefaultMarkupType.getAnnotation(markup, INTERVAL_ANNOTATION) != null) {
				result.append(new HtmlElement("p")
						.clazz("warning")
						.content("@interval is deprecated for InterWikiImport and no longer used. Updates are polled immediately after startup and then every "
								+ TimeUnit.MILLISECONDS.toMinutes(UPDATE_SERVICE.getPollIntervalMillis())
								+ " minutes per source wiki."));
			}
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

					String shortenedUrl = url.toString().replaceAll("%23.+$", "");
					HtmlElement header = new HtmlElement("h2").children(
							new TextNode("Import from wiki "),
							new A(linkLabel, shortenedUrl));

				HelpToolProvider helpToolProvider = new HelpToolProvider();
				if (helpToolProvider.hasTools(markup, user)) {
					Tool[] tools = helpToolProvider.getTools(markup, user);
					for (Tool tool : tools) {
						header.children(new A()
								.attributes("title", tool.getDescription(),
										"class", "tooltipster help-tool",
										tool.getActionType() == Tool.ActionType.ONCLICK ? "onclick" : "href",
										tool.getAction())
								.children(new HtmlNode(tool.getIcon().toHtml())));
					}
				}

				String action = "KNOWWE.core.plugin.setMarkupSectionActivationStatus('" + markup.getID() + "', 'off')";
				header.children(new A()
						.attributes("onclick", action, "class", "include-deactivate tooltipster", "title", "Deactivate import")
						.children(new HtmlNode(Icon.TOGGLE_OFF.toHtml())));
				result.append(header);
			}
		}
	}

	public enum Mode {
		IMPORT,
		TRACKING
	}

	private static class RegistrationScript extends DefaultGlobalCompiler.DefaultGlobalScript<InterWikiImportMarkup> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<InterWikiImportMarkup> section) {
			if (section.get().getUrl(section) == null) {
				UPDATE_SERVICE.deregister(section);
				Messages.clearMessages(section, TrackingMessages.class);
				return;
			}
			UPDATE_SERVICE.register(section);
			section.get().refreshTrackingMessages(section);
			section.get().restoreSyncMessage(section);
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<InterWikiImportMarkup> section) {
			UPDATE_SERVICE.deregister(section);
			Messages.clearMessages(section, TrackingMessages.class);
			Messages.clearMessages(section, SyncMessages.class);
		}
	}
}
