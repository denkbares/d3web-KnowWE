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
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.content.PageRenamer;
import org.apache.wiki.providers.CachingAttachmentProvider;
import org.apache.wiki.providers.CachingProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;

import static org.apache.wiki.providers.git.GitTestWiki.ADMIN;
import static org.apache.wiki.providers.git.GitTestWiki.JANNE;
import static org.apache.wiki.providers.git.GitTestWiki.JANNE_EMAIL;
import static org.apache.wiki.providers.git.GitTestWiki.JANNE_FULL_NAME;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * The production git providers inside a real JSPWiki engine: reads and writes go through the engine's page and
 * attachment managers and the caching wrappers in front of the providers, the way the wiki itself uses them.
 */
public class GitProvidersInEngineTest {

	private static final String JANNE_IDENTITY = JANNE_FULL_NAME + " <" + JANNE_EMAIL + ">";

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private GitTestWiki wiki;

	@Before
	public void setUp() throws Exception {
		File probe = folder.newFolder("git-probe");
		assumeTrue(BareGitConnector.fromPath(probe.getAbsolutePath()).gitInstalledAndReady());
		wiki = GitTestWiki.start(folder.newFolder("wiki"), null);
	}

	@After
	public void tearDown() {
		if (wiki != null) {
			wiki.close();
		}
	}

	@Test
	public void theProvidersRunBehindTheCachingWrappers() {
		PageProvider pageProvider = wiki.pages().getProvider();
		assertTrue(pageProvider.toString(), pageProvider instanceof CachingProvider);
		assertTrue(((CachingProvider) pageProvider).getRealProvider() instanceof GitPageProvider);

		assertTrue("attachments must be enabled", wiki.attachments().attachmentsEnabled());
		assertTrue(wiki.attachments().getCurrentProvider() instanceof CachingAttachmentProvider);
		assertTrue(((CachingAttachmentProvider) wiki.attachments().getCurrentProvider()).getRealProvider()
				instanceof GitAttachmentProvider);
	}

	@Test
	public void everySaveIsOneCommitAndOneVersion() throws Exception {
		HttpServletRequest janne = wiki.login(JANNE);
		wiki.save(janne, "Topic", "first");
		wiki.save(janne, "Topic", "second");

		assertEquals(2, wiki.commitCount("Topic.txt"));
		List<Page> history = wiki.pages().getVersionHistory("Topic");
		assertEquals(2, history.size());
		assertEquals(2, history.get(0).getVersion());
		assertEquals(1, history.get(1).getVersion());

		assertEquals(saved("first"), wiki.pages().getPureText("Topic", 1));
		assertEquals(saved("second"), wiki.pages().getPureText("Topic", 2));
		// the latest page and text come from the cache, which the second save must have refreshed
		assertEquals(saved("second"), wiki.pages().getPureText("Topic", WikiProvider.LATEST_VERSION));
		assertEquals(2, wiki.pages().getPage("Topic").getVersion());
		assertTrue(wiki.pages().wikiPageExists("Topic", 2));
		assertFalse(wiki.pages().wikiPageExists("Topic", 3));
	}

	@Test
	public void unchangedTextDoesNotCommit() throws Exception {
		HttpServletRequest janne = wiki.login(JANNE);
		wiki.save(janne, "Topic", "stable");
		wiki.save(janne, "Topic", "stable");

		assertEquals(1, wiki.commitCount("Topic.txt"));
		assertEquals(1, wiki.pages().getVersionHistory("Topic").size());
	}

	@Test
	public void aSaveIsAuthoredWithTheProfileOfTheLoggedInUser() throws Exception {
		// the wiki sets the login name as the page author, which the provider resolves to the user's profile
		wiki.save(wiki.login(JANNE), "Topic", "text");

		assertEquals(JANNE_IDENTITY, wiki.lastAuthor("Topic.txt"));
	}

