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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.providers.AttachmentProvider;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.cache.CachingManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.AbstractFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.when;

/**
 * Tests the single-wiki git attachment provider: versioning from git inside the page repository, and — the key BR5
 * property — a page+attachment change inside one transaction producing a single commit.
 */
public class GitAttachmentProviderTest {

	private static final String AUTHOR = "UnknownAuthor";

	private File pageDir;
	private Properties properties;
	private Engine engine;
	private GitPageProvider pageProvider;
	private GitAttachmentProvider attachmentProvider;

	@Before
	public void setUp() throws Exception {
		pageDir = new File(System.getProperty("java.io.tmpdir"), "GitAttachmentProviderTest");
		FileUtils.deleteDirectory(pageDir);

		properties = new Properties();
		properties.put(AbstractFileProvider.PROP_PAGEDIR, pageDir.getAbsolutePath());
		properties.put(AttachmentProvider.PROP_STORAGEDIR, pageDir.getAbsolutePath()); // attachments inside the page repo

		assumeTrue(BareGitConnector.fromPath(System.getProperty("java.io.tmpdir")).gitInstalledAndReady());

		pageProvider = new GitPageProvider();
		engine = mockEngine(); // wires PageManager.getProvider() -> pageProvider
		pageProvider.initialize(engine, properties);

		attachmentProvider = new GitAttachmentProvider();
		attachmentProvider.initialize(engine, properties);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(pageDir);
	}

	@Test
	public void putAttachmentCommitsIntoThePageRepository() throws Exception {
		putAttachment("Topic", "diagram.png", "PNGDATA-1");

		assertTrue(new File(pageDir, "Topic-att/diagram.png").exists());
		assertEquals(1, commits("Topic-att/diagram.png"));
	}

	@Test
	public void attachmentVersioningComesFromGit() throws Exception {
		putAttachment("Topic", "doc.txt", "first");
		putAttachment("Topic", "doc.txt", "second");

		Attachment query = new org.apache.wiki.attachment.Attachment(engine, "Topic", "doc.txt");
		List<Attachment> history = attachmentProvider.getVersionHistory(query);
		assertEquals(2, history.size());

		assertEquals("first", readVersion("Topic", "doc.txt", 1));
		assertEquals("second", readVersion("Topic", "doc.txt", WikiProvider.LATEST_VERSION));
	}

	@Test
	public void unchangedAttachmentDoesNotCommit() throws Exception {
		putAttachment("Topic", "doc.txt", "same");
		putAttachment("Topic", "doc.txt", "same");
		assertEquals(1, commits("Topic-att/doc.txt"));
	}

	@Test
	public void deleteAttachmentCommitsRemoval() throws Exception {
		putAttachment("Topic", "doc.txt", "content");
		Attachment att = new org.apache.wiki.attachment.Attachment(engine, "Topic", "doc.txt");
		att.setAuthor(AUTHOR);
		attachmentProvider.deleteAttachment(att);
		assertFalse(new File(pageDir, "Topic-att/doc.txt").exists());
		assertTrue(BareGitConnector.fromPath(pageDir.getAbsolutePath()).status().isClean());
	}

	@Test
	public void listAttachmentsFindsStoredFiles() throws Exception {
		putAttachment("Topic", "a.bin", "aaa");
		putAttachment("Topic", "b.bin", "bbb");

		List<Attachment> attachments = attachmentProvider.listAttachments(new WikiPage(engine, "Topic"));
		assertEquals(2, attachments.size());
	}

	@Test
	public void moveAttachmentsForPageMovesTheDirectoryInOneCommit() throws Exception {
		putAttachment("Before", "doc.txt", "content");

		WikiPage oldParent = new WikiPage(engine, "Before");
		oldParent.setAuthor(AUTHOR);
		attachmentProvider.moveAttachmentsForPage(oldParent, "After");

		assertFalse(new File(pageDir, "Before-att/doc.txt").exists());
		assertTrue(new File(pageDir, "After-att/doc.txt").exists());
		assertEquals(1, commits("After-att/doc.txt"));
		assertTrue(BareGitConnector.fromPath(pageDir.getAbsolutePath()).status().isClean());
	}

