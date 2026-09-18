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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.providers.AbstractFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Pins that {@link GitWikiRepository} resolves a version to the same commit git itself resolves it to, across the
 * history shapes that a wiki repository actually takes: plain edits, a delete and recreate, a rename, and a branch
 * that was reset backwards.
 * <p>
 * Ground truth is read through a connector of its own that holds no cache, so the comparison is against git rather
 * than against another cache.
 */
public class GitWikiRepositoryVersionResolutionTest {

	private File repo;
	private GitConnector connector;
	private GitConnector groundTruth;
	private GitWikiRepository repository;

	@Before
	public void setUp() throws IOException {
		repo = new File(System.getProperty("java.io.tmpdir"), "GitWikiRepositoryVersionResolutionTest");
		FileUtils.deleteDirectory(repo);
		Files.createDirectories(repo.toPath());
		RawGitExecutor.executeGitCommand("git init", repo.getAbsolutePath());
		connector = new CachingGitConnector(JGitBackedGitConnector.fromPath(repo.getAbsolutePath()));
		assumeTrue(connector.gitInstalledAndReady());
		groundTruth = BareGitConnector.fromPath(repo.getAbsolutePath());
		commit("Seed", "seed");
		repository = new GitWikiRepository(connector);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(repo);
	}

	@Test
	public void resolvesEveryVersionOfAPlainHistory() throws IOException {
		for (int version = 1; version <= 4; version++) {
			commit("Article", "text of version " + version);
		}
		assertEveryVersionMatchesGit("Article");
	}

	@Test
	public void resolvesEveryVersionAcrossADeleteAndRecreate() throws IOException {
		commit("Article", "first life");
		repository.commitDelete(file("Article"), fileName("Article"), userData("Alice", "delete"));
		commit("Article", "second life");
		commit("Article", "second life, edited");
		assertEveryVersionMatchesGit("Article");
	}

	@Test
	public void resolvesEveryVersionAcrossARename() throws IOException {
		commit("Before", "content v1");
		commit("Before", "content v2");
		repository.commitMove(file("Before"), file("After"), userData("Alice", "rename"));

		// history restarts at the rename, the pinned behaviour of these providers
		assertEquals(1, repository.history("After").size());
		assertEveryVersionMatchesGit("After");
		assertEveryVersionMatchesGit("Before");
	}

	@Test
	public void resolvesEveryVersionAfterTheBranchWasResetBackwards() throws IOException {
		commit("Article", "version 1");
		String second = commit("Article", "version 2");
		commit("Article", "version 3");
		// warm the read paths at the pre-reset history, so a stale cache would be caught below
		assertEveryVersionMatchesGit("Article");

		RawGitExecutor.executeGitCommand(new String[] { "git", "reset", "--hard", second }, repo.getAbsolutePath());

		assertEquals(2, repository.history("Article").size());
		assertEveryVersionMatchesGit("Article");
	}

	@Test
	public void theBulkTextReadAgreesWithTheSingleOne() throws IOException {
		for (int version = 1; version <= 5; version++) {
			commit("Article", "text of version " + version + "\n".repeat(version));
		}
		List<Integer> versions = List.of(1, 2, 3, 4, 5);
		Map<Integer, String> bulk = repository.textAtVersions("Article", versions);

		assertEquals(versions.size(), bulk.size());
		for (int version : versions) {
			assertEquals("version " + version, repository.textAtVersion("Article", version), bulk.get(version));
		}
	}

	@Test
	public void theBulkTextReadSkipsVersionsThatDoNotExist() throws IOException {
		commit("Article", "only version");
		Map<Integer, String> bulk = repository.textAtVersions("Article", List.of(0, 1, 2, 17));
		assertEquals(Set.of(1), bulk.keySet());

		assertTrue(repository.textAtVersions("Nowhere", List.of(1)).isEmpty());
	}

	@Test
	public void versionsOutsideTheHistoryResolveToNothing() throws IOException {
		commit("Article", "only version");
		assertNull(repository.textAtVersion("Article", 0));
		assertNull(repository.textAtVersion("Article", 2));
		assertNull(repository.bytesAtVersion(fileName("Article"), 2));
	}

	@Test
	public void anUncommittedPageResolvesToNothing() throws IOException {
		writePage("Orphan", "written but never committed");
		assertNull(repository.textAtVersion("Orphan", 1));
	}

	/**
	 * Asserts that every version the repository reports resolves to the content git has at the commit it reports, and
	 * that the version numbering matches git's own oldest-first order for the file.
	 */
	private void assertEveryVersionMatchesGit(String pageName) throws IOException {
		String fileName = fileName(pageName);
		List<String> hashesOldestFirst = groundTruth.log().commitHashesForFile(fileName);
		List<GitPageVersion> history = repository.history(pageName);
		assertEquals("version count of " + pageName, hashesOldestFirst.size(), history.size());

		for (int version = 1; version <= hashesOldestFirst.size(); version++) {
			String hash = hashesOldestFirst.get(version - 1);
			byte[] expected = groundTruth.log().getBytesForCommit(hash, fileName);
			if (expected == null) {
				// the version that recorded the deletion, git has no blob for it
				continue;
			}
			String expectedText = new String(expected, StandardCharsets.UTF_8);
			assertEquals("text of " + pageName + " version " + version, expectedText,
					repository.textAtVersion(pageName, version));
			assertEquals("bytes of " + pageName + " version " + version, expectedText,
					readFully(repository.bytesAtVersion(fileName, version)));
		}
		assertTrue("latest version is served from disk", history.isEmpty()
				|| Files.readString(file(pageName).toPath())
				.equals(repository.textAtVersion(pageName, AbstractFileProvider.LATEST_VERSION)));
	}

	private static String readFully(InputStream stream) throws IOException {
		try (InputStream in = stream) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private String commit(String pageName, String content) throws IOException {
		writePage(pageName, content);
		return connector.commit().changePath(file(pageName).toPath(), userData("Alice", "change"));
	}

	private void writePage(String pageName, String content) throws IOException {
		FileUtils.writeStringToFile(file(pageName), content, StandardCharsets.UTF_8);
	}

	private File file(String pageName) {
		return new File(repo, fileName(pageName));
	}

	private String fileName(String pageName) {
		return JSPUtils.mangleName(pageName) + AbstractFileProvider.FILE_EXT;
	}

	private static CommitUserData userData(String user, String message) {
		return new CommitUserData(user, user.toLowerCase() + "@example.invalid", message);
	}
}
