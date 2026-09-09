/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.build;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

/**
 * Change monitor shared by all builds of a build manager.
 * <p>
 * Every change of any build, be it a progress report, a queued, started or finished build, bumps a single version
 * counter and wakes up waiters. Observers read the version, take their snapshots and then block in
 * {@link #awaitChange(long, Duration)} until something happened. Producers never run observer code, they only notify.
 */
public final class CIBuildChanges {
	private final Object monitor = new Object();
	private long version;

	/**
	 * Returns the current change version. Read it before taking a snapshot and pass it to
	 * {@link #awaitChange(long, Duration)} so that no change between the two calls is missed.
	 */
	public long version() {
		synchronized (monitor) {
			return version;
		}
	}

	/**
	 * Blocks until the version differs from the known one or the timeout elapses, whichever comes first.
	 *
	 * @param knownVersion the version the caller has already seen
	 * @param timeout      maximum time to wait
	 * @return the current version, equal to the known one only if the timeout elapsed
	 */
	public long awaitChange(long knownVersion, @NotNull Duration timeout) throws InterruptedException {
		long deadline = System.nanoTime() + timeout.toNanos();
		synchronized (monitor) {
			while (version == knownVersion) {
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0) break;
				TimeUnit.NANOSECONDS.timedWait(monitor, remaining);
			}
			return version;
		}
	}

	void changed() {
		synchronized (monitor) {
			version++;
			monitor.notifyAll();
		}
	}
}
