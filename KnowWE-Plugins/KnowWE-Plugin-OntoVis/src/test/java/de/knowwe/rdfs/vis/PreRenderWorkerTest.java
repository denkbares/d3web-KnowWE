/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.rdfs.vis;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Test;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.rdfs.vis.markup.PreRenderer;
import de.knowwe.visualization.CleanableArtefact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests that a slow pre-rendering keeps neither the pre-rendering of other sections nor its callers waiting without
 * limit.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 26.11.2026
 */
public class PreRenderWorkerTest {

	private static final long TIMEOUT_SECONDS = 10;

	private final CountDownLatch released = new CountDownLatch(1);
	private final ExecutorService callers = Executors.newCachedThreadPool();
	private PreRenderWorker worker;

	@After
	public void tearDown() {
		released.countDown();
		callers.shutdownNow();
		if (worker != null) worker.close();
	}

	@Test(timeout = 60000)
	public void clearingASlowSectionDoesNotBlockOtherSections() throws Exception {
		worker = new PreRenderWorker(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS * 3));
		Section<?> slowSection = newSection("slow");
		Section<?> otherSection = newSection("other");
		SlowPreRenderer slow = new SlowPreRenderer();

		Future<Artefact> slowArtefact = callers.submit(() -> worker.getPreRenderedArtefact(slowSection, null, slow));
		assertTrue("pre-rendering did not start", slow.started.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		// e.g. a page view with a new concept as parameter: the cached visualization is removed, waiting for the slow one
		Thread clearing = new Thread(() -> worker.clearCache(slowSection));
		clearing.start();
		awaitWaiting(clearing);

		Artefact other = new Artefact();
		Future<Artefact> otherArtefact = callers.submit(() -> worker.getPreRenderedArtefact(otherSection, null, new FastPreRenderer(other)));
		try {
			assertSame(other, otherArtefact.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		}
		catch (TimeoutException e) {
			fail("pre-rendering of another section was blocked while clearing the slow section");
		}

		released.countDown();
		Artefact slowResult = slowArtefact.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		clearing.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
		assertTrue("the cleared artefact was not cleaned up", slowResult.cleanedUp.get());
	}

	@Test(timeout = 60000)
	public void waitingForAHangingPreRenderingIsLimited() throws Exception {
		worker = new PreRenderWorker(TimeUnit.SECONDS.toMillis(1));
		Section<?> section = newSection("hanging");
		SlowPreRenderer hanging = new SlowPreRenderer();

		Future<Artefact> artefact = callers.submit(() -> worker.getPreRenderedArtefact(section, null, hanging));
		try {
			assertNull(artefact.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		}
		catch (TimeoutException e) {
			fail("waiting for the pre-rendering did not end");
		}
		Future<?> clearing = callers.submit(() -> worker.clearCache(section));
		try {
			clearing.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		}
		catch (TimeoutException e) {
			fail("clearing the cache waited for the pre-rendering without limit");
		}
	}

	@Test(timeout = 60000)
	public void preRenderingIsDoneOnceUntilCleared() throws Exception {
		worker = new PreRenderWorker(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
		Section<?> section = newSection("cached");
		CountingPreRenderer counting = new CountingPreRenderer();

		Artefact first = worker.getPreRenderedArtefact(section, null, counting);
		assertSame(first, worker.getPreRenderedArtefact(section, null, counting));
		assertEquals(1, counting.count);

		worker.clearCache(section);
		assertTrue(first.cleanedUp.get());
		Artefact second = worker.getPreRenderedArtefact(section, null, counting);
		assertEquals(2, counting.count);
		assertTrue(second != first);
	}

	/**
	 * Waits until the thread waits for something, i.e. it is blocked in the method it runs.
	 */
	private static void awaitWaiting(Thread thread) throws InterruptedException {
		long end = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS);
		while (thread.getState() != Thread.State.WAITING && thread.getState() != Thread.State.TIMED_WAITING
				&& thread.getState() != Thread.State.BLOCKED) {
			if (System.currentTimeMillis() > end) fail("thread does not wait: " + thread.getState());
			Thread.sleep(10);
		}
	}

	private static Section<?> newSection(String text) {
		return Section.createSection(text, new AbstractType() {
		}, null);
	}

	private static class Artefact implements CleanableArtefact {
		private final AtomicBoolean cleanedUp = new AtomicBoolean(false);

		@Override
		public void cleanUp() {
			cleanedUp.set(true);
		}
	}

	private class SlowPreRenderer implements PreRenderer<Artefact> {
		private final CountDownLatch started = new CountDownLatch(1);

		@Override
		public Artefact preRender(Section<?> section, UserContext user) {
			started.countDown();
			try {
				released.await(TIMEOUT_SECONDS * 3, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return new Artefact();
		}

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
		}
	}

	private record FastPreRenderer(Artefact artefact) implements PreRenderer<Artefact> {

		@Override
		public Artefact preRender(Section<?> section, UserContext user) {
			return artefact;
		}

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
		}
	}

	private static class CountingPreRenderer implements PreRenderer<Artefact> {
		private volatile int count = 0;

		@Override
		public Artefact preRender(Section<?> section, UserContext user) {
			count++;
			return new Artefact();
		}

		@Override
		public void render(Section<?> section, UserContext user, RenderResult result) {
		}
	}
}
