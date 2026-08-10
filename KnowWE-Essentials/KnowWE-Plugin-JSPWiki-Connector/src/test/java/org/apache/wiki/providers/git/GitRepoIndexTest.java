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

import org.apache.commons.io.FileUtils;
import org.apache.wiki.api.providers.WikiProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitFileRevision;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Cache-safety tests for {@link GitRepoIndex} against a real temp git repository (no JSPWiki engine). The index is the
 * eager read model behind {@code getAllPages}/{@code getVersionHistory}. These tests pin its invalidation contract:
 * served from memory when {@code HEAD} is unchanged, folded forward on a fast-forward, and <b>fully rebuilt on a
 * non-fast-forward</b> (backward {@code reset --hard}) or branch switch, which is the gap the per-file connector cache
 * leaves open.
 */
public class GitRepoIndexTest {

	private File repo;
	private GitConnector connector;
	private GitRepoIndex index;

	@Before
	public void setUp() throws IOException {
		repo = new File(System.getProperty("java.io.tmpdir"), "GitRepoIndexTest");
		FileUtils.deleteDirectory(repo);
		Files.createDirectories(repo.toPath());
		RawGitExecutor.executeGitCommand("git init", repo.getAbsolutePath());
		connector = new CachingGitConnector(JGitBackedGitConnector.fromPath(repo.getAbsolutePath()));
		assumeTrue(connector.gitInstalledAndReady());
		index = new GitRepoIndex(connector);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(repo);
	}

	@Test
	public void buildsLatestPerFileWithVersionMapping() throws IOException {
		commit("A.txt", "a1", "Alice", "first");
		String a2 = commit("A.txt", "a2", "Bob", "second");
		commit("B.txt", "b1", "Carol", "only");

		assertEquals(2, index.versionCount("A.txt"));
		assertEquals(1, index.versionCount("B.txt"));

		GitFileRevision latestA = index.latest("A.txt");
		assertNotNull(latestA);
		assertEquals(a2, latestA.commitHash());
		assertEquals("Bob", latestA.author());

		// version numbers are oldest-first (1 = oldest), LATEST = newest
		assertEquals("Alice", index.atVersion("A.txt", 1).author());
		assertEquals("Bob", index.atVersion("A.txt", 2).author());
		assertEquals("Bob", index.atVersion("A.txt", WikiProvider.LATEST_VERSION).author());
		assertNull("no such version", index.atVersion("A.txt", 3));
		assertNull("unknown file", index.latest("Ghost.txt"));
	}

	@Test
	public void servesFromMemoryWhenHeadUnchanged() throws IOException {
		commit("A.txt", "a1", "Alice", "first");
		assertEquals(1, index.versionCount("A.txt"));
		// no new commit: a second read must return the same view (and, implicitly, not rebuild)
		assertEquals(1, index.versionCount("A.txt"));
		assertEquals("Alice", index.latest("A.txt").author());
	}

	@Test
	public void foldsInNewCommitOnFastForward() throws IOException {
		commit("A.txt", "a1", "Alice", "first");
		// prime the index at the first HEAD
		assertEquals(1, index.versionCount("A.txt"));

		// HEAD moves forward: a new commit on the same file and a brand-new file
		String a2 = commit("A.txt", "a2", "Bob", "second");
		commit("C.txt", "c1", "Dave", "new file");

		assertEquals(2, index.versionCount("A.txt"));
		assertEquals(a2, index.latest("A.txt").commitHash());
		assertEquals("Bob", index.latest("A.txt").author());
		// a file first seen after the index was primed is picked up by the forward fold
		assertEquals(1, index.versionCount("C.txt"));
		assertEquals("Dave", index.latest("C.txt").author());
	}

	@Test
	public void rebuildsOnBackwardResetSoStaleHistoryDoesNotSurvive() throws IOException {
		String a1 = commit("A.txt", "a1", "Alice", "first");
		// prime at a1
		assertEquals(1, index.versionCount("A.txt"));

		String a2 = commit("A.txt", "a2", "Bob", "second");
		// read at a2 so the index caches the second commit
		assertEquals(2, index.versionCount("A.txt"));
		assertEquals(a2, index.latest("A.txt").commitHash());

		// backward reset: HEAD rewinds to a1. The naive forward-only path would keep serving the stale a2.
		RawGitExecutor.executeGitCommand("git reset --hard " + a1, repo.getAbsolutePath());

		assertEquals("backward reset must drop the rewound commit", 1, index.versionCount("A.txt"));
		assertEquals(a1, index.latest("A.txt").commitHash());
		assertEquals("Alice", index.latest("A.txt").author());
	}

	@Test
	public void rebuildsPerBranchOnSwitch() throws IOException {
		commit("Main.txt", "m1", "Alice", "on main");
		String mainBranch = connector.branches().currentBranch();
		// prime on main
		assertEquals(1, index.versionCount("Main.txt"));
		assertEquals(0, index.versionCount("Feature.txt"));

		// branch off and add a commit only on the feature branch
		RawGitExecutor.executeGitCommand("git checkout -b feature", repo.getAbsolutePath());
		commit("Feature.txt", "f1", "Bob", "on feature");
		assertEquals(1, index.versionCount("Feature.txt"));
		assertEquals(1, index.versionCount("Main.txt"));

		// switching back to main must serve main's view: Feature.txt does not exist there
		RawGitExecutor.executeGitCommand("git checkout " + mainBranch, repo.getAbsolutePath());
		assertEquals(0, index.versionCount("Feature.txt"));
		assertEquals(1, index.versionCount("Main.txt"));
	}

	@Test
	public void perBranchCacheIsBoundedYetStillCorrect() throws IOException {
		commit("Base.txt", "base", "Alice", "base");
		String mainBranch = connector.branches().currentBranch();

		// visit many more branches than the cache bound, reading (=indexing) on each
		for (int i = 0; i < 10; i++) {
			RawGitExecutor.executeGitCommand("git checkout -b feature" + i + " " + mainBranch, repo.getAbsolutePath());
			commit("Feature" + i + ".txt", "f" + i, "Bob", "on feature" + i);
			assertEquals(1, index.versionCount("Feature" + i + ".txt"));
		}

		// the cache must not have grown to one snapshot per visited branch
		assertTrue("per-branch cache must stay bounded, was " + index.cachedBranchCount(),
				index.cachedBranchCount() <= 4);

		// an evicted branch still reads correctly (it is simply rebuilt on access)
		RawGitExecutor.executeGitCommand("git checkout " + mainBranch, repo.getAbsolutePath());
		assertEquals(1, index.versionCount("Base.txt"));
		assertEquals(0, index.versionCount("Feature0.txt"));
	}

	@Test
	public void emptyRepositoryWithoutCommitsIsHandled() {
		// fresh repo, no commits yet: HEAD does not resolve, the index is simply empty (no failure)
		assertEquals(0, index.versionCount("Anything.txt"));
		assertNull(index.latest("Anything.txt"));
		assertTrue(index.revisionsByFile().isEmpty());
	}

	// --- helpers -------------------------------------------------------------

	private String commit(String fileName, String content, String author, String message) throws IOException {
		File file = new File(repo, fileName);
		FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
		return connector.commit().changePath(file.toPath(),
				new CommitUserData(author, author.toLowerCase() + "@test.invalid", message));
	}
}
