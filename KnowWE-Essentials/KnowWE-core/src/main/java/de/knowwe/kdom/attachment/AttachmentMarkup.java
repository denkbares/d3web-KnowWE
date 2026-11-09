/*
 * Copyright (C) 2020 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.basicType.URLType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.ContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static de.knowwe.core.report.Message.Type.*;

/**
 * A markup to handle attachments. It allows to update an attachment from a given URL in a given interval of time, also
 * allows to compile the attachment.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.06.15
 */
public class AttachmentMarkup extends AttachmentUpdateMarkup implements AttachmentCompileType {

	protected static final DefaultMarkup MARKUP = new DefaultMarkup("Attachment");

	private static final String ATTACHMENT_ANNOTATION = "attachment";
	private static final String URL_ANNOTATION = "url";
	private static final String START_ANNOTATION = "start";
	private static final String VERSIONING_ANNOTATION = "versioning";
	private static final String ZIP_ENTRY_ANNOTATION = "zipEntry";
	private static final String COMPILE_ANNOTATION = "compile";
	private static final String START_PATTERN = "[EEEE ]H:mm";
	private static final DateTimeFormatter START_FORMATTER = DateTimeFormatter.ofPattern(START_PATTERN, Locale.ENGLISH);

	static {
		MARKUP.addAnnotation(ATTACHMENT_ANNOTATION, true);
		MARKUP.addAnnotationContentType(ATTACHMENT_ANNOTATION, () -> new AttachmentType());
		MARKUP.addAnnotation(URL_ANNOTATION);
		MARKUP.addAnnotation(COMPILE_ANNOTATION, false, "true", "false");
		MARKUP.addAnnotationContentType(URL_ANNOTATION, () -> new URLType());
		MARKUP.addAnnotation(INTERVAL_ANNOTATION);
		MARKUP.addAnnotation(REPLACEMENT, false, Pattern.compile(".+->.*"));
		MARKUP.addAnnotation(REGEX_REPLACEMENT, false, Pattern.compile(".+->.*")).addContentType(() -> {
			PlainText regexType = new PlainText();
			regexType.setRenderer((section, user, result) -> result.appendJSPWikiMarkup(section.getText()));
			return regexType;
		});
		MARKUP.addAnnotationContentType(INTERVAL_ANNOTATION, () -> {
			TimeStampType timeStampType = new TimeStampType();
			timeStampType.setRenderer((section, user, result) -> {
				result.append(section.getText());
				Section<AttachmentUpdateMarkup> attachmentUpdate = Sections.ancestor(section, AttachmentUpdateMarkup.class);
				if (attachmentUpdate == null) return;
				long sinceLastRun = attachmentUpdate.get().timeSinceLastRun(attachmentUpdate);
				long sinceLastChange = attachmentUpdate.get().timeSinceLastChange(attachmentUpdate);
				if (sinceLastRun < Long.MAX_VALUE && sinceLastChange < Long.MAX_VALUE) {
					String lastRunDisplay = getDisplay(sinceLastRun);
					String lastChangeDisplay = getDisplay(sinceLastChange);
					result.appendHtmlElement("span",
							" (last check was " + lastRunDisplay + " ago, last change was " + lastChangeDisplay + " ago)",
							"style", "color: grey");
				}
			});
			return timeStampType;
		});
		MARKUP.addAnnotation(START_ANNOTATION);
		MARKUP.addAnnotation(VERSIONING_ANNOTATION, false, "true", "false");
		MARKUP.addAnnotation(ZIP_ENTRY_ANNOTATION);

		MARKUP.setDocumentation("""
				Manages an attachment of the wiki, keeping it up to date with a file downloaded from a URL and/or
				compiling its content.
				<p>With <code>@url</code>, the attachment given in <code>@attachment</code> is replaced by the downloaded
				file, regularly if <code>@interval</code> is set, otherwise only when the update is triggered manually.
				The downloaded text can be modified with <code>@replacement</code> and <code>@regexReplacement</code>
				before it is stored.</p>
				""");
		MARKUP.getAnnotation(ATTACHMENT_ANNOTATION)
				.setDocumentation("The attachment to manage, either a file name of an attachment of this page or " +
								  "<code>article-name/file-name</code>.")
				.setExample("@attachment: semantic-service-model-ilme.ttl");
		MARKUP.getAnnotation(URL_ANNOTATION)
				.setDocumentation("URL to download the content of the attachment from.")
				.setExample("@url: https://www.example.com/downloads/vocabulary.ttl");
		MARKUP.getAnnotation(COMPILE_ANNOTATION)
				.setDocumentation("Set to <code>true</code> to compile the wiki markup contained in the attachment " +
								  "as part of this page (default is <code>false</code>).")
				.setExample("@compile: true");
		MARKUP.getAnnotation(INTERVAL_ANNOTATION)
				.setDocumentation("How often the attachment is updated from the URL, as a duration with unit " +
								  "(<code>s</code>, <code>min</code>, <code>h</code>, <code>d</code>). Without an interval, " +
								  "the attachment is only updated manually.")
				.setExample("@interval: 1d");
		MARKUP.getAnnotation(START_ANNOTATION)
				.setDocumentation("Time of the first update for intervals of at least one hour, in the format " +
								  "<code>H:mm</code> or <code>EEEE H:mm</code> (English day of week). Useful to update at night.")
				.setExample("@start: 3:00");
		MARKUP.getAnnotation(VERSIONING_ANNOTATION)
				.setDocumentation("Whether each update creates a new version of the attachment (default is " +
								  "<code>true</code>). With <code>false</code>, previous versions are removed.")
				.setExample("@versioning: false");
		MARKUP.getAnnotation(ZIP_ENTRY_ANNOTATION)
				.setDocumentation("If the URL provides a ZIP file, the name of the entry in the ZIP file to store as attachment.")
				.setExample("@zipEntry: data/vocabulary.ttl");
		MARKUP.getAnnotation(REPLACEMENT)
				.setDocumentation("Replaces all occurrences of a text in the downloaded content before it is stored, " +
								  "written as <code>source -&gt; target</code>; <code>\\n</code> in the target is a line break.")
				.setExample("@replacement: http://old.example.com/ -> http://www.example.com/");
		MARKUP.getAnnotation(REGEX_REPLACEMENT)
				.setDocumentation("Like <code>@replacement</code>, but the source is a regular expression and the " +
								  "target may reference its groups (<code>$1</code>).")
				.setExample("@regexReplacement: version=\\d+ -> version=1");
		MARKUP.addExample("""
				%%Attachment
				  @attachment: semantic-service-model-ilme.ttl
				  @versioning: false
				%
				""");
		MARKUP.addExample("""
				%%Attachment
				  @attachment: vocabulary.ttl
				  @url: https://www.example.com/downloads/vocabulary.ttl
				  @interval: 1d
				  @start: 3:00
				  @versioning: false
				%
				""");
	}

