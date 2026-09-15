/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.build;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.Test;

import com.denkbares.utils.Stopwatch;

import static org.junit.Assert.*;

public class CIBuildProgressTest {

	private static final Instant START = Instant.parse("2026-07-29T10:15:30Z");

	private final CIBuildChanges changes = new CIBuildChanges();

	private CIBuildProgress progressAt(Instant instant) {
		return new CIBuildProgress(Clock.fixed(instant, ZoneOffset.UTC), changes);
	}

	@Test
	public void buildIsQueuedUntilExecutionStarts() {
		CIBuildProgress progress = progressAt(START);

		CIBuildStatus status = progress.getStatus();

		assertEquals(CIBuildStatus.State.QUEUED, status.state());
		assertEquals(0, status.progress(), 0);
		assertEquals("Queued", status.message());
		assertNull(status.startedAt());
	}

	@Test
	public void buildStartIsRecordedWhenExecutionStarts() {
		CIBuildProgress progress = progressAt(START);
		progress.markStarted();
		progress.getListener().updateProgress(0.42f, "Executing tests");

		CIBuildStatus status = progress.getStatus();

		assertEquals(CIBuildStatus.State.RUNNING, status.state());
		assertEquals(0.42f, status.progress(), 0);
		assertEquals("Executing tests", status.message());
		assertEquals(START, status.startedAt());
	}

	@Test
	public void finishedBuildKeepsItsStartAndReportsCompletion() {
		CIBuildProgress progress = progressAt(START);
		progress.markStarted();
		progress.getListener().updateProgress(0.5f, "Half way");
		progress.markFinished();

		CIBuildStatus status = progress.getStatus();

		assertEquals(CIBuildStatus.State.FINISHED, status.state());
		assertEquals(1, status.progress(), 0);
		assertEquals("Finished", status.message());
		assertEquals(START, status.startedAt());
	}

	@Test
	public void buildAbortedWhileQueuedFinishesWithoutStart() {
		CIBuildProgress progress = progressAt(START);
		progress.markFinished();

		CIBuildStatus status = progress.getStatus();

		assertEquals(CIBuildStatus.State.FINISHED, status.state());
		assertNull(status.startedAt());
	}

	@Test
	public void everyStateChangeIsPublishedToTheChangeMonitor() {
		CIBuildProgress progress = progressAt(START);
		long initial = changes.version();

		progress.markStarted();
		long afterStart = changes.version();
		progress.getListener().updateProgress(0.1f, "first");
		long afterProgress = changes.version();
		progress.markFinished();
		long afterFinish = changes.version();

		assertTrue(afterStart > initial);
		assertTrue(afterProgress > afterStart);
		assertTrue(afterFinish > afterProgress);
	}

	@Test
	public void elapsedDurationUsesStopwatchDisplay() {
		Duration duration = Duration.ofHours(4).plusMinutes(45);

		assertEquals("after " + Stopwatch.getDisplay(duration.toMillis()),
				CIRenderer.formatElapsedDuration(duration));
		assertEquals("after " + Stopwatch.getDisplay(0),
				CIRenderer.formatElapsedDuration(Duration.ofMinutes(-1)));
	}
}
