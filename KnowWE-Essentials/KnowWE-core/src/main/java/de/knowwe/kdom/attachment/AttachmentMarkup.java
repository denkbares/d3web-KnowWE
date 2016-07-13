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

package de.knowwe.kdom.attachment;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Stopwatch;
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
 * A markup to handle attachments. It allows to update an attachment from a given URL in a given interval of time, also
 * allows to compile the attachment.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.06.15
 */
public class AttachmentMarkup extends DefaultMarkupType {

	protected static final DefaultMarkup MARKUP = new DefaultMarkup("Attachment");

	private static final String ATTACHMENT_ANNOTATION = "attachment";
	private static final String URL_ANNOTATION = "url";
	private static final String INTERVAL_ANNOTATION = "interval";
	private static final String START_ANNOTATION = "start";
	private static final String VERSIONING_ANNOTATION = "versioning";
	private static final String ZIP_ENTRY_ANNOTATION = "zipEntry";
	public static final String COMPILE = "compile";

	private static final String LOCK_KEY = "lock_key";

	private static final String START_PATTERN = "[EEEE ]H:mm";
	private static final DateTimeFormatter START_FORMATTER = DateTimeFormatter.ofPattern(START_PATTERN, Locale.ENGLISH);

	private static final long MIN_INTERVAL = TimeUnit.SECONDS.toMillis(1); // we want to wait at least a second before we check again
	private static final String PATH_SEPARATOR = "/";

	private static final Timer UPDATE_TIMER = new Timer();

	private static final Map<String, Long> LAST_RUNS = new HashMap<>();

	static {
		MARKUP.addAnnotation(ATTACHMENT_ANNOTATION, true);
		MARKUP.addAnnotationContentType(ATTACHMENT_ANNOTATION, new AttachmentType());
		MARKUP.addAnnotation(URL_ANNOTATION);
		MARKUP.addAnnotation(COMPILE, false, "true", "false");
		MARKUP.addAnnotationContentType(URL_ANNOTATION, new URLType());
		MARKUP.addAnnotation(INTERVAL_ANNOTATION);
		TimeStampType timeStampType = new TimeStampType();
		timeStampType.setRenderer((section, user, result) -> {
			result.append(section.getText());
			long sinceLastRun = timeSinceLastRun(Sections.ancestor(section, AttachmentMarkup.class));
			if (sinceLastRun < Long.MAX_VALUE) {
				String timeDisplay;
				if (sinceLastRun < 5000) {
					timeDisplay = "moments";
				}
				else {
					timeDisplay = Stopwatch.getDisplay(sinceLastRun);

				}
				result.appendHtmlElement("span",
						" (last update was " + timeDisplay + " ago)",
						"style", "color: grey");
			}
		});
		MARKUP.addAnnotationContentType(INTERVAL_ANNOTATION, timeStampType);
		MARKUP.addAnnotation(START_ANNOTATION);
		MARKUP.addAnnotation(VERSIONING_ANNOTATION, false, "true", "false");
		MARKUP.addAnnotation(ZIP_ENTRY_ANNOTATION);

	}

	public AttachmentMarkup() {
		this(MARKUP);
	}

	public AttachmentMarkup(DefaultMarkup markup) {
		super(markup);
		addCompileScript(new UpdateTaskRegistrationScript());
	}

	private static class UpdateTask extends TimerTask {

		private final Section<AttachmentMarkup> section;

		private UpdateTask(Section<AttachmentMarkup> section) {
			this.section = section;
		}

		@Override
		public void run() {
			// clean up this task, if its section no longer exists..
			if (!Sections.isLive(section)) {
				this.cancel();
				return;
			}

			performUpdate(section);
		}

	}

	private static void logLastRun(Section<?> section) {
		LAST_RUNS.put(section.getID(), System.currentTimeMillis());
	}

	private static void cleanUpLastRuns() {
		for (Iterator<Map.Entry<String, Long>> iterator = LAST_RUNS.entrySet()
				.iterator(); iterator.hasNext(); ) {
			Map.Entry<String, Long> entry = iterator.next();
			if (Sections.get(entry.getKey()) == null) {
				iterator.remove();
			}
		}
	}

	private static long timeSinceLastRun(Section<?> section) {
		Long lastRun = LAST_RUNS.get(section.getID());
		return lastRun == null ? Long.MAX_VALUE : System.currentTimeMillis() - lastRun;
	}

	private static class UpdateTaskRegistrationScript extends DefaultGlobalScript<AttachmentMarkup> {

