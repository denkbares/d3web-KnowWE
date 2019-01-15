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
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;

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

	@AfterClass
	public static void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(TMP_NEW_REPO));
	}
}
