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
		assertTrue(gitConnector.isClean());

		write();
		assertEquals(GitConnector.FileStatus.Untracked, gitConnector.getStatus(FILE));
	}

	@Test
	public void testGetStatusStaged() throws IOException {
		setUp(false);
		assertTrue(gitConnector.isClean());

		writeAndAddContentFile();
		gitConnector.addPath(FILE);
		assertEquals(GitConnector.FileStatus.Staged, gitConnector.getStatus(FILE));
	}

	@Test
	public void testGetStatusCommitted() throws IOException {
		setUp(false);
		assertTrue(gitConnector.isClean());

		writeAndAddContentFile();
		commit();
		assertEquals(GitConnector.FileStatus.Committed_Clean, gitConnector.getStatus(FILE));
	}

	@Test
	public void testGetStatusNotExisting() throws IOException {
		setUp(false);
		assertTrue(gitConnector.isClean());

		assertEquals(GitConnector.FileStatus.NotExisting, gitConnector.getStatus("NonExistentFile.txt"));
	}
}


