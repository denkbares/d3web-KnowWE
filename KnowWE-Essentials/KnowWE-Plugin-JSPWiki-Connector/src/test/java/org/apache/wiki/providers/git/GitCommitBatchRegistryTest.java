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
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Standalone tests for {@link GitCommitBatchRegistry} / {@link GitCommitBatch} against two real temp git repositories,
 * no JSPWiki engine or provider involved.
 */
public class GitCommitBatchRegistryTest {

	private static final String AUTHOR = "Batch Tester";
	private static final String EMAIL = "batch@test.invalid";

	private File repo1;
	private File repo2;
	private GitConnector connector1;
	private GitConnector connector2;
	private GitCommitBatchRegistry registry;

	@Before
	public void setUp() throws IOException {
		File base = new File(System.getProperty("java.io.tmpdir"), "GitCommitBatchRegistryTest");
		FileUtils.deleteDirectory(base);
		repo1 = initRepo(new File(base, "repo1"));
		repo2 = initRepo(new File(base, "repo2"));
		connector1 = BareGitConnector.fromPath(repo1.getAbsolutePath());
		connector2 = BareGitConnector.fromPath(repo2.getAbsolutePath());
		assumeTrue(connector1.gitInstalledAndReady());
		// resolver maps each repo key to its connector, the seam that decouples the registry from the pool
		Map<String, GitConnector> connectors = Map.of(
				repo1.getAbsolutePath(), connector1,
				repo2.getAbsolutePath(), connector2);
		registry = new GitCommitBatchRegistry(connectors::get);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir"), "GitCommitBatchRegistryTest"));
	}

	/**
	 * A batch spanning two repos commits exactly once per repo, even when a repo has several staged files.
	 */
	@Test
	public void commitProducesOneCommitPerRepo() throws IOException {
		registry.open("alice");

		// repo1 gets two files — they must end up in a single commit, not two.
		stageNewFile(connector1, repo1, "alice", repo1Key(), "A.txt");
		stageNewFile(connector1, repo1, "alice", repo1Key(), "B.txt");
		// repo2 gets one file.
		stageNewFile(connector2, repo2, "alice", repo2Key(), "C.txt");

		List<GitCommitBatchRegistry.RepoCommitResult> results =
				registry.commit("alice", "bulk edit", AUTHOR, EMAIL);

		// one result per touched repo
		assertEquals(2, results.size());
		assertFalse("batch must be closed after commit", registry.isOpen("alice"));

		// repo1: both files share the same single commit
		List<String> a = connector1.log().commitHashesForFile("A.txt");
		List<String> b = connector1.log().commitHashesForFile("B.txt");
		assertEquals(1, a.size());
		assertEquals(1, b.size());
		assertEquals("the two repo1 files belong to one commit", a.get(0), b.get(0));

		// repo2: its own single commit, distinct from repo1's
		List<String> c = connector2.log().commitHashesForFile("C.txt");
		assertEquals(1, c.size());

		// results carry the right repos, hashes and paths
		GitCommitBatchRegistry.RepoCommitResult r1 = resultFor(results, repo1Key());
		assertNotNull(r1);
		assertEquals(a.get(0), r1.commitHash());
		assertEquals(Set.of("A.txt", "B.txt"), r1.paths());
		GitCommitBatchRegistry.RepoCommitResult r2 = resultFor(results, repo2Key());
		assertNotNull(r2);
		assertEquals(c.get(0), r2.commitHash());
	}

	/**
	 * Rolling back a batch discards the staged paths and produces no commit.
	 */
	@Test
	public void rollbackDiscardsStagedPaths() throws IOException {
		registry.open("bob");
		stageNewFile(connector1, repo1, "bob", repo1Key(), "Draft.txt");

		List<GitCommitBatchRegistry.RepoRollbackResult> rolledBack = registry.rollback("bob");

		assertEquals("one touched repo", 1, rolledBack.size());
		assertEquals(repo1Key(), rolledBack.get(0).repoKey());
		assertEquals("restored paths reported for cache refresh", Set.of("Draft.txt"), rolledBack.get(0).paths());
		assertFalse("batch must be closed after rollback", registry.isOpen("bob"));
		assertEquals("rolled-back file must not be committed", 0,
				connector1.log().commitHashesForFile("Draft.txt").size());
	}

	/**
	 * Staging without an open batch signals "commit immediately" via a false return; nothing is recorded.
	 */
	@Test
	public void stageWithoutOpenBatchSignalsCommitImmediately() {
		assertFalse(registry.isOpen("carol"));
		boolean staged = registry.stage("carol", repo1Key(), "Loose.txt");
		assertFalse("stage without open batch must report commit-immediately", staged);

		// committing a never-opened user is a harmless no-op
		assertTrue(registry.commit("carol", "msg", AUTHOR, EMAIL).isEmpty());
	}

	/**
	 * Concurrent batches of different users are independent: committing one leaves the other open and intact.
	 */
	@Test
	public void concurrentBatchesOfDifferentUsersDoNotInterfere() throws IOException {
		registry.open("dave");
		registry.open("erin");
		stageNewFile(connector1, repo1, "dave", repo1Key(), "Dave.txt");
		stageNewFile(connector1, repo1, "erin", repo1Key(), "Erin.txt");

		// commit dave only
		List<GitCommitBatchRegistry.RepoCommitResult> daveResults =
				registry.commit("dave", "dave edit", AUTHOR, EMAIL);
		assertEquals(1, daveResults.size());
		assertEquals(1, connector1.log().commitHashesForFile("Dave.txt").size());

		// erin's batch is untouched and still open; her file is not committed yet
		assertTrue(registry.isOpen("erin"));
		assertFalse(registry.isOpen("dave"));
		assertEquals(0, connector1.log().commitHashesForFile("Erin.txt").size());

		// committing erin now picks up only her file in its own commit
		registry.commit("erin", "erin edit", AUTHOR, EMAIL);
		List<String> erinCommits = connector1.log().commitHashesForFile("Erin.txt");
		assertEquals(1, erinCommits.size());
		assertNotEquals(
				"dave's and erin's commits are distinct",
				erinCommits.get(0), connector1.log().commitHashesForFile("Dave.txt").get(0)
		);
	}

	// --- helpers -------------------------------------------------------------

	private String repo1Key() {
		return repo1.getAbsolutePath();
	}

	private String repo2Key() {
		return repo2.getAbsolutePath();
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
	private void stageNewFile(GitConnector connector, File repo, String user, String repoKey, String name) throws IOException {
		FileUtils.writeStringToFile(new File(repo, name), "content of " + name, StandardCharsets.UTF_8);
		connector.commit().addPath(name);
		assertTrue(registry.stage(user, repoKey, name));
	}

	private static GitCommitBatchRegistry.RepoCommitResult resultFor(List<GitCommitBatchRegistry.RepoCommitResult> results, String repoKey) {
		return results.stream().filter(r -> r.repoKey().equals(repoKey)).findFirst().orElse(null);
	}
}
