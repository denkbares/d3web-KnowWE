/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.change;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.Event;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.change.ArticleChangeException.Reason;
import de.knowwe.core.change.ArticleChangeSet.Change;
import de.knowwe.core.change.ArticleChangeSet.Outcome;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.event.ArticleDeletedEvent;
import de.knowwe.event.ArticleRenamedEvent;
import de.knowwe.event.ArticleUpdateEvent;

/**
 * Applies one {@link ArticleChangeSet} and coordinates three layers that each have their own transaction.
 * <ul>
 *   <li>Persistence groups all file writes into one versioning commit via the page transaction of the connector.</li>
 *   <li>The article manager registers all articles in one frame, so they are compiled in a single pass.</li>
 *   <li>Events are fired only after persistence committed, so listeners never see a change that is rolled back.</li>
 * </ul>
 * Persistence deletes are deferred until after the article manager committed. JSPWiki's page delete listener in
 * {@code KnowWEPlugin} forks a thread that deletes the article from the article manager again unless it is already
 * gone, and that thread must not wait for or interfere with the open frame.
 */
final class ArticleChangeTransaction {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArticleChangeTransaction.class);

	private final ArticleChangeSet changes;
	private final Environment environment;
	private final String author;
	@Nullable
	private final UserContext context;
	private final String changeNote;
	private final List<ArticleChangeGuard> guards;

	private final WikiConnector connector;
	private final ArticleManager articleManager;
	private final List<Step> steps = new ArrayList<>();
	private final List<String> deferredDeletes = new ArrayList<>();
	private final List<Event> events = new ArrayList<>();

	/**
	 * A change resolved against the current wiki, with the stored spelling of existing titles.
	 */
	private record Step(
			Outcome.Kind kind,
			String title,
			@Nullable String newTitle,
			@Nullable String text,
			@Nullable Article existing,
			@Nullable Integer oldVersion
	) {
		String label() {
			return newTitle == null ? title : title + " -> " + newTitle;
		}
	}

	ArticleChangeTransaction(
			@NotNull ArticleChangeSet changes,
			@NotNull Environment environment,
			@NotNull String author,
			@Nullable UserContext context,
			@NotNull String changeNote,
			@NotNull List<ArticleChangeGuard> guards
	) {
		this.changes = changes;
		this.environment = environment;
		this.author = author;
		this.context = context;
		this.changeNote = changeNote;
		this.guards = guards;
		this.connector = environment.getWikiConnector();
		this.articleManager = environment.getArticleManager();
	}

	@NotNull
	ArticleChangeSet.Result run() throws ArticleChangeException {
		if (changes.isEmpty()) {
			// no empty versioning commit
			return new ArticleChangeSet.Result(List.of());
		}
		for (ArticleChangeGuard guard : guards) {
			guard.check(environment, author, changes);
		}

		// The page transaction is acquired first and released last, in every outcome. The frame is acquired inside the
		// guarded region since open() can fail while it waits for a running compilation.
		connector.openPageTransaction(author);
		boolean frameOpen = false;
		Throwable primary = null;
		try {
			articleManager.open();
			frameOpen = true;
			// existence and versions are checked inside the frame, so no other registration can interleave
			for (Change change : changes.changes()) {
				steps.add(resolve(change));
			}
			for (Step step : steps) {
				apply(step);
			}
		}
		catch (Throwable t) {
			primary = t;
		}

		if (primary != null) {
			// ArticleManager.rollback() expects the files to be restored already
			try {
				connector.rollbackPageTransaction(author);
			}
			catch (Throwable t) {
				primary = combine(primary, t);
			}
			if (frameOpen) {
				try {
					articleManager.rollback();
				}
				catch (Throwable t) {
					primary = combine(primary, t);
				}
			}
			throw failure(primary);
		}

		// Once the frame is committed its rollback snapshot is gone. A later persistence failure leaves the compiled
		// articles ahead of the files, which only a refresh from persistence repairs.
		boolean compiled = true;
		try {
			articleManager.commit();
		}
		catch (Throwable t) {
			compiled = false;
			primary = new ArticleChangeException(Reason.FAILED, null, "Could not compile the changes", t);
		}
		try {
			finishPersistence(compiled);
		}
		catch (Throwable t) {
			primary = combine(primary, t);
		}
		if (primary != null) throw failure(primary);

		return notifyCommitted();
	}

	private Step resolve(Change change) throws ArticleChangeException {
		Article existing = articleManager.getArticle(change.title());
		String title = existing == null ? change.title() : existing.getTitle();
		Outcome.Kind kind = switch (change.kind()) {
			case CREATE -> {
				if (existing != null) throw conflict(title, "Article \"" + title + "\" already exists");
				yield Outcome.Kind.CREATED;
			}
			case WRITE -> existing == null ? Outcome.Kind.CREATED : Outcome.Kind.UPDATED;
			case UPDATE -> {
				requireExisting(existing, title);
				yield Outcome.Kind.UPDATED;
			}
			case RENAME -> {
				requireExisting(existing, title);
				if (articleManager.getArticle(change.newTitle()) != null) {
					throw conflict(change.newTitle(), "Article \"" + change.newTitle() + "\" already exists");
				}
				yield Outcome.Kind.RENAMED;
			}
			case DELETE -> {
				requireExisting(existing, title);
				yield Outcome.Kind.DELETED;
			}
		};
		if (context != null) {
			checkPermissions(context, kind, title, change.newTitle());
		}
		Integer oldVersion = existing == null ? null : connector.getVersion(title);
		if (changes.hasExpectedVersion(title) && !Objects.equals(changes.expectedVersion(title), oldVersion)) {
			throw conflict(title, "Article \"" + title + "\" was changed in the meantime");
		}
		return new Step(kind, title, change.newTitle(), change.text(), existing, oldVersion);
	}

	private static void requireExisting(@Nullable Article existing, String title) throws ArticleChangeException {
		if (existing == null) throw conflict(title, "Article \"" + title + "\" does not exist");
	}

	private void checkPermissions(UserContext context, Outcome.Kind kind, String title, @Nullable String newTitle) throws ArticleChangeException {
		switch (kind) {
			case CREATED -> requireCreate(context, title);
			case UPDATED -> {
				requireEdit(context, title);
				requireUnlocked(title);
			}
			case RENAMED -> {
				requireDelete(context, title);
				requireUnlocked(title);
				requireCreate(context, Objects.requireNonNull(newTitle));
			}
			case DELETED -> {
				requireDelete(context, title);
				requireUnlocked(title);
			}
		}
	}

	private void requireCreate(UserContext context, String title) throws ArticleChangeException {
		if (!connector.userCanCreateArticles(context)) {
			throw new ArticleChangeException(Reason.FORBIDDEN, title, "No permission to create articles");
		}
		requireEdit(context, title);
	}

	private void requireEdit(UserContext context, String title) throws ArticleChangeException {
		if (!connector.userCanEditArticle(title, context)) {
			throw new ArticleChangeException(Reason.FORBIDDEN, title, "No permission to edit article \"" + title + "\"");
		}
	}

	private void requireDelete(UserContext context, String title) throws ArticleChangeException {
		if (!connector.userCanDeleteArticle(title, context)) {
			throw new ArticleChangeException(Reason.FORBIDDEN, title, "No permission to delete article \"" + title + "\"");
		}
	}

	private void requireUnlocked(String title) throws ArticleChangeException {
		if (connector.isArticleLocked(title) && !connector.isArticleLockedCurrentUser(title, author)) {
			throw new ArticleChangeException(Reason.LOCKED, title, "Article \"" + title + "\" is edited by another user");
		}
	}

	private static ArticleChangeException conflict(String title, String message) {
		return new ArticleChangeException(Reason.CONFLICT, title, message);
	}

	private void apply(Step step) throws ArticleChangeException {
		try {
			switch (step.kind()) {
				case CREATED -> create(step.title(), step.text());
				case UPDATED -> {
					String text = Objects.requireNonNull(step.text());
					// Without a request JSPWiki's KnowWEPlugin.postSave does not update the article itself, which would
					// fire an ArticleUpdateEvent before the commit. The article is registered and announced here instead.
					if (!connector.writeArticleToWikiPersistence(step.title(), text, author, changeNote)) {
						throw new IOException("The wiki did not save the article");
					}
					articleManager.registerArticle(step.title(), text);
					events.add(new ArticleUpdateEvent(step.title(), author));
				}
				case RENAMED -> rename(step);
				case DELETED -> remove(step);
			}
		}
		catch (Exception e) {
			throw new ArticleChangeException(Reason.FAILED, step.title(), "Could not apply change to \"" + step.label() + "\"", e);
		}
	}

	private void create(String title, @Nullable String text) throws IOException {
		String content = text == null ? "" : text;
		if (connector.createArticle(title, author, content, changeNote) == null) {
			throw new IOException("The wiki did not create the article");
		}
		articleManager.registerArticle(title, content);
		events.add(new ArticleUpdateEvent(title, author));
	}

	/**
	 * Renames the page in persistence right away, unlike deletes. The page rename listener in {@code KnowWEPlugin}
	 * waits for the frame and then finds the article manager already up to date, or the page back at its old title.
	 */
	private void rename(Step step) throws IOException {
		String newTitle = Objects.requireNonNull(step.newTitle());
		Article existing = Objects.requireNonNull(step.existing());
		Collection<String> referrers = connector.renameArticle(step.title(), newTitle, author);
		String text = step.text();
		if (text == null) {
			// the renamed page may link to itself, so its text can have changed as well
			String persisted = connector.getArticleText(newTitle);
			text = persisted == null ? existing.getText() : persisted;
		}
		else if (!connector.writeArticleToWikiPersistence(newTitle, text, author, changeNote)) {
			throw new IOException("The wiki did not save the renamed article");
		}
		// remove and register within the same frame, so the compilers never see both articles at once
		articleManager.deleteArticle(step.title());
		Article renamed = articleManager.registerArticle(newTitle, text);
		events.add(new ArticleRenamedEvent(existing, renamed));
		events.add(new ArticleUpdateEvent(newTitle, author));
		for (String referrer : referrers) {
			String referrerText = connector.getArticleText(referrer);
			if (referrerText == null || articleManager.getArticle(referrer) == null) continue;
			articleManager.registerArticle(referrer, referrerText);
			events.add(new ArticleUpdateEvent(referrer, author));
		}
	}

	private void remove(Step step) {
		Article existing = Objects.requireNonNull(step.existing());
		articleManager.deleteArticle(step.title());
		deferredDeletes.add(step.title());
		events.add(new ArticleDeletedEvent(existing));
	}

	private void finishPersistence(boolean compiled) throws ArticleChangeException {
		if (!compiled) {
			connector.rollbackPageTransaction(author);
			return;
		}
		try {
			for (String title : deferredDeletes) {
				connector.deleteArticle(title, author);
			}
			connector.commitPageTransaction(author, changeNote);
		}
		catch (IOException | RuntimeException e) {
			ArticleChangeException failure = new ArticleChangeException(Reason.FAILED, null, "Could not persist the changes", e);
			try {
				connector.rollbackPageTransaction(author);
			}
			catch (RuntimeException rollbackFailure) {
				failure.addSuppressed(rollbackFailure);
			}
			throw failure;
		}
	}

	/**
	 * Reads the committed versions and notifies listeners. A failing listener must not turn the committed change into
	 * a reported failure.
	 */
	private ArticleChangeSet.Result notifyCommitted() {
		Map<String, Integer> latest = new HashMap<>();
		List<Outcome> outcomes = new ArrayList<>(steps.size());
		for (Step step : steps) {
			Integer newVersion = null;
			if (step.kind() != Outcome.Kind.DELETED) {
				int version = latestVersion(latest, step.newTitle() == null ? step.title() : step.newTitle());
				newVersion = version < 0 ? null : version;
			}
			outcomes.add(new Outcome(step.kind(), step.title(), step.newTitle(), step.oldVersion(), newVersion));
		}
		for (Event event : events) {
			try {
				if (event instanceof ArticleUpdateEvent update) {
					update.setVersion(new ArticleUpdateEvent.Version(latestVersion(latest, update.getTitle())));
				}
				environment.getEventManager().fireEvent(event);
			}
			catch (RuntimeException e) {
				LOGGER.warn("Listener for {} failed after the change was committed", event.getClass()
						.getSimpleName(), e);
			}
		}
		return new ArticleChangeSet.Result(outcomes);
	}

	private int latestVersion(Map<String, Integer> cache, String title) {
		return cache.computeIfAbsent(title, key -> {
			try {
				return environment.getLatestVersionNumber(key);
			}
			catch (RuntimeException e) {
				LOGGER.warn("Could not read the version of {}", key, e);
				return -1;
			}
		});
	}

	private static Throwable combine(@Nullable Throwable primary, Throwable next) {
		if (primary == null) return next;
		primary.addSuppressed(next);
		return primary;
	}

	private static ArticleChangeException failure(Throwable t) {
		if (t instanceof ArticleChangeException e) return e;
		if (t instanceof Error e) throw e;
		return new ArticleChangeException(Reason.FAILED, null, "Could not apply the changes", t);
	}
}
