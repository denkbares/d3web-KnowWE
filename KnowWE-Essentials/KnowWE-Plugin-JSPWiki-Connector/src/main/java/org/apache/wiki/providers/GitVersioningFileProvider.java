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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.wiki.util.FileUtil;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.io.DisabledOutputStream;

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
	private static final Logger logger = Logger.getLogger(GitVersioningFileProvider.class);

	/**
	 * {@inheritDoc}
	 *
	 * @throws IOException
	 */
	@Override
	public void initialize(WikiEngine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);
//TODO property exceptions
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
		catch (GitAPIException e) {
			e.printStackTrace();
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
			return null;
		}
	}

	@Override
	public Collection getAllPages() throws ProviderException {
		return super.getAllPages();
	}

	@Override
	public Collection getAllChangedSince(Date date) {
		RevFilter filter = new RevFilter() {
			@Override
			public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException {
				return (1000l * cmit.getCommitTime()) >= date.getTime();
			}

			@Override
			public RevFilter clone() {
				return null;
			}
		};
		Git git = new Git(repository);
		try {
			Iterable<RevCommit> commits = git
					.log()
					.add(git.getRepository().resolve(Constants.HEAD))
//					.setRevFilter(filter)
					.call();
			ObjectReader objRedaer = repository.newObjectReader();
			CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
			ObjectId oldCommit = null;
			CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
			ObjectId newCommit = null;
			List<WikiPage> pages = new ArrayList<>();
			for (RevCommit commit : GitVersioningUtils.reverseToList(commits)) {
				String fullMessage = commit.getFullMessage();
				String author = commit.getCommitterIdent().getName();
				Date modified = new Date(1000l * commit.getCommitTime());

				System.out.println(commit.getParentCount());
				System.out.println(fullMessage);
				if (oldCommit == null) {
					ObjectId resolve = repository.resolve(commit.getName() + "^");
					oldCommit = resolve;
					System.out.println(oldCommit);
					System.out.println(commit.getTree().getName());
//					oldCommit =commit.getTree();
				}
				if (oldCommit != null) {
					newCommit = commit.getTree();
					oldTreeParser.reset(objRedaer, oldCommit);
					newTreeParser.reset(objRedaer, newCommit);
					DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
					diffFormatter.setRepository(repository);
					List<DiffEntry> diffs = diffFormatter.scan(oldTreeParser, newTreeParser);
					for (DiffEntry diff : diffs) {
						diff.getOldPath();
						System.out.println("commit rev: " + diff);
					}
				}

				try (TreeWalk treeWalk = new TreeWalk(repository)) {
					treeWalk.reset(commit.getTree());
					treeWalk.setRecursive(true);
					treeWalk.setFilter(TreeFilter.ANY_DIFF);
					while (treeWalk.next()) {
						String nameString = treeWalk.getPathString();
						System.out.println(nameString);
						WikiPage page = new WikiPage(m_engine, unmangleName(nameString));
						page.setAttribute(WikiPage.CHANGENOTE, fullMessage);
						page.setAuthor(author);
						page.setLastModified(modified);
						pages.add(page);
					}
				}

				oldCommit = commit.getTree();
			}
			return pages;
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getPageCount() {
		return super.getPageCount();
	}

	@Override
	public List<WikiPage> getVersionHistory(String pageName) throws ProviderException {
		File page = findPage(pageName);
		if (page.exists()) {
			try {
				List<WikiPage> pageVerions = new ArrayList<>();
				Git git = new Git(repository);
				List<RevCommit> revCommits = GitVersioningUtils.reverseToList(getRevCommits(page, git));
				int versionNr = 1;
				for (RevCommit revCommit : revCommits) {
					WikiPage version = new WikiPage(m_engine, pageName);
					version.setAuthor(revCommit.getCommitterIdent().getName());
					version.setLastModified(new Date(1000l * revCommit.getCommitTime()));
					version.setVersion(versionNr++);
					version.setAttribute(WikiPage.CHANGENOTE, revCommit.getFullMessage());
					pageVerions.add(version);
				}
				return pageVerions;
			}
			catch (IncorrectObjectTypeException e) {
				e.printStackTrace();
			}
			catch (AmbiguousObjectException e) {
				e.printStackTrace();
			}
			catch (MissingObjectException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (NoHeadException e) {
				e.printStackTrace();
			}
			catch (GitAPIException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getPageText(String pageName, int version) throws ProviderException {
		File page = findPage(pageName);
		if (page.exists()) {
			try {
				Git git = new Git(repository);
				List<RevCommit> revCommits = GitVersioningUtils.reverseToList(getRevCommits(page, git));
				int versionNr = 1;
				for (RevCommit revCommit : revCommits) {
					if (version == versionNr) {
						git.checkout()
								.addPath(page.getName())
								.setName(revCommit.getName())
								.call();
					}
					version++;
				}
				try (InputStream in = new FileInputStream(page)) {
					String pageContent = FileUtil.readContents(in, m_encoding);
					return pageContent;
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			catch (IncorrectObjectTypeException e) {
				e.printStackTrace();
			}
			catch (AmbiguousObjectException e) {
				e.printStackTrace();
			}
			catch (MissingObjectException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (NoHeadException e) {
				e.printStackTrace();
			}
			catch (GitAPIException e) {
				e.printStackTrace();
			}
		}
		else {
			logger.info("New file " + pageName);
		}
		return null;
	}

	protected Iterable<RevCommit> getRevCommits(File page, Git git) throws GitAPIException, IOException {
		return git
				.log()
				.add(git.getRepository().resolve(Constants.HEAD))
				.addPath(page.getName())
				.call();
	}

	@Override
	public void deleteVersion(String pageName, int version) throws ProviderException {
		// Why delete version from git
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
		catch (NoFilepatternException e) {
			e.printStackTrace();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
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
