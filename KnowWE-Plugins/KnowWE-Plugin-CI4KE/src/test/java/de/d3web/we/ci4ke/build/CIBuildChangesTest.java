/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.build;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import static org.junit.Assert.*;

public class CIBuildChangesTest {

	@Test
	public void changedBumpsTheVersion() {
		CIBuildChanges changes = new CIBuildChanges();
		long initial = changes.version();

		changes.changed();
		long once = changes.version();
		changes.changed();
		long twice = changes.version();

		assertTrue(once > initial);
		assertTrue(twice > once);
	}

	@Test
	public void awaitChangeReturnsKnownVersionOnTimeout() throws InterruptedException {
		CIBuildChanges changes = new CIBuildChanges();
		long version = changes.version();

		assertEquals(version, changes.awaitChange(version, Duration.ofMillis(20)));
	}

	@Test
	public void awaitChangeReturnsImmediatelyIfVersionIsAlreadyStale() throws InterruptedException {
		CIBuildChanges changes = new CIBuildChanges();
		long stale = changes.version();
		changes.changed();

		long before = System.nanoTime();
		long result = changes.awaitChange(stale, Duration.ofSeconds(10));

		assertNotEquals(stale, result);
		assertTrue("await must not block on a stale version", System.nanoTime() - before < TimeUnit.SECONDS.toNanos(5));
	}

	@Test
	public void awaitChangeWakesUpOnChangeFromAnotherThread() throws InterruptedException {
		CIBuildChanges changes = new CIBuildChanges();
		long version = changes.version();
		CountDownLatch waiting = new CountDownLatch(1);
		AtomicLong result = new AtomicLong(version);

		Thread waiter = new Thread(() -> {
			try {
				waiting.countDown();
				result.set(changes.awaitChange(version, Duration.ofSeconds(10)));
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		waiter.start();
		assertTrue(waiting.await(5, TimeUnit.SECONDS));
		changes.changed();
		waiter.join(TimeUnit.SECONDS.toMillis(5));

		assertFalse("waiter did not wake up", waiter.isAlive());
		assertNotEquals(version, result.get());
	}
}