		private static final String UPDATE_TASK_KEY = "updateTaskKey";

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<AttachmentMarkup> section) {

			if (section.hasErrorInSubtree()) return;
			if (DefaultMarkupType.getAnnotation(section, INTERVAL_ANNOTATION) == null) return;
			if (DefaultMarkupType.getAnnotation(section, URL_ANNOTATION) == null) return;

			long interval = getInterval(section);

			UpdateTask updateTask = new UpdateTask(section);

			section.storeObject(compiler, UPDATE_TASK_KEY, updateTask);

			UPDATE_TIMER.scheduleAtFixedRate(updateTask, getInitialDelay(section, interval), interval);

		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<AttachmentMarkup> section) {
			UpdateTask updateTask = (UpdateTask) section.removeObject(compiler, UPDATE_TASK_KEY);
			if (updateTask != null) {
				updateTask.cancel();
			}
		}

		private long getInitialDelay(Section<AttachmentMarkup> section, long interval) {

			long timeSinceLastRun = timeSinceLastRun(section);
			if (interval < TimeUnit.HOURS.toMillis(1)) {
				// If we have an interval smaller than one hour, we see it as a
				// delay between executions and start at once. If the updater has run before
				// and was only removed and added again because of a full compilation of the
				// article without change to the updater itself, we continue with
				// desired interval.
				return Math.max(0, interval - timeSinceLastRun);
			}
			else {
				// Otherwise, we see it as a rate and start a the given start delay, taking
				// again into account, if the equal updater has run before.
				return timeSinceLastRun == Long.MAX_VALUE ?
						// updater did not run before, just use the given delay
						getStartDelayFromAnnotation(section) :
						// it did run before, delay for the rest of the interval
						Math.max(0, interval - timeSinceLastRun);

			}

		}

		private long getStartDelayFromAnnotation(Section<AttachmentMarkup> section) {
			LocalDateTime start = LocalDateTime.now();

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

		private long getInterval(Section<AttachmentMarkup> section) {
			Section<TimeStampType> timeStampSection = Sections.successor(section, TimeStampType.class);
			if (timeStampSection == null) return MIN_INTERVAL;
			long timeInMillis = TimeStampType.getTimeInMillis(timeStampSection);
			if (timeInMillis < MIN_INTERVAL) timeInMillis = MIN_INTERVAL;
			return timeInMillis;
		}

	}

	static void performUpdate(Section<AttachmentMarkup> section) {

		logLastRun(section);
		cleanUpLastRuns();

		Section<AttachmentType> attachmentSection = Sections.successor(section, AttachmentType.class);
		Section<URLType> urlSection = Sections.successor(section, URLType.class);

		URL url = URLType.getURL(urlSection);
		if (url == null || attachmentSection == null) {
			return; // nothing to do, error will already be rendered from URLType
		}

		ReentrantLock lock = getLock(section);
		if (!lock.tryLock()) {
			Log.info("Skipped requested updated for attachment '" + Strings.trim(attachmentSection.getText())
					+ "' with resource from URL " + url + ", update already running");
			return;
		}
		String path = AttachmentType.getPath(attachmentSection);
		try {
			String parentName = path.substring(0, path.indexOf(PATH_SEPARATOR));
			String fileName = path.substring(path.indexOf("/") + 1);

			if (path.split("/").length > 2) {
				Messages.storeMessage(section, AttachmentMarkup.class, Messages.error("Unable to update entries in zipped attachments!"));
				return;
			}

			try {
				if (!needsUpdate(attachmentSection, url)) {
					Log.info("Resource at URL " + url + " has not changed, attachment '" + path + "' not updated");
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
								.deleteAttachment(parentName, fileName, "SYSTEM");
					}
					Environment.getInstance()
							.getWikiConnector()
							.storeAttachment(parentName, fileName, "SYSTEM", attachmentStream);
				}
				finally {
					articleManager.commit();
				}

				Log.info("Updated attachment '" + path + "' with resource from URL " + url);
				Messages.clearMessages(section, AttachmentMarkup.class);
				Messages.clearMessages(section, AttachmentMarkup.class);
			}
			catch (Throwable e) { // NOSONAR
				String message = e.getClass().getSimpleName() + " while downloading attachment " + path;
				Messages.storeMessage(section, AttachmentMarkup.class, Messages.error(message + ": " + e.getMessage()));
				Log.severe(message, e);
			}
		}
		finally {
			lock.unlock();
		}
	}

	private static ReentrantLock getLock(Section<AttachmentMarkup> section) {
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

	private static boolean needsUpdate(Section<AttachmentType> attachmentSection, URL url) throws IOException {

		WikiAttachment attachment = AttachmentType.getAttachment(attachmentSection);

		if (attachment == null) return true;

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
