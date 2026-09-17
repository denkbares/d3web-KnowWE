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
import java.io.Writer;
import java.time.Duration;

import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entry point for writing server sent events (SSE) from a servlet response.
 * <p>
 * Callers normally use {@link #open(HttpServletResponse)} and then talk to the returned thread safe
 * {@link ServerSentEventWriter}. The static frame writers below are the low level building blocks. They write to the
 * given writer without any synchronization or flushing and are mainly useful for tests and custom transports.
 */
public final class ServerSentEvents {

	public static final String CONTENT_TYPE = "text/event-stream";

	private ServerSentEvents() {
	}

	/**
	 * Prepares the response for an SSE stream and returns a thread safe writer for it. The response is committed by
	 * this call, so errors afterwards have to be reported as events.
	 */
	@NotNull
	public static ServerSentEventWriter open(@NotNull HttpServletResponse response) throws IOException {
		setHeaders(response);
		return new ServerSentEventWriter(response.getWriter());
	}

	/**
	 * Wraps an arbitrary writer into a thread safe SSE writer.
	 */
	@NotNull
	public static ServerSentEventWriter writer(@NotNull Writer out) {
		return new ServerSentEventWriter(out);
	}

	/**
	 * Sets status, content type, charset and proxy headers for an SSE response and commits them. Must be called
	 * before the writer of the response is obtained, otherwise the charset is not applied.
	 */
	public static void setHeaders(@NotNull HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding("UTF-8");
		response.setContentType(CONTENT_TYPE);
		response.setHeader("Cache-Control", "no-cache, no-transform");
		response.setHeader("Connection", "keep-alive");
		response.setHeader("X-Accel-Buffering", "no");
		response.flushBuffer();
	}

	/**
	 * Writes a named event with the given payload.
	 */
	public static void writeEvent(@NotNull Writer out, @NotNull String eventName, @NotNull String data)
			throws IOException {
		writeEvent(out, null, eventName, data);
	}

	/**
	 * Writes an event with optional id and optional name followed by the payload. Ids and names must be single line
	 * values without NUL characters.
	 */
	public static void writeEvent(@NotNull Writer out, @Nullable String id, @Nullable String eventName,
								  @NotNull String data) throws IOException {
		if (id != null) {
			out.write("id: ");
			out.write(requireFieldValue(id, "id"));
			out.write('\n');
		}
		if (eventName != null) {
			out.write("event: ");
			out.write(requireFieldValue(eventName, "event name"));
			out.write('\n');
		}
		writeData(out, data);
	}

	/**
	 * Writes the payload as one or more data lines followed by the blank line that ends the event. Line breaks of any
	 * kind are normalized to LF. An empty payload produces a single empty data line so the event is still dispatched.
	 */
	public static void writeData(@NotNull Writer out, @NotNull String data) throws IOException {
		writeLines(out, "data: ", data);
		out.write('\n');
	}

	/**
	 * Writes a comment, which clients ignore. Commonly used as a heartbeat to keep connections alive.
	 */
	public static void writeComment(@NotNull Writer out, @Nullable String comment) throws IOException {
		writeLines(out, ": ", comment == null ? "" : comment);
		out.write('\n');
	}

	/**
	 * Writes a retry field that tells the client how long to wait before reconnecting.
	 */
	public static void writeRetry(@NotNull Writer out, @NotNull Duration retry) throws IOException {
		out.write("retry: ");
		out.write(Long.toString(retry.toMillis()));
		out.write("\n\n");
	}

	private static void writeLines(Writer out, String prefix, String text) throws IOException {
		String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
		int length = normalized.length();
		int start = 0;
		do {
			int end = normalized.indexOf('\n', start);
			if (end == -1) end = length;
			out.write(prefix);
			out.write(normalized, start, end - start);
			out.write('\n');
			start = end + 1;
		} while (start < length);
	}

	private static String requireFieldValue(String value, String name) {
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '\n' || c == '\r' || c == '\0') {
				throw new IllegalArgumentException("SSE " + name + " must not contain line breaks or NUL");
			}
		}
		return value;
	}
}
