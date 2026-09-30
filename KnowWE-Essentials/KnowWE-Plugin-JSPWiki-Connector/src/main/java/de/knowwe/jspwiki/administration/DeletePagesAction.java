/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.jspwiki.administration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.Event;
import com.denkbares.events.EventManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.event.ArticleDeletedEvent;
import de.knowwe.event.ArticleUpdateEvent;
import de.knowwe.jspwiki.readOnly.ReadOnlyManager;

/**
 * Deletes or clears several pages at once, offered as a tool of the %%Admin markup (see {@link
 * DeletePagesToolProvider}). The options are the same as when deleting a single page: clearing replaces the content
 * by a "Deleted by ..." note as a new version (can be restored from the history), deleting removes the page with its
 * attachments and its whole revision history. With the parameter <tt>preview=true</tt> nothing is changed, the
 * response only tells what would happen to each page.
 * <p>
 * All pages are removed from resp. updated in the article manager within one open/commit, so the wiki compiles once
 * for the whole batch instead of once per page. The pages are deleted from the persistence only after that commit,
 * so the PAGE_DELETE_REQUEST listener of the KnowWEPlugin finds no article any more and does nothing (same approach
 * as in the wiki agent's change executor).
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 30.09.2026
 */
public class DeletePagesAction extends AbstractAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeletePagesAction.class);

	public static final String PAGES = "pages";
	public static final String MODE = "mode";
	public static final String PREVIEW = "preview";

	/**
	 * The text of a cleared page starts with this, same as when clearing a single page in Delete.jsp
	 */
	static final String CLEARED_PREFIX = "Deleted by ";
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public enum Mode {
		/**
		 * Replace the content by a note, older versions stay in the history
		 */
		CLEAR,
		/**
		 * Delete the page including its attachments and revision history
		 */
		DELETE
	}

	public enum Status {
		/**
		 * The page is (or in the preview: would be) cleared resp. deleted
		 */
		OK,
		NOT_FOUND,
		NO_PERMISSION,
		/**
		 * The page was already listed in a line above
		 */
		DUPLICATE,
		/**
		 * Only when clearing: the page is already cleared
		 */
		ALREADY_CLEARED,
		/**
		 * Clearing or deleting failed with an error
		 */
		FAILED
	}

	/**
	 * What happens (or happened) to one line of the input
	 */
	public static final class Entry {
		private final String name;
		private Status status;
		private @Nullable String message;

		Entry(String name, Status status) {
			this.name = name;
			this.status = status;
		}

		public String getName() {
			return name;
		}

		public Status getStatus() {
			return status;
		}

		public @Nullable String getMessage() {
			return message;
		}

		void fail(String message) {
			this.status = Status.FAILED;
			this.message = message;
		}
	}

	/**
	 * The access to the wiki needed to decide what happens to the pages, so the decision can be tested without a
	 * wiki
	 */
	interface PageLookup {

		/**
		 * Returns the actual title of the page with the given name (that may differ in case), or null if there is no
		 * such page
		 */
		@Nullable
		String findTitle(String name);

		boolean canDelete(String title);

		@Nullable
		String getText(String title);
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!context.userIsAdmin()) {
			fail(context, HttpServletResponse.SC_FORBIDDEN, "Only administrators can delete multiple pages at once");
		}
		boolean preview = Boolean.parseBoolean(context.getParameter(PREVIEW));
		if (!preview && !"POST".equals(context.getRequest().getMethod())) {
			fail(context, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Pages can only be deleted with a POST request");
		}
		if (ReadOnlyManager.isReadOnly()) {
			fail(context, HttpServletResponse.SC_CONFLICT, "The wiki is in read-only mode, no pages can be deleted");
		}
		Mode mode = parseMode(context.getParameter(MODE));
		if (mode == null) {
			failUnexpected(context, "Unknown mode: " + context.getParameter(MODE));
		}

		WikiConnector connector = Environment.getInstance().getWikiConnector();
		ArticleManager articleManager = KnowWEUtils.getDefaultArticleManager();
		List<Entry> entries = plan(parsePageNames(context.getParameter(PAGES)), mode, new PageLookup() {
			@Override
			public @Nullable String findTitle(String name) {
				Article article = articleManager.getArticle(name);
				if (article != null) return article.getTitle();
				return connector.doesArticleExist(name) ? name : null;
			}

			@Override
			public boolean canDelete(String title) {
				return connector.userCanDeleteArticle(title, context);
			}

			@Override
			public @Nullable String getText(String title) {
				return connector.getArticleText(title);
			}
		});

		if (!preview) {
			perform(entries, mode, context.getUserName(), connector, articleManager);
		}

		JSONObject json = new JSONObject();
		json.put("preview", preview);
		json.put("mode", mode.name());
		JSONArray results = new JSONArray();
		for (Entry entry : entries) {
			JSONObject result = new JSONObject();
			result.put("page", entry.getName());
			result.put("status", entry.getStatus().name());
			if (entry.getMessage() != null) result.put("message", entry.getMessage());
			results.put(result);
		}
		json.put("results", results);
		context.setContentType(JSON);
		context.getWriter().write(json.toString());
	}

	static @Nullable Mode parseMode(@Nullable String mode) {
		if (mode == null) return null;
		try {
			return Mode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * One page name per line, surrounding white space is ignored, as are empty lines
	 */
	static List<String> parsePageNames(@Nullable String text) {
		List<String> names = new ArrayList<>();
		if (text == null) return names;
		for (String line : text.split("\r?\n")) {
			String name = line.strip();
			if (!name.isEmpty()) names.add(name);
		}
		return names;
	}

	/**
	 * Decides what happens to each of the given pages, without changing anything. Entries with status {@link
	 * Status#OK} contain the actual title of the page.
	 */
	static List<Entry> plan(List<String> names, Mode mode, PageLookup lookup) {
		List<Entry> entries = new ArrayList<>();
		// page names are case-insensitive in KnowWE
		Set<String> seen = new HashSet<>();
		for (String name : names) {
			String title = lookup.findTitle(name);
			String key = (title == null ? name : title).toLowerCase(Locale.ROOT);
			if (!seen.add(key)) {
				entries.add(new Entry(name, Status.DUPLICATE));
			}
			else if (title == null) {
				entries.add(new Entry(name, Status.NOT_FOUND));
			}
			else if (!lookup.canDelete(title)) {
				entries.add(new Entry(title, Status.NO_PERMISSION));
			}
			else if (mode == Mode.CLEAR && isCleared(lookup.getText(title))) {
				entries.add(new Entry(title, Status.ALREADY_CLEARED));
			}
			else {
				entries.add(new Entry(title, Status.OK));
			}
		}
		return entries;
	}

	private static boolean isCleared(@Nullable String text) {
		return text != null && text.strip().startsWith(CLEARED_PREFIX) && text.strip().lines().count() == 1;
	}

	private static void perform(List<Entry> entries, Mode mode, String user, WikiConnector connector, ArticleManager articleManager) {
		List<Entry> toPerform = entries.stream().filter(entry -> entry.getStatus() == Status.OK).toList();
		if (toPerform.isEmpty()) return;

		String note = CLEARED_PREFIX + user + " at " + FORMATTER.format(LocalDateTime.now());
		List<Entry> deferredDeletes = new ArrayList<>();
		LOGGER.info("{} {} pages, requested by {}", mode == Mode.CLEAR ? "Clearing" : "Deleting", toPerform.size(), user);

		connector.openPageTransaction(user);
		try {
			articleManager.open();
			try {
				for (Entry entry : toPerform) {
					String title = entry.getName();
					try {
						if (mode == Mode.CLEAR) {
							if (!connector.writeArticleToWikiPersistence(title, note, user, "Cleared by batch deletion")) {
								entry.fail("Could not write the page");
								continue;
							}
							articleManager.registerArticle(title, note);
							fire(new ArticleUpdateEvent(title, user));
							LOGGER.info("Cleared page {}", title);
						}
						else {
							Article existing = articleManager.getArticle(title);
							if (existing != null) {
								articleManager.deleteArticle(title);
								fire(new ArticleDeletedEvent(existing));
							}
							deferredDeletes.add(entry);
						}
					}
					catch (Exception e) {
						LOGGER.error("Could not {} page {}", mode == Mode.CLEAR ? "clear" : "delete", title, e);
						entry.fail(String.valueOf(e.getMessage()));
					}
				}
			}
			finally {
				articleManager.commit();
			}

			if (!deferredDeletes.isEmpty()) {
				// compiling the removal may still need the files of the pages, e.g. KnowSEC's SessionManager reads the
				// session cache attachment to remove the session statements from the ontology
				awaitCompilation(articleManager);
			}
			for (Entry entry : deferredDeletes) {
				try {
					// deletes the attachments as well
					connector.deleteArticle(entry.getName(), user);
					LOGGER.info("Deleted page {} including its revision history", entry.getName());
				}
				catch (Exception e) {
					LOGGER.error("Could not delete page {}", entry.getName(), e);
					entry.fail("Could not delete the page files (" + e.getMessage() + "), the page is no longer compiled until the next restart");
				}
			}
		}
		finally {
			connector.commitPageTransaction(user, (mode == Mode.CLEAR ? "Cleared " : "Deleted ") + toPerform.size() + " pages");
		}
	}

	private static void awaitCompilation(ArticleManager articleManager) {
		try {
			articleManager.getCompilerManager().awaitTermination();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.warn("Interrupted while waiting for the compilation, deleting the pages anyway");
		}
	}

	private static void fire(@NotNull Event event) {
		try {
			EventManager.getInstance().fireEvent(event);
		}
		catch (Exception e) {
			// a failing listener must not stop the batch, the article manager is already changed
			LOGGER.warn("Listener for event {} threw an exception, ignored", event.getClass().getSimpleName(), e);
		}
	}
}
