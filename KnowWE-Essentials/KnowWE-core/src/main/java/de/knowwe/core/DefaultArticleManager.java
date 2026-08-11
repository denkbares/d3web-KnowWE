/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.event.ArticleManagerCommitDoneEvent;
import de.knowwe.event.ArticleManagerCommitStartEvent;
import de.knowwe.event.ArticleRegisteredEvent;

/**
 * Manages all the articles of one web in a HashMap
 *
 * @author Jochen Reutelshöfer, Albrecht Striffler (denkbares GmbH)
 */
public class DefaultArticleManager implements ArticleManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultArticleManager.class);

	/**
	 * Stores Articles for article-names
	 */
	private final Map<String, Article> articleMap = new ConcurrentHashMap<>();
	private final Map<String, Article> originalArticleMap = new ConcurrentHashMap<>();

	private final Collection<String> deleteAfterCompile = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private final String web;

	private final CompilerManager compilerManager;

	private final AttachmentManager attachmentManager;

	private final ReentrantLock mainLock = new ReentrantLock(true);
	private volatile boolean registrationFrameOpen;
	private final Set<Article> added = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Set<Article> removed = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private final Object lock = new Object();
	private boolean initialized;

	public DefaultArticleManager(String web) {
		this.web = web;
		this.compilerManager = new CompilerManager(this);
		this.attachmentManager = new AttachmentManager(this);
	}

	public void setInitialized(boolean initialized) {
		synchronized (lock) {
			this.initialized = initialized;
			lock.notifyAll();
		}
	}

	@Override
	public boolean isInitialized() {
		synchronized (lock) {
			return initialized;
		}
	}

	public void awaitInitialization() {
		if (isInitialized()) return;
		synchronized (lock) {
			while (true) {
				if (isInitialized()) return;
				try {
					lock.wait();
				}
				catch (InterruptedException e) {
					LOGGER.warn("Waiting for initialization was interrupted");
					return;
				}
			}
		}
	}

	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	@Override
	public String getWeb() {
		return web;
	}

	/**
	 * Returns the Article for a given article name
	 *
	 * @param title the title of the article to return
	 */
	@Override
	public Article getArticle(String title) {
		if (title == null) return null;
		return articleMap.get(title.toLowerCase());
	}

	@Override
	public Collection<Article> getArticles() {
		return Collections.unmodifiableCollection(articleMap.values());
	}

	/**
	 * Registers a changed or new article in the manager and also compiles it. If you opened a registration frame by
	 * calling {@link ArticleManager#open()}, the added articles since then will not be compiled before calling {@link
	 * ArticleManager#commit()}.<p>
	 * <b>Attention:</b> Do not call this method synchronously from within a compilation thread.
	 *
	 * @param title   is the changed or new article to register
	 * @param content is the changed or new article to register
	 * @return the article we just generated and registered
	 * @created 20.12.2013
	 */
	@Override
	public Article registerArticle(String title, String content) {
		open();
		try {
			Article article = Article.createArticle(content, title, this);
			queueArticle(article);
			return article;
		}
		finally {
			commit();
		}
	}

	@Override
	public void removeAllArticles() {
		Collection<Article> allArticles = getArticles();

		// we clear all articles and compile
		open();
		try {
			allArticles.forEach(article -> {
				registerArticle(article.getTitle(), "");
				deleteAfterCompile.add(article.getTitle().toLowerCase());
			});
		}
		finally {
			commit();
		}
	}

	/**
	 * Queues up an article for registration (and compilation). The article will be compiled the next time {@link
	 * #open()} and {@link #commit()} are called.<p>
	 * This method can be used, to queue up multiple articles using different threads. {@link
	 * #registerArticle(String, String)}
	 * does not allow that, because a deadlock will happen at the internal call of {@link #open()}.<p>
	 * To compile directly after using this method (in a try-block!), you need to call {@link #open()} before, and
	 * {@link #commit()} afterwards (in the finally-block!).
	 */

	public void queueArticle(String title, String content) {
		requireOpenRegistrationFrame();
		queueArticle(Article.createArticle(content, title, this));
	}

	/**
	 * Recreates the supplied articles in parallel and queues the new versions sequentially. Recreating them is required
	 * for recompilation because the supplied instances already contain their previously sectionized KDOM. It deliberately
	 * happens inside the registration frame, after the preceding compilation has finished. Sequential queueing keeps
	 * {@link ArticleRegisteredEvent}s on the thread owning the frame, so their listeners may safely perform reentrant
	 * article operations.
	 */
	public void recreateAndQueueArticles(Collection<Article> articles) {
		requireOpenRegistrationFrame();
		if (!mainLock.isHeldByCurrentThread()) {
			throw new IllegalStateException("Only the registration frame owner can queue an article batch");
		}
		List<Article> sectionizedArticles = articles.parallelStream()
				.map(article -> Article.createArticle(article.getText(), article.getTitle(), this))
				.toList();
		sectionizedArticles.forEach(this::queueArticle);
	}

	private void requireOpenRegistrationFrame() {
		if (!registrationFrameOpen) {
			throw new IllegalStateException("Cannot queue articles outside an open registration frame");
		}
	}

	private void queueArticle(Article article) {
		Objects.requireNonNull(article);
		if (article.isTemporary()) {
			throw new IllegalArgumentException("Cannot add a temporary article to an article manager!");
		}

		String title = article.getTitle();

		if (!added.add(article)) {
			// Old action could not be overwritten so delete and add last action
			added.remove(article);
			added.add(article);
		}

		Article lastVersion = getArticle(title);
		if (lastVersion != null) removed.add(lastVersion);

		synchronized (originalArticleMap) {
			Article originalArticle = articleMap.put(title.toLowerCase(), article);
			if (!originalArticleMap.containsKey(title.toLowerCase()) && (originalArticle != null)) {
				originalArticleMap.put(title.toLowerCase(), originalArticle);
			}
		}

		// in case an article with the same name gets added in the same compilation window
		deleteAfterCompile.remove(title.toLowerCase());

		EventManager.getInstance().fireEvent(new ArticleRegisteredEvent(article));
		article.clearLastVersion();
	}

	@Override
	public @NotNull Collection<Article> getQueuedArticles() {
		synchronized (added) {
			return Collections.unmodifiableCollection(new ArrayList<>(added));
		}
	}

	/**
	 * Deletes the given article from the article map and invalidates all
	 * knowledge content that was in the article.
	 *
	 * @param title The article to delete
	 */
	@Override
	public void deleteArticle(String title) {
		open();
		try {
			registerArticle(title, "");

			deleteAfterCompile.add(title.toLowerCase());
		}
		finally {
			commit();
		}

		LOGGER.info("-> Deleted article '" + title + "'" + " from " + web);
	}

	@NotNull
	@Override
	public CompilerManager getCompilerManager() {
		return compilerManager;
	}

	/**
	 * Opens the manager for registration of articles, waiting for a preceding compilation before opening the outermost
	 * frame. Only after calling the method {@link ArticleManager#commit()} the added articles will be compiled. Make sure
	 * to always call commit in an try-finally block!<p>
	 * <b>Attention:</b> Do not call this method synchronously from within a compilation thread.
	 */
	@Override
	public void open() {
		if (CompilerManager.isCompileThread()) {
			throw new IllegalStateException("Cannot register articles during compilation");
		}
		if (mainLock.isHeldByCurrentThread()) {
			//noinspection LockAcquiredButNotSafelyReleased
			mainLock.lock();
			return;
		}
		while (true) {
			//noinspection LockAcquiredButNotSafelyReleased
			mainLock.lock();
			if (!compilerManager.isCompiling()) {
				registrationFrameOpen = true;
				return;
			}
			// Do not hold mainLock while waiting. A compiler may wait for work on another thread, and that work must not
			// be prevented from taking the ArticleManager lock. Once awakened, retry under mainLock to close the race with
			// another registration frame that may have started a new compilation in the meantime.
			mainLock.unlock();
			try {
				compilerManager.awaitTermination();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while waiting to register articles", e);
			}
		}
	}

	/**
	 * Calls this method after opening with {@link ArticleManager#open()}. It causes the compilation of articles
	 * registered or queued since calling the method {@link ArticleManager#open()}. Make sure to always call commit in
	 * a try-finally block!
	 */
	@Override
	public void commit() {

		if (mainLock.getHoldCount() == 0) {
			// This can happen if a rollback was performed, where we also unlock (reduce hold count)
			// in the finally block. In this case we must not commit (or reduce hold count again).
			return;
		}

		boolean outermostCommit = mainLock.getHoldCount() == 1;
		boolean changesCommitted = false;
		try {
			if (outermostCommit) {
				registrationFrameOpen = false;
				EventManager.getInstance().fireEvent(new ArticleManagerCommitStartEvent(this));
				ArrayList<Section<?>> addedSections = new ArrayList<>();
				ArrayList<Section<?>> removedSections = new ArrayList<>();
				synchronized (added) {
					synchronized (removed) {
						for (Article article : added) {
							addedSections.add(article.getRootSection());
						}
						for (Article article : removed) {
							removedSections.add(article.getRootSection());
						}
						added.clear();
						removed.clear();
					}
				}
				if (!addedSections.isEmpty() || !removedSections.isEmpty()) {
					addedSections.sort(Comparator.naturalOrder());
					removedSections.sort(Comparator.naturalOrder());
					compilerManager.compile(addedSections, removedSections);
					changesCommitted = true;
				}
				originalArticleMap.clear();
				synchronized (deleteAfterCompile) {
					for (Iterator<String> iterator = deleteAfterCompile.iterator(); iterator.hasNext(); ) {
						String next = iterator.next();
						Article removed = articleMap.remove(next);
						if (removed != null) {
							removed.destroy(null);
						}
						iterator.remove();
					}
				}
			}
		}
		finally {
			mainLock.unlock();
			if (outermostCommit) {
				EventManager.getInstance().fireEvent(new ArticleManagerCommitDoneEvent(this, changesCommitted));
			}
		}
	}

	@Override
	public boolean isLive(Section<?> section) {
		//noinspection SimplifiableIfStatement
		if (section == null || section.getTitle() == null) return false;
		if (getArticle(section.getTitle()) != section.getArticle()) return false;
		if (deleteAfterCompile.isEmpty()) return true;
		return !deleteAfterCompile.contains(section.getTitle().toLowerCase());
	}

	/**
	 * Call this method after opening with {@link ArticleManager#open()} in case an error occurred and the changes on
	 * file system have been rolled back also. This could be done in conjunction with a rollback abel FileProvider
	 * (i.e. GitVersioningFileProvider).
	 */
	@Override
	public void rollback() {
		try {
			if (mainLock.getHoldCount() == 1) {
				registrationFrameOpen = false;
				synchronized (added) {
					synchronized (originalArticleMap) {
						if (!originalArticleMap.isEmpty()) {
							articleMap.putAll(originalArticleMap);
						}
						for (Article changed : added) {
							if (!originalArticleMap.containsKey(changed.getTitle().toLowerCase())) {
								Article removed = articleMap.remove(changed.getTitle().toLowerCase());
								removed.destroy(null);
							}
						}
						originalArticleMap.clear();
					}
					added.clear();
				}
				synchronized (removed) {
					removed.clear();
				}
				synchronized (deleteAfterCompile) {
					deleteAfterCompile.clear();
				}
			}
		}
		finally {
			mainLock.unlock();
		}
	}
}
