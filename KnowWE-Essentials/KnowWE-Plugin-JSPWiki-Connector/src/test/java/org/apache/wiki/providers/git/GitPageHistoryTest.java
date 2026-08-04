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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.providers.AbstractFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.impl.bare.RawGitExecutor;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;
import de.uniwue.d3web.gitConnector.impl.mixed.JGitBackedGitConnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Unit tests for {@link GitPageHistory} against a real temp git repository — no JSPWiki engine (plan step 3). Uses the
 * production connector type ({@code CachingGitConnector(JGitBackedGitConnector)}) so delete/move exercise the same code
 * paths as production (the bare connector does not implement single-path delete/move).
 */
public class GitPageHistoryTest {

	private File repo;
	private GitConnector connector;
	private GitPageHistory history;

	@Before
	public void setUp() throws IOException {
		repo = new File(System.getProperty("java.io.tmpdir"), "GitPageHistoryTest");
		FileUtils.deleteDirectory(repo);
		Files.createDirectories(repo.toPath());
		RawGitExecutor.executeGitCommand("git init", repo.getAbsolutePath());
		connector = new CachingGitConnector(JGitBackedGitConnector.fromPath(repo.getAbsolutePath()));
		assumeTrue(connector.gitInstalledAndReady());
		// one initial commit so HEAD exists, like a real wiki repo
		writePage("Seed", "seed");
		connector.commit().changePath(file("Seed").toPath(), userData("Seed Author", "msg"));
		history = new GitPageHistory(connector);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(repo);
	}

	@Test
	public void commitPutCreatesCommitAndHistoryGrowsWithCorrectVersionMapping() throws IOException {
		writePage("Article", "version 1");
		String h1 = history.commitPut(file("Article"), userData("Alice", "first"));
		assertNotNull(h1);

		writePage("Article", "version 2");
		String h2 = history.commitPut(file("Article"), userData("Bob", "second"));
		assertNotNull(h2);

		List<GitPageVersion> versions = history.history("Article");
		assertEquals(2, versions.size());

		// newest first
		GitPageVersion newest = versions.get(0);
		GitPageVersion oldest = versions.get(1);
		assertEquals(2, newest.version());
		assertEquals(1, oldest.version());
		assertEquals(h2, newest.commitHash());
		assertEquals(h1, oldest.commitHash());
		assertEquals("Bob", newest.userData().user);
		assertEquals("Alice", oldest.userData().user);
	}

	@Test
	public void commitPutWithSameContentIsNoOp() throws IOException {
		writePage("Article", "stable");
		assertNotNull(history.commitPut(file("Article"), userData("Alice", "first")));
		assertEquals(1, history.history("Article").size());

		// writing the identical content again must not produce a new commit
		writePage("Article", "stable");
		String again = history.commitPut(file("Article"), userData("Alice", "again"));
		assertNull("unchanged content must not commit", again);
		assertEquals(1, history.history("Article").size());
	}

	@Test
	public void textAtVersionReadsOldFromGitAndLatestFromDisk() throws IOException {
		writePage("Article", "old text");
		history.commitPut(file("Article"), userData("Alice", "first"));
		writePage("Article", "new text");
		history.commitPut(file("Article"), userData("Bob", "second"));

		assertEquals("old text", history.textAtVersion("Article", 1));
		assertEquals("new text", history.textAtVersion("Article", 2));
		assertEquals("new text", history.textAtVersion("Article", AbstractFileProvider.LATEST_VERSION));
	}

	@Test
	public void commitDeleteRemovesFileButKeepsHistory() throws IOException {
		writePage("Doomed", "content");
		history.commitPut(file("Doomed"), userData("Alice", "first"));
		assertTrue(file("Doomed").exists());

		String deleteHash = history.commitDelete(file("Doomed"), userData("Alice", "remove"));
		assertNotNull(deleteHash);
		assertFalse("working-tree file must be gone", file("Doomed").exists());
	}

	@Test
	public void commitMoveRestartsHistoryAtRename() throws IOException {
		writePage("Before", "content v1");
		history.commitPut(file("Before"), userData("Alice", "first"));
		writePage("Before", "content v2");
		history.commitPut(file("Before"), userData("Alice", "second"));
		assertEquals(2, history.history("Before").size());

		String moveHash = history.commitMove(file("Before"), file("After"), userData("Alice", "renamed"));
		assertNotNull(moveHash);

		assertFalse(file("Before").exists());
		assertTrue(file("After").exists());
		// history restarts at the rename (no --follow): the renamed page has a single version
		assertEquals(1, history.history("After").size());
		assertTrue(history.history("Before").isEmpty());
	}

