/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.apache.wiki.providers.git;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Standalone tests for {@link GitCommitBatchRegistry} / {@link GitCommitBatch} against a real temp git repository, no
 * JSPWiki engine or provider involved.
 */
public class GitCommitBatchRegistryTest {

	private static final String AUTHOR = "Batch Tester";
	private static final String EMAIL = "batch@test.invalid";

	private File repo;
	private GitConnector connector;
	private GitCommitBatchRegistry registry;

	@Before
	public void setUp() throws IOException {
		File base = new File(System.getProperty("java.io.tmpdir"), "GitCommitBatchRegistryTest");
		FileUtils.deleteDirectory(base);
		repo = initRepo(new File(base, "repo"));
		connector = BareGitConnector.fromPath(repo.getAbsolutePath());
		assumeTrue(connector.gitInstalledAndReady());
		registry = new GitCommitBatchRegistry(new GitPageHistory(connector));
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir"), "GitCommitBatchRegistryTest"));
	}

	/**
	 * A batch with several staged files commits exactly once, and the result carries the hash and the paths.
	 */
	@Test
	public void commitProducesOneCommitForAllStagedPaths() throws IOException {
		registry.open("alice");
		stageNewFile("alice", "A.txt");
		stageNewFile("alice", "B.txt");

		GitCommitBatchRegistry.CommitResult result = registry.commit("alice", userData("bulk edit"));

		assertNotNull(result);
		assertFalse("batch must be closed after commit", registry.isOpen("alice"));

		// both files share the same single commit
		List<String> a = connector.log().commitHashesForFile("A.txt");
		List<String> b = connector.log().commitHashesForFile("B.txt");
		assertEquals(1, a.size());
		assertEquals(1, b.size());
		assertEquals("the two files belong to one commit", a.get(0), b.get(0));

		assertEquals(a.get(0), result.commitHash());
		assertEquals(Set.of("A.txt", "B.txt"), result.paths());
	}

	/**
	 * Rolling back a batch discards the staged paths and produces no commit.
	 */
	@Test
	public void rollbackDiscardsStagedPaths() throws IOException {
		registry.open("bob");
		stageNewFile("bob", "Draft.txt");

		Set<String> rolledBack = registry.rollback("bob");

		assertEquals("restored paths reported for cache refresh", Set.of("Draft.txt"), rolledBack);
		assertFalse("batch must be closed after rollback", registry.isOpen("bob"));
		assertEquals("rolled-back file must not be committed", 0,
				connector.log().commitHashesForFile("Draft.txt").size());
	}

	/**
	 * Staging without an open batch signals "commit immediately" via a false return; nothing is recorded.
	 */
	@Test
	public void stageWithoutOpenBatchSignalsCommitImmediately() {
		assertFalse(registry.isOpen("carol"));
		boolean staged = registry.stage("carol", "Loose.txt");
		assertFalse("stage without open batch must report commit-immediately", staged);

		// committing a never-opened user is a harmless no-op
		assertNull(registry.commit("carol", userData("msg")));
	}

	/**
	 * Concurrent batches of different users are independent: committing one leaves the other open and intact.
	 */
	@Test
	public void concurrentBatchesOfDifferentUsersDoNotInterfere() throws IOException {
		registry.open("dave");
		registry.open("erin");
		stageNewFile("dave", "Dave.txt");
		stageNewFile("erin", "Erin.txt");

		// commit dave only
		GitCommitBatchRegistry.CommitResult daveResult = registry.commit("dave", userData("dave edit"));
		assertNotNull(daveResult);
		assertEquals(1, connector.log().commitHashesForFile("Dave.txt").size());

		// erin's batch is untouched and still open; her file is not committed yet
		assertTrue(registry.isOpen("erin"));
		assertFalse(registry.isOpen("dave"));
		assertEquals(0, connector.log().commitHashesForFile("Erin.txt").size());

		// committing erin now picks up only her file in its own commit
		GitCommitBatchRegistry.CommitResult erinResult = registry.commit("erin", userData("erin edit"));
		assertNotNull(erinResult);
		assertEquals(Set.of("Erin.txt"), erinResult.paths());
		assertNotEquals("dave's and erin's commits are distinct", daveResult.commitHash(), erinResult.commitHash());
	}

	// --- helpers -------------------------------------------------------------

	private static CommitUserData userData(String message) {
		return new CommitUserData(AUTHOR, EMAIL, message);
	}

	/**
	 * Initializes a git repo with one initial commit, so HEAD exists (mirrors a real wiki repo).
	 */
	private File initRepo(File dir) throws IOException {
		Files.createDirectories(dir.toPath());
		RawGitExecutor.executeGitCommand("git init", dir.getAbsolutePath());
		File init = new File(dir, "Init.txt");
		FileUtils.writeStringToFile(init, "init", StandardCharsets.UTF_8);
		GitConnector c = BareGitConnector.fromPath(dir.getAbsolutePath());
		c.commit().addPath("Init.txt");
		c.commit().commitPathsForUser("initial commit", AUTHOR, EMAIL, Collections.singleton("Init.txt"));
		return dir;
	}

	/**
	 * Writes a new file, stages it in git (the provider's job at put-time), then registers its path
	 * in the user's open batch — exactly the sequence the page provider performs while batching.
	 */
	private void stageNewFile(String user, String name) throws IOException {
		FileUtils.writeStringToFile(new File(repo, name), "content of " + name, StandardCharsets.UTF_8);
		connector.commit().addPath(name);
		assertTrue(registry.stage(user, name));
	}
}
