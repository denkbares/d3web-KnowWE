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
package de.knowwe.core.sse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.*;

public class ServerSentEventWriterTest {

	@Test
	public void event_writesFrameAndFlushes() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEventWriter sse = ServerSentEvents.writer(out);
		sse.event("start", "{}");
		sse.event("7", "step", "a\nb");
		sse.data("plain");
		sse.retry(Duration.ofMillis(500));
		sse.heartbeat();
		String expected = "event: start\ndata: {}\n\n"
				+ "id: 7\nevent: step\ndata: a\ndata: b\n\n"
				+ "data: plain\n\n"
				+ "retry: 500\n\n"
				+ ": keep-alive\n\n";
		assertEquals(expected, out.toString());
		assertTrue(sse.isOpen());
	}

	@Test
	public void event_usesConfiguredSerializer() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEventWriter sse = ServerSentEvents.writer(out)
				.withSerializer(payload -> "<" + payload + ">");
		sse.event("obj", (Object) 42);
		assertEquals("event: obj\ndata: <42>\n\n", out.toString());
	}

	@Test
	public void event_defaultSerializerUsesToString() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writer(out).event("obj", (Object) List.of(1, 2));
		assertEquals("event: obj\ndata: [1, 2]\n\n", out.toString());
	}

	@Test
	public void concurrentWriters_produceOnlyIntactFrames() throws Exception {
		int threads = 8;
		int eventsPerThread = 200;
		StringWriter out = new StringWriter();
		ServerSentEventWriter sse = ServerSentEvents.writer(out);
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		CountDownLatch go = new CountDownLatch(1);
		List<Future<?>> futures = new ArrayList<>();
		for (int t = 0; t < threads; t++) {
			int thread = t;
			futures.add(pool.submit(() -> {
				go.await();
				for (int i = 0; i < eventsPerThread; i++) {
					sse.event("t" + thread, "line1-" + thread + "-" + i + "\nline2-" + thread + "-" + i);
				}
				return null;
			}));
		}
		go.countDown();
		for (Future<?> f : futures) f.get(10, TimeUnit.SECONDS);
		pool.shutdown();

		String output = out.toString();
		Pattern frame = Pattern.compile("event: t(\\d+)\ndata: line1-(\\d+)-(\\d+)\ndata: line2-(\\d+)-(\\d+)\n\n");
		Matcher matcher = frame.matcher(output);
		int count = 0;
		int position = 0;
		while (matcher.find()) {
			assertEquals("frames must be contiguous", position, matcher.start());
			assertEquals(matcher.group(1), matcher.group(2));
			assertEquals(matcher.group(1), matcher.group(4));
			assertEquals(matcher.group(3), matcher.group(5));
			position = matcher.end();
			count++;
		}
		assertEquals(output.length(), position);
		assertEquals(threads * eventsPerThread, count);
	}

	@Test
	public void printWriterError_isReportedAsDisconnectAndClosesWriter() throws IOException {
		FailingWriter failing = new FailingWriter();
		ServerSentEventWriter sse = ServerSentEvents.writer(new PrintWriter(failing));
		sse.event("ok", "1");
		assertTrue(sse.isOpen());

		failing.fail = true;
		try {
			sse.event("lost", "2");
			fail("expected ClientDisconnectedException");
		}
		catch (ClientDisconnectedException expected) {
			// the servlet print writer only records the error, the SSE writer must surface it
		}
		assertFalse(sse.isOpen());

		failing.fail = false;
		int writesBefore = failing.writes;
		try {
			sse.event("after", "3");
			fail("expected ClientDisconnectedException");
		}
		catch (ClientDisconnectedException expected) {
			// closed writers fail fast
		}
		assertEquals("no further writes after disconnect", writesBefore, failing.writes);
	}

	@Test
	public void plainWriterIoException_closesWriterAndPropagates() {
		FailingWriter failing = new FailingWriter();
		failing.fail = true;
		ServerSentEventWriter sse = ServerSentEvents.writer(failing);
		try {
			sse.data("x");
			fail("expected IOException");
		}
		catch (IOException expected) {
			assertFalse(sse.isOpen());
		}
	}

	@Test
	public void await_writesHeartbeatsUntilFutureCompletes() throws Exception {
		StringWriter out = new StringWriter();
		ServerSentEventWriter sse = ServerSentEvents.writer(out);
		CompletableFuture<Void> future = new CompletableFuture<>();
		CompletableFuture.delayedExecutor(120, TimeUnit.MILLISECONDS).execute(() -> future.complete(null));

		sse.await(future, Duration.ofMillis(20));

		assertTrue(future.isDone());
		String output = out.toString();
		assertTrue("expected heartbeats, got " + output, output.startsWith(": keep-alive\n\n"));
		assertEquals("", output.replace(": keep-alive\n\n", ""));
	}

	@Test
	public void await_returnsImmediatelyForDoneFuture() throws Exception {
		StringWriter out = new StringWriter();
		ServerSentEvents.writer(out).await(CompletableFuture.completedFuture(null), Duration.ofSeconds(10));
		assertEquals("", out.toString());
	}

	@Test
	public void await_returnsOnFailedFuture() throws Exception {
		StringWriter out = new StringWriter();
		CompletableFuture<Void> future = new CompletableFuture<>();
		future.completeExceptionally(new IllegalStateException("boom"));
		ServerSentEvents.writer(out).await(future, Duration.ofSeconds(10));
		assertEquals("", out.toString());
	}

	@Test
	public void await_stopsWhenHeartbeatDetectsDisconnect() throws Exception {
		FailingWriter failing = new FailingWriter();
		failing.fail = true;
		ServerSentEventWriter sse = ServerSentEvents.writer(new PrintWriter(failing));
		try {
			sse.await(new CompletableFuture<>(), Duration.ofMillis(10));
			fail("expected ClientDisconnectedException");
		}
		catch (ClientDisconnectedException expected) {
			assertFalse(sse.isOpen());
		}
	}

	private static final class FailingWriter extends Writer {

		volatile boolean fail = false;
		volatile int writes = 0;

		@Override
		public void write(char[] buffer, int offset, int length) throws IOException {
			writes++;
			if (fail) throw new IOException("broken pipe");
		}

		@Override
		public void flush() throws IOException {
			if (fail) throw new IOException("broken pipe");
		}

		@Override
		public void close() {
		}
	}
}
