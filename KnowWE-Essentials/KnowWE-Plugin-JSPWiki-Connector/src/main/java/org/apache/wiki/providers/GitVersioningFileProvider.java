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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.search.QueryItem;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;

/**
 * @author Josua Nürnberger
 * @created 2019-01-02
 */
public class GitVersioningFileProvider extends AbstractFileProvider {

	public static final String JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT = "jspwiki.gitVersioningFileProvider.remoteGit";
	protected Repository repository;
	public static final String GIT_DIR = ".git";
	public static final String JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR = "jspwiki.fileSystemProvider.pageDir";

	/**
	 * {@inheritDoc}
	 *
	 * @throws IOException
	 */
	@Override
	public void initialize(WikiEngine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);

		String filesystemPath = TextUtil.getCanonicalFilePathProperty(properties, JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");
		File pageDir = new File(filesystemPath);
		File gitDir = new File(pageDir.getAbsolutePath() + File.separator + GIT_DIR);

		if (!RepositoryCache.FileKey.isGitRepository(gitDir, FS.DETECTED)) {
			String remoteURL = TextUtil.getStringProperty(properties, JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT, "");
			if (!"".equals(remoteURL)) {
				try {
					Git git = Git.cloneRepository()
							.setURI(remoteURL)
							.setDirectory(pageDir)
							.setGitDir(gitDir)
							.setBare(false)
							.call();
					this.repository = git.getRepository();
				}
				catch (GitAPIException e) {
					e.printStackTrace();
				}
			}
			else {
				this.repository = FileRepositoryBuilder.create(gitDir);
			}
		}
		else {
			this.repository = new FileRepositoryBuilder()
					.setGitDir(gitDir)
					.build();
		}
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningFileProvider.class.getSimpleName();
	}

	@Override
	public void putPageText(WikiPage page, String text) throws ProviderException {
		File changedFile = findPage(page.getName());
		boolean addFile = !changedFile.exists();

		super.putPageText(page, text);

		Git git = new Git(repository);
		try {
			if (addFile) {
				git.add().addFilepattern(changedFile.getName()).call();
			}
			CommitCommand commit = git
					.commit()
					.setOnly(changedFile.getName());
			if (page.getAttributes().containsKey(WikiPage.CHANGENOTE)) {
				commit.setMessage((String) page.getAttribute(WikiPage.CHANGENOTE));
			}
			else if (addFile) {
				commit.setMessage("Added page");
			}
			else {
				commit.setMessage("-");
			}
			addUserInfo(page, commit);
			commit.call();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	private void addUserInfo(WikiPage page, CommitCommand commit) {
		if (null != page.getAuthor() && !"".equals(page.getAuthor())) {
			try {
				UserProfile userProfile = m_engine.getUserManager()
						.getUserDatabase()
						.findByFullName(page.getAuthor());
				commit.setCommitter(userProfile.getFullname(), userProfile.getEmail());
			}
			catch (NoSuchPrincipalException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean pageExists(String page) {
		return super.pageExists(page);
	}

	@Override
	public boolean pageExists(String page, int version) {
		return super.pageExists(page, version);
	}

	@Override
	public Collection findPages(QueryItem[] query) {
		return super.findPages(query);
	}

	@Override
	public WikiPage getPageInfo(String page, int version) throws ProviderException {
		return super.getPageInfo(page, version);
	}

	@Override
	public Collection getAllPages() throws ProviderException {
		return super.getAllPages();
	}

	@Override
	public Collection getAllChangedSince(Date date) {
		return super.getAllChangedSince(date);
	}

	@Override
	public int getPageCount() {
		return super.getPageCount();
	}

	@Override
	public List getVersionHistory(String page) throws ProviderException {
		return super.getVersionHistory(page);
	}

	@Override
	public String getPageText(String page, int version) throws ProviderException {
		return super.getPageText(page, version);
	}

	@Override
	public void deleteVersion(String pageName, int version) throws ProviderException {
		super.deleteVersion(pageName, version);
	}

	@Override
	public void deletePage(String pageName) throws ProviderException {
		super.deletePage(pageName);
	}

	@Override
	public void movePage(String from, String to) throws ProviderException {
		File fromFile = findPage(from);
		File toFile = findPage(to);
		try {
			Files.move(fromFile.toPath(), toFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
			Git git = new Git(repository);
			git.add().addFilepattern(toFile.getName()).call();
			git.rm().addFilepattern(fromFile.getName()).call();
			git.commit()
					.setOnly(toFile.getName())
					.setOnly(fromFile.getName())
					.setMessage("renamed page " + from + " to " + to)
					.call();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new ProviderException("");
		}
		catch (NoFilepatternException e) {
			e.printStackTrace();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
}
