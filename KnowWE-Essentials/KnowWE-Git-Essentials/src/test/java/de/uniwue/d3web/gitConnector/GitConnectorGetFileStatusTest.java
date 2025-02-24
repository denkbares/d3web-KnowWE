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

		assertEquals(GitConnector.FileStatus.NotExisting, gitConnector.getStatus(FILE));

		// we add some file
		write();
		assertEquals(GitConnector.FileStatus.Untracked, gitConnector.getStatus(FILE));
		add();

		assertEquals(GitConnector.FileStatus.Staged, gitConnector.getStatus(FILE));

		commit();

		assertEquals(GitConnector.FileStatus.Committed_Clean, gitConnector.getStatus(FILE));


		FileUtils.write(CONTENT_FILE, "CONTENT2", Charset.defaultCharset());

		assertEquals(GitConnector.FileStatus.Committed_Modified, gitConnector.getStatus(FILE));

		//gitConnector.deletePaths(List.of(FILE), new UserData("huhu", "", ""), false );
		delete();

		assertEquals(GitConnector.FileStatus.Committed_Deleted, gitConnector.getStatus(FILE));


	}
}
