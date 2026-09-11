/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.build;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.json.JSONObject;
import org.junit.Test;

import de.knowwe.core.sse.ServerSentEvents;

import static org.junit.Assert.*;

public class CIBuildProgressStreamTest {

	private static final Instant START = Instant.parse("2026-07-29T10:15:30Z");
	private static final Instant NOW = Instant.parse("2026-07-29T10:20:30Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
	private static final Duration TICK = Duration.ofMillis(20);
	private static final Duration MIN_INTERVAL = Duration.ofMillis(1);

	private final CIBuildChanges changes = new CIBuildChanges();
	private final Map<String, CIBuildProgress> builds = new HashMap<>();
	private final Function<String, CIBuildStatus> lookup =
			name -> builds.containsKey(name) ? builds.get(name).getStatus() : null;

	private static CIBuildProgressStream stream(Duration maxLifetime) {
		return new CIBuildProgressStream(CLOCK, TICK, MIN_INTERVAL, maxLifetime);
	}

	private CIBuildProgress runningBuild(String name) {
		CIBuildProgress progress = new CIBuildProgress(Clock.fixed(START, ZoneOffset.UTC), changes);
		progress.markStarted();
		builds.put(name, progress);
		return progress;
	}

	/**
	 * Parses the wire output into the JSON payloads of the progress events. The retry frame, heartbeat comments and
	 * the end event are dropped.
	 */
	private static List<JSONObject> events(String output) {
		List<JSONObject> events = new ArrayList<>();
		for (String frame : output.split("\n\n")) {
			if (frame.isEmpty() || frame.startsWith(":") || frame.startsWith("retry:")) continue;
			String[] lines = frame.split("\n");
			if (lines[0].equals("event: " + CIBuildProgressStream.END_EVENT_NAME)) continue;
			assertEquals("event: " + CIBuildProgressStream.EVENT_NAME, lines[0]);
			assertEquals(2, lines.length);
			assertTrue(lines[1].startsWith("data: "));
			events.add(new JSONObject(lines[1].substring("data: ".length())));
		}
		return events;
	}

	private static void assertEndsWithEndEvent(String output) {
		assertTrue("stream must announce its regular end, got: " + output,
				output.endsWith("event: " + CIBuildProgressStream.END_EVENT_NAME + "\ndata: {}\n\n"));
	}

	private static void assertNoEndEvent(String output) {
		assertFalse("stream must not announce an end", output.contains("event: " + CIBuildProgressStream.END_EVENT_NAME));
	}

	private static List<JSONObject> eventsOf(List<JSONObject> events, String dashboard) {
		return events.stream().filter(e -> dashboard.equals(e.getString("dashboard"))).toList();
	}

	/**
	 * Runs the stream on a second thread so the test can drive the builds while the stream is following them.
	 */
	private Thread streamInBackground(CIBuildProgressStream stream, List<String> names, Writer out,
									  AtomicReference<Throwable> failure) throws InterruptedException {
		Thread thread = new Thread(() -> {
			try {
				stream.stream(names, lookup, changes, ServerSentEvents.writer(out));
			}
			catch (Throwable e) {
				failure.set(e);
			}
		});
		thread.start();
		// give the stream time to write its first events
		Thread.sleep(50);
		return thread;
	}

	@Test
	public void dashboardsWithoutBuildAreReportedFinishedAndTheStreamEnds() throws IOException {
		StringWriter out = new StringWriter();

		stream(Duration.ofSeconds(10)).stream(List.of("a", "b"), lookup, changes, ServerSentEvents.writer(out));

		assertTrue(out.toString().startsWith("retry: 5000\n\n"));
		assertEndsWithEndEvent(out.toString());
		List<JSONObject> events = events(out.toString());
		assertEquals(2, events.size());
		for (JSONObject event : events) {
			assertEquals("FINISHED", event.getString("state"));
			assertEquals("100", event.getString("progress"));
			assertEquals("Finished", event.getString("message"));
			assertTrue(event.isNull("startedAt"));
			assertEquals("", event.getString("elapsedDuration"));
		}
		assertEquals("a", events.get(0).getString("dashboard"));
		assertEquals("b", events.get(1).getString("dashboard"));
	}

	@Test
	public void finishedBuildYieldsOneFinalEvent() throws IOException {
		runningBuild("a").markFinished();
		StringWriter out = new StringWriter();

		stream(Duration.ofSeconds(10)).stream(List.of("a"), lookup, changes, ServerSentEvents.writer(out));

		List<JSONObject> events = events(out.toString());
		assertEquals(1, events.size());
		JSONObject event = events.get(0);
		assertEquals("FINISHED", event.getString("state"));
		assertEquals(START.toString(), event.getString("startedAt"));
		assertEquals(CIRenderer.formatElapsedDuration(Duration.between(START, NOW)),
				event.getString("elapsedDuration"));
	}

	@Test
	public void streamFollowsProgressUntilTheBuildFinishes() throws Exception {
		CIBuildProgress progress = runningBuild("a");
		StringWriter out = new StringWriter();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = streamInBackground(stream(Duration.ofSeconds(10)), List.of("a"), out, failure);

		progress.getListener().updateProgress(0.25f, "first test");
		Thread.sleep(50);
		progress.getListener().updateProgress(0.5f, "second test");
		Thread.sleep(50);
		progress.markFinished();
		thread.join(TimeUnit.SECONDS.toMillis(5));

		assertFalse("stream did not end after the build finished", thread.isAlive());
		assertNull(failure.get());
		assertEndsWithEndEvent(out.toString());
		List<JSONObject> events = events(out.toString());
		assertEquals("RUNNING", events.get(0).getString("state"));
		assertEquals("0", events.get(0).getString("progress"));
		assertTrue(events.stream().anyMatch(e -> "first test".equals(e.getString("message"))));
		assertTrue(events.stream().anyMatch(e -> "second test".equals(e.getString("message"))
												 && "50".equals(e.getString("progress"))));
		JSONObject last = events.get(events.size() - 1);
		assertEquals("FINISHED", last.getString("state"));
		assertEquals("100", last.getString("progress"));
	}

	@Test
	public void streamReportsSeveralDashboardsAndEndsWhenTheLastOneFinishes() throws Exception {
		CIBuildProgress first = runningBuild("a");
		CIBuildProgress second = runningBuild("b");
		StringWriter out = new StringWriter();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = streamInBackground(stream(Duration.ofSeconds(10)), List.of("a", "b", "c"), out, failure);

		first.getListener().updateProgress(0.5f, "half of a");
		Thread.sleep(50);
		first.markFinished();
		Thread.sleep(50);
		assertTrue("stream must stay open while b is still running", thread.isAlive());
		second.getListener().updateProgress(0.75f, "most of b");
		Thread.sleep(50);
		second.markFinished();
		thread.join(TimeUnit.SECONDS.toMillis(5));

		assertFalse("stream did not end after the last build finished", thread.isAlive());
		assertNull(failure.get());
		List<JSONObject> events = events(out.toString());

		List<JSONObject> a = eventsOf(events, "a");
		assertEquals("RUNNING", a.get(0).getString("state"));
		assertTrue(a.stream().anyMatch(e -> "half of a".equals(e.getString("message"))));
		assertEquals("FINISHED", a.get(a.size() - 1).getString("state"));

		List<JSONObject> b = eventsOf(events, "b");
		assertTrue(b.stream().anyMatch(e -> "most of b".equals(e.getString("message"))));
		assertEquals("FINISHED", b.get(b.size() - 1).getString("state"));

		List<JSONObject> c = eventsOf(events, "c");
		assertEquals("dashboard without build is reported exactly once", 1, c.size());
		assertEquals("FINISHED", c.get(0).getString("state"));
	}

	@Test
	public void buildStartingWhileStreamingIsPickedUp() throws Exception {
		runningBuild("a");
		StringWriter out = new StringWriter();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = streamInBackground(stream(Duration.ofSeconds(10)), List.of("a", "b"), out, failure);

		// b gets a build while the stream is open, as the build manager does it after queueing
		CIBuildProgress late = new CIBuildProgress(Clock.fixed(START, ZoneOffset.UTC), changes);
		builds.put("b", late);
		changes.changed();
		Thread.sleep(50);
		late.markStarted();
		Thread.sleep(50);
		late.markFinished();
		builds.get("a").markFinished();
		thread.join(TimeUnit.SECONDS.toMillis(5));

		assertFalse(thread.isAlive());
		assertNull(failure.get());
		List<String> statesOfB = new ArrayList<>();
		for (JSONObject event : eventsOf(events(out.toString()), "b")) {
			String state = event.getString("state");
			// ticks repeat the running state, only transitions are of interest here
			if (statesOfB.isEmpty() || !statesOfB.get(statesOfB.size() - 1).equals(state)) statesOfB.add(state);
		}
		assertEquals(List.of("FINISHED", "QUEUED", "RUNNING", "FINISHED"), statesOfB);
	}

	@Test
	public void runningBuildKeepsTickingWhileNothingChanges() throws Exception {
		CIBuildProgress progress = runningBuild("a");
		StringWriter out = new StringWriter();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = streamInBackground(stream(Duration.ofSeconds(10)), List.of("a"), out, failure);

		Thread.sleep(150);
		progress.markFinished();
		thread.join(TimeUnit.SECONDS.toMillis(5));

		assertFalse(thread.isAlive());
		assertNull(failure.get());
		List<JSONObject> events = events(out.toString());
		assertTrue("expected several tick events, got " + events.size(), events.size() >= 4);
		assertEquals("FINISHED", events.get(events.size() - 1).getString("state"));
	}

	@Test
	public void queuedBuildProducesHeartbeatsInsteadOfEvents() throws Exception {
		CIBuildProgress queued = new CIBuildProgress(Clock.fixed(START, ZoneOffset.UTC), changes);
		builds.put("a", queued);
		StringWriter out = new StringWriter();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = streamInBackground(stream(Duration.ofSeconds(10)), List.of("a"), out, failure);

		Thread.sleep(150);
		queued.markFinished();
		thread.join(TimeUnit.SECONDS.toMillis(5));

		assertFalse(thread.isAlive());
		assertNull(failure.get());
		String output = out.toString();
		List<JSONObject> events = events(output);
		assertEquals("queued build must not be repeated", 2, events.size());
		assertEquals("QUEUED", events.get(0).getString("state"));
		assertEquals("FINISHED", events.get(1).getString("state"));
		assertTrue("expected heartbeat comments while queued", output.contains(": keep-alive\n\n"));
	}

	@Test
	public void streamEndsSilentlyAfterMaximumLifetime() throws Exception {
		runningBuild("a");
		StringWriter out = new StringWriter();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = streamInBackground(stream(Duration.ofMillis(100)), List.of("a"), out, failure);

		thread.join(TimeUnit.SECONDS.toMillis(5));

		assertFalse("stream did not end after its maximum lifetime", thread.isAlive());
		assertNull(failure.get());
		assertNoEndEvent(out.toString());
		List<JSONObject> events = events(out.toString());
		assertFalse(events.isEmpty());
		assertEquals("RUNNING", events.get(events.size() - 1).getString("state"));
	}

	@Test
	public void clientDisconnectEndsStreamWithException() throws Exception {
		runningBuild("a");
		Writer out = new Writer() {
			private int flushes = 0;

			@Override
			public void write(char[] buffer, int offset, int length) throws IOException {
				if (flushes > 0) throw new IOException("Broken pipe");
			}

			@Override
			public void flush() {
				flushes++;
			}

			@Override
			public void close() {
			}
		};
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = streamInBackground(stream(Duration.ofSeconds(10)), List.of("a"), out, failure);

		thread.join(TimeUnit.SECONDS.toMillis(5));

		assertFalse("stream did not end after the client disconnected", thread.isAlive());
		assertTrue(failure.get() instanceof IOException);
	}

	@Test
	public void finishedEventsCarryTheBubbleWhenALookupIsGiven() throws IOException {
		runningBuild("a").markFinished();
		StringWriter out = new StringWriter();

		stream(Duration.ofSeconds(10)).stream(List.of("a", "b"), lookup,
				name -> name.equals("a") ? "<i class='ci-state'>a</i>" : null, changes, ServerSentEvents.writer(out));

		List<JSONObject> events = events(out.toString());
		assertEquals(2, events.size());
		assertEquals("<i class='ci-state'>a</i>", events.get(0).getString("bubbleHtml"));
		assertFalse("dashboard without bubble must not carry the field", events.get(1).has("bubbleHtml"));
	}

	@Test
	public void runningEventsNeverCarryABubble() throws Exception {
		CIBuildProgress progress = runningBuild("a");
		StringWriter out = new StringWriter();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				stream(Duration.ofSeconds(10)).stream(List.of("a"), lookup, name -> "<i/>", changes,
						ServerSentEvents.writer(out));
			}
			catch (Throwable e) {
				failure.set(e);
			}
		});
		thread.start();
		Thread.sleep(50);
		progress.markFinished();
		thread.join(TimeUnit.SECONDS.toMillis(5));

		assertNull(failure.get());
		List<JSONObject> events = events(out.toString());
		assertTrue(events.size() >= 2);
		for (JSONObject event : events) {
			assertEquals("FINISHED".equals(event.getString("state")), event.has("bubbleHtml"));
		}
	}

	@Test
	public void streamWithoutBubbleLookupStaysCompatible() throws IOException {
		runningBuild("a").markFinished();
		StringWriter out = new StringWriter();

		stream(Duration.ofSeconds(10)).stream(List.of("a"), lookup, changes, ServerSentEvents.writer(out));

		assertFalse(events(out.toString()).get(0).has("bubbleHtml"));
	}

	@Test
	public void jsonCarriesAllFieldsOfTheStatus() {
		CIBuildStatus status = new CIBuildStatus(CIBuildStatus.State.RUNNING, 0.427f, "Some test", START);

		JSONObject json = CIBuildProgressStream.toJson("dash", status, NOW);

		assertEquals("dash", json.getString("dashboard"));
		assertEquals("42", json.getString("progress"));
		assertEquals("Some test", json.getString("message"));
		assertEquals("RUNNING", json.getString("state"));
		assertEquals(START.toString(), json.getString("startedAt"));
		assertEquals(CIRenderer.formatElapsedDuration(Duration.ofMinutes(5)), json.getString("elapsedDuration"));
	}
}
