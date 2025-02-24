/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class GitConnectorSwitchBranchTest extends GitConnectorTestTemplate {

	public GitConnectorSwitchBranchTest(GitConnector connector) {
		this.gitConnector = connector;
	}

	@Test
	public void testSwitchBranch() throws IOException {
		setUp(false);
		assertEquals("main", gitConnector.currentBranch());
		String otherBranch = "myBranch";

		// we need an initial commit before we can switch a branch
		writeAndAddContentFile();
		gitConnector.commitPathsForUser("huhu", "markus merged", "m@merged.com", Collections.singleton(FILE));

		gitConnector.switchToBranch(otherBranch, true);
		assertEquals(otherBranch, gitConnector.currentBranch());

	}


}
