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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.providers.AttachmentProvider;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.pages.PageManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.when;

/**
 * Tests the git multi-wiki attachment provider: routing to the correct sub-wiki repo, versioning from git, and — the
 * key BR5 property — a page+attachment change inside one transaction producing a single commit per repo.
 */
public class GitVersioningAttachmentProviderMultiWikiTest {

	private static final String AUTHOR = "UnknownAuthor";

	private File pageDir;
	private File subRepo;
	private Properties properties;
	private Engine engine;
	private GitVersioningFileProviderMultiWiki pageProvider;
	private GitVersioningAttachmentProviderMultiWiki attachmentProvider;

	@Before
	public void setUp() throws Exception {
		pageDir = new File(System.getProperty("java.io.tmpdir"), "GitMultiWikiAttTest");
		FileUtils.deleteDirectory(pageDir);
		Files.createDirectories(new File(pageDir, "Main").toPath());
		subRepo = new File(pageDir, "Sub1");
		Files.createDirectories(subRepo.toPath());

		properties = new Properties();
		properties.put(AbstractFileProvider.PROP_PAGEDIR, pageDir.getAbsolutePath());
		properties.put(AttachmentProvider.PROP_STORAGEDIR, pageDir.getAbsolutePath()); // attachments inside the page repos
		properties.put(SubWikiUtils.MAIN_FOLDER_NAME, "Main");

		assumeTrue(BareGitConnector.fromPath(subRepo.getAbsolutePath()).gitInstalledAndReady());

		pageProvider = new GitVersioningFileProviderMultiWiki();
		engine = mockEngine(); // wires PageManager.getProvider() -> pageProvider
		pageProvider.initialize(engine, properties);

		attachmentProvider = new GitVersioningAttachmentProviderMultiWiki();
		attachmentProvider.initialize(engine, properties);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(pageDir);
	}

	@Test
	public void putAttachmentCommitsIntoTheParentsSubWikiRepo() throws Exception {
		putAttachment("Sub1&&Topic", "diagram.png", "PNGDATA-1");

		assertTrue(new File(subRepo, "Topic-att/diagram.png").exists());
		assertEquals(1, commits(subRepo, "Topic-att/diagram.png"));
		// nothing leaked into the main repo
		assertEquals(0, commits(new File(pageDir, "Main"), "Topic-att/diagram.png"));
	}

	@Test
	public void attachmentVersioningComesFromGit() throws Exception {
		putAttachment("Sub1&&Topic", "doc.txt", "first");
		putAttachment("Sub1&&Topic", "doc.txt", "second");

		Attachment query = new org.apache.wiki.attachment.Attachment(engine, "Sub1&&Topic", "doc.txt");
		List<Attachment> history = attachmentProvider.getVersionHistory(query);
		assertEquals(2, history.size());

		assertEquals("first", readVersion("Sub1&&Topic", "doc.txt", 1));
		assertEquals("second", readVersion("Sub1&&Topic", "doc.txt", WikiProvider.LATEST_VERSION));
	}

	@Test
	public void unchangedAttachmentDoesNotCommit() throws Exception {
		putAttachment("Sub1&&Topic", "doc.txt", "same");
		putAttachment("Sub1&&Topic", "doc.txt", "same");
		assertEquals(1, commits(subRepo, "Topic-att/doc.txt"));
	}

	@Test
	public void deleteAttachmentCommitsRemoval() throws Exception {
		putAttachment("Sub1&&Topic", "doc.txt", "content");
		Attachment att = new org.apache.wiki.attachment.Attachment(engine, "Sub1&&Topic", "doc.txt");
		att.setAuthor(AUTHOR);
		attachmentProvider.deleteAttachment(att);
		assertFalse(new File(subRepo, "Topic-att/doc.txt").exists());
	}

	/** BR5: a page edit and an attachment upload to the same sub-wiki inside one transaction → exactly one commit. */
	@Test
	public void pageAndAttachmentInOneTransactionYieldOneCommit() throws Exception {
		pageProvider.openCommit(AUTHOR);
		putPage("Sub1&&Topic", "page body");
		putAttachment("Sub1&&Topic", "attach.bin", "bytes");

		// staged, nothing committed yet
		assertEquals(0, commits(subRepo, "Topic.txt"));
		assertEquals(0, commits(subRepo, "Topic-att/attach.bin"));

		pageProvider.commit(AUTHOR, "bulk save with attachment");

		// both the page file and the attachment share one and the same commit
		String pageCommit = headCommitFor(subRepo, "Topic.txt");
		String attCommit = headCommitFor(subRepo, "Topic-att/attach.bin");
		assertEquals(1, commits(subRepo, "Topic.txt"));
		assertEquals(1, commits(subRepo, "Topic-att/attach.bin"));
		assertEquals("page and attachment must be in the same commit", pageCommit, attCommit);
	}

	// --- helpers -------------------------------------------------------------

	private void putPage(String fullName, String text) throws Exception {
		WikiPage page = new WikiPage(engine, fullName);
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

	private int commits(File repo, String relPath) {
		return BareGitConnector.fromPath(repo.getAbsolutePath()).log().commitHashesForFile(relPath).size();
	}

	private String headCommitFor(File repo, String relPath) {
		List<String> hashes = BareGitConnector.fromPath(repo.getAbsolutePath()).log().commitHashesForFile(relPath);
		return hashes.get(hashes.size() - 1);
	}

	private Engine mockEngine() throws Exception {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		when(engine.getWikiProperties()).thenReturn(properties);
		UserManager userManager = Mockito.mock(UserManager.class);
		UserDatabase userDatabase = Mockito.mock(UserDatabase.class);
		when(engine.getManager(UserManager.class)).thenReturn(userManager);
		when(userManager.getUserDatabase()).thenReturn(userDatabase);
		PageManager pageManager = Mockito.mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);
		// the attachment provider locates the page provider through the PageManager
		when(pageManager.getProvider()).thenReturn(pageProvider);
		return engine;
	}
}
