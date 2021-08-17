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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.search.QueryItem;
import org.apache.wiki.util.FileUtil;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.jetbrains.annotations.NotNull;

import static org.apache.wiki.providers.GitVersioningUtils.addUserInfo;
import static org.apache.wiki.providers.GitVersioningUtils.reverseToList;

/**
 * @author Josua Nürnberger
 * @created 2019-03-13
 */
@SuppressWarnings("rawtypes")
public class GitVersioningAttachmentProvider extends BasicAttachmentProvider {

	private static final Logger log = Logger.getLogger(GitVersioningAttachmentProvider.class);

	private Repository repository;
	private WikiEngine engine;
	private String storageDir;
	private GitVersioningFileProvider gitVersioningFileProvider;
	private GitVersionCache cache;
	private GitCommentStrategy gitCommentStrategy;
	private IgnoreNode ignoreNode;

	@Override
	public void initialize(WikiEngine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);

		this.engine = engine;
		storageDir = TextUtil.getCanonicalFilePathProperty(properties, PROP_STORAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");

		engine.getWikiProperties().getProperty(PROP_DISABLECACHE);
		WikiPageProvider provider = engine.getPageManager().getProvider();
		if (provider instanceof CachingProvider) {
			provider = ((CachingProvider) provider).getRealProvider();
		}
		if (provider instanceof GitVersioningFileProvider) {
			gitVersioningFileProvider = (GitVersioningFileProvider) provider;
			repository = ((GitVersioningFileProvider) provider).getRepository();
			File baseDir = new File(((GitVersioningFileProvider) provider).getFilesystemPath());
			ignoreNode = new IgnoreNode();
			ignoreNode.parse(new FileInputStream(baseDir+"/.gitignore"));
			cache = gitVersioningFileProvider.getCache();
			gitCommentStrategy = ((GitVersioningFileProvider)provider).getGitCommentStrategy();
		}
		else {
			throw new NoRequiredPropertyException("GitVersioningFileProvider is not configured", "jspwiki.pageProvider");
		}
	}

	private File findPageDir(String page) throws ProviderException {
		File dir = new File(storageDir, getAttachmentDir(page));
		if (dir.exists() && !dir.isDirectory()) {
			throw new ProviderException("Attachment directory for " + page + " is not a directory");
		}
		return dir;
	}

	@NotNull
	public static String getAttachmentDir(String page) {
		return mangleName(page) + DIR_EXTENSION;
	}

	public File findAttachmentDir(Attachment att) throws ProviderException {
		return findPageDir(att.getParentName());
	}

	private File findAttachmentFile(String parentName, String fileName) throws ProviderException {
		return new File(findPageDir(parentName), mangleName(fileName));
	}

	private String getAttachmentDir(Attachment att) {
		return mangleName(att.getParentName()) + DIR_EXTENSION;
	}

	private String getPath(Attachment att) {
		return getAttachmentDir(att.getParentName()) + "/" + mangleName(att.getFileName());
	}

