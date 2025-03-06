/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

import java.io.IOException;

@RunWith(Parameterized.class)
public class GitConnectorGetStatusTest extends GitConnectorTestTemplate {

	public GitConnectorGetStatusTest(GitConnector connector) {
		this.gitConnector = connector;
	}

	@Test
	public void testGetStatusUntracked() throws IOException {
		setUp(false);
		assertTrue(gitConnector.status().isClean());

		write();
		assertEquals(GitConnectorStatus.FileStatus.Untracked, gitConnector.status().ofFile(FILE));
	}

	@Test
	public void testGetStatusStaged() throws IOException {
		setUp(false);
		assertTrue(gitConnector.status().isClean());

		writeAndAddContentFile();
		gitConnector.addPath(FILE);
		GitConnectorStatus status = gitConnector.status();
		assertEquals(GitConnectorStatus.FileStatus.Staged, status.ofFile(FILE));
	}

	@Test
	public void testGetStatusCommitted() throws IOException {
		setUp(false);
		assertTrue(gitConnector.status().isClean());

		writeAndAddContentFile();
		gitCommit();
		assertEquals(GitConnectorStatus.FileStatus.Committed_Clean, gitConnector.status().ofFile(FILE));
	}

	@Test
	public void testGetStatusNotExisting() throws IOException {
		setUp(false);
		assertTrue(gitConnector.status().isClean());

		assertEquals(GitConnectorStatus.FileStatus.NotExisting, gitConnector.status().ofFile("NonExistentFile.txt"));
	}
}