	@Test
	public void thePageAuthorReadBackIdentifiesTheUser() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "text");

		// whatever form the provider reports the author in, the user database must map it back to the user
		String author = wiki.pages().getPage("Topic").getAuthor();
		UserProfile profile = wiki.engine.getManager(UserManager.class).getUserDatabase().find(author);
		assertEquals(JANNE, profile.getLoginName());
		String historyAuthor = wiki.pages().getVersionHistory("Topic").get(0).getAuthor();
		assertEquals(JANNE, wiki.engine.getManager(UserManager.class).getUserDatabase().find(historyAuthor)
				.getLoginName());
	}

	@Test
	public void aCommitMadeOutsideTheWikiReplacesTheCachedPage() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "from the wiki");
		// fill the caches
		assertEquals(saved("from the wiki"), wiki.pages().getPureText("Topic", WikiProvider.LATEST_VERSION));
		assertEquals(1, wiki.pages().getPage("Topic").getVersion());

		Files.writeString(new File(wiki.repository, "Topic.txt").toPath(), "from a pull");
		wiki.git("commit", "-am", "pulled");
		// a history read moves the provider's index to the new HEAD, which evicts the moved pages from the cache
		assertEquals(2, wiki.pages().getVersionHistory("Topic").size());

		assertEquals(2, wiki.pages().getPage("Topic").getVersion());
		assertEquals("from a pull", wiki.pages().getPureText("Topic", WikiProvider.LATEST_VERSION));
		assertEquals(saved("from the wiki"), wiki.pages().getPureText("Topic", 1));
	}

	@Test
	public void attachmentsAreStoredListedAndReadThroughTheAttachmentManager() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "text");
		storeAttachment("Topic", "data.txt", "version one", JANNE);
		storeAttachment("Topic", "data.txt", "version two", JANNE);

		assertEquals(2, wiki.commitCount("Topic-att/data.txt"));
		assertEquals(JANNE_IDENTITY, wiki.lastAuthor("Topic-att/data.txt"));

		List<Attachment> listed = wiki.attachments().listAttachments(wiki.pages().getPage("Topic"));
		assertEquals(1, listed.size());
		assertEquals("Topic/data.txt", listed.get(0).getName());
		assertEquals(2, listed.get(0).getVersion());

		Attachment latest = wiki.attachments().getAttachmentInfo("Topic/data.txt");
		assertNotNull(latest);
		assertEquals(2, latest.getVersion());
		assertEquals("version two".length(), latest.getSize());
		assertEquals("version two", read(latest));

		Attachment first = wiki.attachments().getAttachmentInfo("Topic/data.txt", 1);
		assertNotNull(first);
		assertEquals("version one", read(first));

		List<Attachment> history = wiki.attachments().getVersionHistory("Topic/data.txt");
		assertEquals(2, history.size());
		assertEquals(2, history.get(0).getVersion());
	}

	@Test
	public void aRenameMovesThePageAndItsAttachmentsAndIsAuthoredByTheRenamingUser() throws Exception {
		wiki.save(wiki.login(ADMIN), "Before", "text");
		storeAttachment("Before", "data.txt", "content", ADMIN);

		HttpServletRequest janne = wiki.login(JANNE);
		wiki.engine.getManager(PageRenamer.class).renamePage(wiki.context(janne, "Before"), "Before", "After", true);

		String tracked = wiki.git("ls-files");
		assertFalse(tracked, tracked.contains("Before"));
		assertTrue(tracked, tracked.contains("After.txt"));
		assertTrue(tracked, tracked.contains("After-att/data.txt"));
		assertEquals("the rename must leave no uncommitted change", "", wiki.git("status", "--porcelain"));

		assertFalse(wiki.pages().wikiPageExists("Before"));
		assertNull(wiki.pages().getPage("Before"));
		assertTrue(wiki.pages().wikiPageExists("After"));
		assertEquals(saved("text"), wiki.pages().getPureText("After", WikiProvider.LATEST_VERSION));
		assertNull(wiki.attachments().getAttachmentInfo("Before/data.txt"));
		Attachment moved = wiki.attachments().getAttachmentInfo("After/data.txt");
		assertNotNull(moved);
		assertEquals("content", read(moved));

		assertEquals(JANNE_IDENTITY, wiki.lastAuthor("After.txt"));
	}

	@Test
	public void aDeleteRemovesThePageAndItsAttachments() throws Exception {
		wiki.save(wiki.login(JANNE), "Topic", "text");
		storeAttachment("Topic", "data.txt", "content", JANNE);

		wiki.pages().deletePage("Topic");

		assertEquals("", wiki.git("ls-files"));
		assertEquals("the delete must leave no uncommitted change", "", wiki.git("status", "--porcelain"));
		assertFalse(new File(wiki.repository, "Topic.txt").exists());
		assertFalse(new File(wiki.repository, "Topic-att/data.txt").exists());
		assertFalse(wiki.pages().wikiPageExists("Topic"));
		assertNull(wiki.pages().getPage("Topic"));
		assertNull(wiki.attachments().getAttachmentInfo("Topic/data.txt"));
		// the history stays in git
		assertEquals(2, wiki.commitCount("Topic.txt"));
	}

	/**
	 * The text as the wiki stores it, with CRLF line endings and a final line break.
	 */
	private static String saved(String text) {
		return text + "\r\n";
	}

	private void storeAttachment(String page, String fileName, String content, String author) throws Exception {
		Attachment attachment = Wiki.contents().attachment(wiki.engine, page, fileName);
		attachment.setAuthor(author);
		wiki.attachments().storeAttachment(attachment,
				new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
	}

	private String read(Attachment attachment) throws Exception {
		try (InputStream in = wiki.attachments().getAttachmentStream(attachment)) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new AssertionError("cannot read " + attachment.getName() + " v" + attachment.getVersion(), e);
		}
	}
}
