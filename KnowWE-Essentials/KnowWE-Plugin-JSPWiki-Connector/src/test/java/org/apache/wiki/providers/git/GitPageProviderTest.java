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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.cache.CachingManager;
import org.apache.wiki.providers.AbstractFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import de.knowwe.event.GitCommitEvent;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for the single-wiki git page provider: commit-on-save, history from git, transaction batching, startup
 * reconciliation, and the write+commit locking bracket. Uses a mocked engine against a real temp repository.
 */
public class GitPageProviderTest {

	private static final String AUTHOR = "UnknownAuthor";

	private File pageDir;
	private Properties properties;
	private Engine engine;
	private CachingManager cachingManager;
	private GitPageProvider provider;

	@Before
	public void setUp() throws Exception {
		pageDir = new File(System.getProperty("java.io.tmpdir"), "GitPageProviderTest");
		FileUtils.deleteDirectory(pageDir);

		properties = new Properties();
		properties.put(AbstractFileProvider.PROP_PAGEDIR, pageDir.getAbsolutePath());

		engine = mockEngine();
		assumeTrue(BareGitConnector.fromPath(System.getProperty("java.io.tmpdir")).gitInstalledAndReady());

		provider = new GitPageProvider();
		provider.initialize(engine, properties);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(pageDir);
	}

	@Test
	public void initializeCreatesTheRepository() {
		assertTrue("page directory must be git-initialized", new File(pageDir, ".git").isDirectory());
	}

	@Test
	public void saveCommitsWithTheSavingUser() throws ProviderException {
		putPage("Welcome", "content");

		assertTrue(new File(pageDir, "Welcome.txt").exists());
		assertEquals(1, commitsForFile("Welcome.txt"));
		List<Page> history = provider.getVersionHistory("Welcome");
		assertEquals(1, history.size());
		assertEquals(AUTHOR, history.get(0).getAuthor());
	}

	@Test
	public void historyAndTextComeFromGit() throws ProviderException {
		putPage("Topic", "first");
		putPage("Topic", "second");

		List<Page> history = provider.getVersionHistory("Topic");
		assertEquals(2, history.size());
		// newest first
		assertEquals(2, history.get(0).getVersion());
		assertEquals(1, history.get(1).getVersion());

		assertEquals("first", provider.getPageText("Topic", 1));
		assertEquals("second", provider.getPageText("Topic", 2));
		assertEquals("second", provider.getPageText("Topic", WikiProvider.LATEST_VERSION));
		assertTrue(provider.pageExists("Topic", 2));
		assertFalse(provider.pageExists("Topic", 3));
	}

	@Test
	public void unchangedContentDoesNotCommit() throws ProviderException {
		putPage("Welcome", "stable");
		putPage("Welcome", "stable");
		assertEquals(1, commitsForFile("Welcome.txt"));
		assertEquals(1, provider.getVersionHistory("Welcome").size());
	}

	@Test
	public void transactionYieldsOneCommit() throws ProviderException {
		provider.openCommit(AUTHOR);
		putPage("PageA", "content a");
		putPage("PageB", "content b");

		// staged, not yet committed
		assertEquals(0, commitsForFile("PageA.txt"));
		assertEquals(0, commitsForFile("PageB.txt"));

		provider.commit(AUTHOR, "bulk save");

		// exactly one commit, shared by both pages
		assertEquals(1, commitsForFile("PageA.txt"));
		assertEquals(1, commitsForFile("PageB.txt"));
		assertEquals(headCommitFor("PageA.txt"), headCommitFor("PageB.txt"));
		assertEquals(1, provider.getVersionHistory("PageA").size());
	}

