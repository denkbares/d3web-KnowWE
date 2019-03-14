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
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
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
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Josua Nürnberger
 * @created 2019-01-07
 */
public class GitVersioningFileProviderTest {

	private static final String TMP_NEW_REPO = "/tmp/newRepo";

	private Properties properties;

	@Before
	public void init() {
		properties = new Properties();
		properties.put(AbstractFileProvider.PROP_PAGEDIR, TMP_NEW_REPO);
	}

	@Test
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

		WikiPage page2 = new WikiPage(engine, "test2");
		page2.setLastModified(new Date());
		page2.setAuthor("UnknownAuthor");
		page2.setAttribute(WikiPage.CHANGENOTE, "add test2");
		fileProvider.putPageText(page2, "text of test page 2");

		Collection allChangedSince = fileProvider.getAllChangedSince(Date.from(nowMinusOneHour));
//		assertEquals(2, allChangedSince.size());
		System.out.println(allChangedSince);

		fileProvider.deletePage("test");
		allChangedSince = fileProvider.getAllChangedSince(Date.from(nowMinusOneHour));
//		assertEquals(3, allChangedSince.size());
		System.out.println(allChangedSince);
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

		fileProvider.deletePage("test");
		allChangedSince = fileProvider.getVersionHistory("test");
		assertNull("deleted pages have no version log anymore", allChangedSince);
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

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(TMP_NEW_REPO));
	}
}
