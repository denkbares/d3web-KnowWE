/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.build;

import java.time.Instant;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * Immutable snapshot of a queued or running CI build.
 * <p>
 * A queued build has not entered its build callable yet and therefore has no start timestamp. Once the callable
 * starts, the state changes to {@link State#RUNNING} and {@link #startedAt()} remains stable for the lifetime of that
 * build. Finished builds are not represented by this type; {@link CIBuildManager#getBuildStatus} returns {@code null}
 * for them.
 *
 * @param state     current execution state
 * @param progress  current progress from {@code 0} to {@code 1}
 * @param message   human-readable progress message
 * @param startedAt actual start of execution, only present for running builds
 */
public record CIBuildStatus(State state, float progress, String message, @Nullable Instant startedAt) {

	public CIBuildStatus {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(message, "message");
		if (state == State.QUEUED && startedAt != null) {
			throw new IllegalArgumentException("Queued builds must not have a start timestamp");
		}
		if (state == State.RUNNING && startedAt == null) {
			throw new IllegalArgumentException("Running builds must have a start timestamp");
		}
	}

	public enum State {
		QUEUED,
		RUNNING
	}
}
