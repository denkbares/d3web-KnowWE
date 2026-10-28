/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.knowwe.core.Environment;
import de.knowwe.core.user.UserContext;
import de.knowwe.plugin.Plugins;

/**
 * A set of article changes that is applied to the wiki as one change. Either all changes are applied or none.
 * <p>
 * The changes are persisted as a single versioning commit where the wiki supports it and compiled in a single pass.
 * Listeners are notified only after the change was persisted. Each article may be touched by at most one change of the
 * set, and the changes are applied in the order they were added.
 * <p>
 * Optional version preconditions make the set fail with {@link ArticleChangeException.Reason#CONFLICT} instead of
 * overwriting an article that was changed concurrently. All preconditions are checked before anything is written.
 * Every registered {@link ArticleChangeGuard} is consulted before the set is applied.
 * <pre>
 * ArticleChangeSet.builder()
 *     .write("Pump", pumpText).expectVersion("Pump", 3)
 *     .create("Pump Service", serviceText)
 *     .build()
 *     .apply(context, "Add pump service");
 * </pre>
 */
public final class ArticleChangeSet {

	private static final ArticleChangeSet EMPTY = new ArticleChangeSet(List.of(), Map.of());

	private final List<Change> changes;
	private final Map<String, Integer> expectedVersions;

	private ArticleChangeSet(List<Change> changes, Map<String, Integer> expectedVersions) {
		this.changes = changes;
		this.expectedVersions = expectedVersions;
	}

	@NotNull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * The changes in application order.
	 */
	@NotNull
	public List<Change> changes() {
		return changes;
	}

	public boolean isEmpty() {
		return changes.isEmpty();
	}

	/**
	 * The titles of all articles this set touches, including the old and the new title of renamed articles.
	 */
	@NotNull
	public Set<String> titles() {
		Set<String> titles = new LinkedHashSet<>();
		for (Change change : changes) {
			titles.add(change.title());
			if (change.newTitle() != null) titles.add(change.newTitle());
		}
		return Collections.unmodifiableSet(titles);
	}

	boolean hasExpectedVersion(@NotNull String title) {
		return expectedVersions.containsKey(key(title));
	}

	@Nullable
	Integer expectedVersion(@NotNull String title) {
		return expectedVersions.get(key(title));
	}

	/**
	 * Applies the changes in the name of the given user. Fails without changing anything if the user lacks the
	 * permission to edit, create or delete one of the articles, if another user holds the edit lock of one of them or
	 * if a guard or precondition rejects the set.
	 *
	 * @param context    the user the changes are applied for
	 * @param changeNote the note to record in the history of the articles
	 * @return what was changed, empty if this set is empty
	 * @throws ArticleChangeException if the set was rejected or could not be applied
	 */
	@NotNull
	public Result apply(@NotNull UserContext context, @NotNull String changeNote) throws ArticleChangeException {
		return new ArticleChangeTransaction(
				this,
				context.getEnvironment(),
				context.getUserName(),
				context,
				changeNote,
				Plugins.getArticleChangeGuards()
		).run();
	}

	/**
	 * Applies the changes in the name of the given author without checking wiki permissions or edit locks. Use this
	 * only if the caller has authorized the changes itself. Guards and preconditions are still checked.
	 *
	 * @param environment the wiki to apply the changes to
	 * @param author      the name of the user the changes are attributed to
	 * @param changeNote  the note to record in the history of the articles
	 * @return what was changed, empty if this set is empty
	 * @throws ArticleChangeException if the set was rejected or could not be applied
	 */
	@NotNull
	public Result applyAs(@NotNull Environment environment, @NotNull String author, @NotNull String changeNote) throws ArticleChangeException {
		return new ArticleChangeTransaction(
				this,
				environment,
				author,
				null,
				changeNote,
				Plugins.getArticleChangeGuards()
		).run();
	}

	static String key(String title) {
		return title.toLowerCase(Locale.ROOT);
	}

	/**
	 * One requested change of a single article.
	 *
	 * @param kind     what to do with the article
	 * @param title    the article to change, the old title of a renamed article
	 * @param newTitle the new title of a renamed article, null otherwise
	 * @param text     the new content, null for a deletion or for a rename that keeps the content
	 */
	public record Change(@NotNull Kind kind, @NotNull String title, @Nullable String newTitle, @Nullable String text) {

		public enum Kind {
			/**
			 * creates a new article, fails if it already exists
			 */
			CREATE,
			/**
			 * replaces the content of an existing article, fails if it does not exist
			 */
			UPDATE,
			/**
			 * creates the article or replaces its content
			 */
			WRITE,
			/**
			 * moves an existing article with its history and attachments to a new title that must not exist yet and
			 * updates the links in referring articles
			 */
			RENAME,
			/**
			 * deletes an existing article
			 */
			DELETE
		}
	}