	public AttachmentMarkup() {
		this(MARKUP);
	}

	public AttachmentMarkup(DefaultMarkup markup) {
		super(markup);
		setRenderer(new DefaultMarkupRenderer() {

			@Override
			public void renderMessages(Section<?> section, RenderResult string, UserContext context) {
				renderMessageBlock(section, string, context, ERROR, WARNING, INFO);
			}

			@Override
			protected void renderContents(Section<? extends DefaultMarkupType> markupSection, List<Section<ContentType>> contentSections, UserContext user, RenderResult result) {
				ReentrantLock lock = $(markupSection).closest(AttachmentUpdateMarkup.class)
						.mapFirst(s -> s.get().getLock(s));
				if (lock != null && lock.isLocked()) {
					result.appendHtml("<span style='color:green'>")
							.append("Update currently ongoing...")
							.appendHtml("</span>");
				}
			}
		});
	}



	@Override
	public @Nullable URL getUrl(Section<? extends AttachmentUpdateMarkup> section) {
		if(section == null) return null;
		Section<URLType> urlSection = Sections.successor(section, URLType.class);
		if(urlSection == null) return null;
		return URLType.getURL(urlSection);
	}

	@Nullable
	@Override
	public WikiAttachment getWikiAttachment(Section<? extends AttachmentUpdateMarkup> section) throws IOException {
		if (section == null) return null;
		Section<AttachmentType> attachmentSection = Sections.successor(section, AttachmentType.class);
		return AttachmentType.getAttachment(attachmentSection);
	}

