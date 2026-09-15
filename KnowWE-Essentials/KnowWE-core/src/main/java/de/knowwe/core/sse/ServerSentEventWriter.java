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
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.collections.CloseableIterator;

/**
 * Thread safe writer for a single SSE stream.
 * <p>
 * Every event, comment and retry frame is written and flushed atomically, so producers on any number of threads may
 * share one instance. A client disconnect is detected on flush and reported as {@link ClientDisconnectedException}.
 * Afterwards {@link #isOpen()} is false and all further writes fail fast with the same exception.
 * <p>
 * Push style producers write events from their own threads while the servlet thread blocks in
 * {@link #await(Future, Duration)}, which keeps the connection alive with comment heartbeats. Pull style producers
 * hand an iterator to {@link #streamBatched(Iterator, StreamOptions, BatchWriter)}. Both work equally from a normal
 * action and from a servlet that started an {@code AsyncContext}, the latter has to complete the context itself.
 */
public final class ServerSentEventWriter {

	private static final String HEARTBEAT = "keep-alive";

	private final Writer out;
	private final Object lock = new Object();
	private volatile boolean open = true;
	private volatile long lastWriteNanos = System.nanoTime();
	private volatile PayloadSerializer serializer = String::valueOf;

	ServerSentEventWriter(@NotNull Writer out) {
		this.out = out;
	}

	/**
	 * Replaces the serializer used by {@link #event(String, Object)}. The default is {@link String#valueOf(Object)},
	 * which suits strings and org.json values.
	 */
	@NotNull
	public ServerSentEventWriter withSerializer(@NotNull PayloadSerializer serializer) {
		this.serializer = serializer;
		return this;
	}

	/**
	 * Returns false once the client has disconnected. Producers should stop when this becomes false.
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Writes a named event with a pre serialized payload.
	 */
	public void event(@NotNull String name, @NotNull String data) throws IOException {
		event(null, name, data);
	}

	/**
	 * Writes an event with optional id and optional name and a pre serialized payload.
	 */
	public void event(@Nullable String id, @Nullable String name, @NotNull String data) throws IOException {
		write(w -> ServerSentEvents.writeEvent(w, id, name, data));
	}

	/**
	 * Writes a named event whose payload is serialized with the configured {@link PayloadSerializer}.
	 */
	public void event(@NotNull String name, @NotNull Object payload) throws IOException {
		event(name, serializer.serialize(payload));
	}

	/**
	 * Writes an unnamed event, which clients receive as a plain message.
	 */
	public void data(@NotNull String data) throws IOException {
		write(w -> ServerSentEvents.writeData(w, data));
	}

	/**
	 * Writes a comment line, which clients ignore.
	 */
	public void comment(@Nullable String comment) throws IOException {
		write(w -> ServerSentEvents.writeComment(w, comment));
	}

	/**
	 * Writes a keep alive comment.
	 */
	public void heartbeat() throws IOException {
		comment(HEARTBEAT);
	}

	/**
	 * Tells the client how long to wait before reconnecting.
	 */
	public void retry(@NotNull Duration retry) throws IOException {
		write(w -> ServerSentEvents.writeRetry(w, retry));
	}

	/**
	 * Blocks until the future is done and writes a heartbeat whenever the stream has been idle for the given interval.
	 * The outcome of the future is not inspected, callers do that themselves. Returns early with
	 * {@link ClientDisconnectedException} if a heartbeat reveals that the client is gone.
	 */
	public void await(@NotNull Future<?> future, @NotNull Duration heartbeatInterval)
			throws IOException, InterruptedException {
		long intervalNanos = heartbeatInterval.toNanos();
		if (intervalNanos <= 0) throw new IllegalArgumentException("heartbeatInterval must be > 0");
		while (!future.isDone()) {
			long remaining = intervalNanos - (System.nanoTime() - lastWriteNanos);
			if (remaining <= 0) {
				heartbeat();
				continue;
			}
			try {
				future.get(remaining, TimeUnit.NANOSECONDS);
				return;
			}
			catch (TimeoutException ignored) {
				// idle for the whole interval, the next iteration writes the heartbeat
			}
			catch (ExecutionException | CancellationException e) {
				return;
			}
		}
	}

	/**
	 * Drains the iterator on the calling thread and hands time and size bounded batches to the batch writer. A
	 * {@link CloseableIterator} source is closed afterwards. Thread interruption ends the stream with an
	 * {@link InterruptedIOException}.
	 *
	 * @return true if the source still has items because the item limit was reached, false if it was fully consumed
	 */
	public <T> boolean streamBatched(@NotNull Iterator<T> source, @NotNull StreamOptions options,
									 @NotNull BatchWriter<T> writer) throws IOException {
		try {
			long budgetNanos = options.batchDuration().toNanos();
			int emitted = 0;
			while (emitted < options.maxItems() && source.hasNext()) {
				if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("streaming interrupted");
				if (!open) throw new ClientDisconnectedException();
				List<T> batch = new ArrayList<>();
				long start = System.nanoTime();
				do {
					batch.add(source.next());
					emitted++;
				} while (emitted < options.maxItems()
						&& batch.size() < options.maxBatchItems()
						&& System.nanoTime() - start < budgetNanos
						&& source.hasNext());
				writer.write(this, batch);
			}
			return source.hasNext();
		}
		finally {
			if (source instanceof CloseableIterator<?> closeable) {
				closeable.close();
			}
		}
	}

	private void write(Frame frame) throws IOException {
		synchronized (lock) {
			if (!open) throw new ClientDisconnectedException();
			try {
				frame.writeTo(out);
				out.flush();
			}
			catch (IOException e) {
				open = false;
				throw e;
			}
			// the servlet writer swallows IO errors and only records them in its error flag
			if (out instanceof PrintWriter printWriter && printWriter.checkError()) {
				open = false;
				throw new ClientDisconnectedException();
			}
			lastWriteNanos = System.nanoTime();
		}
	}

	@FunctionalInterface
	private interface Frame {
		void writeTo(Writer out) throws IOException;
	}
}