	@Test
	public void rollbackDiscardsStagedChangesAndEvictsCaches() throws ProviderException {
		provider.openCommit(AUTHOR);
		putPage("Draft", "draft");
		provider.rollback(AUTHOR);

		assertEquals(0, commitsForFile("Draft.txt"));
		assertTrue(provider.getVersionHistory("Draft").isEmpty());
		// the discarded edit must be evicted from the JSPWiki page caches
		Mockito.verify(cachingManager).remove(CachingManager.CACHE_PAGES, "Draft");
		Mockito.verify(cachingManager).remove(CachingManager.CACHE_PAGES_TEXT, "Draft");
		Mockito.verify(cachingManager).remove(CachingManager.CACHE_PAGES_HISTORY, "Draft");
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
	public void deletePageCommitsRemoval() throws ProviderException {
		putPage("Doomed", "content");
		WikiPage page = new WikiPage(engine, "Doomed");
		page.setAuthor(AUTHOR);
		provider.deletePage(page);

		assertFalse(new File(pageDir, "Doomed.txt").exists());
		assertFalse(provider.pageExists("Doomed"));
	}

	@Test
	public void movePageRestartsHistory() throws ProviderException {
		putPage("Before", "v1");
		putPage("Before", "v2");

		WikiPage from = new WikiPage(engine, "Before");
		from.setAuthor(AUTHOR);
		provider.movePage(from, "After");

		assertFalse(new File(pageDir, "Before.txt").exists());
		assertTrue(new File(pageDir, "After.txt").exists());
		assertEquals("history restarts at the rename", 1, provider.getVersionHistory("After").size());
	}

	@Test
	public void currentPageHasNoChangeNoteButHistoryDoes() throws ProviderException {
		putPage("Topic", "v1"); // putPage sets CHANGENOTE = "edit"

		// the live/current page (what getPage()/the cache serves, and the basis for the next write) must not carry a
		// change note - otherwise a subsequent save/delete with no fresh note would reuse the last commit message
		Page current = provider.getPageInfo("Topic", WikiProvider.LATEST_VERSION);
		assertNull("current page must not expose the last commit message as a change note",
				current.getAttribute(WikiPage.CHANGENOTE));

		// history entries, by contrast, keep their commit message as the change note for display
		List<Page> history = provider.getVersionHistory("Topic");
		assertEquals("edit", history.get(0).getAttribute(WikiPage.CHANGENOTE));
	}

	@Test
	public void saveWithoutFreshNoteDoesNotInheritPreviousMessage() throws ProviderException {
		putPage("Topic", "v1"); // first commit carries the note "edit"

		// second save with no change note set on the page (as the engine does when the user supplies none)
		WikiPage page = new WikiPage(engine, "Topic");
		page.setAuthor(AUTHOR);
		provider.putPageText(page, "v2");

		List<Page> history = provider.getVersionHistory("Topic");
		assertEquals(2, history.size());
		String latestNote = history.get(0).getAttribute(WikiPage.CHANGENOTE);
		assertNotEquals("the 2nd commit must not reuse the 1st commit's message", "edit", latestNote);
		assertEquals("with no fresh note an edit falls back to the provider default", "Edited Topic", latestNote);
	}

	@Test
	public void getAllPagesListsWithGitMetadataFromTheIndex() throws ProviderException {
		putPage("Welcome", "content");
		putPage("Topic", "v1");
		putPage("Topic", "v2");

		Collection<Page> all = provider.getAllPages();

		Page welcome = byName(all, "Welcome");
		Page topic = byName(all, "Topic");
		assertNotNull(welcome);
		assertNotNull(topic);
		// author/date/version come from git via the eager index (one walk, no per-page git call)
		assertEquals(AUTHOR, welcome.getAuthor());
		assertEquals(1, welcome.getVersion());
		assertEquals("two commits -> version 2", 2, topic.getVersion());
		assertEquals(AUTHOR, topic.getAuthor());
	}

	@Test
	public void getAllChangedSinceReportsChangesFromGit() throws ProviderException {
		putPage("Welcome", "content");
		putPage("Topic", "content");

		Collection<Page> changed = provider.getAllChangedSince(new Date(0L));
		assertNotNull(byName(changed, "Welcome"));
		assertNotNull(byName(changed, "Topic"));

		// nothing changed in the future
		Collection<Page> future = provider.getAllChangedSince(new Date(System.currentTimeMillis() + 3_600_000L));
		assertTrue("no changes after a future cut-off", future.isEmpty());
	}

	@Test
	public void startupReconciliationSweepsUpADirtyTreeAndFiresACompleteEvent() throws Exception {
		// simulate a crash during a save: a written-but-never-committed page in an existing repo
		putPage("Committed", "content");
		FileUtils.writeStringToFile(new File(pageDir, "Orphan.txt"), "orphaned save", StandardCharsets.UTF_8);

		// a fresh provider on the same directory must self-heal the dirty tree at startup (BR10)
		List<GitCommitEvent> events = new ArrayList<>();
		EventListener listener = new EventListener() {
			@Override
			public Collection<Class<? extends Event>> getEvents() {
				return List.of(GitCommitEvent.class);
			}

			@Override
			public void notify(Event event) {
				events.add((GitCommitEvent) event);
			}
		};
		EventManager.getInstance().registerListener(listener);
		try {
			GitPageProvider restarted = new GitPageProvider();
			restarted.initialize(engine, properties);
			assertEquals(1, restarted.getVersionHistory("Orphan").size());
		}
		finally {
			EventManager.getInstance().unregister(listener);
		}

		assertTrue(BareGitConnector.fromPath(pageDir.getAbsolutePath()).status().isClean());
		// the reconciliation commit carries a complete event: origin and the swept pages (BR9)
		assertEquals(1, events.size());
		assertEquals(GitCommitEvent.Origin.RECONCILIATION, events.get(0).origin());
		// compare canonical paths, the connector canonicalizes /var to /private/var on macOS
		assertEquals(pageDir.getCanonicalPath(), new File(events.get(0).repoPath()).getCanonicalPath());
		assertTrue("the swept page must be named in the event", events.get(0).pages().contains("Orphan"));
	}

	/**
	 * Locking fix B observable at the provider level: while a save's write+commit bracket is held, a concurrent
	 * transaction close of another user must wait, so the batch cannot scoop up the half-finished save.
	 */
	@Test
	public void concurrentBatchCloseWaitsForARunningSave() throws Exception {
		provider.openCommit("alice");
		WikiPage alicePage = new WikiPage(engine, "AlicePage");
		alicePage.setAuthor("alice");
		provider.putPageText(alicePage, "alice draft");

		CountDownLatch insideBracket = new CountDownLatch(1);
		CountDownLatch releaseBracket = new CountDownLatch(1);
		Thread saver = new Thread(() -> {
			try {
				provider.backend().repository().withCommitLock(() -> {
					insideBracket.countDown();
					releaseBracket.await();
					return null;
				});
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		saver.start();
		assertTrue(insideBracket.await(5, TimeUnit.SECONDS));

		Thread committer = new Thread(() -> provider.commit("alice", "bulk save"));
		committer.start();
		GitWikiRepositoryTest.assertBlocked("batch close must wait for the commit lock", committer);
		assertEquals(0, commitsForFile("AlicePage.txt"));

		releaseBracket.countDown();
		committer.join(5000);
		saver.join(5000);
		assertFalse(committer.isAlive());
		assertEquals(1, commitsForFile("AlicePage.txt"));
	}

	// --- helpers -------------------------------------------------------------

	private static Page byName(Collection<Page> pages, String name) {
		return pages.stream().filter(p -> name.equals(p.getName())).findFirst().orElse(null);
	}

	private void putPage(String name, String text) throws ProviderException {
		WikiPage page = new WikiPage(engine, name);
		page.setAuthor(AUTHOR);
		page.setAttribute(WikiPage.CHANGENOTE, "edit");
		page.setLastModified(new Date());
		provider.putPageText(page, text);
	}

	private int commitsForFile(String fileName) {
		return BareGitConnector.fromPath(pageDir.getAbsolutePath()).log().commitHashesForFile(fileName).size();
	}

	private String headCommitFor(String fileName) {
		List<String> hashes = BareGitConnector.fromPath(pageDir.getAbsolutePath()).log().commitHashesForFile(fileName);
		return hashes.get(hashes.size() - 1);
	}

	private Engine mockEngine() {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		when(engine.getWikiProperties()).thenReturn(properties);
		// userData: profile lookup returns null (no such user) -> raw author is used; just avoid NPEs in the chain
		UserManager userManager = Mockito.mock(UserManager.class);
		UserDatabase userDatabase = Mockito.mock(UserDatabase.class);
		when(engine.getManager(UserManager.class)).thenReturn(userManager);
		when(userManager.getUserDatabase()).thenReturn(userDatabase);
		// cache eviction after commit()/rollback() goes through the CachingManager; kept in a field for verification
		cachingManager = Mockito.mock(CachingManager.class);
		when(engine.getManager(CachingManager.class)).thenReturn(cachingManager);
		return engine;
	}
}
