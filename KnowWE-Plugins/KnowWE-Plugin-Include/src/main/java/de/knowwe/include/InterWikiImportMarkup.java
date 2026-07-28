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
import java.util.Collection;
import java.util.LinkedHashMap;
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
import de.knowwe.core.kdom.rendering.elements.HtmlElement;
import de.knowwe.core.kdom.rendering.elements.HtmlNode;
import de.knowwe.core.kdom.rendering.elements.Span;
import de.knowwe.core.kdom.rendering.elements.TextNode;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
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
	private static final String MODE_ANNOTATION = "mode";
	private static final String VALIDATION_MODE_ANNOTATION = "validationMode";
	private static final String LATEST_CHANGE_ANNOTATION = "latestChange";
	private static final String TRACKING_ACCEPTED_AT_ANNOTATION = "trackingAcceptedAt";

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
		Section<?> acceptedAtContent = DefaultMarkupType.getAnnotationContentSection(section, TRACKING_ACCEPTED_AT_ANNOTATION);
		if (acceptedAtContent != null) {
			replacements.put(acceptedAtContent.getID(), acceptedAt.toString());
			return true;
		}

		Section<?> closingTag = $(section).children().getLast();
		if (closingTag == null || !"%".equals(Strings.trim(closingTag.getText()))) return false;
		replacements.put(closingTag.getID(),
				"\n@" + TRACKING_ACCEPTED_AT_ANNOTATION + ": " + acceptedAt
						+ "\n" + Strings.trimLeft(closingTag.getText()));
		return true;
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

	boolean collectSwitchToReferenceReplacement(Section<InterWikiImportMarkup> section, Map<String, String> replacements) throws IOException {
		String referenceText = getTrackingReferenceText(section);
		if (referenceText == null) return false;

		int[] range = getLocalComparisonRange(section);
		if (range == null) return false;

		Article article = section.getArticle();
		String articleText = article.getText();
		String newArticleText = articleText.substring(0, range[0])
				+ "\n\n" + Strings.trimRight(referenceText) + "\n"
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
				+ "xhr.responseText || 'Unable to refresh InterWikiImport.',"
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
						Messages.warning("InterWikiImport tracking differences are not acknowledged yet."));
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

	@Override
	protected long getIntervalMillis(Section<? extends AttachmentUpdateMarkup> section) {
		return Long.MAX_VALUE;
	}

	@Override
	protected boolean usesOwnScheduling(Section<? extends AttachmentUpdateMarkup> section) {
		return false;
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

		private void renderTracking(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			InterWikiTrackingService.TrackingStatus trackingStatus;
			try {
				trackingStatus = InterWikiTrackingService.getTrackingStatus(markup);
			}
			catch (IOException e) {
				result.append(new HtmlElement("p").clazz("warning").content("Unable to read tracking reference attachment: " + e.getMessage()));
				return;
			}

			if (trackingStatus.state() == InterWikiTrackingService.State.MISSING_REFERENCE) {
				result.append(new HtmlElement("p").clazz("note").content("Tracking reference attachment not (yet) available"));
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
					.content("Tracking mode: local copy is empty. Insert the current reference text from the source wiki below the markup as a starting point."));
			result.append(new HtmlElement("button")
					.attributes("type", "button",
							"class", "tracking-action-button",
							"onclick", buildTrackingActionScript(
									"InitInterWikiTrackingLocalCopyAction", markup.getID(),
									"tracking-init", "Unable to initialize local copy.", null))
					.content("Insert reference text below"));
		}

		private void renderDiffActionButtons(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result,
				boolean canAcknowledge, @Nullable String toggleDiffContainerId) {
			boolean canWrite = KnowWEUtils.canWrite(markup, user);
			if (!canWrite && toggleDiffContainerId == null) return;
			HtmlElement container = new HtmlElement("div").clazz("tracking-action-buttons");
			if (toggleDiffContainerId != null) {
				container.children(new HtmlElement("button")
						.attributes("type", "button",
								"class", "tracking-action-button",
								"onclick", "var e=document.getElementById('" + toggleDiffContainerId + "');"
										+ "if(!e)return;"
										+ "var show=(e.style.display==='none');"
										+ "e.style.display=show?'block':'none';"
										+ "this.textContent=show?'Hide current differences':'Show current differences';")
						.content("Show current differences"));
			}
			if (!canWrite) {
				result.append(container);
				return;
			}
			if (canAcknowledge) {
				container.children(new HtmlElement("button")
						.attributes("type", "button",
								"class", "tracking-action-button",
								"onclick", buildTrackingActionScript(
										"AcceptInterWikiTrackingDiffAction", markup.getID(),
										"tracking-accept", "Unable to acknowledge tracking differences.", null))
						.content("Acknowledge differences"));
			}
			container.children(new HtmlElement("button")
					.attributes("type", "button",
							"class", "tracking-action-button",
							"onclick", buildTrackingActionScript(
									"SwitchInterWikiTrackingToReferenceAction", markup.getID(),
									"tracking-switch", "Unable to switch to reference content.",
									"Replace the local content below the markup with the current reference text from the source wiki? Local edits in this range will be lost."))
					.content("Switch to changes from reference"));
			result.append(container);
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
					+ "xhr.responseText || '" + fallbackError + "',"
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
						.content("Tracking mode: local content matches the reference."));
				case UNACCEPTED_DIFF -> {
					result.append(new HtmlElement("p").clazz("note")
							.content("Tracking mode: local content differs from the reference."));
					trackingStatus.diffOptional().ifPresent(diff -> result.appendHtml(renderTrackingDiff(diff, user)));
					renderDiffActionButtons(markup, user, result, true, null);
				}
				case ACCEPTED_DIFF -> {
					result.append(new HtmlElement("p").clazz("note")
							.content("Tracking mode: local content differs from the reference (already acknowledged)."));
					String diffContainerId = "tracking-diff-" + markup.getID();
					renderDiffActionButtons(markup, user, result, false, diffContainerId);
					trackingStatus.diffOptional().ifPresent(diff -> result.append(
							new HtmlElement("div")
									.attributes("id", diffContainerId, "style", "display:none;")
									.children(new HtmlNode(renderTrackingDiff(diff, user)))));
				}
				default -> result.append(new HtmlElement("p").clazz("note")
						.content("Tracking mode: status currently unavailable."));
			}
		}

		private String renderTrackingDiff(TextDiff diff, UserContext user) {
			String theme = getDiffTheme(user);
			return DiffHtmlRenderer.renderTextDiff(diff)
					.replaceFirst("<knowwe-text-diff ", "<knowwe-text-diff data-theme=\"" + theme + "\" ");
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

		private void renderImport(Section<InterWikiImportMarkup> markup, UserContext user, RenderResult result) {
			String path = markup.get().getWikiAttachmentPath(markup);
			Article article = user.getArticleManager().getArticle(path);
			if (article == null) {
				result.append(new Span("Included article not (yet) available").clazz("warning"));
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
				result.append(new HtmlElement("p").attributes("style", "color:green").content("Update currently ongoing..."));
			}
			else {
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
