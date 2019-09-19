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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.wiki.PageManager;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.WikiProvider;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.denkbares.utils.Stopwatch;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VersioningFileProviderStressTest {

	public static final String NAME1 = "Test1";
	public static final String AUTHOR = "author";
	int NUM_PAGES = 1000;

	private File wikiDir;

	private Properties properties;
	private WikiEngine engine;
	private VersioningFileProvider fileProvider;
	private int numConcurrentUsers;
	private int numEdits;
	private int numConcurrentPages;

	@Before
	public void setUp() throws Exception {

		this.NUM_PAGES = Integer.valueOf(System.getProperty("pageprovider.stresstest.numPages", "1000"));
		this.numEdits = Integer.valueOf(System.getProperty("pageprovider.stresstest.numEdits", "100"));
		this.numConcurrentUsers = Integer.valueOf(System.getProperty("pageprovider.stresstest.concurrentUsers", "100"));
		this.numConcurrentPages = Integer.valueOf(System.getProperty("pageprovider.stresstest.concurrentPages", "10"));

//		TestEngine
		this.engine = Mockito.mock(WikiEngine.class);
		final String path = System.getProperty("pageprovider.stresstest.workdir", System.getProperty("java.io.tmpdir") + "/stresstest");
		assertNotNull(path);
		this.wikiDir = new File(path);
		assertTrue(this.wikiDir.getParentFile().exists());
		System.out.println(this.wikiDir.getAbsolutePath());
		this.properties = new Properties();
		this.properties.put(AbstractFileProvider.PROP_PAGEDIR, this.wikiDir.getAbsolutePath());
		this.properties.put(BasicAttachmentProvider.PROP_STORAGEDIR, this.wikiDir.getAbsolutePath());
		this.engine = Mockito.mock(WikiEngine.class);
		when(this.engine.getWikiProperties()).thenReturn(this.properties);

		final PageManager pageManager = Mockito.mock(PageManager.class);
		this.fileProvider = new VersioningFileProvider();
		this.fileProvider.initialize(this.engine, this.properties);
		when(this.engine.getPageManager()).thenReturn(pageManager);
		when(pageManager.getProvider()).thenReturn(this.fileProvider);
		final UserManager um = Mockito.mock(UserManager.class);
		when(this.engine.getUserManager()).thenReturn(um);
		final UserDatabase udb = mock(UserDatabase.class);
		when(um.getUserDatabase()).thenReturn(udb);
		final UserProfile up = mock(UserProfile.class);
		when(udb.findByFullName(AUTHOR)).thenReturn(up);
		when(up.getFullname()).thenReturn(AUTHOR);
		when(up.getEmail()).thenReturn("author@nowhere.com");
	}

	@After
	public void tearDown() {
		try {
			FileUtils.deleteDirectory(this.wikiDir);
			Assert.assertFalse(this.wikiDir.exists());
		}
		catch (final IOException ex) {
			Assert.fail("Could not delete wiki directory");
		}
	}

	@Test
	public void testCreatePages()
			throws Exception {
		final Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		createPages(this.NUM_PAGES);

		// check that we have NUM_PAGES file in the wiki dir
		Assert.assertEquals(this.NUM_PAGES, countWikiPages());
		Assert.assertEquals(this.NUM_PAGES, countWikiPageFiles());

		System.out.println("testCreatePages (" + this.NUM_PAGES + " pages) :" + stopwatch.getDisplay());
	}

	private void createPages(final int numPages) throws ProviderException {
		for (int i = 0; i < numPages; i++) {
			final String content = RandomStringUtils.randomAlphabetic(1024);
			createPage("page " + i, content);
		}
	}

	@Test
	public void testEditPages()
			throws Exception {
		final Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		final int NUM_EDITS = 10;
		for (int i = 0; i < this.NUM_PAGES; i++) {
			final String initialContent = RandomStringUtils.randomAlphabetic(4 * 1024);
			final String pageName = "page " + i;
			final WikiPage page = createPage(pageName, initialContent);

			for (int j = 0; j < NUM_EDITS; j++) {
				final String editedContent = RandomStringUtils.randomAlphabetic(4 * 1024);
				updatePage(page, editedContent);
				verifyPage(page, editedContent);
			}
		}

		// check that we have NUM_PAGES file in the wiki dir
		Assert.assertEquals(this.NUM_PAGES, countWikiPageFiles());
		System.out.println("testEditPages (" + this.NUM_PAGES + " pages) :" + stopwatch.getDisplay());
	}

	@Test
	public void testDeletePages()
			throws Exception {
		final Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		final List<WikiPage> pages = new ArrayList(this.NUM_PAGES);
		for (int i = 0; i < this.NUM_PAGES; i++) {
			final String initialContent = RandomStringUtils.randomAlphabetic(4 * 1024);
			final String pageName = "page " + i;
			final WikiPage page = createPage(pageName, initialContent);
			pages.add(page);
		}

		for (final WikiPage p : pages) {
			deletePage(p);
			verifyPageDeleted(p);
		}

		// check that we dont have any files in the wiki dir
		Assert.assertEquals(0, countWikiPageFiles());
		System.out.println("testDeletePages (" + this.NUM_PAGES + " pages) :" + stopwatch.getDisplay());
	}

	private void verifyPageDeleted(final WikiPage page) throws ProviderException {
		final WikiPage info = this.fileProvider.getPageInfo(page.getName(), WikiProvider.LATEST_VERSION);
		assertNull(info);
	}

	private void deletePage(final WikiPage page) throws ProviderException {
		this.fileProvider.deletePage(page);
	}

	private void verifyPage(final WikiPage page, final String expectedContent) throws ProviderException {
		final String actualContent = this.fileProvider.getPageText(page.getName(), WikiProvider.LATEST_VERSION);
		Assert.assertEquals(expectedContent, actualContent);
	}

	private void updatePage(final WikiPage page, final String updatedContent) throws ProviderException {
		this.fileProvider.putPageText(page, updatedContent);
	}

	/**
	 * Liefert die Anzahl an Pages laut PageProvider
	 *
	 * @return
	 */
	private int countWikiPages() throws ProviderException {
		return this.fileProvider.getAllPages().size();
	}

	private int countWikiPageFiles() {
		final File[] pageFiles = this.wikiDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				return file.isFile() && file.getName().endsWith(".txt");
			}
		});
		return pageFiles.length;
	}

	private WikiPage createPage(final String name, final String initialContent) throws ProviderException {
		final WikiPage p = new WikiPage(this.engine, name);
		p.setAuthor(AUTHOR);
		this.fileProvider.putPageText(p, initialContent);
		return p;
	}

	private void runEditTask(final int user) throws Exception {
		final String pageName = "page for " + user;
		final WikiPage page = createPage(pageName, "initial content for " + user);
		for (int i = 0; i < this.numEdits; i++) {
			System.out.println("User " + user + " edit #" + i);
			final String editedContent = RandomStringUtils.randomAlphabetic(10 * 1024);
			updatePage(page, editedContent);
			verifyPage(page, editedContent);
		}
		deletePage(page);
	}

	/**
	 * Multiuser-Zugriff auf den PageProvider, allerdings auf unabhängigen Pages , so dass keine Kollisionen auftretefn
	 */
	@Test
	public void testMultithreadedNoPageCollisions() throws Exception {
		final ExecutorService executorService = Executors.newFixedThreadPool(4); //Runtime.getRuntime().availableProcessors());
		try {
			final List<Callable<Void>> tasks = new ArrayList<>();
			for (int i = 0; i < this.numConcurrentUsers; i++) {
				final int userIndex = i;
				tasks.add(() -> {
					runEditTask(userIndex);
					return null;
				});
			}
			final List<Future<Void>> executedTasks = executorService.invokeAll(tasks);
			for (final Future<Void> f : executedTasks
			) {
				// checks task result, throws Exception if any occured
				f.get();
			}

			// check all pages have been deleted since each user deletes their pages
			assertEquals(0, countWikiPages());
			assertEquals(0, countWikiPageFiles());
		}
		finally {
			executorService.shutdownNow();
		}
	}

	private void runRandomEditTask(final int user, final List<WikiPage> allPages) throws Exception {
		for (int i = 0; i < this.numEdits; i++) {
			final String editedContent = RandomStringUtils.randomAlphabetic(10 * 1024);
			final WikiPage page = allPages.get(RandomUtils.nextInt(0, allPages.size()));
			System.out.println("User " + user + " edit #" + i + " for page " + page.getName());
			updatePage(page, editedContent);
		}
	}

	/**
	 * Multiuser-Zugriff auf den PageProvider, Zugriff auf zufällige Seiten, inkl. möglicher Kollisionen. Deshalb wird
	 * das Ergebnis einer Änderung auch nicht überprüft.
	 */
	@Test
	public void testMultithreadedRandomEdits() throws Exception {
		final ExecutorService executorService = Executors.newFixedThreadPool(16);
		try {
			final List<WikiPage> pages = new ArrayList<>(this.numConcurrentPages);
			for (int i = 0; i < this.numConcurrentPages; i++) {
				pages.add(createPage("Random Page " + i, "initial content"));
			}

			final List<Callable<Void>> tasks = new ArrayList<>();
			for (int i = 0; i < this.numConcurrentUsers; i++) {
				final int userIndex = i;
				tasks.add(() -> {
					runRandomEditTask(userIndex, pages);
					return null;
				});
			}
			final List<Future<Void>> executedTasks = executorService.invokeAll(tasks);
			for (final Future<Void> f : executedTasks
			) {
				// checks task result, throws Exception if any occured in task
				f.get();
			}

			final int totalEdits = pages.stream().mapToInt(p -> {
				try {
					final List<WikiPage> history = this.fileProvider.getVersionHistory(p.getName());
					// Anzahl edits = Anzahl Versionen - 1, da die initiale Version nicht mitzählt
					return history.size() - 1;
				}
				catch (final ProviderException e) {
					return 0;
				}
			}).sum();
			assertEquals(this.numEdits * this.numConcurrentUsers, totalEdits);
		}
		finally {
			executorService.shutdownNow();
		}
	}
}
