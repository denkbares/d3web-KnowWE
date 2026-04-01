package de.knowwe.include;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

import org.apache.wiki.util.CsrfProtectionAllowList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.action.Action;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.ServletContextEventListener;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.ActionAllowListChecker;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;

public final class InterWikiImportUpdateService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InterWikiImportUpdateService.class);

	private final long pollIntervalMillis = TimeUnit.MINUTES.toMillis(10);
	private final AtomicBoolean initialized = new AtomicBoolean(false);
	private final Set<String> registeredMarkupIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		private final AtomicLong number = new AtomicLong(1);

		@Override
		public Thread newThread(@NotNull Runnable runnable) {
			Thread thread = new Thread(runnable, "InterWikiImportPoller-" + number.getAndIncrement());
			thread.setDaemon(true);
			return thread;
		}
	});

	private record ImportInfo(String sectionId, String wiki, String page, @Nullable String sectionHeading,
	                          @Nullable Instant latestChange) {
	}

	private record PollResult(List<InterWikiChanges.Update> updates, boolean notAuthorized, boolean failed) {
		private PollResult {
			updates = List.copyOf(updates);
		}
	}

	public void initialize() {
		if (!initialized.compareAndSet(false, true)) return;
		ServletContextEventListener.registerOnContextDestroyedTask(servletContextEvent -> {
			LOGGER.info("Shutting down InterWikiImport update poller.");
			poller.shutdownNow();
		});
		CsrfProtectionAllowList.register(new ActionAllowListChecker(GetWikiChangesSinceAction.class));

		poller.scheduleWithFixedDelay(this::pollAllSources, 0, pollIntervalMillis,
				TimeUnit.MILLISECONDS);
	}

	public void register(Section<InterWikiImportMarkup> markup) {
		if (markup == null) return;
		registeredMarkupIds.add(markup.getID());
	}

	public void deregister(Section<InterWikiImportMarkup> markup) {
		if (markup == null) return;
		registeredMarkupIds.remove(markup.getID());
	}

	public long getPollIntervalMillis() {
		return pollIntervalMillis;
	}

	private void pollAllSources() {
		if (!AttachmentUpdateMarkup.isAutoUpdatingActive()) return;

		ArticleManager articleManager = KnowWEUtils.getDefaultArticleManager();
		if (articleManager instanceof DefaultArticleManager defaultArticleManager) {
			defaultArticleManager.awaitInitialization();
		}

		List<ImportInfo> snapshots = getImportSnapshots();
		if (snapshots.isEmpty()) return;

		Map<String, List<ImportInfo>> markupsByWiki = snapshots.stream()
				.collect(Collectors.groupingBy(ImportInfo::wiki));

		int updatedImports = 0;
		int notAuthorizedSources = 0;
		int failedSources = 0;
		List<InterWikiChanges.Update> updates = new ArrayList<>();

		articleManager.open();
		try {
			for (Map.Entry<String, List<ImportInfo>> entry : markupsByWiki.entrySet()) {
				PollResult result = pollSource(entry.getKey(), entry.getValue());
				updates.addAll(result.updates());
				if (result.notAuthorized()) notAuthorizedSources++;
				if (result.failed()) failedSources++;
			}

			if (!updates.isEmpty()) {
				applyLatestChangeAnnotations(updates);
				for (InterWikiChanges.Update update : updates) {
					updateAttachment(update.requestingSectionId(), update.sourceText());
				}
			}
			updatedImports = updates.size();
		}
		finally {
			articleManager.commit();
		}

		StringBuilder summary = new StringBuilder("InterWikiImport polling finished for ")
				.append(markupsByWiki.size())
				.append(" source wikis and ")
				.append(snapshots.size())
				.append(" registered imports: ")
				.append(updatedImports)
				.append(" imports updated");
		if (notAuthorizedSources > 0) {
			summary.append(", ").append(notAuthorizedSources).append(" sources were not authorized");
		}
		if (failedSources > 0) {
			summary.append(", ").append(failedSources).append(" sources failed");
		}
		summary.append('.');
		LOGGER.info(summary.toString());
	}

	private List<ImportInfo> getImportSnapshots() {
		List<ImportInfo> snapshots = new ArrayList<>();
		for (String markupId : registeredMarkupIds) {
			Section<InterWikiImportMarkup> markup = Sections.get(markupId, InterWikiImportMarkup.class);
			if (!Sections.isLive(markup)) {
				registeredMarkupIds.remove(markupId);
				continue;
			}

			snapshots.add(new ImportInfo(
					markup.getID(),
					InterWikiImportMarkup.normalizeWiki(markup.get().getWiki(markup)),
					markup.get().getPageName(markup),
					markup.get().getSectionName(markup),
					markup.get().getLatestChange(markup)));
		}
		return snapshots;
	}

	private PollResult pollSource(String wiki, List<ImportInfo> snapshots) {
		if (wiki == null || wiki.isBlank() || snapshots.isEmpty()) {
			return new PollResult(List.of(), false, false);
		}

		try {
			LOGGER.info("Starting to poll InterWikiImport changes from {} for {} registered imports.", wiki, snapshots.size());
			Stopwatch stopwatch = new Stopwatch();
			InterWikiChanges changes = requestChanges(wiki, snapshots);
			if (changes == null) return new PollResult(List.of(), false, false);

			if (changes.getStatus() == InterWikiChanges.Status.not_authorized) {
				LOGGER.warn("Not authorized to poll InterWikiImport changes from {}.", wiki);
				return new PollResult(List.of(), true, false);
			}

			if (changes.getUpdates().isEmpty()) return new PollResult(List.of(), false, false);
			LOGGER.info("Found {} changed InterWikiImport markups for {}.", changes.getUpdates().size(), wiki);
			stopwatch.log(LOGGER, "InterWikiImport markup updates completed for " + changes.getUpdates()
					.size() + " markups");
			return new PollResult(changes.getUpdates(), false, false);
		}
		catch (Exception e) {
			LOGGER.warn("Failed to poll InterWikiImport changes from {}", wiki, e);
			return new PollResult(List.of(), false, true);
		}
	}

	private void applyLatestChangeAnnotations(List<InterWikiChanges.Update> updates) {
		Map<String, String> replacements = new LinkedHashMap<>();
		for (InterWikiChanges.Update update : updates) {
			Section<InterWikiImportMarkup> markup = Sections.get(update.requestingSectionId(), InterWikiImportMarkup.class);
			if (!Sections.isLive(markup)) {
				registeredMarkupIds.remove(update.requestingSectionId());
				continue;
			}
			markup.get().collectLatestChangeReplacement(markup, update.sourceLatestChange(), replacements);
		}
		if (!replacements.isEmpty()) {
			Sections.replaceAsSystem(replacements, "Update InterWikiImport latestChange after change from remote wiki");
		}
	}

	private void updateAttachment(String sectionId, @Nullable String sourceText) {
		Section<InterWikiImportMarkup> markup = Sections.get(sectionId, InterWikiImportMarkup.class);
		if (!Sections.isLive(markup)) {
			registeredMarkupIds.remove(sectionId);
			return;
		}

		if (sourceText != null) {
			markup.get().updateAttachmentWithSourceText(markup, sourceText);
		}
	}

	private InterWikiChanges requestChanges(String wiki, List<ImportInfo> snapshots) throws IOException {
		List<InterWikiChanges.RequestedImport> requestedImports = snapshots.stream()
				.map(snapshot -> new InterWikiChanges.RequestedImport(
						snapshot.sectionId(),
						snapshot.page(),
						snapshot.sectionHeading(),
						snapshot.latestChange()))
				.toList();

		String urlString = wiki + AttachmentUpdateMarkup.getActionFragment() + "/GetWikiChangesSinceAction";
		URL url = new URL(urlString);
		HttpURLConnection connection = AttachmentUpdateMarkup.openHttpConnection(url);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", Action.JSON);
		connection.setDoOutput(true);

		byte[] requestBytes = InterWikiChanges.toRequestJson(requestedImports).getBytes(StandardCharsets.UTF_8);
		// should we optimize the request size?
		LOGGER.info("Polling InterWikiImport changes from {} with request payload of {} kB.", wiki,
				String.format("%.1f", requestBytes.length / 1024.0));
		try (OutputStream outputStream = connection.getOutputStream()) {
			outputStream.write(requestBytes);
			outputStream.flush();
		}

		int responseCode = connection.getResponseCode();
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
