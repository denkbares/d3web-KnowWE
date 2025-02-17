/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class GitConnectorIsCleanTest extends GitConnectorTestTemplate{

	public GitConnectorIsCleanTest(GitConnector connector) throws IOException {
		this.gitConnector = connector;
	}

	@Test
	public void testIsClean() throws IOException {
		setUp();
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		// we add some file
		writeAndAddContentFile();

		// then it should not be clean anymore
		assertFalse(gitConnector.isClean());

		// then we commit the file
		gitConnector.commitPathsForUser("huhu", "", "", Collections.singleton(FILE));

		// should be clean again
		assertTrue(gitConnector.isClean());

	}
}
