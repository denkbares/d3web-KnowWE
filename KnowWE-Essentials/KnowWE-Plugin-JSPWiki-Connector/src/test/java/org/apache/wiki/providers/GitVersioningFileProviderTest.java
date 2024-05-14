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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.pages.PageManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Josua Nürnberger
 * @created 2019-01-07
 */
@SuppressWarnings("rawtypes")
public class GitVersioningFileProviderTest {

	private String TMP_NEW_REPO = "/tmp/newRepo";

	private Properties properties;

	@Before
	public void init() {
		TMP_NEW_REPO = System.getProperty("java.io.tmpdir") + "/newRepo";
//		System.out.println(TMP_NEW_REPO);
		properties = new Properties();
		properties.put(AbstractFileProvider.PROP_PAGEDIR, TMP_NEW_REPO);
		properties.put("var.basedir", TMP_NEW_REPO);
		properties.put(GitVersioningFileProvider.JSPWIKI_GIT_DEFAULT_BRANCH,"maintenance");
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(TMP_NEW_REPO));
	}

	@Test
	@Ignore
	public void testInitializeWithoutExistingRepo() throws IOException, NoRequiredPropertyException {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		Mockito.when(engine.getWikiProperties()).thenReturn(properties);
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		properties.put(GitVersioningFileProvider.JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT, "ssh://git@gitlab.example.com:2222/root/knowwetest.git");

		fileProvider.initialize(engine, properties);

		Repository repo = getRepository();
		assertTrue(repo.getObjectDatabase().exists());
		properties.remove(GitVersioningFileProvider.JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT);
	}

	Repository getRepository() throws IOException {
		return new FileRepositoryBuilder().setGitDir(new File(TMP_NEW_REPO + "/.git")).build();
	}

	@Test
	public void testInitializeLocalGit() throws IOException, NoRequiredPropertyException {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		Mockito.when(engine.getWikiProperties()).thenReturn(properties);
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);

		Repository repo = getRepository();
		assertTrue(repo.getObjectDatabase().exists());
	}

	@Test
	public void testPutPageText() throws NoSuchPrincipalException, IOException, NoRequiredPropertyException, ProviderException {
		String author = "UnknownAuthor";
		Engine engine = getWikiEngineMock(author);
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);

		WikiPage page = new WikiPage(engine, "tess");
		page.setLastModified(new Date());
		page.setAuthor(author);
		page.setAttribute(WikiPage.CHANGENOTE, "add test");
		fileProvider.putPageText(page, "test file text");

		//these are essentially NO-OPs as they do not change anything
		fileProvider.putPageText(page, "test file text");
		fileProvider.putPageText(page, "test file text");
		fileProvider.putPageText(page, "test file text");

		page.setLastModified(new Date());
		fileProvider.putPageText(page, "test file text2");

		List<Page> versionHistory = fileProvider.getVersionHistory(page.getName());
		assertEquals(2, versionHistory.size());
	}

	@Test
	public void testGetPageText() throws NoSuchPrincipalException, IOException, NoRequiredPropertyException, ProviderException {
		String author = "UnknownAuthor";
		Engine engine = getWikiEngineMock(author);
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);

		WikiPage page = new WikiPage(engine, "tess");
		page.setLastModified(new Date());
		page.setAuthor(author);
		page.setAttribute(WikiPage.CHANGENOTE, "add test");
		fileProvider.putPageText(page, "test file text");

		page.setLastModified(new Date());
		fileProvider.putPageText(page, "test file text2");

		String tess = fileProvider.getPageText("tess", 1);
		assertEquals("test file text", tess);

		Page pageInfo = fileProvider.getPageInfo(page.getName(), 2);
		assertNotNull("get page info version 2", pageInfo);
		pageInfo = fileProvider.getPageInfo(page.getName(), PageProvider.LATEST_VERSION);
		assertNotNull("get page info latest version", pageInfo);
		String pageText = fileProvider.getPageText(page.getName(), 2);
		assertEquals("get text version 2", "test file text2", pageText);
		pageText = fileProvider.getPageText(page.getName(), PageProvider.LATEST_VERSION);
		assertEquals("get text latest version", "test file text2", pageText);
	}

	@Test
	public void testMovePage() throws ProviderException, IOException, NoRequiredPropertyException, NoSuchPrincipalException {
		String author = "UnknownAuthor";
		Engine engine = getWikiEngineMock(author);
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);

		WikiPage page = new WikiPage(engine, "test seite");
		page.setLastModified(new Date());
		page.setAuthor(author);
		page.setAttribute(WikiPage.CHANGENOTE, "add test");
		fileProvider.putPageText(page, "test file text");
		WikiPage from = new WikiPage(engine, "test seite");
		from.setAuthor("UnknownAuthor");
		fileProvider.movePage(from, "Neue Seite");

		assertTrue(new File(TMP_NEW_REPO + "/Neue+Seite.txt").exists());
	}

	@Test
	public void testGetChangedSince() throws IOException, NoRequiredPropertyException, ProviderException, NoSuchPrincipalException {
		final String author = "UnknownAuthor";
		Engine engine = getWikiEngineMock(author);

		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);
		Instant nowMinusOneHour = Instant.now();
		nowMinusOneHour = nowMinusOneHour.minus(1, ChronoUnit.HOURS);

		WikiPage page = new WikiPage(engine, "test");
		page.setLastModified(new Date());
		page.setAuthor("UnknownAuthor");
		page.setAttribute(WikiPage.CHANGENOTE, "add test");
		fileProvider.putPageText(page, "test file text");

		page.setAttribute(WikiPage.CHANGENOTE, "changed test");
		fileProvider.putPageText(page, "new text");

		WikiPage page2 = new WikiPage(engine, "test 2");
		page2.setLastModified(new Date());
		page2.setAuthor("UnknownAuthor");
		page2.setAttribute(WikiPage.CHANGENOTE, "add test2");
		fileProvider.putPageText(page2, "text of test page 2");

		List<Page> allChangedSince = new ArrayList<>(fileProvider.getAllChangedSince(Date.from(nowMinusOneHour)));
		assertEquals(3, allChangedSince.size());
		assertEquals("test", allChangedSince.get(0).getName());
		assertEquals("test", allChangedSince.get(1).getName());
		assertEquals("test 2", allChangedSince.get(2).getName());

		WikiPage p = new WikiPage(engine, "test");
		p.setAuthor("UnknownAuthor");
		fileProvider.deletePage(p);
		allChangedSince = new ArrayList<>(fileProvider.getAllChangedSince(Date.from(nowMinusOneHour)));
		assertEquals(4, allChangedSince.size());
		assertEquals("test", allChangedSince.get(3).getName());
	}

	@Test
	public void testGetVersions() throws IOException, NoRequiredPropertyException, ProviderException, NoSuchPrincipalException {
		final String author = "UnknownAuthor";
		Engine engine = getWikiEngineMock(author);

		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);

		WikiPage page = new WikiPage(engine, "test");
		page.setLastModified(new Date());
		page.setAuthor("UnknownAuthor");
		page.setAttribute(WikiPage.CHANGENOTE, "add test");
		fileProvider.putPageText(page, "test file text");

		WikiPage page2 = new WikiPage(engine, "test");
		page2.setLastModified(new Date());
		page2.setAuthor("UnknownAuthor");
		page2.setAttribute(WikiPage.CHANGENOTE, "add test2");
		fileProvider.putPageText(page2, "text of test page ");

		Collection allChangedSince = fileProvider.getVersionHistory("test");
		assertEquals(2, allChangedSince.size());

		WikiPage p = new WikiPage(engine, "test");
		p.setAuthor("UnknownAuthor");
		fileProvider.deletePage(p);
		allChangedSince = fileProvider.getVersionHistory("test");
		assertTrue("deleted pages have no version log anymore", allChangedSince.isEmpty());
	}

	@Test
	public void testEmptyCommit() throws NoSuchPrincipalException, IOException, NoRequiredPropertyException, ProviderException, GitAPIException {
		Engine engine = getWikiEngineMock("egal");
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);
		Git git = new Git(getRepository());

		List<RevCommit> revCommitsInitially = GitVersioningUtils.reverseToList(git.log().call());
		WikiPage page = getWikiPage(engine, "test", "egal");
		fileProvider.putPageText(page, "text");
		fileProvider.putPageText(page, "text");
		fileProvider.putPageText(page, "text");


		List<RevCommit> revCommits = GitVersioningUtils.reverseToList(git.log().call());
		assertEquals(1, revCommits.size()-revCommitsInitially.size());
		Instant nowMinusOneHour = Instant.now().minus(1,ChronoUnit.HOURS);
		List<Page> allChangedSince = new ArrayList<>(fileProvider.getAllChangedSince(Date.from(nowMinusOneHour)));
		assertEquals(1, allChangedSince.size());
	}

	@Test
	public void testCommitTransaction() throws NoSuchPrincipalException, IOException, NoRequiredPropertyException, ProviderException, GitAPIException {
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		String user1 = "User1";
		String user2 = "User2";
		Engine engine = getWikiEngineMock(user1, user2);
		properties.put(GitVersioningAttachmentProvider.PROP_STORAGEDIR, TMP_NEW_REPO);
		PageManager pm = Mockito.mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pm);
		when(pm.getProvider()).thenReturn(fileProvider);
		GitVersioningAttachmentProvider attachmentProvider = new GitVersioningAttachmentProvider();
		fileProvider.initialize(engine, properties);
		attachmentProvider.initialize(engine, properties);
		fileProvider.openCommit(user1);
		WikiPage page = getWikiPage(engine, "test", user1);
		fileProvider.putPageText(page, "new Text");
		fileProvider.putPageText(getWikiPage(engine, "other commit", user2), "other commit");

		Attachment att = new org.apache.wiki.attachment.Attachment(engine, "test", "dings.txt");
		att.setAuthor(user1);
		attachmentProvider.putAttachmentData(att, new ByteArrayInputStream("test inhalt".getBytes(StandardCharsets.UTF_8)));

		WikiPage page3 = getWikiPage(engine, "another page", user1);
		fileProvider.putPageText(page3, "more content");

		fileProvider.commit(user1, "one commit");

		Repository repo = getRepository();
		Git git = new Git(repo);
		Iterable<RevCommit> commitLog = git.log().call();
		List<RevCommit> revCommits = GitVersioningUtils.reverseToList(commitLog);
		assertEquals("expected commits", 3, revCommits.size());
	}

	@Test
	public void testRollback() throws NoSuchPrincipalException, IOException, NoRequiredPropertyException, ProviderException, GitAPIException {
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		String user1 = "User1";
		String user2 = "User2";

		@NotNull Engine engine = getWikiEngineMock(user1, user2);
		properties.put(GitVersioningAttachmentProvider.PROP_STORAGEDIR, TMP_NEW_REPO);
		PageManager pm = Mockito.mock(PageManager.class);
		when(engine.getManager(PageManager.class)).thenReturn(pm);
		when(pm.getProvider()).thenReturn(fileProvider);
		GitVersioningAttachmentProvider attachmentProvider = new GitVersioningAttachmentProvider();
		fileProvider.initialize(engine, properties);
		attachmentProvider.initialize(engine, properties);

		Repository repo = getRepository();
		Git git = new Git(repo);

		List<RevCommit> initialCommits = GitVersioningUtils.reverseToList(git.log().call());

		fileProvider.openCommit(user1);
		WikiPage page = getWikiPage(engine, "to revert", user1);
		fileProvider.putPageText(page, "new Text");

		fileProvider.openCommit(user2);
		fileProvider.putPageText(getWikiPage(engine, "other commit", user2), "other commit");

		Attachment att = new org.apache.wiki.attachment.Attachment(engine, "to revert", "dingsToRevert.txt");
		att.setAuthor(user1);
		attachmentProvider.putAttachmentData(att, new ByteArrayInputStream("test inhalt".getBytes(StandardCharsets.UTF_8)));

		WikiPage page3 = getWikiPage(engine, "another page to revert", user1);
		fileProvider.putPageText(page3, "more content");

		//rollbck user 1
		fileProvider.rollback(user1);
		String commitMsg = "should be committed after reverting user1";

		fileProvider.commit(user2, commitMsg);

		Iterable<RevCommit> commitLog = git.log().call();
		List<RevCommit> revCommits = GitVersioningUtils.reverseToList(commitLog);

		assertEquals("expected commits", 1, revCommits.size()-initialCommits.size());
		assertEquals(commitMsg, revCommits.get(revCommits.size()-1).getFullMessage());

		Status status = git.status().call();
		//TODO
//		assertEquals(0, status.getUntracked().size());
		assertEquals(0, status.getAdded().size());
		assertEquals(0, status.getModified().size());

		String name4 = "first create";
		WikiPage page4 = getWikiPage(engine, name4, user1);
		page4.setAttribute(WikiPage.CHANGENOTE, "created");
		fileProvider.putPageText(page4, "created");
		Attachment att4 = new org.apache.wiki.attachment.Attachment(engine, name4, "keep.txt");
		att4.setAuthor(user1);
		att4.setAttribute(Attachment.CHANGENOTE, "created");
		attachmentProvider.putAttachmentData(att4, new ByteArrayInputStream("keep".getBytes(StandardCharsets.UTF_8)));
		fileProvider.openCommit(user1);
		Attachment toRevert = new org.apache.wiki.attachment.Attachment(engine, name4, "revert.txt");
		toRevert.setAuthor(user1);
		attachmentProvider.putAttachmentData(toRevert, new ByteArrayInputStream("revert".getBytes(StandardCharsets.UTF_8)));
		attachmentProvider.putAttachmentData(att4, new ByteArrayInputStream("only revert this text".getBytes(StandardCharsets.UTF_8)));
		fileProvider.rollback(user1);
		Collection<Attachment> attachments = attachmentProvider.listAttachments(page4);

		assertEquals(1, attachments.size());
		Attachment attachmentInfo = attachmentProvider.getAttachmentInfo(page4, "keep.txt", PageProvider.LATEST_VERSION);
		String string = IOUtils.toString(attachmentProvider.getAttachmentData(attachmentInfo), StandardCharsets.UTF_8);
		assertEquals("keep", string);
	}

	private WikiPage getWikiPage(Engine engine, String name, String user) {
		WikiPage page = new WikiPage(engine, name);
		page.setAuthor(user);
		return page;
	}

	@NotNull Engine getWikiEngineMock(String... authors) throws NoSuchPrincipalException {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		when(engine.getWikiProperties()).thenReturn(properties);
		UserManager uMan = Mockito.mock(UserManager.class);
		UserDatabase uDB = Mockito.mock(UserDatabase.class);
		when(engine.getManager(UserManager.class)).thenReturn(uMan);
		when(uMan.getUserDatabase()).thenReturn(uDB);
		for (String author : authors) {
			UserProfile uP = Mockito.mock(UserProfile.class);
			when(uDB.findByFullName(author)).thenReturn(uP);
			when(uP.getFullname()).thenReturn(author);
			when(uP.getEmail()).thenReturn(author + "@example.com");
		}
		return engine;
	}
}
