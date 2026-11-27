/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.diaflux.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Tests the call of Graphviz for the layout of flowcharts: a hanging dot must be killed after the timeout instead of
 * blocking the request forever. The tests run a shell script in place of dot, so they need a unix shell.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 26.11.2026
 */
public class GraphvizConnectorTest {

	private Path directory;

	@Before
	public void setUp() throws IOException {
		assumeTrue("needs /bin/sh", new File("/bin/sh").canExecute());
		directory = Files.createTempDirectory("graphviz-connector-test");
	}

	@After
	public void tearDown() throws IOException {
		if (directory == null) return;
		try (var files = Files.list(directory)) {
			for (Path file : files.toList()) Files.deleteIfExists(file);
		}
		Files.deleteIfExists(directory);
	}

	@Test(timeout = 60000)
	public void hangingDotIsKilledAfterTheTimeout() throws Exception {
		Path pidFile = directory.resolve("pid");
		String dot = script("echo $$ > '" + pidFile + "'\nexec sleep 600");
		long start = System.currentTimeMillis();
		try {
			new GraphvizConnector(dot, 1000).execute("digraph { a -> b }");
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
	public void outputOfDotIsReturned() throws Exception {
		// dot -q -o <output> -Tdot <input>: the fake one copies the input to the output
		String dot = script("cp \"$5\" \"$3\"");
		assertEquals("digraph { a -> b }", new GraphvizConnector(dot, 20000).execute("digraph { a -> b }"));
	}

	@Test(timeout = 60000)
	public void errorOfDotIsReported() throws Exception {
		String dot = script("echo 'syntax error in line 1' >&2");
		try {
			new GraphvizConnector(dot, 20000).execute("digraph { a -> }");
			fail("a failing dot must fail");
		}
		catch (IOException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("syntax error in line 1"));
		}
	}

	/**
	 * Creates an executable shell script with the specified commands, to be run in place of dot.
	 */
	private String script(String commands) throws IOException {
		Path script = directory.resolve("dot");
		Files.writeString(script, "#!/bin/sh\n" + commands + "\n");
		assertTrue(script.toFile().setExecutable(true));
		return script.toString();
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
