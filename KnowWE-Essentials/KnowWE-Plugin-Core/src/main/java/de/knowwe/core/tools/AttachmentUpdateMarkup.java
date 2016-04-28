/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.core.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.basicType.URLType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * This markup allows to update an attachment based on changes to web resource specified via URL. If the resource at
 * the URL changes, it will be downloaded and attached as a new version of the original attachment.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.06.15
 */
public class AttachmentUpdateMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP = new DefaultMarkup("AttachmentUpdater");

	private static final String ATTACHMENT_ANNOTATION = "attachment";
	private static final String URL_ANNOTATION = "url";
	private static final String INTERVAL_ANNOTATION = "interval";
	private static final String START_ANNOTATION = "start";
	private static final String VERSIONING_ANNOTATION = "versioning";
	private static final String ZIP_ENTRY_ANNOTATION = "zipEntry";

	private static final String EXECUTOR_KEY = "executor_key";
	private static final String LOCK_KEY = "lock_key";

	public static final String START_PATTERN = "[EEEE ]H:mm";
	private static final DateTimeFormatter START_FORMATTER = DateTimeFormatter.ofPattern(START_PATTERN, Locale.ENGLISH);

	private static final long MIN_INTERVAL = TimeUnit.SECONDS.toMillis(1); // we want to wait at least a second before we check again

	static {
		MARKUP.addAnnotation(ATTACHMENT_ANNOTATION, true);
		MARKUP.addAnnotationContentType(ATTACHMENT_ANNOTATION, new AttachmentType());
		MARKUP.addAnnotation(URL_ANNOTATION, true);
		MARKUP.addAnnotationContentType(URL_ANNOTATION, new URLType());
		MARKUP.addAnnotation(INTERVAL_ANNOTATION, true);
		MARKUP.addAnnotationContentType(INTERVAL_ANNOTATION, new TimeStampType());
		MARKUP.addAnnotation(START_ANNOTATION);
		MARKUP.addAnnotation(VERSIONING_ANNOTATION, false, "true", "false");
		MARKUP.addAnnotation(ZIP_ENTRY_ANNOTATION);

	}

	public AttachmentUpdateMarkup() {
		super(MARKUP);
		addCompileScript(new UpdateScript());
	}

	private static class UpdateScript extends DefaultGlobalScript<AttachmentUpdateMarkup> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<AttachmentUpdateMarkup> section) {

			if (section.hasErrorInSubtree()) return;

			long interval = getInterval(section);
			ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
			section.storeObject(compiler, EXECUTOR_KEY, scheduledExecutorService);

			if (interval < TimeUnit.HOURS.toMillis(1)) {
				// if we have an interval smaller than one hour, we see it as a
				// delay between executions and start at once
				scheduledExecutorService.scheduleWithFixedDelay(
						() -> performUpdate(section),
						0, interval, TimeUnit.MILLISECONDS);
			}
			else {
				// otherwise, we see it as a rate and start a the given start delay
				scheduledExecutorService.scheduleAtFixedRate(
						() -> performUpdate(section),
						getInitialDelay(section), interval, TimeUnit.MILLISECONDS);
			}

		}

		private long getInitialDelay(Section<AttachmentUpdateMarkup> section) {
			LocalDateTime start = LocalDateTime.now();

			Section<? extends AnnotationContentType> annotationContentSection = DefaultMarkupType.getAnnotationContentSection(section, START_ANNOTATION);

			if (annotationContentSection == null) return LocalDateTime.now().until(start, ChronoUnit.MILLIS);

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

		private long getInterval(Section<AttachmentUpdateMarkup> section) {
			Section<TimeStampType> timeStampSection = Sections.successor(section, TimeStampType.class);
			if (timeStampSection == null) return MIN_INTERVAL;
			long timeInMillis = TimeStampType.getTimeInMillis(timeStampSection);
			if (timeInMillis < MIN_INTERVAL) timeInMillis = MIN_INTERVAL;
			return timeInMillis;
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<AttachmentUpdateMarkup> section) {
			ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) section.getObject(compiler, EXECUTOR_KEY);
			if (scheduledExecutorService != null) {
				scheduledExecutorService.shutdownNow();
			}
		}
	}

	public static void performUpdate(Section<AttachmentUpdateMarkup> section) {
		Section<AttachmentType> attachmentSection = Sections.successor(section, AttachmentType.class);
		Section<URLType> urlSection = Sections.successor(section, URLType.class);

		URL url = URLType.getURL(urlSection);
		if (url == null || attachmentSection == null) {
			return; // nothing to do, error will already be rendered from URLType
		}

		ReentrantLock lock = getLock(section);
		if (!lock.tryLock()) {
			Log.info("Skipped requested updated for attachment '" + Strings.trim(attachmentSection.getText()) + "' with resource from URL " + url
					.toString() + ", update already running");
			return;
		}
		try {
			WikiAttachment attachment;
			try {
				attachment = AttachmentType.getAttachment(attachmentSection);
			}
			catch (IOException e) {
				// we already get error messages from AttachmentType
				return;
			}

			if (attachment.getPath().split("/").length > 2) {
				Messages.storeMessage(section, AttachmentUpdateMarkup.class, Messages.error("Unable to update entries in zipped attachments!"));
				return;
			}

			try {
				if (!needsUpdate(attachment, url)) {
					Log.info("Resource at URL " + url.toString() + " has not changed, attachment '" + attachment.getPath() + "' not updated");
					return;
				}

				final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				if (url.getUserInfo() != null) {
					String basicAuth = "Basic " + new String(Base64.getEncoder().encode(url.getUserInfo().getBytes()));
					connection.setRequestProperty("Authorization", basicAuth);
				}

				InputStream attachmentStream = null;
				String zipEntryName = DefaultMarkupType.getAnnotation(section, ZIP_ENTRY_ANNOTATION);
				if (zipEntryName != null) {
					ZipInputStream zipStream = new ZipInputStream(connection.getInputStream());
					for (ZipEntry zipEntry; (zipEntry = zipStream.getNextEntry()) != null; ) {
						if (zipEntry.getName().equals(zipEntryName)) {
							attachmentStream = zipStream;
							break;
						}
					}
					if (attachmentStream == null) {
						throw new ZipException(zipEntryName + " not found at linked resource.");
					}
				}
				else {
					attachmentStream = connection.getInputStream();
				}

				ArticleManager articleManager = section.getArticleManager();
				articleManager.open();
				try {
					if ("false".equalsIgnoreCase(DefaultMarkupType.getAnnotation(section, VERSIONING_ANNOTATION))) {
						Environment.getInstance()
								.getWikiConnector()
								.deleteAttachment(attachment.getParentName(), attachment.getFileName(), "SYSTEM");
					}
					Environment.getInstance()
							.getWikiConnector()
							.storeAttachment(attachment.getParentName(), attachment.getFileName(), "SYSTEM", attachmentStream);
				}
				finally {
					articleManager.commit();
				}

				Log.info("Updated attachment '" + attachment.getPath() + "' with resource from URL " + url.toString());
				Messages.clearMessages(section, AttachmentUpdateMarkup.class);
			}
			catch (Throwable e) { // NOSONAR
				String message = "Exception while downloading attachment";
				Messages.storeMessage(section, AttachmentUpdateMarkup.class, Messages.error(message + ": " + e.getMessage()));
				Log.severe(message, e);
			}
		}
		finally {
			lock.unlock();
		}
	}

	private static ReentrantLock getLock(Section<AttachmentUpdateMarkup> section) {
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (section) {
			ReentrantLock lock = (ReentrantLock) section.getObject(LOCK_KEY);
			if (lock == null) {
				lock = new ReentrantLock();
				section.storeObject(LOCK_KEY, lock);
			}
			return lock;
		}
	}

	protected static boolean needsUpdate(WikiAttachment attachment, URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		if (url.getUserInfo() != null) {
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(url.getUserInfo().getBytes()));
			connection.setRequestProperty("Authorization", basicAuth);
		}

		connection.setRequestMethod("HEAD");
		connection.connect();

		LocalDateTime urlDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(connection.getLastModified()), ZoneId
				.systemDefault());
		urlDateTime = urlDateTime.withSecond(0);

		LocalDateTime attachmentDateTime = LocalDateTime.ofInstant(attachment.getDate().toInstant(), ZoneId
				.systemDefault());
		attachmentDateTime = attachmentDateTime.withSecond(0);

		// we only compare seconds, because http header does not transmit milliseconds...
		boolean update = urlDateTime.truncatedTo(ChronoUnit.SECONDS)
				.isAfter(attachmentDateTime.truncatedTo(ChronoUnit.SECONDS));

		if (!update) {
			// check if urlDateTime is equal to unix time 0
			// if it is, the time was not set (maybe because of server settings) and we just update always
			update = urlDateTime.equals(LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()));
		}

		connection.disconnect();

		return update;
	}

}
