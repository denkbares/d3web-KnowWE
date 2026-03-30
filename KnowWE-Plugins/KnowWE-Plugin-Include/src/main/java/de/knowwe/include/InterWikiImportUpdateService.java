package de.knowwe.include;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.ServletContextEventListener;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;

final class InterWikiImportUpdateService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InterWikiImportUpdateService.class);
	private static final long POLL_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(5);
	private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
	private static final Map<String, Instant> LAST_SUCCESSFUL_POLLS = new ConcurrentHashMap<>();
	private static final Set<String> REGISTERED_MARKUP_IDS = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private static final ScheduledExecutorService POLLER = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		private final AtomicLong number = new AtomicLong(1);

		@Override
		public Thread newThread(@NotNull Runnable runnable) {
			Thread thread = new Thread(runnable, "InterWikiImportPoller-" + number.getAndIncrement());
			thread.setDaemon(true);
			return thread;
		}
	});

	private InterWikiImportUpdateService() {
	}

	static void initialize() {
		if (!INITIALIZED.compareAndSet(false, true)) return;

		ServletContextEventListener.registerOnContextDestroyedTask(servletContextEvent -> {
			LOGGER.info("Shutting down InterWikiImport update poller.");
			POLLER.shutdownNow();
		});

		POLLER.scheduleWithFixedDelay(InterWikiImportUpdateService::pollAllSources, 0, POLL_INTERVAL_MILLIS,
				TimeUnit.MILLISECONDS);
	}

	static void register(Section<InterWikiImportMarkup> markup) {
		if (markup == null) return;
		REGISTERED_MARKUP_IDS.add(markup.getID());
	}

	static void deregister(Section<InterWikiImportMarkup> markup) {
		if (markup == null) return;
		REGISTERED_MARKUP_IDS.remove(markup.getID());
	}

	static void updateAllNow() {
		initialize();
		POLLER.execute(InterWikiImportUpdateService::pollAllSources);
	}

	private static void pollAllSources() {
		if (!AttachmentUpdateMarkup.isAutoUpdatingActive()) return;

		ArticleManager articleManager = KnowWEUtils.getDefaultArticleManager();
		if (articleManager instanceof DefaultArticleManager defaultArticleManager) {
			defaultArticleManager.awaitInitialization();
		}

		List<Section<InterWikiImportMarkup>> markups = new ArrayList<>();
		for (String markupId : REGISTERED_MARKUP_IDS) {
			Section<?> section = Sections.get(markupId);
			if (!Sections.isLive(section)) {
				REGISTERED_MARKUP_IDS.remove(markupId);
				continue;
			}

			Section<InterWikiImportMarkup> markup = Sections.cast(section, InterWikiImportMarkup.class);
			markups.add(markup);
		}
		if (markups.isEmpty()) return;

		Map<String, List<Section<InterWikiImportMarkup>>> markupsByWiki = markups.stream()
				.collect(Collectors.groupingBy(markup -> InterWikiImportMarkup.normalizeWiki(markup.get().getWiki(markup))));

		for (Map.Entry<String, List<Section<InterWikiImportMarkup>>> entry : markupsByWiki.entrySet()) {
			pollSource(entry.getKey(), entry.getValue());
		}
	}

	private static void pollSource(String wiki, List<Section<InterWikiImportMarkup>> markups) {
		if (wiki == null || wiki.isBlank() || markups.isEmpty()) return;

		Instant since = LAST_SUCCESSFUL_POLLS.get(wiki);
		try {
			Stopwatch stopwatch = new Stopwatch();
			InterWikiChanges changes = requestChanges(wiki, since);
			if (changes.isFullRefreshRequired()) {
				LOGGER.info("Wiki {} does not provide GetWikiChangesSinceAction yet, updating all {} markups of that source.",
						wiki, markups.size());
				markups.parallelStream().forEach(markup -> markup.get().performUpdate(markup, false, true));
				return;
			}
			if (changes.getAttachments().isEmpty() && changes.getPages().isEmpty()) return;
			List<Section<InterWikiImportMarkup>> affectedMarkups = markups.stream()
					.filter(markup -> referencesChangedObject(markup, changes))
					.toList();

			if (!affectedMarkups.isEmpty()) {
				LOGGER.info("Found {} changed InterWikiImport markups for {}.", affectedMarkups.size(), wiki);
				affectedMarkups.parallelStream().forEach(markup -> markup.get().performUpdate(markup, false, true));
			}

			LAST_SUCCESSFUL_POLLS.put(wiki, changes.getNextSince());
			stopwatch.log(LOGGER,
					"Polled InterWikiImport delta for " + wiki + " and matched " + affectedMarkups.size() + " markup(s)");
		}
		catch (Exception e) {
			LOGGER.warn("Failed to poll InterWikiImport changes from {}", wiki, e);
		}
	}

	private static boolean referencesChangedObject(Section<InterWikiImportMarkup> markup, InterWikiChanges changes) {
		String pageName = markup.get().getPageName(markup);
		return changes.getPages().contains(pageName) || changes.getAttachments().contains(pageName);
	}

	private static InterWikiChanges requestChanges(String wiki, Instant since) throws IOException {
		StringBuilder urlBuilder = new StringBuilder(wiki).append(AttachmentUpdateMarkup.getActionFragment())
				.append("/GetWikiChangesSinceAction");
		if (since != null) {
			urlBuilder.append("?")
					.append(InterWikiChanges.SINCE_PARAMETER)
					.append("=")
					.append(Strings.encodeURL(since.toString()));
		}

		URL url = new URL(urlBuilder.toString());
		HttpURLConnection connection = AttachmentUpdateMarkup.openHttpConnection(url);
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();
		if (responseCode == HttpServletResponse.SC_NOT_FOUND) {
			connection.disconnect();
			return InterWikiChanges.fullRefreshRequired();
		}
		if (responseCode != HttpServletResponse.SC_OK) {
			throw new IOException("Unexpected response " + responseCode + " while polling " + url);
		}

		try (InputStream inputStream = connection.getInputStream()) {
			return InterWikiChanges.fromJson(Strings.readStream(inputStream));
		}
		finally {
			connection.disconnect();
		}
	}

}
