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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Pins what a caller may rely on when it stores several attachments as one change, without going through the wiki
 * engine: the files land in one commit, each keeps its own content, and every one of them gains exactly one version.
 * <p>
 * The engine facing side ({@code JSPWikiConnector.storeAttachments}) is what brackets the writes, so this exercises the
 * repository level operations that bracket stands on.
 */
public class GitAttachmentBulkStoreTest {

	private static final CommitUserData CI = new CommitUserData("CI-process", "ci@example.invalid", "CI build 7");

	private File repo;
	private GitConnector connector;
	private GitWikiRepository repository;

	@Before
	public void setUp() throws IOException {
		repo = new File(System.getProperty("java.io.tmpdir"), "GitAttachmentBulkStoreTest");
		FileUtils.deleteDirectory(repo);
		Files.createDirectories(repo.toPath());
		RawGitExecutor.executeGitCommand("git init", repo.getAbsolutePath());
		connector = new CachingGitConnector(JGitBackedGitConnector.fromPath(repo.getAbsolutePath()));
		assumeTrue(connector.gitInstalledAndReady());
		Files.writeString(new File(repo, "Dashboard.txt").toPath(), "seed");
		connector.commit().changePath(new File(repo, "Dashboard.txt").toPath(),
				new CommitUserData("Seed", "seed@example.invalid", "seed"));
		repository = new GitWikiRepository(connector);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(repo);
	}

	@Test
	public void severalAttachmentsLandInOneCommitAndEachGainsOneVersion() throws Exception {
		Map<String, InputStream> files = new LinkedHashMap<>();
		files.put("one.xml", stream("content one"));
		files.put("two.xml", stream("content two"));
		files.put("three.xml", stream("content three"));

		String commitHash = storeAsOneChange(files);

		Set<String> committed = Set.copyOf(connector.log().listChangedFilesForHash(commitHash));
		assertEquals("one commit carries all three files",
				Set.of("Dashboard-att/one.xml", "Dashboard-att/two.xml", "Dashboard-att/three.xml"), committed);

		for (String name : files.keySet()) {
			List<GitFileRevision> revisions = repository.index().revisionsNewestFirst("Dashboard-att/" + name);
			assertEquals("exactly one version of " + name, 1, revisions.size());
			assertEquals("the commit message is the change note", "CI build 7", revisions.get(0).message());
		}
		assertEquals("content one", contentOf("Dashboard-att/one.xml", 1));
		assertEquals("content three", contentOf("Dashboard-att/three.xml", 1));
	}

	/**
	 * A second bulk store of the same files is a second version of each, not a second copy inside the first. This is
	 * what keeps a caller that stores one file repeatedly, a build writing its results, from losing versions.
	 */
	@Test
	public void asecondBulkStoreAddsASecondVersionOfEach() throws Exception {
		String first = storeAsOneChange(Map.of("build.xml", stream("build 1")));
		String second = storeAsOneChange(Map.of("build.xml", stream("build 2")));

		assertNotEquals(first, second);
		List<GitFileRevision> revisions = repository.index().revisionsNewestFirst("Dashboard-att/build.xml");
		assertEquals(2, revisions.size());
		assertEquals("build 1", contentOf("Dashboard-att/build.xml", 1));
		assertEquals("build 2", contentOf("Dashboard-att/build.xml", 2));
	}

	@Test
	public void deletingAnAttachmentKeepsItsHistory() throws Exception {
		storeAsOneChange(Map.of("build.xml", stream("build 1")));
		storeAsOneChange(Map.of("build.xml", stream("build 2")));

		File attachment = new File(repo, "Dashboard-att/build.xml");
		repository.commitDelete(attachment, "Dashboard-att/build.xml",
				new CommitUserData("CI-process", "ci@example.invalid", "prune"));

		// the delete is another commit on the path, so a caller cannot shorten a history by deleting and rewriting it
		assertEquals("history grows by the delete rather than shrinking",
				3, repository.index().revisionsNewestFirst("Dashboard-att/build.xml").size());
	}

	/**
	 * Writes the given files and commits them as one change, which is what the connector's bulk store does around the
	 * engine's per attachment write.
	 */
	private String storeAsOneChange(Map<String, InputStream> files) throws Exception {
		return repository.withRepositoryLock(() -> {
			File dir = new File(repo, "Dashboard-att");
			assertTrue(dir.exists() || dir.mkdirs());
			for (Map.Entry<String, InputStream> file : files.entrySet()) {
				Files.write(new File(dir, file.getKey()).toPath(), file.getValue().readAllBytes());
				repository.index();
			}
			List<String> paths = files.keySet().stream().map(name -> "Dashboard-att/" + name).toList();
			connector.commit().addPaths(paths);
			return connector.commit().commitPathsForUser(CI.message, CI.user, CI.email, Set.copyOf(paths));
		});
	}

	private String contentOf(String relPath, int version) {
		try (InputStream in = repository.bytesAtVersion(relPath, version)) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static InputStream stream(String content) {
		return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
	}
}
