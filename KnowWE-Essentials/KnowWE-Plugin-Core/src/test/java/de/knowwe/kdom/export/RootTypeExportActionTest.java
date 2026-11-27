/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.kdom.export;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * The export runs dot as an external process. A dot that does not end (e.g. on a huge or pathological graph) must not
 * keep the request thread forever, and must not stay behind as a process.
 */
public class RootTypeExportActionTest {

	@Test
	public void aCommandNotEndingInTimeIsKilled() throws Exception {
		assumeTrue("needs the sleep command", new java.io.File("/bin/sleep").canExecute()
				|| new java.io.File("/usr/bin/sleep").canExecute());
		long start = System.nanoTime();
		try {
			RootTypeExportAction.runCommand(List.of("sleep", "20"), Duration.ofSeconds(1));
			fail("A command exceeding the timeout must fail");
		}
		catch (IOException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("sleep"));
		}
		assertTrue("The command must be given up after the timeout",
				Duration.ofNanos(System.nanoTime() - start).compareTo(Duration.ofSeconds(10)) < 0);
		assertTrue("The command must be killed", ProcessHandle.current().children()
				.filter(ProcessHandle::isAlive)
				.noneMatch(child -> child.info().command().map(command -> command.endsWith("sleep")).orElse(false)));
	}

	@Test
	public void aFailingCommandIsReported() {
		assertThrows(IOException.class, () -> RootTypeExportAction.runCommand(List.of("false"), Duration.ofSeconds(10)));
	}
}
