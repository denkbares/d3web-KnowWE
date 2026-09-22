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

import org.apache.commons.io.FileUtils;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.providers.AbstractFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.GitConnectorLog;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * Pins what {@link GitWikiRepository} remembers rather than asking git again: the content of a file at a commit, and
 * whether a path is ignored. Both are measured in git calls, because a git call is a process and the cost of these
 * paths is the number of processes they spawn.
 * <p>
 * The connector is a counting stand-in in front of a real one against a real temp repository, so the answers are the
 * answers git gives and only the number of questions is under test.
 */
public class GitWikiRepositoryCachingTest {

	private File repo;
	private GitConnector connector;
	private GitConnectorLog log;
	private GitWikiRepository repository;

	@Before
	public void setUp() throws IOException {
		repo = new File(System.getProperty("java.io.tmpdir"), "GitWikiRepositoryCachingTest");
		FileUtils.deleteDirectory(repo);
		Files.createDirectories(repo.toPath());
		RawGitExecutor.executeGitCommand("git init", repo.getAbsolutePath());
		GitConnector real = new CachingGitConnector(JGitBackedGitConnector.fromPath(repo.getAbsolutePath()));
		assumeTrue(real.gitInstalledAndReady());
		write("Seed.txt", "seed");
		real.commit().changePath(new File(repo, "Seed.txt").toPath(), userData("Seed Author", "seed"));

		log = mock(GitConnectorLog.class, withSettings().defaultAnswer(delegatesTo(real.log())));
		connector = mock(GitConnector.class, withSettings().defaultAnswer(delegatesTo(real)));
		doReturn(log).when(connector).log();
		repository = new GitWikiRepository(connector);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(repo);
	}

	// --- file content --------------------------------------------------------

	@Test
	public void readingOneVersionRepeatedlyAsksGitOnce() throws IOException {
		commitPage("Article", "version 1");
		commitPage("Article", "version 2");

		assertEquals("version 1", repository.textAtVersion("Article", 1));
		assertEquals("version 1", repository.textAtVersion("Article", 1));
		assertEquals("version 1", repository.textAtVersion("Article", 1));

		verify(log, times(1)).getBytesForCommit(anyString(), anyString());
		assertEquals(1, repository.cachedBlobCount());
	}

	@Test
	public void everyVersionIsRememberedOnItsOwn() throws IOException {
		commitPage("Article", "version 1");
		commitPage("Article", "version 2");
		commitPage("Article", "version 3");

		assertEquals("version 1", repository.textAtVersion("Article", 1));
		assertEquals("version 2", repository.textAtVersion("Article", 2));
		assertEquals("version 1", repository.textAtVersion("Article", 1));
		assertEquals("version 2", repository.textAtVersion("Article", 2));

		verify(log, times(2)).getBytesForCommit(anyString(), anyString());
		assertEquals(2, repository.cachedBlobCount());
		assertEquals("version 1".length() + "version 2".length(), repository.cachedBlobBytes());
	}

	@Test
	public void laterCommitsLeaveWhatWasAlreadyReadAlone() throws IOException {
		commitPage("Article", "version 1");
		commitPage("Article", "version 2");
		assertEquals("version 1", repository.textAtVersion("Article", 1));

		commitPage("Article", "version 3");
		commitPage("Other", "unrelated");

		assertEquals("version 1", repository.textAtVersion("Article", 1));
		verify(log, times(1)).getBytesForCommit(anyString(), anyString());
	}

	@Test
	public void theLatestVersionIsReadFromDiskAndNotRemembered() throws IOException {
		commitPage("Article", "version 1");
		commitPage("Article", "version 2");

		assertEquals("version 2", repository.textAtVersion("Article", WikiProvider.LATEST_VERSION));
		assertEquals("version 2", repository.textAtVersion("Article", WikiProvider.LATEST_VERSION));

		verify(log, times(0)).getBytesForCommit(anyString(), anyString());
		assertEquals(0, repository.cachedBlobCount());
	}

	@Test
	public void attachmentContentIsRememberedTheSameWay() throws IOException {
		commitPage("Article-att/note", "attachment 1");
		commitPage("Article-att/note", "attachment 2");

		assertEquals("attachment 1", read(repository.bytesAtVersion("Article-att/note.txt", 1)));
		assertEquals("attachment 1", read(repository.bytesAtVersion("Article-att/note.txt", 1)));

		verify(log, times(1)).getBytesForCommit(anyString(), anyString());
	}

	// --- ignore rules --------------------------------------------------------

	@Test
	public void checkingOnePathRepeatedlyAsksGitOnce() {
		assertFalse(repository.isIgnored("Article.txt"));
		assertFalse(repository.isIgnored("Article.txt"));
		assertFalse(repository.isIgnored("Article.txt"));

		verify(connector, times(1)).isIgnored("Article.txt");
		assertEquals(1, repository.cachedIgnoreCount());
	}

	@Test
	public void aCommitThatLeavesTheIgnoreRulesAloneKeepsTheAnswer() throws IOException {
		assertFalse(repository.isIgnored("Article.txt"));

		commitPage("Other", "some page");
		commitPage("Another", "another page");

		assertFalse(repository.isIgnored("Article.txt"));
		verify(connector, times(1)).isIgnored("Article.txt");
	}

	@Test
	public void committingAnIgnoreRuleChangesTheAnswer() throws IOException {
		assertFalse(repository.isIgnored("Secret.txt"));
		assertFalse(repository.isIgnored("Secret.txt"));
		verify(connector, times(1)).isIgnored("Secret.txt");

		write(".gitignore", "Secret.txt\n");
		repository.commitFile(new File(repo, ".gitignore"), ".gitignore", userData("Alice", "ignore the secret"));

		assertTrue(repository.isIgnored("Secret.txt"));
		verify(connector, times(2)).isIgnored("Secret.txt");
	}

	@Test
	public void removingAnIgnoreRuleChangesTheAnswerBack() throws IOException {
		write(".gitignore", "Secret.txt\n");
		repository.commitFile(new File(repo, ".gitignore"), ".gitignore", userData("Alice", "ignore the secret"));
		assertTrue(repository.isIgnored("Secret.txt"));

		write(".gitignore", "\n");
		repository.commitFile(new File(repo, ".gitignore"), ".gitignore", userData("Alice", "stop ignoring it"));

		assertFalse(repository.isIgnored("Secret.txt"));
	}

	@Test
	public void theCommitFormAlwaysAsksGit() {
		assertFalse(repository.isIgnoredForCommit("Article.txt"));
		assertFalse(repository.isIgnoredForCommit("Article.txt"));

		verify(connector, times(2)).isIgnored("Article.txt");
		assertEquals("the commit form leaves nothing behind", 0, repository.cachedIgnoreCount());
	}

	// --- helpers -------------------------------------------------------------

	private void commitPage(String pageName, String content) throws IOException {
		String relPath = pageName.contains("/")
				? pageName + AbstractFileProvider.FILE_EXT
				: JSPUtils.mangleName(pageName) + AbstractFileProvider.FILE_EXT;
		write(relPath, content);
		repository.commitFile(new File(repo, relPath), relPath, userData("Alice", "edit " + pageName));
	}

	private void write(String relPath, String content) throws IOException {
		FileUtils.writeStringToFile(new File(repo, relPath), content, StandardCharsets.UTF_8);
	}

	private static String read(InputStream stream) throws IOException {
		try (InputStream in = stream) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static CommitUserData userData(String author, String message) {
		return new CommitUserData(author, author.replace(' ', '.').toLowerCase() + "@test.invalid", message);
	}
}
