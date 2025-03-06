/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class GitConnectorIsCleanTest extends GitConnectorTestTemplate {

	public GitConnectorIsCleanTest(GitConnector connector) {
		this.gitConnector = connector;
	}

	@Test
	public void testIsClean() throws IOException {
		setUp(false);
		// should be clean at the beginning
		assertTrue(gitConnector.status().isClean());

		// we add some file
		writeAndAddContentFile();

		// then it should not be clean anymore
		assertFalse(gitConnector.status().isClean());

		// then we commit the file
		gitCommit();

		// should be clean again
		assertTrue(gitConnector.status().isClean());

	}


}
