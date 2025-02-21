/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package de.uniwue.d3web.gitConnector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.uniwue.d3web.gitConnector.impl.raw.status.GitStatusCommandResult;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class GitConnectorDeleteFileTest extends GitConnectorTestTemplate {

	public GitConnectorDeleteFileTest(GitConnector connector) {
		this.gitConnector = connector;
	}

	@Test
	public void testDeleteAdded() throws IOException {
		setUp();
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		// we add some file
		writeAndAddContentFile();

		assertTrue(CONTENT_FILE.exists());

		// then we delete the file
		gitConnector.deletePaths(List.of(FILE), new UserData("huhu", "", ""), false );

		assertFalse(CONTENT_FILE.exists());
		assertTrue(gitConnector.isClean());

	}

	@Test
	public void testDeleteCommited() throws IOException {
		setUp();
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		// we add some file
		writeAndAddContentFile();
		gitConnector.commitPathsForUser("huhu", "", "", Collections.singleton(FILE));

		assertTrue(CONTENT_FILE.exists());

		// then we delete the file
		gitConnector.deletePaths(List.of(FILE), new UserData("huhu", "", ""), false );


		assertFalse(CONTENT_FILE.exists());

	}

	@Test
	public void testDeleteUntracked() throws IOException {
		setUp();
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		// we add some file
		FileUtils.write(CONTENT_FILE, "CONTENT", Charset.defaultCharset());

		assertTrue(CONTENT_FILE.exists());

		// then we delete the file
		gitConnector.deletePaths(List.of(FILE), new UserData("huhu", "", ""), false );

		assertTrue(CONTENT_FILE.exists());

	}


}
