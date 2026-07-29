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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CIBuildProgressTest {

	@Test
	public void buildIsQueuedUntilExecutionStarts() {
		CIBuildProgress progress = new CIBuildProgress(Clock.fixed(
				Instant.parse("2026-07-29T10:15:30Z"), ZoneOffset.UTC));

		CIBuildStatus status = progress.getStatus();

		assertEquals(CIBuildStatus.State.QUEUED, status.state());
		assertEquals(0, status.progress(), 0);
		assertEquals("Queued", status.message());
		assertNull(status.startedAt());
	}

	@Test
	public void buildStartIsRecordedWhenExecutionStarts() {
		Instant start = Instant.parse("2026-07-29T10:15:30Z");
		CIBuildProgress progress = new CIBuildProgress(Clock.fixed(start, ZoneOffset.UTC));
		progress.markStarted();
		progress.getListener().updateProgress(0.42f, "Executing tests");

		CIBuildStatus status = progress.getStatus();

		assertEquals(CIBuildStatus.State.RUNNING, status.state());
		assertEquals(0.42f, status.progress(), 0);
		assertEquals("Executing tests", status.message());
		assertEquals(start, status.startedAt());
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
