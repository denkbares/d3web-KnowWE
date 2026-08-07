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

package de.knowwe.search;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.event.ArticleDeletedEvent;
import de.knowwe.event.ArticleManagerCommitDoneEvent;
import de.knowwe.event.ArticleRegisteredEvent;
import de.knowwe.event.FullParseEvent;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.jspwiki.JSPWikiConnector;
import de.knowwe.search.index.SearchFields;
import de.knowwe.search.index.SectionDocumentBuilder;
import de.knowwe.search.index.WikiSearchIndex;
import de.knowwe.search.query.WikiSearcher;

/**
 * Keeps the section index in step with the wiki.
 * <p>
 * The initial build is triggered by {@link InitializedArticlesEvent}, which is the only event that guarantees every
 * KDOM is parsed and compiled — JSPWiki's own indexer starts on a timer instead and would happily index a half
 * compiled wiki. It runs on its own daemon thread so the wiki serves requests while it works.
 * <p>
 * Updates are collected per article and flushed at the commit boundary, never per page, so an import of a thousand
 * pages does not produce a thousand commits.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiSearchService implements EventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiSearchService.class);

	private static final String INDEX_DIRECTORY = "knowwe-search";
	private static volatile WikiSearchService instance;

	private final Path indexPath;
	private final SectionDocumentBuilder documents = new SectionDocumentBuilder();
	private final Set<String> pending = new LinkedHashSet<>();
	private final ExecutorService worker =
			Executors.newSingleThreadExecutor(runnable -> {
				Thread thread = new Thread(runnable, "KnowWE-Search-Indexer");
				thread.setDaemon(true);
				return thread;
			});

	private volatile WikiSearchIndex index;
	private volatile WikiSearcher searcher;
	private volatile boolean building;

	WikiSearchService(@NotNull Path indexPath) {
		this.indexPath = indexPath;
	}

	public static synchronized @NotNull WikiSearchService getInstance() {
		if (instance == null) {
			instance = new WikiSearchService(defaultIndexPath());
			instance.open();
			EventManager.getInstance().registerListener(instance, EventManager.RegistrationType.PERSISTENT);
		}
		return instance;
	}

	/**
	 * Under the wiki's work directory, not under its page directory: an index below {@code var.basedir} would be picked
	 * up by the file or git page provider as if it were content. The schema version is part of the path, so an
	 * incompatible index is simply not found rather than being repaired.
	 */
	private static Path defaultIndexPath() {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		if (connector instanceof JSPWikiConnector jspWiki) {
			return Path.of(jspWiki.getEngine().getWorkDir())
					.resolve(INDEX_DIRECTORY).resolve(SearchFields.SCHEMA_VERSION);
		}
		// no wiki engine, for instance in a headless run: next to the page directory, never inside it
		return Path.of(connector.getSavePath())
				.resolveSibling(INDEX_DIRECTORY).resolve(SearchFields.SCHEMA_VERSION);
	}

	/** Opens the index without touching the singleton, so tests can drive a service on a temporary directory. */
	void openForTest() {
		open();
	}

	/** Waits until the indexer has caught up. Only for tests; production never needs to know. */
	void awaitIdle() throws InterruptedException {
		java.util.concurrent.CountDownLatch idle = new java.util.concurrent.CountDownLatch(1);
		worker.submit(idle::countDown);
		if (!idle.await(60, TimeUnit.SECONDS)) throw new IllegalStateException("indexer did not become idle");
	}

	private void open() {
		try {
			this.index = new WikiSearchIndex(indexPath);
			this.searcher = new WikiSearcher(index);
		}
		catch (IOException e) {
			LOGGER.error("Cannot open the search index at {}; wiki search will be unavailable", indexPath, e);
		}
	}

	public @Nullable WikiSearcher getSearcher() {
		return searcher;
	}

	/** Whether the initial build is still running, so the interface can say "still indexing" instead of "no hits". */
	public boolean isBuilding() {
		return building;
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return List.of(
				InitializedArticlesEvent.class,
				ArticleRegisteredEvent.class,
				ArticleManagerCommitDoneEvent.class,
				ArticleDeletedEvent.class,
				FullParseEvent.class);
	}

	@Override
	public void notify(Event event) {
		if (index == null) return;
		if (event instanceof InitializedArticlesEvent initialized) {
			rebuildInBackground(initialized.getArticleManager());
		}
		else if (event instanceof ArticleRegisteredEvent registered) {
			// fires before compilation, so only remember the title here
			synchronized (pending) {
				pending.add(registered.getArticle().getTitle());
			}
		}
		else if (event instanceof ArticleManagerCommitDoneEvent commit) {
			if (commit.changesCommitted()) flush(commit.getArticleManager());
		}
		else if (event instanceof ArticleDeletedEvent deleted) {
			String title = deleted.getArticle().getTitle();
			submit(() -> index.removePage(title));
		}
		else if (event instanceof FullParseEvent fullParse) {
			reindex(fullParse.getArticles());
		}
	}

	/**
	 * Takes the documents <b>now</b>, while the articles are still the live ones, and only hands the finished
	 * documents to the background thread. Extracting them there instead would race the next reparse and index sections
	 * that no longer exist.
	 */
	private void flush(ArticleManager articleManager) {
		List<String> titles;
		synchronized (pending) {
			if (pending.isEmpty()) return;
			titles = List.copyOf(pending);
			pending.clear();
		}
		List<PageUpdate> updates = new ArrayList<>(titles.size());
		for (String title : titles) {
			Article article = articleManager.getArticle(title);
			updates.add(new PageUpdate(title, article == null ? List.of() : documents.build(article)));
		}
		submit(() -> {
			for (PageUpdate update : updates) {
				index.replacePage(update.title(), update.documents());
			}
			index.commit();
		});
	}

	private void rebuildInBackground(ArticleManager articleManager) {
		building = true;
		reindex(articleManager.getArticles());
	}

	/** Extracts the documents on the calling thread, then indexes them in the background; see {@link #flush}. */
	private void reindex(Collection<Article> articles) {
		List<PageUpdate> updates = new ArrayList<>(articles.size());
		for (Article article : articles) {
			updates.add(new PageUpdate(article.getTitle(), documents.build(article)));
		}
		submit(() -> {
			try {
				Stopwatch stopwatch = new Stopwatch();
				for (PageUpdate update : updates) {
					index.replacePage(update.title(), update.documents());
				}
				index.commit();
				stopwatch.log(LOGGER, "Indexed " + updates.size() + " pages into " + index.documentCount()
									  + " searchable sections");
			}
			finally {
				building = false;
			}
		});
	}

	private void submit(IndexTask task) {
		worker.submit(() -> {
			try {
				task.run();
				index.refresh();
			}
			catch (IOException | RuntimeException e) {
				LOGGER.error("Search index update failed", e);
			}
		});
	}

	/**
	 * Drain, commit, then close. A writer left open keeps the index files undeletable on Windows for the lifetime of
	 * the JVM.
	 */
	public synchronized void shutdown() {
		worker.shutdown();
		try {
			if (!worker.awaitTermination(30, TimeUnit.SECONDS)) {
				LOGGER.warn("Search indexer did not finish within 30 s, closing anyway");
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		try {
			if (index != null) index.close();
		}
		catch (IOException e) {
			LOGGER.warn("Could not close the search index cleanly", e);
		}
		index = null;
		searcher = null;
		instance = null;
	}

	private record PageUpdate(String title, List<Document> documents) {
	}

	@FunctionalInterface
	private interface IndexTask {
		void run() throws IOException;
	}
}
