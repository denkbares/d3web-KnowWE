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

package org.apache.wiki.providers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.pages.PageManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.when;

/**
 * Focused tests for the multi-wiki git provider router: per-sub-wiki repos, commit-on-save routing, history from git,
 * and transaction batching across repos. Uses a mocked engine + two sub-wiki folders (the full-engine integration
 * tests modeled on the multiwiki NestedTest/NonNestedTest are step 8).
 */
public class GitVersioningFileProviderMultiWikiTest {

	private static final String AUTHOR = "UnknownAuthor";

	private File pageDir;
	private File mainRepo;
	private File subRepo;
	private Properties properties;
	private Engine engine;
	private PageManager pageManager;
	private GitVersioningFileProviderMultiWiki provider;

	@Before
	public void setUp() throws Exception {
		pageDir = new File(System.getProperty("java.io.tmpdir"), "GitMultiWikiTest");
		FileUtils.deleteDirectory(pageDir);
		mainRepo = new File(pageDir, "Main");
		subRepo = new File(pageDir, "Sub1");
		Files.createDirectories(mainRepo.toPath());
		Files.createDirectories(subRepo.toPath());

		properties = new Properties();
		properties.put(AbstractFileProvider.PROP_PAGEDIR, pageDir.getAbsolutePath());
		properties.put(SubWikiUtils.MAIN_FOLDER_NAME, "Main"); // non-nested layout

		engine = mockEngine();
		assumeTrue(BareGitConnector.fromPath(mainRepo.getAbsolutePath()).gitInstalledAndReady());

		provider = new GitVersioningFileProviderMultiWiki();
		provider.initialize(engine, properties);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(pageDir);
	}

	@Test
	public void savesRouteToTheCorrectSubWikiRepo() throws ProviderException {
		putPage("Welcome", "main wiki content");          // main wiki (folder Main)
		putPage("Sub1&&Topic", "sub wiki content");       // sub wiki (folder Sub1)

		// each file lives in its own folder/repo
		assertTrue(new File(mainRepo, "Welcome.txt").exists());
		assertTrue(new File(subRepo, "Topic.txt").exists());
		assertFalse(new File(mainRepo, "Topic.txt").exists());

		// and is committed only in its own repo
		assertEquals(1, commitsForFile(mainRepo, "Welcome.txt"));
		assertEquals(1, commitsForFile(subRepo, "Topic.txt"));
		assertEquals(0, commitsForFile(mainRepo, "Topic.txt"));
	}

	@Test
	public void historyAndTextComeFromGit() throws ProviderException {
		putPage("Sub1&&Topic", "first");
		putPage("Sub1&&Topic", "second");

		List<Page> history = provider.getVersionHistory("Sub1&&Topic");
		assertEquals(2, history.size());
		// newest first; names keep the sub-wiki prefix
		assertEquals("Sub1&&Topic", history.get(0).getName());
		assertEquals(2, history.get(0).getVersion());
		assertEquals(1, history.get(1).getVersion());

		assertEquals("first", provider.getPageText("Sub1&&Topic", 1));
		assertEquals("second", provider.getPageText("Sub1&&Topic", 2));
		assertEquals("second", provider.getPageText("Sub1&&Topic", WikiProvider.LATEST_VERSION));
	}

	@Test
	public void unchangedContentDoesNotCommit() throws ProviderException {
		putPage("Welcome", "stable");
		putPage("Welcome", "stable");
		assertEquals(1, commitsForFile(mainRepo, "Welcome.txt"));
		assertEquals(1, provider.getVersionHistory("Welcome").size());
	}

	@Test
	public void transactionSpanningTwoReposYieldsOneCommitPerRepo() throws ProviderException {
		provider.openCommit(AUTHOR);
		putPage("Welcome", "main content");
		putPage("Sub1&&Topic", "sub content");

		// staged, not yet committed
		assertEquals(0, commitsForFile(mainRepo, "Welcome.txt"));
		assertEquals(0, commitsForFile(subRepo, "Topic.txt"));

		provider.commit(AUTHOR, "bulk save");

		// exactly one commit per repo
		assertEquals(1, commitsForFile(mainRepo, "Welcome.txt"));
		assertEquals(1, commitsForFile(subRepo, "Topic.txt"));
		assertEquals(1, provider.getVersionHistory("Welcome").size());
		assertEquals(1, provider.getVersionHistory("Sub1&&Topic").size());
	}

	@Test
	public void rollbackDiscardsStagedChanges() throws ProviderException {
		provider.openCommit(AUTHOR);
		putPage("Sub1&&Draft", "draft");
		provider.rollback(AUTHOR);

		assertEquals(0, commitsForFile(subRepo, "Draft.txt"));
		assertTrue(provider.getVersionHistory("Sub1&&Draft").isEmpty());
		// the discarded edit must be evicted from the JSPWiki page cache / Lucene (refreshCache -> deleteVersion)
		Mockito.verify(pageManager).deleteVersion(Mockito.any(Page.class));
	}

	@Test
	public void deleteVersionThrows() throws ProviderException {
		putPage("Welcome", "content");
		try {
			provider.deleteVersion(new WikiPage(engine, "Welcome"), 1);
			fail("deleteVersion must throw ProviderException");
		}
		catch (ProviderException expected) {
			assertTrue(expected.getMessage().toLowerCase().contains("not supported"));
		}
	}

	@Test
	public void movePageWithinSubWikiRestartsHistory() throws ProviderException {
		putPage("Sub1&&Before", "v1");
		putPage("Sub1&&Before", "v2");

		WikiPage from = new WikiPage(engine, "Sub1&&Before");
		from.setAuthor(AUTHOR);
		provider.movePage(from, "Sub1&&After");

		assertFalse(new File(subRepo, "Before.txt").exists());
		assertTrue(new File(subRepo, "After.txt").exists());
		assertEquals("history restarts at the rename", 1, provider.getVersionHistory("Sub1&&After").size());
	}

	// --- helpers -------------------------------------------------------------

	private void putPage(String fullName, String text) throws ProviderException {
		WikiPage page = new WikiPage(engine, fullName);
		page.setAuthor(AUTHOR);
		page.setAttribute(WikiPage.CHANGENOTE, "edit");
		page.setLastModified(new Date());
		provider.putPageText(page, text);
	}

	private int commitsForFile(File repo, String fileName) {
		return BareGitConnector.fromPath(repo.getAbsolutePath()).log().commitHashesForFile(fileName).size();
	}

	private Engine mockEngine() throws Exception {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		when(engine.getWikiProperties()).thenReturn(properties);
		// getUserData: profile lookup returns null (no such user) -> raw author is used; just avoid NPEs in the chain
		UserManager userManager = Mockito.mock(UserManager.class);
		UserDatabase userDatabase = Mockito.mock(UserDatabase.class);
		when(engine.getManager(UserManager.class)).thenReturn(userManager);
		when(userManager.getUserDatabase()).thenReturn(userDatabase);
		// refreshCache during commit()/rollback() needs a PageManager; kept in a field so tests can verify eviction
		pageManager = Mockito.mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);
		return engine;
	}
}
