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
public class GitConnectorDeleteFileTest extends GitConnectorTestTemplate {

	public GitConnectorDeleteFileTest(GitConnector connector) {
		this.gitConnector = connector;
	}

	@Test
	public void testDeleteAdded() throws IOException {
		setUp(false);
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		// we add some file
		writeAndAddContentFile();

		assertTrue(CONTENT_FILE.exists());

		// then we delete the file
		gitDelete(false);

		assertFalse(CONTENT_FILE.exists());
		assertTrue(gitConnector.isClean());

	}

	@Test
	public void testDeleteAddedCached() throws IOException {
		setUp(true);
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		// we add some file
		writeAndAddContentFile();

		assertTrue(CONTENT_FILE.exists());

		// then we delete the file
		gitDelete(true);

		assertTrue(CONTENT_FILE.exists());
		assertFalse(gitConnector.isClean());
		GitConnectorStatus.FileStatus status = gitConnector.status().ofFile(FILE);
		assertEquals(GitConnectorStatus.FileStatus.Untracked, status);
	}

	@Test
	public void testDeleteCommited() throws IOException {
		setUp(false);
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		// we add some file
		writeAndAddContentFile();
		gitCommit();

		assertTrue(CONTENT_FILE.exists());

		// then we delete the file
		gitDelete(false);

		assertFalse(CONTENT_FILE.exists());

	}

	@Test
	public void testDeleteCommitedCached() throws IOException {
		setUp(false);
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		// we add some file
		writeAndAddContentFile();
		gitCommit();

		assertTrue(CONTENT_FILE.exists());

		// then we delete the file
		gitDelete(true);

		assertTrue(CONTENT_FILE.exists());

	}



	@Test
	public void testDeleteUntracked() throws IOException {
		setUp(false);
		// should be clean at the beginning
		assertTrue(gitConnector.isClean());

		// we add some file
		FileUtils.write(CONTENT_FILE, "CONTENT", Charset.defaultCharset());

		assertTrue(CONTENT_FILE.exists());

		// then we delete the file
		gitDelete(false);

		assertTrue(CONTENT_FILE.exists());

	}


}
