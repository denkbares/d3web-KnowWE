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
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.util.TextUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Josua Nürnberger
 * @created 2019-03-28
 */

public class GitVersioningBenchmarkTest {

	private static final String NAME1 = "Test1";
	private static final String AUTHOR = "author";
	private String TMP_NEW_REPO = "/tmp/newRepo";
	private Engine engine;
	private GitVersioningFileProvider fileProvider;

	@Before
	public void setUp() throws Exception {
		System.setProperty("logback.configurationFile", "/resources/logback.xml");


//		TestEngine
		engine = Mockito.mock(WikiEngine.class);
		TMP_NEW_REPO = System.getProperty("java.io.tmpdir") + "/newRepo";
		System.out.println(TMP_NEW_REPO);
		Properties properties = new Properties();
		properties.put(AbstractFileProvider.PROP_PAGEDIR, TMP_NEW_REPO);
		properties.put(GitVersioningAttachmentProvider.PROP_STORAGEDIR, TMP_NEW_REPO);
		properties.setProperty(GitProviderProperties.JSPWIKI_GIT_DEFAULT_BRANCH,"maintenance");
//		properties.setProperty(PROPERTIES_KEY_GIT_VERSION_CACHE,"sync");
		engine = Mockito.mock(WikiEngine.class);
		when(engine.getWikiProperties()).thenReturn(properties);

		PageManager pageManager = Mockito.mock(PageManager.class);
		fileProvider = new GitVersioningFileProvider();
		fileProvider.initialize(engine, properties);
		when(engine.getManager(PageManager.class)).thenReturn(pageManager);
		when(pageManager.getProvider()).thenReturn(fileProvider);
		UserManager um = Mockito.mock(UserManager.class);
		when(engine.getManager(UserManager.class)).thenReturn(um);
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
	public void testMillionChanges()
			throws Exception {
		String text = "";
		int maxver = 2000; // Save 2000 versions.
		StopWatch watch = new StopWatch();

		watch.start();
		for (int i = 0; i < maxver; i++) {
			text = text + ".";
			saveText(NAME1, text);
		}

		watch.stop();

		System.out.println("Benchmark: " + getTime(watch, 2000) + " pages/second");

		watch.reset();
		watch.start();
		Page pageinfo = getPage(NAME1);
		watch.stop();
		assertEquals("wrong version", maxver, pageinfo.getVersion());
		// +2 comes from \r\n.
		assertEquals("wrong text", maxver, getText(NAME1).length());
		System.out.println("Benchmark read versions: " + watch);

		watch.reset();
		watch.start();
		Collection<Page> pages = getAllPages();
		watch.stop();
		assertEquals("only one element", 1, pages.size());
		//this test can no longer suceed as you will always get -1 when you call getAllPages()
//		assertEquals("wrong version", maxver, pages.toArray(new Page[1])[0].getVersion());
		// +2 comes from \r\n.
		System.out.println("Benchmark read all files many versions: " + watch);
	}

	private String getText(String name1) throws ProviderException {
		return TextUtil.replaceEntities(fileProvider.getPageText(name1, PageProvider.LATEST_VERSION));
	}

	private Page getPage(String name1) throws ProviderException {
		return fileProvider.getPageInfo(name1, GitVersioningFileProvider.LATEST_VERSION);
	}

	private void saveText(String name, String text) throws ProviderException {
		WikiPage p = new WikiPage(engine, name);
		p.setAuthor(AUTHOR);
		this.fileProvider.putPageText(p, text);
	}

	private String getTime(StopWatch stopWatch, int operations) {
		double time = stopWatch.getTime();
		double result = (operations / time) * 1000.0;
		return String.format("%.4f", result);
	}

	private void runMassiveFileTest(int maxpages)
			throws Exception {
		String text = "Testing, 1, 2, 3: ";
		StopWatch mark = new StopWatch();

		System.out.println("Building a massive repository of " + maxpages + " pages...");
		StopWatch zwischenZeit = new StopWatch();
		boolean zz = false;
		mark.start();
		if (maxpages > 100) {
			zwischenZeit.start();
			zz = true;
		}
		for (int i = 0; i < maxpages; i++) {
			if (zz && i % 100 == 0) {
				zwischenZeit.stop();
				System.out.println("time to save 100 pages was " + zwischenZeit.toString() + " (" + getTime(zwischenZeit, 100) + ") " + i + "/" + maxpages);
				zwischenZeit.reset();
				zwischenZeit.start();
			}
			saveText(NAME1 + i, text + i);
		}
		mark.stop();

		System.out.println("Total time to save " + maxpages + " pages was " + mark.toString());
		System.out.println("Saved " + getTime(mark, maxpages) + " pages/second");

		mark.reset();

		mark.start();
		Collection<Page> pages = getAllPages();
		mark.stop();

		System.out.println("Got a list of all pages in " + mark);

		mark.reset();
		mark.start();

		for (Page page : pages) {
			String foo = getPureText(page);
			assertNotNull(foo);
		}
		mark.stop();

		System.out.println("Read through all of the pages in " + mark);
		System.out.println("which is " + getTime(mark, maxpages) + " pages/second");
	}

	private Collection<Page> getAllPages() throws ProviderException {
		return fileProvider.getAllPages();
	}

	private String getPureText(Page page) throws ProviderException {
		return fileProvider.getPageText(page.getName(), PageProvider.LATEST_VERSION);
	}

	@Test
	public void testMillionFiles1() throws Exception {
		runMassiveFileTest(100);
	}

	@Test
	public void testMillionFiles2() throws Exception {
		runMassiveFileTest(1000);
	}

	@Test
	public void testMillionFiles3() throws Exception {
		runMassiveFileTest(5000);
	}
}
