/*
 * Copyright (C) 2019 denkbares GmbH, Germany
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
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.wiki.PageManager;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.denkbares.utils.Files;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Josua Nürnberger
 * @created 2019-03-14
 */
public class GitVersioningAttachmentProviderTest {

	private String TMP_NEW_REPO = "/tmp/newRepo";
	public static final String AUTHOR = "author";

	private Properties properties;
	private WikiEngine engine;

	@Before
	public void init() throws Exception {
		TMP_NEW_REPO = new File(Files.getSystemTempDir(), "newRepo").getAbsolutePath();
//		System.out.println(TMP_NEW_REPO);
		properties = new Properties();
		properties.put(AbstractFileProvider.PROP_PAGEDIR, TMP_NEW_REPO);
		properties.put(GitVersioningAttachmentProvider.PROP_STORAGEDIR, TMP_NEW_REPO);
		engine = Mockito.mock(WikiEngine.class);
		when(engine.getWikiProperties()).thenReturn(properties);
		PageManager pageManager = Mockito.mock(PageManager.class);
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);
		when(engine.getPageManager()).thenReturn(pageManager);
		when(pageManager.getProvider()).thenReturn(fileProvider);
		UserManager um = Mockito.mock(UserManager.class);
		when(engine.getUserManager()).thenReturn(um);
		UserDatabase udb = mock(UserDatabase.class);
		when(um.getUserDatabase()).thenReturn(udb);
		UserProfile up = mock(UserProfile.class);
		when(udb.findByFullName(AUTHOR)).thenReturn(up);
		when(up.getFullname()).thenReturn(AUTHOR);
		when(up.getEmail()).thenReturn("author@nowhere.com");
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(TMP_NEW_REPO));
	}

	@Test
	public void testPutAttachment() throws IOException, NoRequiredPropertyException, ProviderException {
		GitVersioningAttachmentProvider attProvider = new GitVersioningAttachmentProvider();
		attProvider.initialize(engine, properties);
		Attachment att = new Attachment(engine, "test", "testAtt.txt");
		att.setAuthor(AUTHOR);
		att.setAttribute(Attachment.CHANGENOTE, "add");
		InputStream in = new ByteArrayInputStream("text file contents".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);
		in = new ByteArrayInputStream("text file contents".getBytes(StandardCharsets.UTF_8));
		att.setAttribute(Attachment.CHANGENOTE, "nothing changed");
		attProvider.putAttachmentData(att, in);

		att.setAttribute(Attachment.CHANGENOTE, "changed");
		in = new ByteArrayInputStream("text file contents 2".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);

		List<Attachment> versionHistory = attProvider.getVersionHistory(att);
//		for (Attachment attachment : versionHistory) {
//			System.out.println(attachment.getAttribute(Attachment.CHANGENOTE));
//		}

		assertEquals(2, versionHistory.size());
	}

	@Test
	public void testGetVersionHistory() throws IOException, NoRequiredPropertyException, ProviderException {
		GitVersioningAttachmentProvider attProvider = new GitVersioningAttachmentProvider();
		attProvider.initialize(engine, properties);
		Attachment att = new Attachment(engine, "test", "testAtt.txt");
		att.setAuthor(AUTHOR);
		InputStream in = new ByteArrayInputStream("text file contents".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);
		in = new ByteArrayInputStream("text file contents 2".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);
		in = new ByteArrayInputStream("text file contents 3".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);

		List<Attachment> versionHistory = attProvider.getVersionHistory(att);
		assertEquals(3, versionHistory.size());
		assertEquals(18, versionHistory.get(0).getSize());
		assertEquals(20, versionHistory.get(2).getSize());
	}

	@Test
	public void testGetAttachmentData() throws IOException, NoRequiredPropertyException, ProviderException {
		GitVersioningAttachmentProvider attProvider = new GitVersioningAttachmentProvider();
		attProvider.initialize(engine, properties);
		Attachment att = new Attachment(engine, "test", "testAtt.txt");
		att.setAuthor(AUTHOR);
		InputStream in = new ByteArrayInputStream("text file contents".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);
		in = new ByteArrayInputStream("text file contents 2".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);
		in = new ByteArrayInputStream("text file contents 3".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);

		att.setVersion(2);
		InputStream attachmentData = attProvider.getAttachmentData(att);
		String s = IOUtils.toString(attachmentData, StandardCharsets.UTF_8);
		assertEquals("text file contents 2", s);
	}

	@Test
	public void testDeleteAttachment() throws IOException, NoRequiredPropertyException, ProviderException {
		GitVersioningAttachmentProvider attProvider = new GitVersioningAttachmentProvider();
		attProvider.initialize(engine, properties);
		Attachment att = new Attachment(engine, "test", "testAtt.txt");
		att.setAuthor(AUTHOR);
		InputStream in = new ByteArrayInputStream("text file contents".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);

		attProvider.deleteAttachment(att);
		assertFalse(new File(TMP_NEW_REPO + "/test-att/testAtt.txt").exists());
	}

	@Test
	public void testListAttachments() throws IOException, NoRequiredPropertyException, ProviderException {
		GitVersioningAttachmentProvider attProvider = new GitVersioningAttachmentProvider();
		attProvider.initialize(engine, properties);
		Attachment att = new Attachment(engine, "test", "testAtt.txt");
		att.setAuthor(AUTHOR);
		InputStream in = new ByteArrayInputStream("text file contents".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);
		att = new Attachment(engine, "test", "testAtt2.txt");
		att.setAuthor(AUTHOR);
		in = new ByteArrayInputStream("text file contents".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);

		WikiPage page = new WikiPage(engine, "test");
		Collection<Attachment> attachments = attProvider.listAttachments(page);
		assertEquals(2, attachments.size());
	}

	@Test
	public void testMoveAttachments() throws IOException, NoRequiredPropertyException, ProviderException {
		GitVersioningAttachmentProvider attProvider = new GitVersioningAttachmentProvider();
		attProvider.initialize(engine, properties);
		Attachment att = new Attachment(engine, "test page", "testAtt.txt");
		att.setAuthor(AUTHOR);
		InputStream in = new ByteArrayInputStream("text file contents".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);
		att = new Attachment(engine, "test page", "testAtt2.txt");
		att.setAuthor(AUTHOR);
		in = new ByteArrayInputStream("text file contents".getBytes(StandardCharsets.UTF_8));
		attProvider.putAttachmentData(att, in);
		WikiPage from = new WikiPage(engine, "test page");
		from.setAuthor("UnknownAuthor");
		attProvider.moveAttachmentsForPage(from, "new test page");
		File dir = new File(TMP_NEW_REPO + "/new+test+page-att");
		assertNotNull(dir);
		assertTrue(dir.exists());
		assertEquals(2, dir.listFiles().length);
	}
}
