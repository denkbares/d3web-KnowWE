/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.rdf2go;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import com.denkbares.semanticcore.config.RdfConfig;
import de.knowwe.core.Environment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests that shutting down a core does not leave threads waiting forever for tasks the core will never run.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 26.11.2026
 */
public class Rdf2GoCoreShutdownTest {

	private static final long TIMEOUT_SECONDS = 20;

	@Test(timeout = 120000)
	public void callersOfTasksQueuedAtShutdownDoNotWaitForever() throws Exception {
		// a plain in-memory repository, no plugins needed
		Rdf2GoCore core = new Rdf2GoCore((Environment) null, "ShutdownTest", "http://example.org/shutdown#", new RdfConfig());
		CountDownLatch destroying = new CountDownLatch(1);
		CountDownLatch released = new CountDownLatch(1);
		EventListener holdShutdown = new EventListener() {
			@Override
			public Collection<Class<? extends Event>> getEvents() {
				return List.of(Rdf2GoCoreDestroyEvent.class);
			}

			@Override
			public void notify(Event event) {
				if (((Rdf2GoCoreDestroyEvent) event).getCore() != core) return;
				destroying.countDown();
				try {
					released.await(TIMEOUT_SECONDS * 3, TimeUnit.SECONDS);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};
		EventManager.getInstance().registerListener(holdShutdown);
		try {
			// the teardown of a closing environment, held while it owns the core exclusively
			Thread closing = daemon(core::closeNow);
			closing.start();
			assertTrue("shutdown did not start", destroying.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

			// each call runs a task in the core's pool, the tasks wait for the shutdown to release the core; there is
			// one call more than threads in the pool, so one task is still queued when the pool is shut down
			int calls = Runtime.getRuntime().availableProcessors() + 1;
			Collection<RuntimeException> failures = new ConcurrentLinkedQueue<>();
			List<Thread> callers = new ArrayList<>();
			for (int i = 0; i < calls; i++) {
				Thread caller = daemon(() -> {
					try {
						core.getNamespaces();
					}
					catch (RuntimeException e) {
						failures.add(e);
					}
				});
				callers.add(caller);
				caller.start();
			}
			for (Thread caller : callers) awaitWaiting(caller);

			released.countDown();
			closing.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
			assertFalse("shutdown did not finish", closing.isAlive());
			for (Thread caller : callers) {
				caller.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
				assertFalse("a caller waits forever for a task the core will not run any more", caller.isAlive());
			}
			// the queued task was not run, its caller is told so
			assertFalse("no task was queued at the shutdown", failures.isEmpty());
			for (RuntimeException failure : failures) {
				assertTrue(failure.toString(), failure.getMessage().contains("shut down"));
			}
		}
		finally {
			released.countDown();
			EventManager.getInstance().unregister(holdShutdown);
		}
	}

	private static Thread daemon(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	}

	/**
	 * Waits until the thread waits, i.e. it waits for its task.
	 */
	private static void awaitWaiting(Thread thread) throws InterruptedException {
		long end = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS);
		while (thread.getState() != Thread.State.WAITING && thread.getState() != Thread.State.TIMED_WAITING) {
			if (System.currentTimeMillis() > end) fail("caller does not wait: " + thread.getState());
			Thread.sleep(10);
		}
	}
}