	@Override
	@Nullable
	public String getWikiAttachmentPath(Section<? extends AttachmentUpdateMarkup> section) {
		if (section == null) return null;
		Section<AttachmentType> attachmentTypeSection = Sections.successor(section, AttachmentType.class);
		if (attachmentTypeSection == null) return null;
		return AttachmentType.getPath(attachmentTypeSection);
	}

	@Override
	public @Nullable WikiAttachment getCompiledAttachment(Section<? extends AttachmentCompileType> section) throws IOException {
		if (isCompilingTheAttachment(section)) {
			return getWikiAttachment($(section).closest(AttachmentUpdateMarkup.class).getFirst());
		}
		return null;
	}

	@Override
	public boolean isCompilingTheAttachment(Section<? extends AttachmentCompileType> section) {
		return "true".equals(DefaultMarkupType.getAnnotation(section, COMPILE_ANNOTATION));
	}

	@Override
	public String getCompiledAttachmentPath(Section<? extends AttachmentCompileType> section) {
		return $(section).successor(AttachmentType.class).mapFirst(AttachmentType::getPath);
	}

	@Override
	protected boolean isVersioning(Section<? extends AttachmentUpdateMarkup> section) {
		return !"false".equalsIgnoreCase(DefaultMarkupType.getAnnotation(section, VERSIONING_ANNOTATION));
	}

	@Override
	protected InputStream getConnectionStream(Section<? extends AttachmentUpdateMarkup> section, URLConnection connection) throws IOException {
		InputStream connectionStream = null;
		String zipEntryName = DefaultMarkupType.getAnnotation(section, ZIP_ENTRY_ANNOTATION);
		if (zipEntryName != null) {
			ZipInputStream zipStream = new ZipInputStream(connection.getInputStream());
			for (ZipEntry zipEntry; (zipEntry = zipStream.getNextEntry()) != null; ) {
				if (zipEntry.getName().equals(zipEntryName)) {
					connectionStream = zipStream;
					break;
				}
			}
			if (connectionStream == null) {
				throw new ZipException(zipEntryName + " not found at linked resource.");
			}
		}
		else {
			connectionStream = connection.getInputStream();
		}
		return connectionStream;
	}

	@Override
	protected long getStartDelayFromAnnotation(Section<? extends AttachmentUpdateMarkup> section) {

		Section<? extends AnnotationContentType> annotationContentSection = DefaultMarkupType.getAnnotationContentSection(section, START_ANNOTATION);

		if (annotationContentSection == null) return 0;

		String startString = annotationContentSection.getText();

		TemporalAccessor parsedStart;
		try {
			parsedStart = START_FORMATTER.parse(startString);
			Messages.clearMessages(annotationContentSection, this.getClass());
		}
		catch (DateTimeParseException e) {
			Messages.storeMessage(annotationContentSection, this.getClass(), Messages.error(startString
					+ " is not a valid start definition. Please use the following pattern: " + START_PATTERN));
			return 0;
		}

		LocalDateTime start = LocalDateTime.now();
		LocalDateTime now = start;
		start = start.withNano(0);
		start = start.withSecond(0);

		start = start.withMinute(parsedStart.get(ChronoField.MINUTE_OF_HOUR));
		if (start.isBefore(now)) start = start.plusHours(1);

		start = start.withHour(parsedStart.get(ChronoField.HOUR_OF_DAY));
		if (start.isBefore(now)) start = start.plusDays(1);

		if (parsedStart.isSupported(ChronoField.DAY_OF_WEEK)) {
			start = start.with(DayOfWeek.from(parsedStart));
			if (start.isBefore(now)) start = start.plusWeeks(1);
		}

		if (parsedStart.isSupported(ChronoField.MONTH_OF_YEAR)) {
			start = start.withMonth(parsedStart.get(ChronoField.MONTH_OF_YEAR));
			if (start.isBefore(now)) start = start.plusYears(1);
		}

		return LocalDateTime.now().until(start, ChronoUnit.MILLIS);
	}
}
