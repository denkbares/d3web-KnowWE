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
import java.io.StringWriter;
import java.time.Duration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Wire format tests against the examples of the WHATWG server sent events specification.
 */
public class ServerSentEventsTest {

	@Test
	public void specStockTickerExample() throws IOException {
		// the spec shows a three line payload as three data lines followed by a blank line
		StringWriter out = new StringWriter();
		ServerSentEvents.writeData(out, "YHOO\n+2\n10");
		assertEquals("data: YHOO\ndata: +2\ndata: 10\n\n", out.toString());
	}

	@Test
	public void specNamedEventExample() throws IOException {
		// the spec dispatches named events with an event line before the data line
		StringWriter out = new StringWriter();
		ServerSentEvents.writeEvent(out, "add", "73857293");
		ServerSentEvents.writeEvent(out, "remove", "2153");
		assertEquals("event: add\ndata: 73857293\n\nevent: remove\ndata: 2153\n\n", out.toString());
	}

	@Test
	public void specIdAndCommentExample() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writeComment(out, "test stream");
		ServerSentEvents.writeEvent(out, "1", null, "first event");
		assertEquals(": test stream\n\nid: 1\ndata: first event\n\n", out.toString());
	}

	@Test
	public void writeData_normalizesCrlfAndCr() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writeData(out, "a\r\nb\nc\rd");
		assertEquals("data: a\ndata: b\ndata: c\ndata: d\n\n", out.toString());
	}

	@Test
	public void writeData_emptyPayloadStillDispatches() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writeData(out, "");
		assertEquals("data: \n\n", out.toString());
	}

	@Test
	public void writeData_trailingLineBreakProducesNoEmptyLine() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writeData(out, "x\n");
		assertEquals("data: x\n\n", out.toString());
	}

	@Test
	public void writeEvent_withIdAndName() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writeEvent(out, "42", "progress", "{\"p\":0.5}");
		assertEquals("id: 42\nevent: progress\ndata: {\"p\":0.5}\n\n", out.toString());
	}

	@Test
	public void writeComment_multiLineBecomesSeveralCommentLines() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writeComment(out, "keep\r\nalive");
		assertEquals(": keep\n: alive\n\n", out.toString());
	}

	@Test
	public void writeComment_nullIsEmptyComment() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writeComment(out, null);
		assertEquals(": \n\n", out.toString());
	}

	@Test
	public void writeRetry_inMillis() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writeRetry(out, Duration.ofSeconds(3));
		assertEquals("retry: 3000\n\n", out.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void writeEvent_rejectsLineBreakInName() throws IOException {
		ServerSentEvents.writeEvent(new StringWriter(), "bad\nname", "x");
	}

	@Test(expected = IllegalArgumentException.class)
	public void writeEvent_rejectsNulInId() throws IOException {
		ServerSentEvents.writeEvent(new StringWriter(), "a\0b", "name", "x");
	}

	@Test
	public void writeData_preservesNonAsciiCharacters() throws IOException {
		StringWriter out = new StringWriter();
		ServerSentEvents.writeData(out, "Grüße ✓");
		assertEquals("data: Grüße ✓\n\n", out.toString());
	}
}