	@Override
	public void putAttachmentData(Attachment att, InputStream data) throws ProviderException, IOException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			gitVersioningFileProvider.commitLock();
			File attDir = findAttachmentDir(att);
			Git git = new Git(repository);
			if (!attDir.exists()) {
				attDir.mkdirs();
				try {
					GitVersioningFileProvider.retryGitOperation(() -> {
						git.add().addFilepattern(attDir.getName()).call();
						return null;
					}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");
					// not needed anymore, because new jgit version also commits directory
//					if (gitVersioningFileProvider.openCommits.containsKey(att.getAuthor())) {
//						gitVersioningFileProvider.openCommits.get(att.getAuthor()).add(attDir.getName());
//					}
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}

			File newFile = findAttachmentFile(att.getParentName(), att.getFileName());
			Boolean ignored = ignoreNode.checkIgnored(newFile.getName(), newFile.isDirectory());
			boolean add = !newFile.exists() && (ignored == null || !ignored);
			try (OutputStream out = new FileOutputStream(newFile)) {
				log.info("Uploading attachment " + att.getFileName() + " to page " + att.getParentName());
				log.info("Saving attachment contents to " + newFile.getAbsolutePath());
				FileUtil.copyContents(data, out);
			}
			catch (IOException e) {
				log.error(e.getMessage(), e);
				throw new ProviderException("Can't write file " + getPath(att) + ": " + e.getMessage());
			}
			finally {
				if (data != null) {
					try {
						data.close();
					}
					catch (IOException e) {
						// Ignore
					}
				}
			}
			try {
				if (add) {
					try {
						GitVersioningFileProvider.retryGitOperation(() -> {
							git.add().addFilepattern(attDir.getName()).call();
							return null;
						}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");

						Status status = GitVersioningFileProvider.retryGitOperation(
								() -> git.status().addPath(attDir.getName()).call()
								, LockFailedException.class, "Retry status of repo, because of lock failed exception");

						if (status.getUntracked().contains(attDir.getName())) {
							GitVersioningFileProvider.retryGitOperation(() -> {
								git.add().addFilepattern(attDir.getName()).call();
								return null;
							}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");
						}
					}
					catch (GitAPIException e) {
						log.error(e.getMessage(), e);
					}
					GitVersioningFileProvider.retryGitOperation(() -> {
						git.add().addFilepattern(getPath(att)).call();
						return null;
					}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");
				}
				att.setSize(newFile.length());
				commitAttachment(att, git, GitVersioningWikiEvent.UPDATE);
			}
			catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	private void setMessage(Attachment att, CommitCommand commitCommand) {
		String changeNote = (String) att.getAttribute(Attachment.CHANGENOTE);
		String comment = gitCommentStrategy.getComment(att);
		if(comment.isEmpty()) {
			comment = gitCommentStrategy.getCommentForUser(att.getAuthor());
		}
		if (comment.isEmpty()) {
			if (changeNote != null && !"".equals(changeNote)) {
				commitCommand.setMessage(changeNote);
			}
			else {
				commitCommand.setMessage("-");
			}
		} else {
			commitCommand.setMessage(comment);
		}
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningAttachmentProvider.class.getSimpleName();
	}

	@Override
	public InputStream getAttachmentData(Attachment att) throws IOException, ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			File attFile = findAttachmentFile(att.getParentName(), att.getFileName());
			if (!attFile.exists()) {
				throw new ProviderException("File " + att.getFileName() + " does not exist");
			}
			int version = att.getVersion();
			Git git = new Git(repository);
			try {
				InputStream ret = null;
				AttachmentCacheItem cacheItem = cache.getAttachment(att);
				Boolean ignored = ignoreNode.checkIgnored(att.getName(), false);
				if(ignored != null && ignored){
					ret = new FileInputStream(attFile);
				} else {
					if (cacheItem != null) {
						if (version == LATEST_VERSION) {
							ret = new FileInputStream(attFile);
						}
						else {
							try (RevWalk revWalk = new RevWalk(repository)) {
								RevCommit revCommit = revWalk.parseCommit(cacheItem.getId());
								ObjectId fileId = getObjectOfCommit(revCommit, att);
								if (fileId != null) {
									ObjectLoader loader = repository.open(fileId);
									ret = loader.openStream();
								}
								else {
									throw new ProviderException("Can't load Git object for " + getPath(att) + " version " + version);
								}
							}
						}
					}
					else {
						List<RevCommit> revCommitList = getRevCommitList(att, git);
						if (revCommitList.isEmpty()) {
							if (version == LATEST_VERSION) {
								ret = new FileInputStream(attFile);
							}
							else {
								throw new ProviderException("Can't load Git object for " + getPath(att) + " version " + version);
							}
						}
						else {
							if (version == LATEST_VERSION || (version > 0 && version <= revCommitList.size())) {
								RevCommit revCommit = revCommitList.get(version == LATEST_VERSION ? revCommitList.size() - 1 : version - 1);
								ObjectId fileId = getObjectOfCommit(revCommit, att);
								if (fileId != null) {
									ObjectLoader loader = repository.open(fileId);
									ret = loader.openStream();
								}
								else {
									throw new ProviderException("Can't load Git object for " + getPath(att) + " version " + version);
								}
							}
						}
					}
				}
				return ret;
			}
			catch (GitAPIException e) {
				log.error(e.getMessage(), e);
				throw new ProviderException("Git call for " + getPath(att) + " throws an error");
			}
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	private ObjectId getObjectOfCommit(RevCommit commit, Attachment att) throws IOException {
		try (TreeWalk treeWalkDir = new TreeWalk(repository)) {
			treeWalkDir.reset(commit.getTree());
			treeWalkDir.setFilter(PathFilter.create(getAttachmentDir(att)));
			treeWalkDir.setRecursive(false);
			//only the attachment directory
			while (treeWalkDir.next()) {
				ObjectId objectId = treeWalkDir.getObjectId(0);
				// so now walk a step down
				try (TreeWalk treeWalkFile = new TreeWalk(repository)) {
					treeWalkFile.reset(objectId);
					treeWalkFile.setFilter(PathFilter.create(mangleName(att.getFileName())));
					treeWalkFile.setRecursive(false);
					if (treeWalkFile.next()) {
						// now we should have our file
						return treeWalkFile.getObjectId(0);
					}
				}
			}
		}
		return null;
	}

	@Override
	public Collection<Attachment> listAttachments(WikiPage page) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			List<Attachment> ret = new ArrayList<>();
			File attachmentDir = findPageDir(page.getName());
			if (attachmentDir.exists()) {

				File[] files = attachmentDir.listFiles(file -> !file.isHidden());
				if (files != null) {
					for (File file : files) {
						Attachment attachmentInfo = getAttachmentInfo(page, unmangleName(file.getName()), LATEST_VERSION);
						if (attachmentInfo != null) {
							ret.add(attachmentInfo);
						}
					}
				}
			}
			return ret;
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Override
	public Collection findAttachments(QueryItem[] query) {
		return super.findAttachments(query);
	}

	@Override
	public List<Attachment> listAllChanged(Date timestamp) throws ProviderException {
		try {
			boolean initCache = timestamp.getTime() == 0;
			gitVersioningFileProvider.canWriteFileLock();
			List<Attachment> attachments = new ArrayList<>();
			if (initCache) {
				return cache.getAllLatestAttachments();
			}
			else {
				Iterable<RevCommit> commits = GitVersioningUtils.getRevCommitsSince(timestamp, repository);
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
								if (path.contains("/") && path.contains(DIR_EXTENSION)) {
									Attachment att = getAttachment(fullMessage, author, modified, path);
									attachments.add(att);
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
									if (path.contains("/") && path.contains(DIR_EXTENSION)) {
										Attachment att = getAttachment(fullMessage, author, modified, path);
										attachments.add(att);
									}
								}
							}
						}

						oldCommit = commit.getTree();
					}
				}
				catch (IOException e) {
					log.error(e.getMessage(), e);
					throw new ProviderException("Can't get differences for a version");
				}
			}
			return attachments;
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	private Attachment getAttachment(String fullMessage, String author, Date modified, String path) {
		String parent = path
				.substring(0, path.indexOf("/"))
				.replace(DIR_EXTENSION, "");
		parent = unmangleName(parent);
		String attachmentName = unmangleName(path.substring(path.indexOf("/")));
		Attachment att = new Attachment(engine, parent, attachmentName);
		att.setAttribute(Attachment.CHANGENOTE, fullMessage);
		att.setAuthor(author);
		att.setLastModified(modified);
		return att;
	}

	@Override
	public Attachment getAttachmentInfo(WikiPage page, String name, int version) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			Attachment att = new Attachment(engine, page.getName(), name);
			att.setVersion(version);
			File attFile = findAttachmentFile(page.getName(), name);
			if (attFile.exists()) {
				Boolean ignored = ignoreNode.checkIgnored(attFile.getName(), false);
				if(ignored != null && ignored) {
					att.setVersion(1);
					return att;
				}
				AttachmentCacheItem cacheItem = cache.getAttachment(att);
				if (cacheItem != null) {
					att.setAuthor(cacheItem.getAuthor());
					att.setLastModified(cacheItem.getDate());
					att.setSize(cacheItem.getSize());
					if (att.getSize() == -1 && version == LATEST_VERSION) {
						att.setSize(attFile.length());
						cacheItem.setSize(att.getSize());
					}
					att.setVersion(cacheItem.getVersion());
					return att;
				}
				else {
					log.warn("Cache miss " + att.getParentName() + "/" + att.getFileName());
					List<Attachment> versionHistory = getVersionHistory(att);
					if (versionHistory.isEmpty() && version == LATEST_VERSION) {
						att.setCacheable(false);
						att.setSize(attFile.length());
						att.setLastModified(new Date(attFile.lastModified()));
						return att;
					}
					else if (version == LATEST_VERSION) {
						return versionHistory.get(versionHistory.size() - 1);
					}
					else if (version > 0 && version <= versionHistory.size()) {
						return versionHistory.get(version - 1);
					}
				}
			}
			return null;
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Override
	public List<Attachment> getVersionHistory(Attachment att) {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			List<Attachment> ret = cache.getAttachmentHistory(att);
			if (ret == null) {
				ret = new ArrayList<>();
				try {
					File attFile = findAttachmentFile(att.getParentName(), att.getFileName());
					if (attFile.exists()) {
						Boolean ignored = ignoreNode.checkIgnored(attFile.getName(), false);
						if(ignored == null || !ignored) {
							Git git = new Git(repository);
							try {
								List<RevCommit> revCommitList = getRevCommitList(att, git);
								int version = 1;
								for (RevCommit revCommit : revCommitList) {
									Attachment attVersion = getAttachment(att, version, revCommit);
									cache.addAttachmentVersion(att, revCommit.getFullMessage(), revCommit.getId());
									ret.add(attVersion);
									version++;
								}
								Collections.reverse(ret);
								return ret;
							}
							catch (GitAPIException | IOException e) {
								log.error(e.getMessage(), e);
							}
						} else {
							att.setVersion(1);
							return Collections.singletonList(att);
						}
					}
				}
				catch (ProviderException e) {
					log.error(e.getMessage(), e);
				}
			}
			Collections.reverse(ret);
			return ret;
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@NotNull
	private Attachment getAttachment(Attachment att, int version, RevCommit revCommit) {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			Attachment attVersion = new Attachment(engine, att.getParentName(), att.getFileName());
			//TODO check pattern
			attVersion.setCacheable(false);
			attVersion.setAuthor(revCommit.getCommitterIdent().getName());
			attVersion.setVersion(version);
			attVersion.setAttribute(WikiPage.CHANGENOTE, revCommit.getFullMessage());
			try {
				attVersion.setSize(getObjectSize(revCommit, att));
			}
			catch (IOException e) {
				attVersion.setSize(-1);
			}
			attVersion.setLastModified(new Date(1000L * revCommit.getCommitTime()));
			return attVersion;
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	private List<RevCommit> getRevCommitList(Attachment att, Git git) throws GitAPIException, IOException {
		Iterable<RevCommit> revCommits = git
				.log()
				.add(repository.resolve(Constants.HEAD))
				.addPath(getPath(att))
				.call();
		return reverseToList(revCommits);
	}

	private long getObjectSize(RevCommit version, Attachment att) throws IOException {
		long ret;
		//TODO look why this is not working every time
		ObjectId objectId = getObjectOfCommit(version, att);
		if (objectId != null) {
			ObjectLoader loader = repository.open(objectId);
			ret = loader.getSize();
		}
		else {
			ret = 0;
		}
		return ret;
	}

	private long getObjectSize(ObjectId commitId, Attachment att) throws IOException {
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevCommit revCommit = revWalk.parseCommit(commitId);
			return getObjectSize(revCommit, att);
		}
	}

	@Override
	public void deleteVersion(Attachment att) {
		// Can't delete version from git
	}

	@Override
	public void deleteAttachment(Attachment att) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			gitVersioningFileProvider.commitLock();
			File attFile = findAttachmentFile(att.getParentName(), att.getFileName());
			if (attFile.exists()) {
				boolean delete = attFile.delete();
				if (!delete) log.debug("File " + getPath(att) + " could not be deleted on filesystem");
				try {
					Git git = new Git(repository);
					GitVersioningFileProvider.retryGitOperation(() -> {
						Boolean ignored = ignoreNode.checkIgnored(getPath(att), false);
						if(ignored == null || !ignored) {
							git.rm().addFilepattern(getPath(att)).call();
						}
						return null;
					}, LockFailedException.class, "Retry removing to repo, because of lock failed exception");

					commitAttachment(att, git, GitVersioningWikiEvent.DELETE);
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new ProviderException("File " + getPath(att) + " could not be deleted");
				}
			}
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	private void commitAttachment(Attachment att, Git git, int type) throws Exception {
		Boolean ignored = ignoreNode.checkIgnored(getPath(att), false);
		if(ignored==null || !ignored) {
			if (gitVersioningFileProvider.openCommits.containsKey(att.getAuthor())) {
				gitVersioningFileProvider.openCommits.get(att.getAuthor()).add(getPath(att));
				if (type == GitVersioningWikiEvent.UPDATE) {
					cache.addCacheCommand(att.getAuthor(), new CacheCommand.AddAttachmentVersion(att));
				}
				else if (type == GitVersioningWikiEvent.DELETE) {
					cache.addCacheCommand(att.getAuthor(), new CacheCommand.DeleteAttachmentVersion(att));
				}
			}
			else {
				CommitCommand commitCommand = git.commit().setAllowEmpty(true).setOnly(getPath(att));
				setMessage(att, commitCommand);
				addUserInfo(engine, att.getAuthor(), commitCommand);
				try {
					gitVersioningFileProvider.commitLock();
					GitVersioningFileProvider.retryGitOperation(() -> {
						RevCommit revCommit = commitCommand.call();
						WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, type,
								att.getAuthor(),
								att.getParentName() + "/" + att.getFileName(),
								revCommit.getId().getName()));
						if (type == GitVersioningWikiEvent.UPDATE) {
							cache.addAttachmentVersion(att, commitCommand.getMessage(), revCommit.getId());
						}
						else if (type == GitVersioningWikiEvent.DELETE) {
							cache.deleteAttachment(att, commitCommand.getMessage(), revCommit.getId());
						}
						return null;
					}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");
					gitVersioningFileProvider.periodicalGitGC(git);
				}
				finally {
					gitVersioningFileProvider.commitUnlock();
				}
			}
		}
	}

	@Override
	public void moveAttachmentsForPage(WikiPage oldParent, String newParent) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			gitVersioningFileProvider.commitLock();
			File oldDir = findPageDir(oldParent.getName());
			File newDir = findPageDir(newParent);
			if (newDir.exists() && !newDir.isDirectory()) {
				throw new ProviderException(newParent + DIR_EXTENSION + " is not a directory");
			}
			if (newDir.exists() && !newDir.canWrite()) {
				throw new ProviderException("Can't write to directory " + newParent + DIR_EXTENSION);
			}
			if (!newDir.exists()) {
				newDir.mkdirs();
			}
			File[] files = oldDir.listFiles();
			if (files != null) {
				try {
					List<String> filesForEvent = new ArrayList<>();
					if (oldDir.getName().equalsIgnoreCase(newDir.getName())) {
						File tmpDir = findPageDir(newParent + "_tmp");
						Files.move(oldDir.toPath(), tmpDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
						Files.move(tmpDir.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
					else {
						Files.move(oldDir.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
					Git git = new Git(repository);
					RmCommand rm = git.rm().setCached(true);
					AddCommand add = git.add();
					CommitCommand commit = git.commit();
					for (File file : files) {
						String oldPath = getAttachmentDir(oldParent.getName()) + "/" + file.getName();
						String newPath = getAttachmentDir(newParent) + "/" + file.getName();
						rm.addFilepattern(oldPath);
						add.addFilepattern(newPath);
						if (gitVersioningFileProvider.openCommits.containsKey(oldParent.getAuthor())) {
							gitVersioningFileProvider.openCommits.get(oldParent.getAuthor()).add(oldPath);
							gitVersioningFileProvider.openCommits.get(oldParent.getAuthor()).add(newPath);
							cache.addCacheCommand(oldParent.getAuthor(), new CacheCommand.MoveAttachment(oldParent, newParent, file));
						}
						else {
							commit.setOnly(oldPath);
							commit.setOnly(newPath);
							filesForEvent.add(oldParent.getName() + "/" + file.getName());
							filesForEvent.add(newParent + "/" + file.getName());
						}
					}
					rm.addFilepattern(getAttachmentDir(oldParent.getName()));
					add.addFilepattern(getAttachmentDir(newParent));

					GitVersioningFileProvider.retryGitOperation(() -> {
						rm.call();
						return null;
					}, LockFailedException.class, "Retry removing to repo, because of lock failed exception");

					GitVersioningFileProvider.retryGitOperation(() -> {
						add.call();
						return null;
					}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");

					if (!gitVersioningFileProvider.openCommits.containsKey(oldParent.getAuthor())) {
						String comment = gitCommentStrategy.getComment(oldParent);
						if(comment.isEmpty()) {
							commit.setMessage("move attachments form " + oldParent.getName() + " to " + newParent);
						} else {
							commit.setMessage(comment);
						}
						try {
							gitVersioningFileProvider.commitLock();
							GitVersioningFileProvider.retryGitOperation(() -> {
								RevCommit revCommit = commit.call();
								WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.MOVED,
										oldParent.getAuthor(),
										filesForEvent,
										revCommit.getId().getName()));
								cache.moveAttachments(oldParent.getName(), newParent, files, revCommit.getId(), commit.getMessage(), oldParent
										.getAuthor());
								return null;
							}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");

							gitVersioningFileProvider.periodicalGitGC(git);
						}
						finally {
							gitVersioningFileProvider.commitUnlock();
						}
					}
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
					throw new ProviderException("Can't move attachments form " + oldParent.getName() + " to " + newParent);
				}
			}
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
			gitVersioningFileProvider.writeFileUnlock();
		}
	}
}
