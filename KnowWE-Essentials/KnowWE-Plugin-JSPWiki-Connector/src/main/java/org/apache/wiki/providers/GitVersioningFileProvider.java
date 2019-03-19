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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.search.QueryItem;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;

import static org.apache.wiki.providers.GitVersioningUtils.addUserInfo;

/**
 * @author Josua Nürnberger
 * @created 2019-01-02
 */
public class GitVersioningFileProvider extends AbstractFileProvider {

	public static final String JSPWIKI_GIT_VERSIONING_FILE_PROVIDER_REMOTE_GIT = "jspwiki.gitVersioningFileProvider.remoteGit";
	protected Repository repository;
	public static final String GIT_DIR = ".git";
	public static final String JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR = "jspwiki.fileSystemProvider.pageDir";
	private static final Logger log = Logger.getLogger(GitVersioningFileProvider.class);

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
					throw new IOException(e);
				}
			}
			else {
				this.repository = FileRepositoryBuilder.create(gitDir);
				this.repository.create();
			}
		}
		else {
			this.repository = new FileRepositoryBuilder()
					.setGitDir(gitDir)
					.build();
		}
	}

	Repository getRepository() {
		return this.repository;
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

			Status status = git.status().addPath(changedFile.getName()).call();
			boolean isChanged = status.getModified().contains(changedFile.getName());
			if (isChanged || addFile) {
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
				addUserInfo(m_engine, page.getAuthor(), commit);
				commit.call();
			}
		}
		catch (GitAPIException e) {
			log.error(e.getMessage(), e);
			throw new ProviderException("File " + page.getName() + " could not be commited to git");
		}
	}

	@Override
	public boolean pageExists(String page) {
		return super.pageExists(page);
	}

	@Override
	public boolean pageExists(String page, int version) {
		if (pageExists(page)) {
			try {
				List<WikiPage> versionHistory = getVersionHistory(page);
				return version == WikiPageProvider.LATEST_VERSION
						|| (version > 0 && version <= versionHistory.size());
			}
			catch (ProviderException e) {
				return false;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public Collection findPages(QueryItem[] query) {
		return super.findPages(query);
	}

	@Override
	public WikiPage getPageInfo(String pageName, int version) throws ProviderException {
		if (pageExists(pageName)) {
			List<WikiPage> versionHistory = getVersionHistory(pageName);
			if (version == WikiPageProvider.LATEST_VERSION) {
				return versionHistory.get(versionHistory.size() - 1);
			}
			else if (version > 0 && version <= versionHistory.size()) {
				return versionHistory.get(version - 1);
			}
			else {
				throw new ProviderException("Version " + version + " of page " + pageName + " does not exist");
			}
		}
		else {
			return new WikiPage(m_engine, pageName);
		}
	}

	@Override
	public Collection getAllPages() throws ProviderException {
		return super.getAllPages();
	}

	@Override
	public Collection getAllChangedSince(Date date) {
		Iterable<RevCommit> commits = GitVersioningUtils.getRevCommitsSince(date, repository);
		List<WikiPage> pages = new ArrayList<>();

		try {
			ObjectId oldCommit = null;
			ObjectId newCommit;

			for (RevCommit commit : GitVersioningUtils.reverseToList(commits)) {
				String fullMessage = commit.getFullMessage();
				String author = commit.getCommitterIdent().getName();
				Date modified = new Date(1000L * commit.getCommitTime());

				if (oldCommit != null) {
					newCommit = commit.getTree();
					List<DiffEntry> diffs = GitVersioningUtils.getDiffEntries(oldCommit, newCommit, repository);
					for (DiffEntry diff : diffs) {
						String path = diff.getOldPath();
						if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
							path = diff.getNewPath();
						}
						if (path.endsWith(FILE_EXT) && !path.contains("/")) {
							WikiPage page = getWikiPage(fullMessage, author, modified, path);
							pages.add(page);
						}
					}
				}
				else {
					try (TreeWalk treeWalk = new TreeWalk(repository)) {
						treeWalk.reset(commit.getTree());
						treeWalk.setRecursive(true);
						treeWalk.setFilter(TreeFilter.ANY_DIFF);
						while (treeWalk.next()) {
							String path = treeWalk.getPathString();
							if (path.endsWith(FILE_EXT) && !path.contains("/")) {
								WikiPage page = getWikiPage(fullMessage, author, modified, path);
								pages.add(page);
							}
						}
					}
				}
				oldCommit = commit.getTree();
			}
		}
		catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return pages;
	}

	@NotNull
	private WikiPage getWikiPage(String fullMessage, String author, Date modified, String path) {
		WikiPage page = new WikiPage(m_engine, unmangleName(path));
		page.setAttribute(WikiPage.CHANGENOTE, fullMessage);
		page.setAuthor(author);
		page.setLastModified(modified);
		return page;
	}

	@Override
	public int getPageCount() {
		return super.getPageCount();
	}

	@Override
	public List<WikiPage> getVersionHistory(String pageName) throws ProviderException {
		File page = findPage(pageName);
		List<WikiPage> pageVersions = new ArrayList<>();
		if (page.exists()) {
			try {
				Git git = new Git(repository);
				List<RevCommit> revCommits = GitVersioningUtils.reverseToList(getRevCommits(page, git));
				int versionNr = 1;
				for (RevCommit revCommit : revCommits) {
					WikiPage version = new WikiPage(m_engine, pageName);
					version.setAuthor(revCommit.getCommitterIdent().getName());
					version.setLastModified(new Date(1000L * revCommit.getCommitTime()));
					version.setVersion(versionNr++);
					version.setAttribute(WikiPage.CHANGENOTE, revCommit.getFullMessage());
					pageVersions.add(version);
				}
			}
			catch (IOException | GitAPIException e) {
				log.error(e.getMessage(), e);
				throw new ProviderException("Can't get version history for page " + pageName + ": " + e.getMessage());
			}
		}
		return pageVersions;
	}

	@Override
	public String getPageText(String pageName, int version) throws ProviderException {
		File page = findPage(pageName);
		if (page.exists()) {
			try {
				Git git = new Git(repository);
				List<RevCommit> revCommits = GitVersioningUtils.reverseToList(getRevCommits(page, git));
				if (version == LATEST_VERSION || (version > 0 && version <= revCommits.size())) {
					RevCommit revCommit = revCommits.get(version == LATEST_VERSION ? revCommits.size() - 1 : version - 1);
					TreeWalk treeWalkDir = new TreeWalk(repository);
					treeWalkDir.reset(revCommit.getTree());
					treeWalkDir.setFilter(PathFilter.create(page.getName()));
					treeWalkDir.setRecursive(false);
					//here we have our file directly
					if (treeWalkDir.next()) {
						ObjectId fileId = treeWalkDir.getObjectId(0);
						ObjectLoader loader = repository.open(fileId);
						return new String(loader.getBytes(), StandardCharsets.UTF_8);
					}
					else {
						throw new ProviderException("Can't load Git object for " + pageName + " version " + version);
					}
				}
			}
			catch (IOException | GitAPIException e) {
				log.error(e.getMessage(), e);
			}
		}
		else {
			log.info("New file " + pageName);
		}
		return null;
	}

	private Iterable<RevCommit> getRevCommits(File page, Git git) throws GitAPIException, IOException {
		return git
				.log()
				.add(git.getRepository().resolve(Constants.HEAD))
				.addPath(page.getName())
				.call();
	}

	@Override
	public void deleteVersion(String pageName, int version) {
		// Can't delete version from git
	}

	@Override
	public void deletePage(String pageName) throws ProviderException {
		File page = findPage(pageName);
		page.delete();
		try {
			Git git = new Git(repository);
			git.rm().addFilepattern(page.getName()).call();
			git.commit()
					.setOnly(page.getName())
					.setMessage("removed page")
					.call();
		}
		catch (GitAPIException e) {
			log.error(e.getMessage(), e);
			throw new ProviderException("Can't delete page " + pageName + ": " + e.getMessage());
		}
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
		catch (IOException | GitAPIException e) {
			log.error(e.getMessage(), e);
			throw new ProviderException("Can't move page from " + from + " to " + to + ": " + e.getMessage());
		}
	}
}