	/**
	 * What happened to a single article.
	 *
	 * @param kind       how the article was changed
	 * @param title      the changed article, the old title of a renamed article
	 * @param newTitle   the new title of a renamed article, null otherwise
	 * @param oldVersion the version before the change, null for a created article
	 * @param newVersion the version after the change, null for a deleted article or if the wiki does not provide it
	 */
	public record Outcome(
			@NotNull Kind kind,
			@NotNull String title,
			@Nullable String newTitle,
			@Nullable Integer oldVersion,
			@Nullable Integer newVersion
	) {

		public enum Kind {CREATED, UPDATED, RENAMED, DELETED}

		/**
		 * The title the article has after the change.
		 */
		@NotNull
		public String currentTitle() {
			return newTitle == null ? title : newTitle;
		}
	}

	/**
	 * What an applied change set changed, in application order.
	 */
	public record Result(@NotNull List<Outcome> outcomes) {

		public Result {
			outcomes = List.copyOf(outcomes);
		}

		@NotNull
		public List<String> created() {
			return titles(Outcome.Kind.CREATED);
		}

		@NotNull
		public List<String> updated() {
			return titles(Outcome.Kind.UPDATED);
		}

		@NotNull
		public List<String> deleted() {
			return titles(Outcome.Kind.DELETED);
		}

		/**
		 * The renamed articles, from old to new title.
		 */
		@NotNull
		public Map<String, String> renamed() {
			Map<String, String> renamed = new LinkedHashMap<>();
			for (Outcome outcome : outcomes) {
				if (outcome.kind() == Outcome.Kind.RENAMED) renamed.put(outcome.title(), outcome.newTitle());
			}
			return Collections.unmodifiableMap(renamed);
		}

		private List<String> titles(Outcome.Kind kind) {
			return outcomes.stream().filter(outcome -> outcome.kind() == kind).map(Outcome::title).toList();
		}
	}

	/**
	 * Collects the changes of a set. Adding a second change for an article that is already touched fails.
	 */
	public static final class Builder {

		private final List<Change> changes = new ArrayList<>();
		private final Map<String, String> touched = new HashMap<>();
		private final Map<String, Integer> expectedVersions = new LinkedHashMap<>();

		private Builder() {
		}

		@NotNull
		public Builder create(@NotNull String title, @NotNull String text) {
			return add(new Change(Change.Kind.CREATE, title, null, Objects.requireNonNull(text)));
		}

		@NotNull
		public Builder update(@NotNull String title, @NotNull String text) {
			return add(new Change(Change.Kind.UPDATE, title, null, Objects.requireNonNull(text)));
		}

		@NotNull
		public Builder write(@NotNull String title, @NotNull String text) {
			return add(new Change(Change.Kind.WRITE, title, null, Objects.requireNonNull(text)));
		}

		/**
		 * Renames the article. Its history and attachments move along, and links in referring articles are updated as
		 * part of the same change.
		 */
		@NotNull
		public Builder rename(@NotNull String title, @NotNull String newTitle) {
			return add(new Change(Change.Kind.RENAME, title, requireTitle(newTitle), null));
		}

		/**
		 * Renames the article and replaces its content in the same step.
		 */
		@NotNull
		public Builder rename(@NotNull String title, @NotNull String newTitle, @NotNull String text) {
			return add(new Change(Change.Kind.RENAME, title, requireTitle(newTitle), Objects.requireNonNull(text)));
		}

		@NotNull
		public Builder delete(@NotNull String title) {
			return add(new Change(Change.Kind.DELETE, title, null, null));
		}

		/**
		 * Declares the version the article must have when the set is applied, null if it must not exist. A rename
		 * declares the version of its old title.
		 */
		@NotNull
		public Builder expectVersion(@NotNull String title, @Nullable Integer version) {
			expectedVersions.put(key(requireTitle(title)), version);
			return this;
		}

		@NotNull
		public ArticleChangeSet build() {
			Set<String> sources = new HashSet<>();
			changes.forEach(change -> sources.add(key(change.title())));
			for (String title : expectedVersions.keySet()) {
				if (!sources.contains(title)) {
					throw new IllegalStateException("Version expected for an article that is not changed " + title);
				}
			}
			if (changes.isEmpty()) return EMPTY;
			return new ArticleChangeSet(List.copyOf(changes), Collections.unmodifiableMap(new LinkedHashMap<>(expectedVersions)));
		}

		private Builder add(Change change) {
			requireTitle(change.title());
			touch(change.title());
			if (change.newTitle() != null) touch(change.newTitle());
			changes.add(change);
			return this;
		}

		private void touch(String title) {
			String previous = touched.putIfAbsent(key(title), title);
			if (previous != null) {
				throw new IllegalArgumentException("Article " + title + " is already changed by this set");
			}
		}

		private static String requireTitle(String title) {
			if (title == null || title.isBlank()) throw new IllegalArgumentException("Article title must not be blank");
			return title;
		}
	}
}
