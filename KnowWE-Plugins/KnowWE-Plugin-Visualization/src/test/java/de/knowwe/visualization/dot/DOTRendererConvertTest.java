/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.visualization.dot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.knowwe.visualization.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Tests the execution of dot: a hanging dot process must be killed after the timeout instead of blocking the
 * rendering thread forever, and lots of error output must not block dot. The tests run a shell script in place of
 * dot, so they need a unix shell.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 26.11.2026
 */
public class DOTRendererConvertTest {

	private static final String SHELL = "/bin/sh";

	private Path directory;
	private File dot;
	private File output;

	@Before
	public void setUp() throws IOException {
		assumeTrue("needs " + SHELL, new File(SHELL).canExecute());
		directory = Files.createTempDirectory("dot-renderer-test");
		dot = Files.writeString(directory.resolve("graph.dot"), "digraph { a -> b }").toFile();
		output = directory.resolve("graph.svg").toFile();
		Config.GRAPH_HAS_ERRORS = false;
		Config.GRAPH_ERROR_MESSAGE = null;
	}

	@After
	public void tearDown() throws IOException {
		Config.GRAPH_HAS_ERRORS = false;
		Config.GRAPH_ERROR_MESSAGE = null;
		if (directory == null) return;
		try (var files = Files.list(directory)) {
			for (Path file : files.toList()) Files.deleteIfExists(file);
		}
		Files.deleteIfExists(directory);
	}

	@Test(timeout = 60000)
	public void hangingDotIsKilledAfterTheTimeout() throws Exception {
		Path pidFile = directory.resolve("pid");
		long start = System.currentTimeMillis();
		try {
			DOTRenderer.convertDot(output, dot, 1000, SHELL, "-c", "echo $$ > '" + pidFile + "'; exec sleep 600");
			fail("a hanging dot must fail");
		}
		catch (IOException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("timed out"));
		}
		assertTrue("waited too long for the hanging dot", System.currentTimeMillis() - start < 20000);
		long pid = Long.parseLong(Files.readString(pidFile).trim());
		assertFalse("the hanging dot is still running", isAlive(pid));
	}

	@Test(timeout = 60000)
	public void muchErrorOutputDoesNotBlockDot() throws Exception {
		// far more than fits into the buffer of the error pipe, dot would wait for it to be read
		DOTRenderer.convertDot(output, dot, 20000, SHELL, "-c", "seq 1 50000 >&2; exit 0");
		assertFalse(Config.GRAPH_HAS_ERRORS);
	}

	@Test(timeout = 60000)
	public void errorOfDotIsReported() throws Exception {
		try {
			DOTRenderer.convertDot(output, dot, 20000, SHELL, "-c", "echo 'syntax error in line 1' >&2; exit 1");
			fail("a failing dot must fail");
		}
		catch (IOException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("could not successfully be executed"));
		}
		assertTrue(Config.GRAPH_HAS_ERRORS);
		assertTrue(Config.GRAPH_ERROR_MESSAGE, Config.GRAPH_ERROR_MESSAGE.contains("syntax error in line 1"));
	}

	private static boolean isAlive(long pid) throws InterruptedException {
		// killing the process takes a moment
		long end = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
		while (System.currentTimeMillis() < end) {
			if (ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false)) {
				Thread.sleep(50);
			}
			else {
				return false;
			}
		}
		return true;
	}
}
