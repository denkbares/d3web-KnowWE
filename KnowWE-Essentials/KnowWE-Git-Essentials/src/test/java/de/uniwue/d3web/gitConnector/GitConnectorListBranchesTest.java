/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class GitConnectorListBranchesTest extends GitConnectorTestTemplate {

	public GitConnectorListBranchesTest(GitConnector connector)  {
		this.gitConnector = connector;
	}


	@Test
	public void testListBranches() throws IOException {
		setUp();

		// for some reason git only list branches after the first commit has been made: before -> 0
		List<String> branches0 = gitConnector.listBranches(false);
		assertEquals(0, branches0.size());

		writeAndAddContentFile();
		gitConnector.commitPathsForUser("", "markus merged", "m@merged.com", Collections.singleton(FILE));
		List<String> branches1 = gitConnector.listBranches(false);
		assertEquals(1, branches1.size());
		assertTrue(branches1.contains(GitConnector.DEFAULT_BRANCH));

		String otherBranch = "otherBranch";
		gitConnector.switchToBranch(otherBranch, true);

		List<String> branches2 = gitConnector.listBranches(false);
		assertEquals(2, branches2.size());
		assertTrue(branches2.contains(GitConnector.DEFAULT_BRANCH));
		assertTrue(branches2.contains(otherBranch));

	}
}
