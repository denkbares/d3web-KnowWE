/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.build;

import java.time.Clock;
import java.time.Instant;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.utils.progress.DefaultAjaxProgressListener;

/**
 * Live progress of a single CI build.
 * <p>
 * The test threads report through {@link #getListener()}. Every report, the start and the end of the build are
 * published to the {@link CIBuildChanges} monitor the build was created with, so observers can wait for changes
 * instead of polling.
 */
public final class CIBuildProgress {
	private static final String QUEUED_MESSAGE = "Queued";
	static final String FINISHED_MESSAGE = "Finished";

	private final Clock clock;
	private final CIBuildChanges changes;
	private final DefaultAjaxProgressListener listener = new DefaultAjaxProgressListener() {
		@Override
		public void updateProgress(float percent, String message) {
			super.updateProgress(percent, message);
			changes.changed();
		}
	};
	private volatile Instant startedAt;
	private volatile boolean finished;

	CIBuildProgress(CIBuildChanges changes) {
		this(Clock.systemUTC(), changes);
	}

	CIBuildProgress(Clock clock, CIBuildChanges changes) {
		this.clock = clock;
		this.changes = changes;
	}

	DefaultAjaxProgressListener getListener() {
		return listener;
	}

	void markStarted() {
		startedAt = clock.instant();
		changes.changed();
	}

	void markFinished() {
		finished = true;
		changes.changed();
	}

	/**
	 * Returns an immutable snapshot of the current build state.
	 */
	@NotNull
	public CIBuildStatus getStatus() {
		Instant start = startedAt;
		if (finished) {
			return new CIBuildStatus(CIBuildStatus.State.FINISHED, 1, FINISHED_MESSAGE, start);
		}
		if (start == null) {
			return new CIBuildStatus(CIBuildStatus.State.QUEUED, 0, QUEUED_MESSAGE, null);
		}
		return new CIBuildStatus(CIBuildStatus.State.RUNNING, listener.getProgress(), listener.getMessage(), start);
	}
}
