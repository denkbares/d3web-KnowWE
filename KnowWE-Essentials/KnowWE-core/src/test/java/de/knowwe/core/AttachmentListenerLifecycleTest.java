/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Rule;
import org.junit.Test;

import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.event.AttachmentStoredEvent;

import static de.knowwe.core.ArticleLifecycleFixture.line;
import static org.junit.Assert.*;

/**
 * Exercises the production attachment callback with the real article lifecycle. Reflection only constructs and
 * unregisters the private listener: fixture lines provide its text/article data without booting global scripts.
 * Direct invocation after unregister models a callback already snapshotted by EventManager.
 */
public class AttachmentListenerLifecycleTest {

	@Rule public final ArticleLifecycleFixture wiki = new ArticleLifecycleFixture();

	@Test
	public void callbackRechecksOwnershipAfterWaitingForRegistration() throws Exception {
		Article old = wiki.register("Page", "file.bin\n");
		Field field = DefaultArticleManager.class.getDeclaredField("mainLock");
		field.setAccessible(true);
		ReentrantLock lock = (ReentrantLock) field.get(wiki.manager);
		AtomicReference<Thread> callbackThread = new AtomicReference<>();
		var executor = Executors.newSingleThreadExecutor();
		try (Listener listener = new Listener(old)) {
			wiki.manager.open();
			java.util.concurrent.Future<?> callback;
			Article edited;
			try {
				callback = executor.submit(() -> {
					callbackThread.set(Thread.currentThread());
					listener.callback.notify(event(old));
				});
				long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
				while ((callbackThread.get() == null || !lock.hasQueuedThread(callbackThread.get())) && System.nanoTime() < deadline) {
					Thread.onSpinWait();
				}
				assertNotNull(callbackThread.get());
				assertTrue("Callback must reach the registration lock", lock.hasQueuedThread(callbackThread.get()));
				edited = wiki.manager.registerArticle("Page", "file.bin\nconcurrent edit\n");
			}
			finally {
				wiki.manager.commit();
			}
			callback.get(5, TimeUnit.SECONDS);
			wiki.awaitCompilation();
			assertSame(edited, wiki.manager.getArticle("Page"));
		}
		finally {
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	@Test
	public void dispatchedRetiredCallbackDoesNotRestoreOldText() throws Exception {
		Article old = wiki.register("Page", "file.bin\n");
		try (Listener listener = new Listener(old)) {
			Article edited = wiki.register("Page", "file.bin\nnew content\n");
			listener.close();
			listener.callback.notify(event(old));
			wiki.awaitCompilation();
			assertSame(edited, wiki.manager.getArticle("Page"));
		}
	}

	@Test
	public void lateDestroyDoesNotUnregisterEqualIdSuccessor() throws Exception {
		Article old = wiki.register("Page", "file.bin\n");
		try (Listener previous = new Listener(old)) {
			Article current = wiki.register("Page", old.getText());
			assertEquals(line(old, 0).getID(), line(current, 0).getID());
			try (Listener successor = new Listener(current)) {
				assertNotEquals(previous.callback, successor.callback);
				previous.close();
				EventManager.getInstance().fireEvent(event(current));
				wiki.awaitCompilation();
				assertNotSame("Current listener must still recompile the article", current, wiki.manager.getArticle("Page"));
				assertEquals(current.getText(), wiki.manager.getArticle("Page").getText());
			}
		}
	}

	@Test
	public void retiredCallbackDoesNotRecompileEqualIdSuccessor() throws Exception {
		Article old = wiki.register("Page", "file.bin\n");
		try (Listener listener = new Listener(old)) {
			Article current = wiki.register("Page", old.getText());
			listener.callback.notify(event(old));
			wiki.awaitCompilation();
			assertSame(current, wiki.manager.getArticle("Page"));
		}
	}

	private static AttachmentStoredEvent event(Article article) {
		return new AttachmentStoredEvent(article.getWeb(), article.getTitle(), "file.bin");
	}

	private static final class Listener implements AutoCloseable {
		final EventListener callback;
		final Method destroy;

		Listener(Article article) throws Exception {
			Class<?> type = Class.forName("de.knowwe.core.kdom.basicType.AttachmentType$AttachmentChangedListener");
			Constructor<?> constructor = type.getDeclaredConstructor(Section.class);
			constructor.setAccessible(true);
			callback = (EventListener) constructor.newInstance(line(article, 0));
			destroy = type.getDeclaredMethod("destroy");
			destroy.setAccessible(true);
		}

		@Override public void close() throws Exception {
			destroy.invoke(callback);
		}
	}
}
