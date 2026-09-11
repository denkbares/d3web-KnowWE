/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Rule;
import org.junit.Test;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Sections;

import static de.knowwe.core.ArticleLifecycleFixture.line;
import static org.junit.Assert.*;

/** A browser edit waiting behind another registration must validate its IDs against the resulting article version. */
public class SectionReplacementLifecycleTest {

	@Rule
	public final ArticleLifecycleFixture wiki = new ArticleLifecycleFixture();

	@Test
	public void editWaitingForRegistrationRejectsIdsInvalidatedWhileWaiting() throws Exception {
		Article original = wiki.register("Page", "old\n");
		String oldId = line(original, 0).getID();
		Field field = DefaultArticleManager.class.getDeclaredField("mainLock");
		field.setAccessible(true);
		ReentrantLock lock = (ReentrantLock) field.get(wiki.manager);
		AtomicReference<Thread> editingThread = new AtomicReference<>();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Sections.ReplaceResult> edit;
		wiki.manager.open();
		try {
			edit = executor.submit(() -> {
				editingThread.set(Thread.currentThread());
				return Sections.replace(Sections.ReplaceContext.of(wiki.manager.getWeb(), title -> true, "test"),
						Map.of(oldId, "browser edit\n"), null);
			});
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
			while ((editingThread.get() == null || !lock.hasQueuedThread(editingThread.get())) && System.nanoTime() < deadline) {
				Thread.onSpinWait();
			}
			assertNotNull(editingThread.get());
			assertTrue("Edit did not reach the registration lock", lock.hasQueuedThread(editingThread.get()));
			wiki.manager.queueArticle("Page", "concurrent edit\n");
		}
		finally {
			wiki.manager.commit();
			executor.shutdown();
		}
		try {
			assertEquals(List.of(oldId), List.copyOf(edit.get(5, TimeUnit.SECONDS).getMissingSectionIDs()));
			assertEquals("concurrent edit\n", wiki.manager.getArticle("Page").getText());
			// The fixture rejects persistence writes, so an accepted edit would also fail this test.
		}
		finally {
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}
	}
}
