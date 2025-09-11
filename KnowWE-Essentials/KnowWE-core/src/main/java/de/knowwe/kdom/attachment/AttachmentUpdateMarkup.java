/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.attachment;

import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import com.denkbares.utils.Streams;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.ServletContextEventListener;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationNameType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.util.CredentialProvider;
import de.knowwe.util.CredentialProviders;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Abstract class for types/markups updating an attachment base upon a url resource
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 06.12.21
 */
public abstract class AttachmentUpdateMarkup extends DefaultMarkupType {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentUpdateMarkup.class);

	public static final String INTERVAL_ANNOTATION = "interval";
	public static final String REPLACEMENT = "replacement";
	public static final String REGEX_REPLACEMENT = "regexReplacement";
	private static final String LOCK_KEY = "lockKey";
	protected static final String PATH_SEPARATOR = "/";
	private static final Map<String, Long> LAST_RUNS = new ConcurrentHashMap<>();

	private static final long MIN_INTERVAL = TimeUnit.SECONDS.toMillis(1); // we want to wait at least a second before we check again
	public static final String KNOWWE_ATTACHMENTS_AUTO_UPDATE_ACTIVE_KEY = "knowwe.attachments.update.auto";

	public enum State {
		OUTDATED, UP_TO_DATE, UNKNOWN
	}

	public AttachmentUpdateMarkup(DefaultMarkup markup) {
		super(markup);
		addCompileScript(new UpdateTaskRegistrationScript());
		addCompileScript(new InactiveWarningScript());
	}

	@Nullable
	public abstract WikiAttachment getWikiAttachment(Section<? extends AttachmentUpdateMarkup> section) throws IOException;

	@Nullable
	public abstract String getWikiAttachmentPath(Section<? extends AttachmentUpdateMarkup> section);

	protected void logLastRun(Section<? extends AttachmentUpdateMarkup> section) {
		String wikiAttachmentPath = section.get().getWikiAttachmentPath(section);
		if (wikiAttachmentPath != null) {
			// we never clean up here, but since it is hard to generate new keys, memory footprint should be negligible
			LAST_RUNS.put(wikiAttachmentPath, System.currentTimeMillis());
		}
	}

	protected long timeSinceLastRun(Section<? extends AttachmentUpdateMarkup> section) {
		if (section == null) return Long.MAX_VALUE;
		String wikiAttachmentPath = section.get().getWikiAttachmentPath(section);
		if (wikiAttachmentPath == null) return Long.MAX_VALUE;
		return LAST_RUNS.getOrDefault(wikiAttachmentPath, Long.MAX_VALUE);
	}

	protected long timeSinceLastChange(Section<? extends AttachmentUpdateMarkup> section) {
		if (section == null) return Long.MAX_VALUE;
		try {
			WikiAttachment attachment = section.get().getWikiAttachment(section);
			if (attachment != null) {
				return System.currentTimeMillis() - attachment.getDate().getTime();
			}
		}
		catch (IOException e) {
			LOGGER.warn("Unable to get last change of attachment...", e);
		}
		return Long.MAX_VALUE;
	}

	protected static String getDisplay(long since) {
		String timeDisplay;
		if (since < 5000) {
			timeDisplay = "moments";
		}
		else if (since <= 60000) {
			// no milliseconds required
			timeDisplay = Stopwatch.getDisplay(since, TimeUnit.MINUTES);
		}
		else {

			timeDisplay = Stopwatch.getDisplay(since);
		}
		return timeDisplay;
	}

	@NotNull
	protected ReentrantLock getLock(Section<? extends AttachmentUpdateMarkup> section) {
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (section) {
			ReentrantLock lock = section.getObject(LOCK_KEY);
			if (lock == null) {
				lock = new ReentrantLock();
				section.storeObject("lockKey", lock);
			}
			return lock;
		}
	}

	private static boolean isAutoUpdatingActive() {
		String wikiProperty = Environment.getInstance()
				.getWikiConnector()
				.getWikiProperty(KNOWWE_ATTACHMENTS_AUTO_UPDATE_ACTIVE_KEY);
		if (wikiProperty == null) wikiProperty = "true";
		return Boolean.parseBoolean(System.getProperty(KNOWWE_ATTACHMENTS_AUTO_UPDATE_ACTIVE_KEY, wikiProperty));
	}

	public void performUpdate(Section<? extends AttachmentUpdateMarkup> section) {
		performUpdate(section, false);
	}

	public void performUpdate(Section<? extends AttachmentUpdateMarkup> section, boolean force) {
		if (section.getArticleManager() == null) return;

		section.get().logLastRun(section);
		Messages.clearMessages(section, getClass());

		String path = section.get().getWikiAttachmentPath(section);
		URL url = getUrl(section);
		if (url == null || path == null) {
			return; // nothing to do, error will already be rendered from URLType
		}

		ReentrantLock lock = getLock(section);
		if (!lock.tryLock()) {
			LOGGER.info("Skipped requested updated for attachment '" + Strings.trim(path)
						+ "' with resource from URL " + url + ", update already running");
			return;
		}
		try {
			String parentName = path.substring(0, path.indexOf(PATH_SEPARATOR));
			String fileName = path.substring(path.indexOf("/") + 1);

			if (path.split("/").length > 2) {
				Messages.storeMessage(section, getClass(), Messages.error("Unable to update entries in zipped attachments!"));
				return;
			}

			try {
				Stopwatch stopwatch = new Stopwatch();
				WikiAttachment attachment = section.get().getWikiAttachment(section);
				State attachmentState = needsUpdate(attachment, url);
				if (!force && attachmentState == AttachmentMarkup.State.UP_TO_DATE) {
					LOGGER.debug("Resource at URL " + url + " has not changed, attachment '" + path + "' not updated (based on header info).");
					return;
				}

				URLConnection connection;
				if ("file".equals(url.getProtocol())) {
					connection = url.openConnection();
				}
				else {
					connection = openHttpConnection(url);

					int responseCode = ((HttpURLConnection) connection).getResponseCode();
					if (responseCode != HttpServletResponse.SC_OK) {
						Messages.storeMessage(section, getClass(), Messages.warning("Invalid response code, skipping update: " + responseCode));
						LOGGER.warn("Invalid response code " + responseCode + ", skipping update: " + url);
						return;
					}
				}

				InputStream connectionStream = getConnectionStream(section, connection);

				connectionStream = applyReplacements(section, connectionStream);

				if (force || attachmentState == AttachmentMarkup.State.UNKNOWN || attachmentState == AttachmentMarkup.State.OUTDATED) {
					// if state is unknown, compare contents, so we don't produce unnecessary attachment versions and compiles
					// to be sure that there is actually change, also compare content if we see outdated based on header info...
					if (attachment != null) {
						byte[] connectionBytes = Streams.getBytesAndClose(connectionStream);
						byte[] attachmentBytes = Streams.getBytesAndClose(attachment.getInputStream());
						if (Arrays.equals(connectionBytes, attachmentBytes)) {
							LOGGER.debug("Resource at URL " + url + " has not changed, attachment '" + path + "' not updated (based on content comparison).");
							return;
						}
						connectionStream = new ByteArrayInputStream(connectionBytes);
					}
				}

				ArticleManager articleManager = section.getArticleManager();
				articleManager.open();
				try {
					// check for versioning
					boolean versioning = section.get().isVersioning(section);

					// read and store
					Environment.getInstance().getWikiConnector()
							.storeAttachment(parentName, fileName, "SYSTEM", connectionStream, versioning);
				}
				finally {
					articleManager.commit();
				}

				stopwatch.log(LOGGER, "Updated attachment '" + path + "' with resource from URL " + url);
			}
			catch (UnknownHostException e) {
				LOGGER.warn("Unable to reach " + url + " while trying to update attachment " + path);
			}
			catch (Throwable e) { // NOSONAR
				String message = e.getClass().getSimpleName() + " while trying to update attachment " + path;
				Messages.storeMessage(section, getClass(), Messages.error(message + ": " + e.getMessage()));
				LOGGER.error(message, e);
			}
		}
		finally {
			lock.unlock();
		}
	}

	@Nullable
	public abstract URL getUrl(Section<? extends AttachmentUpdateMarkup> section);

	protected InputStream applyReplacements(Section<? extends AttachmentUpdateMarkup> section, InputStream connectionStream) {
		if (DefaultMarkupType.getAnnotation(section, REPLACEMENT) != null || DefaultMarkupType.getAnnotation(section, REGEX_REPLACEMENT) != null) {

			String connectionString = Strings.readStream(connectionStream);

			connectionString = applyReplacements(section, connectionString);
			connectionString = applyPackages(section, connectionString);

			connectionStream = new ByteArrayInputStream(connectionString.getBytes(StandardCharsets.UTF_8));
		}
		return connectionStream;
	}

	private static String applyPackages(Section<? extends AttachmentUpdateMarkup> section, String connectionString) {
		@NotNull String[] packageAnnotations = DefaultMarkupType.getAnnotations(section, PackageManager.PACKAGE_ATTRIBUTE_NAME);
		if (packageAnnotations.length > 0) {
			StringBuilder packageAnnotationText = new StringBuilder();
			for (@NotNull String packageAnnotation : packageAnnotations) {
				packageAnnotationText.append("@" + PackageManager.PACKAGE_ATTRIBUTE_NAME + ": ")
						.append(packageAnnotation)
						.append("\n");
			}
			Article tempArticle = Article.createTemporaryArticle(connectionString, section.getTitle() + "_Import", section.getWeb());
			Map<String, String> replacementMap = new HashMap<>();
			for (Section<DefaultMarkupType> markupSection : $(tempArticle).successor(DefaultMarkupType.class)) {

				// clear existing package annotations
				for (Section<AnnotationType> annotationSection : $(markupSection).successor(AnnotationType.class)) {
					if ($(annotationSection).successor(AnnotationNameType.class).anyMatch(
							s -> s.get().getName(s).equals(PackageManager.PACKAGE_ATTRIBUTE_NAME))) {
						replacementMap.put(annotationSection.getID(), "");
					}
				}

				// append new package annotations
				Section<?> closingTag = $(markupSection).children().getLast();
				if (closingTag != null && Strings.trim(closingTag.getText()).equals("%")) {
					replacementMap.put(closingTag.getID(), packageAnnotationText + Strings.trimLeft(closingTag.getText()));
				}
			}

			connectionString = Sections.collectTextAndReplace(tempArticle, replacementMap);
		}
		return connectionString;
	}

	private static String applyReplacements(Section<? extends AttachmentUpdateMarkup> section, String connectionString) {
		for (Section<AnnotationContentType> annotationContent : $(section).successor(AnnotationContentType.class)
				.asList()) {

			String replacement = annotationContent.getText();
			if (Strings.isBlank(replacement)) continue;
			String[] parsedReplacement = Strings.parseConcat("->", replacement);
			if (parsedReplacement.length < 2) continue;

			String sourceText = parsedReplacement[0];
			String targetText = parsedReplacement[1].replace("\\n", "\n");
			if (annotationContent.get().getName(annotationContent).equals(REGEX_REPLACEMENT)) {
				if (Pattern.compile(sourceText).matcher(connectionString).find()) {
					connectionString = connectionString.replaceAll(sourceText, targetText);
					Messages.clearMessages(annotationContent, AttachmentMarkup.class);
				}
				else {
					Messages.storeMessage(annotationContent, AttachmentMarkup.class,
							Messages.info("Replacement regex /" + sourceText
										  + "/ does not match to any text in this attachment."));
				}
			}
			else if (annotationContent.get().getName(annotationContent).equals(REPLACEMENT)) {
				if (connectionString.contains(sourceText)) {
					connectionString = connectionString.replace(sourceText, targetText);
					Messages.clearMessages(annotationContent, AttachmentMarkup.class);
				}
				else {
					Messages.storeMessage(annotationContent, AttachmentMarkup.class,
							Messages.info("Replacement pattern '" + sourceText
										  + "' does not match to any text in this attachment."));
				}
			}
		}
		return connectionString;
	}

	protected boolean isVersioning(Section<? extends AttachmentUpdateMarkup> section) {
		return false;
	}

	protected InputStream getConnectionStream(Section<? extends AttachmentUpdateMarkup> section, URLConnection connection) throws IOException {
		return connection.getInputStream();
	}

	private static State needsUpdate(WikiAttachment attachment, URL url) throws IOException {
		if (attachment == null) return AttachmentMarkup.State.OUTDATED;

		URLConnection connection;
		if (url.getProtocol().equals("file")) {
			connection = url.openConnection();
		}
		else {
			HttpURLConnection httpURLConnection = openHttpConnection(url);
			httpURLConnection.setRequestMethod("HEAD");
			connection = httpURLConnection;
		}
		connection.connect();

		Date attachmentSectionDate = Environment.getInstance()
				.getWikiConnector()
				.getLastModifiedDate(attachment.getParentName(), -1);

		LocalDateTime attachmentSectionDateTime = LocalDateTime.ofInstant(attachmentSectionDate.toInstant(), ZoneId.systemDefault());

		LocalDateTime urlDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(connection.getLastModified()),
				ZoneId.systemDefault());

		LocalDateTime attachmentDateTime = LocalDateTime.ofInstant(attachment.getDate().toInstant(),
				ZoneId.systemDefault());

		State state;
		// we only compare seconds, because http header does not transmit milliseconds...
		if (urlDateTime.truncatedTo(ChronoUnit.SECONDS)
				.isAfter(attachmentDateTime.truncatedTo(ChronoUnit.SECONDS))) {
			state = AttachmentMarkup.State.OUTDATED;
		}
		else if (urlDateTime.equals(LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()))
				 || attachmentSectionDateTime.isAfter(attachmentDateTime)) {
			// check if urlDateTime is equal to unix time 0
			// if it is, the time was not set (maybe because of server settings)
			state = AttachmentMarkup.State.UNKNOWN;
			LOGGER.warn("Unable to get valid lastModified info from http connection, " +
						"cannot assess changes to resource without downloading it:\n" + url);
		}
		else {
			state = AttachmentMarkup.State.UP_TO_DATE;
		}
		if (connection instanceof HttpURLConnection) {
			((HttpURLConnection) connection).disconnect();
		}
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
	private static HttpURLConnection openHttpConnection(URL url) throws IOException {
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
														   new String(Base64.getEncoder()
																   .encode(basicAuth.getBytes(UTF_8)), UTF_8)
			);
		}
		else {
			final String username = CredentialProviders.match(url.toString(), CredentialProvider.Credential.USERNAME);

			if (username != null) {
				final String password = CredentialProviders.match(url.toString(), CredentialProvider.Credential.PASSWORD);
				connection.setRequestProperty("Authorization", "Basic " +
															   new String(Base64.getEncoder()
																	   .encode((username + ":" + password).getBytes(UTF_8)), UTF_8)
				);
			}
		}

		return connection;
	}

	protected long getInitialDelay(Section<AttachmentUpdateMarkup> section, long interval) {

		long timeSinceLastRun = section.get().timeSinceLastRun(section);
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

	protected long getStartDelayFromAnnotation(Section<? extends AttachmentUpdateMarkup> section) {
		return 0; // be default, just start immediately
	}

	protected long getIntervalMillis(Section<? extends AttachmentUpdateMarkup> section) {
		Section<TimeStampType> timeStampSection = Sections.successor(section, TimeStampType.class);
		if (timeStampSection == null) return Long.MAX_VALUE;
		long timeInMillis = TimeStampType.getTimeInMillis(timeStampSection);
		if (timeInMillis < MIN_INTERVAL) timeInMillis = MIN_INTERVAL;
		return timeInMillis;
	}

	private static class UpdateTaskRegistrationScript extends DefaultGlobalCompiler.DefaultGlobalScript<AttachmentUpdateMarkup> {
		private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTaskRegistrationScript.class);

		private static final String UPDATE_TASK_KEY = "updateTaskKey";
		private static final Timer UPDATE_TIMER = new Timer(true);

		static {
			ServletContextEventListener.registerOnContextDestroyedTask(servletContextEvent -> {
				LOGGER.info("Shutting down attachment update timer.");
				UPDATE_TIMER.cancel();
			});
		}

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<AttachmentUpdateMarkup> section) {

			if (section.getArticleManager() == null) return;
			if (section.get().getUrl(section) == null) return;
			long interval = section.get().getIntervalMillis(section);
			if (interval == Long.MAX_VALUE) return;

			UpdateTask updateTask = new UpdateTask(section);
			section.storeObject(compiler, UPDATE_TASK_KEY, updateTask);

			UPDATE_TIMER.scheduleAtFixedRate(updateTask, section.get().getInitialDelay(section, interval), interval);
		}

		@Override
		public void destroy(DefaultGlobalCompiler compiler, Section<AttachmentUpdateMarkup> section) {
			UpdateTask updateTask = section.removeObject(compiler, UPDATE_TASK_KEY);
			if (updateTask != null) {
				updateTask.cancel();
			}
		}
	}

	private static class UpdateTask extends TimerTask {

		private final Section<AttachmentUpdateMarkup> section;

		private UpdateTask(Section<AttachmentUpdateMarkup> section) {
			this.section = section;
		}

		@Override
		public void run() {
			// clean up this task, if its section no longer exists..
			if (!Sections.isLive(section)) {
				this.cancel();
				return;
			}

			if (!isAutoUpdatingActive()) return;

			// only perform section update if articles are initialized in case we reference articles from this wiki
			ArticleManager articleManager = section.getArticleManager();
			if (articleManager instanceof DefaultArticleManager) {
				((DefaultArticleManager) articleManager).awaitInitialization();
				section.get().performUpdate(section);
			}
		}
	}

	private static class InactiveWarningScript extends DefaultGlobalCompiler.DefaultGlobalScript<AttachmentUpdateMarkup> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<AttachmentUpdateMarkup> section) throws CompilerMessage {
			if (!isAutoUpdatingActive()) {
				Messages.storeMessage(section, this.getClass(), Messages.info("Auto updating is deactivated using the system property " + KNOWWE_ATTACHMENTS_AUTO_UPDATE_ACTIVE_KEY + "=true/false"));
			}
		}
	}
}
