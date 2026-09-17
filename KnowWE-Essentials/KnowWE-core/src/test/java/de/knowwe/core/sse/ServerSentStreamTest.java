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
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.denkbares.collections.CloseableIterator;

import static org.junit.Assert.*;

public class ServerSentStreamTest {

	private static final BatchWriter<Integer> CSV = (out, batch) ->
			out.data(batch.stream().map(String::valueOf).collect(Collectors.joining(",")));

	@Test
	public void streamBatched_allItemsInOneBatch() throws IOException {
		StringWriter out = new StringWriter();
		boolean hasMore = ServerSentEvents.writer(out)
				.streamBatched(List.of(1, 2, 3, 4, 5).iterator(), StreamOptions.DEFAULT, CSV);
		assertEquals("data: 1,2,3,4,5\n\n", out.toString());
		assertFalse(hasMore);
	}

	@Test
	public void streamBatched_respectsMaxBatchItems() throws IOException {
		StringWriter out = new StringWriter();
		boolean hasMore = ServerSentEvents.writer(out).streamBatched(
				List.of(1, 2, 3, 4, 5).iterator(), StreamOptions.DEFAULT.withMaxBatchItems(2), CSV);
		assertEquals("data: 1,2\n\ndata: 3,4\n\ndata: 5\n\n", out.toString());
		assertFalse(hasMore);
	}

	@Test
	public void streamBatched_oneItemPerEvent() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writer(out).streamBatched(
				List.of(1, 2, 3).iterator(), StreamOptions.DEFAULT.withMaxBatchItems(1), CSV);
		assertEquals("data: 1\n\ndata: 2\n\ndata: 3\n\n", out.toString());
	}

	@Test
	public void streamBatched_respectsMaxItems_andReportsRemainingItems() throws IOException {
		StringWriter out = new StringWriter();
		boolean hasMore = ServerSentEvents.writer(out).streamBatched(
				List.of(1, 2, 3, 4, 5).iterator(), StreamOptions.DEFAULT.withMaxItems(3), CSV);
		assertEquals("data: 1,2,3\n\n", out.toString());
		assertTrue(hasMore);
	}

	@Test
	public void streamBatched_maxItemsZeroEmitsNothing() throws IOException {
		StringWriter out = new StringWriter();
		boolean hasMore = ServerSentEvents.writer(out).streamBatched(
				List.of(1, 2).iterator(), StreamOptions.DEFAULT.withMaxItems(0), CSV);
		assertEquals("", out.toString());
		assertTrue(hasMore);
	}

	@Test
	public void streamBatched_emptyIteratorEmitsNothing() throws IOException {
		StringWriter out = new StringWriter();
		boolean hasMore = ServerSentEvents.writer(out)
				.streamBatched(Collections.emptyIterator(), StreamOptions.DEFAULT, CSV);
		assertEquals("", out.toString());
		assertFalse(hasMore);
	}

	@Test
	public void streamBatched_batchDurationSplitsSlowSources() throws IOException {
		StringWriter out = new StringWriter();
		Iterator<Integer> slow = new Iterator<>() {
			private int next = 1;

			@Override
			public boolean hasNext() {
				return next <= 4;
			}

			@Override
			public Integer next() {
				try {
					Thread.sleep(15);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return next++;
			}
		};
		ServerSentEvents.writer(out).streamBatched(slow, StreamOptions.DEFAULT.withBatchDuration(Duration.ofMillis(5)), CSV);
		assertEquals("data: 1\n\ndata: 2\n\ndata: 3\n\ndata: 4\n\n", out.toString());
	}

	@Test
	public void streamBatched_closesCloseableIterator() throws IOException {
		CloseableListIterator source = new CloseableListIterator(List.of(1, 2));
		ServerSentEvents.writer(new StringWriter()).streamBatched(source, StreamOptions.DEFAULT, CSV);
		assertTrue(source.closed);
	}

	@Test
	public void streamBatched_interruptionEndsStream() throws IOException {
		CloseableListIterator source = new CloseableListIterator(List.of(1, 2, 3));
		Thread.currentThread().interrupt();
		try {
			ServerSentEvents.writer(new StringWriter()).streamBatched(source, StreamOptions.DEFAULT, CSV);
			fail("expected InterruptedIOException");
		}
		catch (InterruptedIOException expected) {
			assertTrue("source is closed on interruption", source.closed);
		}
		finally {
			assertTrue(Thread.interrupted());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void streamOptions_rejectZeroBatchDuration() {
		StreamOptions.DEFAULT.withBatchDuration(Duration.ZERO);
	}

	@Test(expected = IllegalArgumentException.class)
	public void streamOptions_rejectZeroMaxBatchItems() {
		StreamOptions.DEFAULT.withMaxBatchItems(0);
	}

	private static final class CloseableListIterator implements CloseableIterator<Integer> {

		private final Iterator<Integer> delegate;
		boolean closed = false;

		CloseableListIterator(List<Integer> items) {
			this.delegate = items.iterator();
		}

		@Override
		public void close() {
			closed = true;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public Integer next() {
			return delegate.next();
		}
	}
}
