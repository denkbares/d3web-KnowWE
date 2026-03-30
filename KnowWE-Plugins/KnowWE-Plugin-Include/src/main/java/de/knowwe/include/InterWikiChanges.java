package de.knowwe.include;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;

final class InterWikiChanges {

	private static final Logger LOGGER = LoggerFactory.getLogger(InterWikiChanges.class);

	static final String SINCE_PARAMETER = "since";
	private static final String NEXT_SINCE = "nextSince";
	private static final String PAGES = "pages";
	private static final String ATTACHMENTS = "attachments";
	private static final String FULL_REFRESH_REQUIRED = "fullRefreshRequired";

	private final Instant nextSince;
	private final Set<String> pages;
	private final Set<String> attachments;
	private final boolean fullRefreshRequired;

	InterWikiChanges(Instant nextSince, Set<String> pages, Set<String> attachments) {
		this(nextSince, pages, attachments, false);
	}

	InterWikiChanges(Instant nextSince, Set<String> pages, Set<String> attachments, boolean fullRefreshRequired) {
		this.nextSince = nextSince;
		this.pages = Set.copyOf(pages);
		this.attachments = Set.copyOf(attachments);
		this.fullRefreshRequired = fullRefreshRequired;
	}

	Instant getNextSince() {
		return nextSince;
	}

	Set<String> getPages() {
		return pages;
	}

	Set<String> getAttachments() {
		return attachments;
	}

	boolean isFullRefreshRequired() {
		return fullRefreshRequired;
	}

	JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put(NEXT_SINCE, nextSince.toString());
		json.put(PAGES, new JSONArray(pages));
		json.put(ATTACHMENTS, new JSONArray(attachments));
		json.put(FULL_REFRESH_REQUIRED, fullRefreshRequired);
		return json;
	}

	static InterWikiChanges fromJson(String jsonString) {
		if (Strings.isBlank(jsonString)) return null;
		if (jsonString.startsWith("<!DOCTYPE html>")) throw new IllegalStateException("Got HTML response instead of JSON, probably because the request wasn't authorized.");
		JSONObject json = new JSONObject(jsonString);
		return new InterWikiChanges(
				Instant.parse(json.getString(NEXT_SINCE)),
				readStringSet(json.optJSONArray(PAGES)),
				readStringSet(json.optJSONArray(ATTACHMENTS)),
				json.optBoolean(FULL_REFRESH_REQUIRED, false));
	}

	static InterWikiChanges fullRefreshRequired() {
		return new InterWikiChanges(Instant.now(), Set.of(), Set.of(), true);
	}

	static InterWikiChanges collect(@Nullable Instant since) throws IOException {
		Stopwatch stopwatch = new Stopwatch();
		Instant nextSince = Instant.now();
		WikiConnector connector = Environment.getInstance().getWikiConnector();

		Set<String> pages = new TreeSet<>();
		for (Article article : KnowWEUtils.getDefaultArticleManager().getArticles()) {
			String title = article.getTitle();
			if (hasChanged(connector.getLastModifiedDate(title, -1), since)) {
				pages.add(title);
			}
		}

		stopwatch.log(LOGGER, "Page scan completed").start();


		Set<String> attachments = new TreeSet<>();
		Collection<WikiAttachment> wikiAttachments = connector.getAttachments();
		for (WikiAttachment attachment : wikiAttachments) {
			if (hasChanged(attachment.getDate(), since)) {
				attachments.add(attachment.getPath());
			}
		}

		stopwatch.log(LOGGER, "Attachment scan completed");

		return new InterWikiChanges(nextSince, pages, attachments);
	}

	private static boolean hasChanged(@Nullable Date lastModified, @Nullable Instant since) {
		if (lastModified == null) return since == null;
		if (since == null) return true;
		return !lastModified.toInstant().isBefore(since);
	}

	private static Set<String> readStringSet(@Nullable JSONArray array) {
		Set<String> values = new TreeSet<>();
		if (array == null) return values;
		for (int i = 0; i < array.length(); i++) {
			values.add(array.getString(i));
		}
		return values;
	}
}
