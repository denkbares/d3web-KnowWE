/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;

import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.event.InitializedArticlesEvent;
import de.knowwe.event.WikiContentReplacedEvent;

import static org.junit.Assert.*;

/**
 * After the wiki content is replaced (deploying a snapshot), the wiki initializes all its articles again, queueing them
 * on the workers of a parallel stream into the frame of the initializing thread. The attachment manager must then queue
 * the articles of compiled attachments as on the first start: registering them opens the article manager, so the
 * worker waited for the frame, while the owner of the frame waited for its workers.
 */
public class AttachmentRegistrationAfterContentReplacementTest {

	private static final String PATH = "Page/data.txt";

	@Rule public final ArticleLifecycleFixture wiki = new ArticleLifecycleFixture();

	@Test
	public void pageWithANewCompiledAttachmentIsQueuedByAWorkerAfterTheContentWasReplaced() throws Exception {
		wiki.attachments.put(PATH, "attached line\n");
		replaceWikiContent();

		initializeByWorker("Page", "attachment: " + PATH + "\n");

		assertNotNull("The attachment must be registered", wiki.manager.getArticle(PATH));
		assertEquals(Long.valueOf(1), wiki.compiler.contributions().get("attached line"));
	}

	@Test
	public void pageThatNoLongerCompilesItsAttachmentIsQueuedByAWorkerAfterTheContentWasReplaced() throws Exception {
		wiki.attachments.put(PATH, "attached line\n");
		wiki.register("Page", "attachment: " + PATH + "\n");
		assertNotNull(wiki.manager.getArticle(PATH));
		replaceWikiContent();

		initializeByWorker("Page", "no attachment\n");

		assertNull("The attachment article must be deleted", wiki.manager.getArticle(PATH));
		assertFalse(wiki.compiler.contributions().containsKey("attached line"));
	}

	/** What {@link Environment#reinitializeForNewWikiContent()} announces before the wiki initializes again. */
	private void replaceWikiContent() {
		KnowWEUtils.getEventManager(wiki.manager).fireEvent(new WikiContentReplacedEvent());
	}

	/**
	 * Queues the given article like the wiki initializes its articles: by a worker into a frame opened by this thread,
	 * which waits for the worker, and announces the end of the initialization afterward.
	 */
	private void initializeByWorker(String title, String text) throws Exception {
		ExecutorService worker = Executors.newSingleThreadExecutor();
		try {
			wiki.manager.open();
			try {
				Future<?> queued = worker.submit(() -> wiki.manager.queueArticle(title, text));
				queued.get(10, TimeUnit.SECONDS);
			}
			finally {
				wiki.manager.commit();
			}
			wiki.awaitCompilation();
			KnowWEUtils.getEventManager(wiki.manager).fireEvent(new InitializedArticlesEvent(wiki.manager));
		}
		finally {
			worker.shutdownNow();
			assertTrue("The worker did not end", worker.awaitTermination(10, TimeUnit.SECONDS));
		}
	}
}