	@Test
	public void sweepUpCommitsDirtyTreeAndIsNoOpWhenClean() throws IOException {
		// no-op on a clean tree
		assertNull(history.sweepUp("startup"));

		// dirty the tree behind git's back: a written-but-never-committed page
		writePage("Orphan", "written but not committed");
		GitPageHistory.SweepUp sweepUp = history.sweepUp("startup");
		assertNotNull("dirty tree must be reconciled", sweepUp);
		assertNotNull(sweepUp.commitHash());
		assertEquals("the swept paths are reported for the commit events", List.of("Orphan.txt"), sweepUp.paths());
		assertTrue("tree must be clean after sweep", connector.status().isClean());

		// the reconciled file is now part of history
		assertEquals(1, history.history("Orphan").size());

		// a second sweep finds nothing to do
		assertNull(history.sweepUp("startup"));
	}

	/**
	 * The write+commit bracket: while a provider holds {@link GitPageHistory#withCommitLock}, a concurrent sweep-up
	 * must wait, so it can never commit a half-finished save as a reconciliation commit with the wrong author. After
	 * the bracket committed the save itself, the sweep finds a clean tree and does nothing.
	 */
	@Test
	public void sweepUpCannotInterleaveWithACommitLockBracket() throws Exception {
		CountDownLatch insideBracket = new CountDownLatch(1);
		CountDownLatch releaseBracket = new CountDownLatch(1);
		AtomicReference<String> bracketCommit = new AtomicReference<>();

		Thread saver = new Thread(() -> {
			try {
				history.withCommitLock(() -> {
					// the dirty window: file written, commit not made yet
					writePage("Bracketed", "saved content");
					insideBracket.countDown();
					releaseBracket.await();
					bracketCommit.set(history.commitPut(file("Bracketed"), userData("Alice", "bracketed save")));
					return null;
				});
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		saver.start();
		assertTrue(insideBracket.await(5, TimeUnit.SECONDS));

		Thread sweeper = new Thread(() -> history.sweepUp("concurrent sweep"));
		sweeper.start();
		assertBlocked("sweep must block while the bracket is held", sweeper);

		releaseBracket.countDown();
		saver.join(5000);
		sweeper.join(5000);
		assertFalse(saver.isAlive());
		assertFalse(sweeper.isAlive());

		// the save was committed by the bracket with the right author; the sweep found nothing left to reconcile
		assertNotNull(bracketCommit.get());
		List<GitPageVersion> versions = history.history("Bracketed");
		assertEquals(1, versions.size());
		assertEquals("Alice", versions.get(0).userData().user);
		assertTrue(connector.status().isClean());
	}

	// --- helpers -------------------------------------------------------------

	/**
	 * Waits (up to 5s) until the thread parks on the commit lock, instead of sleeping a fixed interval. A thread that
	 * wrongly proceeds either terminates (failed here) or its effect is caught by the caller's follow-up asserts.
	 */
	static void assertBlocked(String message, Thread thread) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 5000;
		while (System.currentTimeMillis() < deadline) {
			switch (thread.getState()) {
				case WAITING, TIMED_WAITING, BLOCKED -> {
					assertTrue(message, thread.isAlive());
					return;
				}
				case TERMINATED -> fail(message + " (thread finished instead of blocking)");
				default -> Thread.sleep(10);
			}
		}
		fail(message + " (thread never blocked)");
	}

	// Mirrors how AbstractFileProvider.findPage names files on disk (mangled page name + .txt). PageIdentifier's
	// accordingFile() can't be used here because it returns null for files that don't exist yet.
	private File file(String pageName) {
		return new File(repo, JSPUtils.mangleName(pageName) + AbstractFileProvider.FILE_EXT);
	}

	private void writePage(String pageName, String content) throws IOException {
		FileUtils.writeStringToFile(file(pageName), content, StandardCharsets.UTF_8);
	}

	private static CommitUserData userData(String author, String message) {
		return new CommitUserData(author, author.replace(' ', '.').toLowerCase() + "@test.invalid", message);
	}
}