	/**
	 * The provider anchors attachments at the page repository, so a storage dir pointing anywhere else is a broken
	 * configuration that must fail loudly at startup instead of scattering files across two directories.
	 */
	@Test
	public void initializeRejectsAStorageDirOutsideThePageRepository() throws Exception {
		Properties broken = new Properties();
		broken.putAll(properties);
		broken.put(AttachmentProvider.PROP_STORAGEDIR, new File(pageDir, "elsewhere").getAbsolutePath());

		GitAttachmentProvider misconfigured = new GitAttachmentProvider();
		try {
			misconfigured.initialize(engine, broken);
			fail("a storage dir outside the page repository must be rejected");
		}
		catch (IOException expected) {
			assertTrue(expected.getMessage().contains(AttachmentProvider.PROP_STORAGEDIR));
		}
	}

	/** BR5: a page edit and an attachment upload inside one transaction produce exactly one commit. */
	@Test
	public void pageAndAttachmentInOneTransactionYieldOneCommit() throws Exception {
		pageProvider.openCommit(AUTHOR);
		putPage("Topic", "page body");
		putAttachment("Topic", "attach.bin", "bytes");

		// staged, nothing committed yet
		assertEquals(0, commits("Topic.txt"));
		assertEquals(0, commits("Topic-att/attach.bin"));

		pageProvider.commit(AUTHOR, "bulk save with attachment");

		// both the page file and the attachment share one and the same commit
		String pageCommit = headCommitFor("Topic.txt");
		String attCommit = headCommitFor("Topic-att/attach.bin");
		assertEquals(1, commits("Topic.txt"));
		assertEquals(1, commits("Topic-att/attach.bin"));
		assertEquals("page and attachment must be in the same commit", pageCommit, attCommit);
	}

	// --- helpers -------------------------------------------------------------

	private void putPage(String name, String text) throws Exception {
		WikiPage page = new WikiPage(engine, name);
		page.setAuthor(AUTHOR);
		page.setAttribute(WikiPage.CHANGENOTE, "edit");
		page.setLastModified(new Date());
		pageProvider.putPageText(page, text);
	}

	private void putAttachment(String parent, String fileName, String content) throws Exception {
		Attachment att = new org.apache.wiki.attachment.Attachment(engine, parent, fileName);
		att.setAuthor(AUTHOR);
		att.setAttribute(Attachment.CHANGENOTE, "upload");
		att.setLastModified(new Date());
		try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
			attachmentProvider.putAttachmentData(att, in);
		}
	}

	private String readVersion(String parent, String fileName, int version) throws Exception {
		Attachment att = new org.apache.wiki.attachment.Attachment(engine, parent, fileName);
		att.setVersion(version);
		try (InputStream in = attachmentProvider.getAttachmentData(att)) {
			return IOUtils.toString(in, StandardCharsets.UTF_8);
		}
	}

	private int commits(String relPath) {
		return BareGitConnector.fromPath(pageDir.getAbsolutePath()).log().commitHashesForFile(relPath).size();
	}

	private String headCommitFor(String relPath) {
		List<String> hashes = BareGitConnector.fromPath(pageDir.getAbsolutePath()).log().commitHashesForFile(relPath);
		return hashes.get(hashes.size() - 1);
	}

	private Engine mockEngine() {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		when(engine.getWikiProperties()).thenReturn(properties);
		UserManager userManager = Mockito.mock(UserManager.class);
		UserDatabase userDatabase = Mockito.mock(UserDatabase.class);
		when(engine.getManager(UserManager.class)).thenReturn(userManager);
		when(userManager.getUserDatabase()).thenReturn(userDatabase);
		when(engine.getManager(CachingManager.class)).thenReturn(Mockito.mock(CachingManager.class));
		// the attachment provider locates the page provider through the PageManager
		PageManager pageManager = Mockito.mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);
		when(pageManager.getProvider()).thenReturn(pageProvider);
		return engine;
	}
}
