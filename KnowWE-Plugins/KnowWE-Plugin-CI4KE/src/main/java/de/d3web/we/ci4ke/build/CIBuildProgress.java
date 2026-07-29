/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.build;

import java.time.Clock;
import java.time.Instant;

import de.knowwe.core.utils.progress.DefaultAjaxProgressListener;

/**
 * Tracks the live status of one CI build independently of the long-lived dashboard.
 */
final class CIBuildProgress {
	private static final String QUEUED_MESSAGE = "Queued";

	private final Clock clock;
	private final DefaultAjaxProgressListener listener = new DefaultAjaxProgressListener();
	private volatile Instant startedAt;

	CIBuildProgress() {
		this(Clock.systemUTC());
	}

	CIBuildProgress(Clock clock) {
		this.clock = clock;
	}

	DefaultAjaxProgressListener getListener() {
		return listener;
	}

	void markStarted() {
		startedAt = clock.instant();
	}

	CIBuildStatus getStatus() {
		Instant start = startedAt;
		if (start == null) {
			return new CIBuildStatus(CIBuildStatus.State.QUEUED, 0, QUEUED_MESSAGE, null);
		}
		return new CIBuildStatus(CIBuildStatus.State.RUNNING, listener.getProgress(), listener.getMessage(), start);
	}
}
