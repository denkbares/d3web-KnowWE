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

package de.d3web.we.ci4ke.build;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schedules a successor asynchronously after its predecessor has actually stopped executing.
 */
final class AsyncBuildScheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBuildScheduler.class);

	private AsyncBuildScheduler() {
	}

	/**
	 * Schedules the wait without blocking the caller. Once the predecessor is done, {@code tryStartSuccessor} must
	 * atomically verify that the successor is still current and submit it. If waiting fails or the successor is no
	 * longer valid, {@code discardSuccessor} is invoked.
	 */
	static Future<?> schedule(
			@NotNull ExecutorService waitExecutor,
			@Nullable Future<?> predecessor,
			@NotNull BooleanSupplier tryStartSuccessor,
			@NotNull Runnable discardSuccessor) {
		return waitExecutor.submit(() -> {
			boolean started = false;
			try {
				if (awaitTermination(predecessor)) {
					started = tryStartSuccessor.getAsBoolean();
				}
			}
			finally {
				if (!started) {
					discardSuccessor.run();
				}
			}
		});
	}

	/**
	 * Waits for actual execution termination. Cancellation is a terminal state; interruption aborts the wait while
	 * preserving the interrupt flag.
	 */
	static boolean awaitTermination(@Nullable Future<?> predecessor) {
		if (predecessor == null) return true;
		try {
			predecessor.get();
			return true;
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.warn("Interrupted waiting for CI build termination");
			return false;
		}
		catch (CancellationException e) {
			return true;
		}
		catch (ExecutionException e) {
			LOGGER.warn(e.getClass().getSimpleName() + " in CI build...", e);
			return true;
		}
	}
}
