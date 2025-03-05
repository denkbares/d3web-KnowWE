/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class GitConnectorGetFileStatusTest extends GitConnectorTestTemplate {

	public GitConnectorGetFileStatusTest(GitConnector connector) {
		this.gitConnector = connector;
	}

	@Test
	public void testCycle() throws IOException {
		setUp(false);
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		assertEquals(GitConnectorStatus.FileStatus.NotExisting, gitConnector.status().ofFile(FILE));

		// we add some file
		write();
		assertEquals(GitConnectorStatus.FileStatus.Untracked, gitConnector.status().ofFile(FILE));
		gitAdd();

		assertEquals(GitConnectorStatus.FileStatus.Staged, gitConnector.status().ofFile(FILE));

		gitCommit();

		assertEquals(GitConnectorStatus.FileStatus.Committed_Clean, gitConnector.status().ofFile(FILE));


		FileUtils.write(CONTENT_FILE, "CONTENT2", Charset.defaultCharset());

		assertEquals(GitConnectorStatus.FileStatus.Committed_Modified, gitConnector.status().ofFile(FILE));

		gitDelete();

		assertEquals(GitConnectorStatus.FileStatus.Committed_Deleted, gitConnector.status().ofFile(FILE));


	}
}
