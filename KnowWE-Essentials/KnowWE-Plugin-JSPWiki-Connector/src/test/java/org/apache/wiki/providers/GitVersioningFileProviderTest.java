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

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.WikiProvider;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.eclipse.jgit.lib.Repository;
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
public class GitVersioningFileProviderTest {

	private String TMP_NEW_REPO = "/tmp/newRepo";

	private Properties properties;

	@Before
	public void init() {
		TMP_NEW_REPO = System.getProperty("java.io.tmpdir") + "/newRepo";
//		System.out.println(TMP_NEW_REPO);
		properties = new Properties();
		properties.put(AbstractFileProvider.PROP_PAGEDIR, TMP_NEW_REPO);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(TMP_NEW_REPO));
	}

	@Test
	@Ignore
	public void testInitializeWithoutExistingRepo() throws IOException, NoRequiredPropertyException {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		properties.put(GitVersioningFileProvider.JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT, "ssh://git@gitlab.example.com:2222/root/knowwetest.git");

		fileProvider.initialize(engine, properties);

		Repository repo = new FileRepositoryBuilder().setGitDir(new File(TMP_NEW_REPO + "/.git")).build();
		assertTrue(repo.getObjectDatabase().exists());
		properties.remove(GitVersioningFileProvider.JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT);
	}

	@Test
	public void testInitializeLocalGit() throws IOException, NoRequiredPropertyException {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);

		Repository repo = new FileRepositoryBuilder().setGitDir(new File(TMP_NEW_REPO + "/.git")).build();
		assertTrue(repo.getObjectDatabase().exists());
	}

	@Test
	public void testPutPageText() throws NoSuchPrincipalException, IOException, NoRequiredPropertyException, ProviderException {
		String author = "UnknownAuthor";
		WikiEngine engine = getWikiEngineMock(author);
		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);

		WikiPage page = new WikiPage(engine, "tess");
		page.setLastModified(new Date());
		page.setAuthor(author);
		page.setAttribute(WikiPage.CHANGENOTE, "add test");
		fileProvider.putPageText(page, "test file text");

		fileProvider.putPageText(page, "test file text");

		page.setLastModified(new Date());
		fileProvider.putPageText(page, "test file text2");

		List<WikiPage> versionHistory = fileProvider.getVersionHistory(page.getName());
		assertEquals(2, versionHistory.size());
	}

	@Test
	public void testGetPageText() throws NoSuchPrincipalException, IOException, NoRequiredPropertyException, ProviderException {
		String author = "UnknownAuthor";
		WikiEngine engine = getWikiEngineMock(author);
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

		WikiPage pageInfo = fileProvider.getPageInfo(page.getName(), 2);
		assertNotNull("get page info version 2", pageInfo);
		pageInfo = fileProvider.getPageInfo(page.getName(), WikiProvider.LATEST_VERSION);
		assertNotNull("get page info latest version", pageInfo);
		String pageText = fileProvider.getPageText(page.getName(), 2);
		assertEquals("get text version 2", "test file text2", pageText);
		pageText = fileProvider.getPageText(page.getName(), WikiProvider.LATEST_VERSION);
		assertEquals("get text latest version", "test file text2", pageText);
	}

	@Test
	public void testMovePage() throws ProviderException, IOException, NoRequiredPropertyException, NoSuchPrincipalException {
		String author = "UnknownAuthor";
		WikiEngine engine = getWikiEngineMock(author);
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
		WikiEngine engine = getWikiEngineMock(author);

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

		List<WikiPage> allChangedSince = new ArrayList<>(fileProvider.getAllChangedSince(Date.from(nowMinusOneHour)));
		assertEquals(3, allChangedSince.size());
//		for (Object o : allChangedSince) {
//			WikiPage o1 = (WikiPage) o;
//			System.out.println(o1.getName());
//			System.out.println(o1.getAttribute(WikiPage.CHANGENOTE));
//			System.out.println(o1.getLastModified());
//		}
		assertEquals("test", allChangedSince.get(0).getName());
		assertEquals("test", allChangedSince.get(1).getName());
		assertEquals("test 2", allChangedSince.get(2).getName());

		WikiPage p = new WikiPage(engine, "test");
		p.setAuthor("UnknownAuthor");
		fileProvider.deletePage(p);
		allChangedSince = new ArrayList<>(fileProvider.getAllChangedSince(Date.from(nowMinusOneHour)));
		assertEquals(4, allChangedSince.size());
		assertEquals("test", allChangedSince.get(3).getName());
//		System.out.println(allChangedSince);
	}

	@Test
	public void testGetVersions() throws IOException, NoRequiredPropertyException, ProviderException, NoSuchPrincipalException {
		final String author = "UnknownAuthor";
		WikiEngine engine = getWikiEngineMock(author);

		GitVersioningFileProvider fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);
		Instant nowMinusOneHour = Instant.now();
		nowMinusOneHour = nowMinusOneHour.minus(1, ChronoUnit.HOURS);

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

	@NotNull WikiEngine getWikiEngineMock(String author) throws NoSuchPrincipalException {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		UserManager uMan = Mockito.mock(UserManager.class);
		UserDatabase uDB = Mockito.mock(UserDatabase.class);
		UserProfile uP = Mockito.mock(UserProfile.class);
		when(engine.getUserManager()).thenReturn(uMan);
		when(uMan.getUserDatabase()).thenReturn(uDB);
		when(uDB.findByFullName(author)).thenReturn(uP);
		when(uP.getFullname()).thenReturn(author);
		when(uP.getEmail()).thenReturn(author + "@example.com");
		return engine;
	}

}
