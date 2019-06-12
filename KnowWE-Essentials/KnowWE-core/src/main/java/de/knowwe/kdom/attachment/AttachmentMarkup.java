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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletResponse;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Stopwatch;
import com.denkbares.utils.Streams;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.ServletContextEventListener;
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
import de.knowwe.util.CredentialProvider;
import de.knowwe.util.CredentialProviders;

import static java.nio.charset.StandardCharsets.UTF_8;

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
	private static final String REPLACEMENT = "replacement";
	public static final String COMPILE = "compile";

	private static final String LOCK_KEY = "lock_key";

	private static final String START_PATTERN = "[EEEE ]H:mm";
	private static final DateTimeFormatter START_FORMATTER = DateTimeFormatter.ofPattern(START_PATTERN, Locale.ENGLISH);

	private static final long MIN_INTERVAL = TimeUnit.SECONDS.toMillis(1); // we want to wait at least a second before we check again
	private static final String PATH_SEPARATOR = "/";

	private static final Timer UPDATE_TIMER = new Timer(true);

	private static final Map<String, Long> LAST_RUNS = new HashMap<>();

	private enum State {
		OUTDATED, UP_TO_DATE, UNKNOWN
	}

	static {
		MARKUP.addAnnotation(ATTACHMENT_ANNOTATION, true);
		MARKUP.addAnnotationContentType(ATTACHMENT_ANNOTATION, new AttachmentType());
		MARKUP.addAnnotation(URL_ANNOTATION);
		MARKUP.addAnnotation(COMPILE, false, "true", "false");
		MARKUP.addAnnotationContentType(URL_ANNOTATION, new URLType());
		MARKUP.addAnnotation(INTERVAL_ANNOTATION);
		MARKUP.addAnnotation(REPLACEMENT, false, Pattern.compile(".+->.*"));
		TimeStampType timeStampType = new TimeStampType();
		timeStampType.setRenderer((section, user, result) -> {
			result.append(section.getText());
			long sinceLastRun = timeSinceLastRun(Sections.ancestor(section, AttachmentMarkup.class));
			long sinceLastChange = timeSinceLastChange(Sections.ancestor(section, AttachmentMarkup.class));
			if (sinceLastRun < Long.MAX_VALUE && sinceLastChange < Long.MAX_VALUE) {
				String lastRunDisplay = getDisplay(sinceLastRun);
				String lastChangeDisplay = getDisplay(sinceLastChange);
				result.appendHtmlElement("span",
						" (last update was " + lastRunDisplay + " ago, last change was " + lastChangeDisplay + " ago)",
						"style", "color: grey");
			}
		});
		MARKUP.addAnnotationContentType(INTERVAL_ANNOTATION, timeStampType);
		MARKUP.addAnnotation(START_ANNOTATION);
		MARKUP.addAnnotation(VERSIONING_ANNOTATION, false, "true", "false");
		MARKUP.addAnnotation(ZIP_ENTRY_ANNOTATION);

		ServletContextEventListener.registerOnContextDestroyedTask(servletContextEvent -> {
			Log.info("Shutting down attachment update timer.");
			UPDATE_TIMER.cancel();
		});
	}

	private static String getDisplay(long since) {
		String timeDisplay;
		if (since < 5000) {
			timeDisplay = "moments";
		}
		else {
			timeDisplay = Stopwatch.getDisplay(since);
		}
		return timeDisplay;
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
			// only perform section update if articles are initialized in case we reference articles from this wiki
			if (section.getArticleManager().isInitialized()) {
				performUpdate(section);
			}
		}
	}

	private static void logLastRun(Section<?> section) {
		LAST_RUNS.put(section.getID(), System.currentTimeMillis());
	}

	private static void cleanUpLastRuns() {
		LAST_RUNS.entrySet().removeIf(entry -> Sections.get(entry.getKey()) == null);
	}

	private static long timeSinceLastRun(Section<?> section) {
		if (section == null) return Long.MAX_VALUE;
		Long lastRun = LAST_RUNS.get(section.getID());
		return lastRun == null ? Long.MAX_VALUE : System.currentTimeMillis() - lastRun;
	}

	private static long timeSinceLastChange(Section<?> section) {
		Section<AttachmentType> attachmentSection = Sections.successor(section, AttachmentType.class);
		try {
			WikiAttachment attachment = AttachmentType.getAttachment(attachmentSection);
			if (attachment != null) {
				return System.currentTimeMillis() - attachment.getDate().getTime();
			}
		}
		catch (IOException e) {
			Log.warning("Unable to get last change of attachment...", e);
		}
		return Long.MAX_VALUE;
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
		Messages.clearMessages(section, AttachmentMarkup.class);

		Section<AttachmentType> attachmentSection = Sections.successor(section, AttachmentType.class);
		Section<URLType> urlSection = Sections.successor(section, URLType.class);
		Objects.requireNonNull(urlSection);
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
				State attachmentState = needsUpdate(attachmentSection, url);
				if (attachmentState == State.UP_TO_DATE) {
					Log.fine("Resource at URL " + url + " has not changed, attachment '" + path + "' not updated (based on header info).");
					return;
				}

				final HttpURLConnection connection = openConnection(url);

				int responseCode = connection.getResponseCode();
				if (responseCode != HttpServletResponse.SC_OK) {
					Messages.storeMessage(section, AttachmentMarkup.class, Messages.error("Invalid response code, skipping update: " + responseCode));
					return;
				}
				InputStream connectionStream = getAttachmentStream(section, connection);

				// configure replacements, if applicable
				String[] replacements = DefaultMarkupType.getAnnotations(section, REPLACEMENT);
				if (replacements.length > 0) {
					String connectionString = Strings.readStream(connectionStream);
					for (String replacement : replacements) {
						if (Strings.isBlank(replacement)) continue;
						String[] parsedReplacement = Strings.parseConcat("->", replacement);
						if (parsedReplacement.length < 2) continue;
						try {
							connectionString = connectionString.replaceAll(parsedReplacement[0], parsedReplacement[1]);
						}
						catch (PatternSyntaxException e) {
							connectionString = connectionString.replace(parsedReplacement[0], parsedReplacement[1]);
						}
					}
					connectionStream = new ByteArrayInputStream(connectionString.getBytes(StandardCharsets.UTF_8));
				}

				if (attachmentState == State.UNKNOWN || attachmentState == State.OUTDATED) {
					// if state is unknown, compare contents, so we don't produce unnecessary attachment versions and compiles
					// to be sure that there is actually change, also compare content if we see outdated based on header info...
					WikiAttachment attachment = Environment.getInstance().getWikiConnector().getAttachment(path);
					if (attachment != null) {
						byte[] connectionBytes = Streams.getBytesAndClose(connectionStream);
						byte[] attachmentBytes = Streams.getBytesAndClose(attachment.getInputStream());
						if (Arrays.equals(connectionBytes, attachmentBytes)) {
							Log.fine("Resource at URL " + url + " has not changed, attachment '" + path + "' not updated (based on content comparison).");
							return;
						}
						connectionStream = new ByteArrayInputStream(connectionBytes);
					}
				}

				ArticleManager articleManager = section.getArticleManager();
				articleManager.open();
				try {
					// check for versioning
					boolean versioning = !"false".equalsIgnoreCase(DefaultMarkupType.getAnnotation(section, VERSIONING_ANNOTATION));

					// read and store
					Environment.getInstance().getWikiConnector()
							.storeAttachment(parentName, fileName, "SYSTEM", connectionStream, versioning);
				}
				finally {
					articleManager.commit();
				}

				Log.info("Updated attachment '" + path + "' with resource from URL " + url);
			}
			catch (UnknownHostException e) {
				Log.warning("Unable to reach " + url + " while trying to update attachment " + path);
			}
			catch (Throwable e) { // NOSONAR
				String message = e.getClass().getSimpleName() + " while trying to update attachment " + path;
				Messages.storeMessage(section, AttachmentMarkup.class, Messages.error(message + ": " + e.getMessage()));
				Log.severe(message, e);
			}
		}
		finally {
			lock.unlock();
		}
	}

	private static InputStream getAttachmentStream(Section<AttachmentMarkup> section, HttpURLConnection connection) throws IOException {
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

	private static State needsUpdate(Section<AttachmentType> attachmentSection, URL url) throws IOException {
		WikiAttachment attachment = AttachmentType.getAttachment(attachmentSection);
		if (attachment == null) return State.OUTDATED;

		HttpURLConnection connection = openConnection(url);
		connection.setRequestMethod("HEAD");
		connection.connect();

		Date attachmentSectionDate = Environment.getInstance()
				.getWikiConnector()
				.getLastModifiedDate(attachmentSection.getTitle(), -1);

		LocalDateTime attachmentSectionDateTime = LocalDateTime.ofInstant(attachmentSectionDate.toInstant(), ZoneId.systemDefault());

		LocalDateTime urlDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(connection.getLastModified()),
				ZoneId.systemDefault());

		LocalDateTime attachmentDateTime = LocalDateTime.ofInstant(attachment.getDate().toInstant(),
				ZoneId.systemDefault());

		State state;
		// we only compare seconds, because http header does not transmit milliseconds...
		if (urlDateTime.truncatedTo(ChronoUnit.SECONDS)
				.isAfter(attachmentDateTime.truncatedTo(ChronoUnit.SECONDS))) {
			state = State.OUTDATED;
		}
		else if (urlDateTime.equals(LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()))
				|| attachmentSectionDateTime.isAfter(attachmentDateTime)) {
			// check if urlDateTime is equal to unix time 0
			// if it is, the time was not set (maybe because of server settings)
			state = State.UNKNOWN;
		}
		else {
			state = State.UP_TO_DATE;
		}
		connection.disconnect();
		return state;
	}

	/**
	 * Opens the {@link HttpURLConnection} for the given URL.
	 * <p>
	 * If supplied, username and password will be sent as basic authorization credentials to the server.
	 * Also, loading credentials from the environment is supported by prefixing either the user or password part
	 * with <tt>env+</tt>.
	 * Non-existing environment variables will result in an empty user or password.
	 *
	 * @param url URL to open the connection for
	 * @return HttpURLConnection instance
	 */
	private static HttpURLConnection openConnection(URL url) throws IOException {
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		if (url.getUserInfo() != null) {
			final String[] parts = url.getUserInfo().split(":");
			boolean envLoaded = false;

			// iterate through user and password, load from env, or default to empty String
			for (int i = 0; i <= 1; i++) {
				if (parts[i] != null && parts[i].startsWith("env+")) {
					final String envValue = System.getenv(parts[i].substring(4));
					parts[i] = (envValue != null) ? envValue : "";
					envLoaded = true;
				}
			}

			// use env-loaded values instead of provided ones
			final String basicAuth = envLoaded ? String.join(":", parts) : url.getUserInfo();

			connection.setRequestProperty("Authorization", "Basic " +
					new String(Base64.getEncoder().encode(basicAuth.getBytes(UTF_8)), UTF_8)
			);
		}
		else {
			final String username = CredentialProviders.match(url.toString(), CredentialProvider.Credential.USERNAME);

			if (username != null) {
				final String password = CredentialProviders.match(url.toString(), CredentialProvider.Credential.PASSWORD);
				connection.setRequestProperty("Authorization", "Basic " +
						new String(Base64.getEncoder().encode((username + ":" + password).getBytes(UTF_8)), UTF_8)
				);
			}
		}

		return connection;
	}
}
