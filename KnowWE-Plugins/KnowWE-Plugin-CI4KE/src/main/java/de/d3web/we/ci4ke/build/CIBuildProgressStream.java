/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.build;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import de.knowwe.core.sse.ServerSentEventWriter;

/**
 * Streams the build status of a set of dashboards as server-sent events.
 * <p>
 * Each event carries the JSON snapshot of one dashboard's build, including the dashboard name. Events of finished
 * builds may carry the rendered state bubble, so the browser can update it without a further request. On connect
 * every dashboard is reported once. Afterward a dashboard is reported when its snapshot changed, coalesced to a minimum
 * interval, and running builds are reported at least once per tick so that their elapsed duration keeps counting.
 * A heartbeat comment is written when a whole tick passed without any event. The stream ends with an {@code end}
 * event once no requested dashboard has a queued or running build any more. It ends without that event when the
 * client disconnects, when the streaming thread is interrupted, or once the maximum lifetime is reached. Browsers
 * reconnect on their own in the latter case, after the announced retry delay.
 */
public final class CIBuildProgressStream {

	public static final String EVENT_NAME = "progress";
	public static final String END_EVENT_NAME = "end";

	private static final Duration RETRY_DELAY = Duration.ofSeconds(5);

	private static final Duration DEFAULT_TICK = Duration.ofSeconds(1);
	private static final Duration DEFAULT_MIN_INTERVAL = Duration.ofMillis(100);
	private static final Duration DEFAULT_MAX_LIFETIME = Duration.ofMinutes(10);
	private static final CIBuildStatus NO_BUILD = new CIBuildStatus(
			CIBuildStatus.State.FINISHED, 1, CIBuildProgress.FINISHED_MESSAGE, null);

	private final Clock clock;
	private final Duration tick;
	private final Duration minInterval;
	private final Duration maxLifetime;

	public CIBuildProgressStream() {
		this(Clock.systemUTC(), DEFAULT_TICK, DEFAULT_MIN_INTERVAL, DEFAULT_MAX_LIFETIME);
	}

	CIBuildProgressStream(Clock clock, Duration tick, Duration minInterval, Duration maxLifetime) {
		this.clock = clock;
		this.tick = tick;
		this.minInterval = minInterval;
		this.maxLifetime = maxLifetime;
	}

	/**
	 * Streams the builds of the given dashboards until none of them is queued or running any more. Finished events
	 * carry no rendered state bubble, see the overload with a bubble lookup.
	 */
	public void stream(
			@NotNull List<String> dashboardNames,
			@NotNull Function<String, @Nullable CIBuildStatus> statusLookup,
			@NotNull CIBuildChanges changes,
			@NotNull ServerSentEventWriter sse
	) throws IOException {
		stream(dashboardNames, statusLookup, name -> null, changes, sse);
	}

	/**
	 * Streams the builds of the given dashboards until none of them is queued or running any more.
	 *
	 * @param dashboardNames the dashboards to observe, reported back verbatim in the events
	 * @param statusLookup   resolves the current build status of a dashboard, {@code null} if it has no build
	 * @param bubbleLookup   renders the state bubble of a dashboard for finished events, {@code null} to omit it
	 * @param changes        the change monitor waking up the stream when any build changed
	 * @param sse            the open event stream to write to
	 * @throws IOException if the client disconnected or the response could not be written
	 */
	public void stream(
			@NotNull List<String> dashboardNames,
			@NotNull Function<String, @Nullable CIBuildStatus> statusLookup,
			@NotNull Function<String, @Nullable String> bubbleLookup,
			@NotNull CIBuildChanges changes,
			@NotNull ServerSentEventWriter sse
	) throws IOException {
		long deadline = System.nanoTime() + maxLifetime.toNanos();
		Map<String, CIBuildStatus> reported = new HashMap<>();
		long lastTick = System.nanoTime();
		long lastWrite = lastTick;
		try {
			sse.retry(RETRY_DELAY);
			while (true) {
				long version = changes.version();
				long now = System.nanoTime();
				boolean tickDue = now - lastTick >= tick.toNanos();
				if (tickDue) lastTick = now;

				Instant instant = clock.instant();
				boolean anyActive = false;
				boolean written = false;
				for (String name : dashboardNames) {
					CIBuildStatus status = statusLookup.apply(name);
					if (status == null) status = NO_BUILD;
					boolean active = status.state() != CIBuildStatus.State.FINISHED;
					anyActive |= active;
					boolean changed = !status.equals(reported.get(name));
					boolean ticking = tickDue && status.state() == CIBuildStatus.State.RUNNING;
					if (changed || ticking) {
						JSONObject json = toJson(name, status, instant);
						if (!active) {
							String bubble = bubbleLookup.apply(name);
							if (bubble != null) json.put("bubbleHtml", bubble);
						}
						sse.event(EVENT_NAME, json.toString());
						reported.put(name, status);
						written = true;
					}
				}
				if (!anyActive) {
					sse.event(END_EVENT_NAME, "{}");
					return;
				}
				if (written) {
					lastWrite = System.nanoTime();
				}
				else if (tickDue && System.nanoTime() - lastWrite >= tick.toNanos()) {
					sse.heartbeat();
					lastWrite = System.nanoTime();
				}

				long remainingLifetime = deadline - System.nanoTime();
				if (remainingLifetime <= 0) return;
				long untilTick = tick.toNanos() - (System.nanoTime() - lastTick);
				changes.awaitChange(version, Duration.ofNanos(Math.max(1, Math.min(untilTick, remainingLifetime))));

				long pause = minInterval.toNanos() - (System.nanoTime() - lastWrite);
				if (pause > 0) TimeUnit.NANOSECONDS.sleep(pause);
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Serializes a status snapshot for the browser, with the elapsed duration computed against the given time.
	 */
	@NotNull
	static JSONObject toJson(@NotNull String dashboardName, @NotNull CIBuildStatus status, @NotNull Instant now) {
		Instant startedAt = status.startedAt();
		JSONObject json = new JSONObject();
		json.put("dashboard", dashboardName);
		json.put("progress", String.valueOf((int) (status.progress() * 100)));
		json.put("message", status.message());
		json.put("state", status.state().name());
		json.put("startedAt", startedAt == null ? JSONObject.NULL : startedAt.toString());
		json.put("elapsedDuration", startedAt == null
				? ""
				: CIRenderer.formatElapsedDuration(Duration.between(startedAt, now)));
		return json;
	}
}
