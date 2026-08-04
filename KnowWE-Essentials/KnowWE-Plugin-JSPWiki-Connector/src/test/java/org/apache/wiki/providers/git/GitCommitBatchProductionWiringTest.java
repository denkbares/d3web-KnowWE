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
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Batch-commit tests against the exact connector wiring the multi-wiki provider builds in production
 * ({@code CachingGitConnector(JGitBackedGitConnector)}), guarding the seam a plain {@code BareGitConnector} test
 * cannot cover: the routing of {@code commitPathsForUser} inside the mixed connector. Historically that route ignored
 * the pathspec and committed the whole index (empty commits for modified pages, cross-user contamination), which the
 * bare-connector tests could not detect.
 */
public class GitCommitBatchProductionWiringTest {

	private static final String AUTHOR = "Batch Tester";
	private static final String EMAIL = "batch@test.invalid";

	private File repo;
	private GitConnector connector;
	private GitCommitBatchRegistry registry;

	@Before
	public void setUp() throws IOException {
		File base = new File(System.getProperty("java.io.tmpdir"), "GitCommitBatchProductionWiringTest");
		FileUtils.deleteDirectory(base);
		repo = new File(base, "repo");
		Files.createDirectories(repo.toPath());
		RawGitExecutor.executeGitCommand(new String[] { "git", "init" }, repo.getAbsolutePath());
		// the provider's production wiring, see GitVersioningFileProviderMultiWiki#createHistory
		connector = new CachingGitConnector(JGitBackedGitConnector.fromPath(repo.getAbsolutePath()));
		assumeTrue(connector.gitInstalledAndReady());
		commitNewFile("Existing.txt", "original content");
		registry = new GitCommitBatchRegistry(Map.of(repoKey(), connector)::get);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir"), "GitCommitBatchProductionWiringTest"));
	}

	/**
	 * Editing an existing (tracked) page inside a transaction must produce a real commit containing the change, with
	 * the batch author and message, and leave the working tree clean. Tracked modifications are not staged by the
	 * provider (only new files are), the pathspec commit has to pick them up from the working tree.
	 */
	@Test
	public void editingExistingPageInBatchCommitsTheChange() throws IOException {
		registry.open("alice");
		FileUtils.writeStringToFile(new File(repo, "Existing.txt"), "edited content", StandardCharsets.UTF_8);
		assertTrue(registry.stage("alice", repoKey(), "Existing.txt"));

		List<GitCommitBatchRegistry.RepoCommitResult> results =
				registry.commit("alice", "transaction edit", AUTHOR, EMAIL);

		assertEquals(1, results.size());
		String commitHash = results.get(0).commitHash();
		assertNotNull("commit hash must be reported", commitHash);

		// the commit is the file's second version and actually contains the change
		List<String> commits = connector.log().commitHashesForFile("Existing.txt");
		assertEquals("edit must create a second commit for the file", 2, commits.size());
		assertEquals(commitHash, commits.get(commits.size() - 1));
		assertEquals(List.of("Existing.txt"), connector.log().listChangedFilesForHash(commitHash));

		// authorship and message are the batch's, not lost to a later reconciliation commit
		CommitUserData userData = connector.commit().userDataFor(commitHash);
		assertEquals(AUTHOR, userData.user);
		assertEquals(EMAIL, userData.email);
		assertEquals("transaction edit", userData.message);

		// nothing left dirty for a sweep-up to scoop up
		assertTrue("working tree must be clean after the batch commit", connector.status().isClean());
	}

	/**
	 * A batch commit must contain only its own staged paths: a concurrently open batch of another user (with a new
	 * file already added to the git index) must not be swept into the commit.
	 */
	@Test
	public void concurrentOpenBatchIsNotSweptIntoOtherUsersCommit() throws IOException {
		// alice has an open batch with a new page, already staged in the index (the provider stages new files)
		registry.open("alice");
		FileUtils.writeStringToFile(new File(repo, "AlicePage.txt"), "alice draft", StandardCharsets.UTF_8);
		connector.commit().addPath("AlicePage.txt");
		assertTrue(registry.stage("alice", repoKey(), "AlicePage.txt"));

		// bob edits an existing page in his own batch and commits first
		registry.open("bob");
		FileUtils.writeStringToFile(new File(repo, "Existing.txt"), "bob's edit", StandardCharsets.UTF_8);
		assertTrue(registry.stage("bob", repoKey(), "Existing.txt"));
		List<GitCommitBatchRegistry.RepoCommitResult> bobResults =
				registry.commit("bob", "bob edit", AUTHOR, EMAIL);

		assertEquals(1, bobResults.size());
		String bobCommit = bobResults.get(0).commitHash();
		assertEquals("bob's commit must contain only his page",
				List.of("Existing.txt"), connector.log().listChangedFilesForHash(bobCommit));
		assertEquals("alice's staged page must not be committed",
				0, connector.log().commitHashesForFile("AlicePage.txt").size());
		assertTrue(registry.isOpen("alice"));
	}

	/**
	 * A batch whose staged paths have no changes left (e.g. the edit was reverted on disk) commits nothing and
	 * reports no result, instead of producing an empty commit or logging a failure.
	 */
	@Test
	public void batchWithoutChangesCommitsNothing() {
		registry.open("carol");
		// stage the tracked file without modifying it
		assertTrue(registry.stage("carol", repoKey(), "Existing.txt"));

		String headBefore = connector.log().currentHEAD();
		List<GitCommitBatchRegistry.RepoCommitResult> results =
				registry.commit("carol", "no-op", AUTHOR, EMAIL);

		assertTrue("no commit result for a change-less batch", results.isEmpty());
		assertEquals("HEAD must not move", headBefore, connector.log().currentHEAD());
		assertFalse(registry.isOpen("carol"));
	}

	/**
	 * Null users (pages saved without an author) never see an open batch and cannot open one, the caller falls back
	 * to the immediate-commit path instead of hitting an NPE.
	 */
	@Test
	public void nullUserIsToleratedByTheRegistry() {
		assertFalse(registry.isOpen(null));
		assertFalse(registry.stage(null, repoKey(), "Existing.txt"));
		registry.open(null);
		assertFalse(registry.isOpen(null));
		assertTrue(registry.commit(null, "msg", AUTHOR, EMAIL).isEmpty());
		assertTrue(registry.rollback(null).isEmpty());
	}

	// --- helpers -------------------------------------------------------------

	private String repoKey() {
		return repo.getAbsolutePath();
	}

	private void commitNewFile(String name, String content) throws IOException {
		FileUtils.writeStringToFile(new File(repo, name), content, StandardCharsets.UTF_8);
		connector.commit().addPath(name);
		connector.commit().commitPathsForUser("initial commit", AUTHOR, EMAIL, java.util.Set.of(name));
	}
}
