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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.api.search.QueryItem;
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.gitBridge.JspGitBridge;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.commentStrategy.GitCommentStrategy;
import org.apache.wiki.structs.VersionCarryingRevCommit;
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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Stopwatch;

import static org.apache.wiki.gitBridge.JSPUtils.getAttachmentDir;
import static org.apache.wiki.gitBridge.JSPUtils.getPath;

/**
 * @author Josua Nürnberger
 * @created 2019-03-13
 */
@SuppressWarnings("rawtypes")
public class GitVersioningAttachmentProvider extends BasicAttachmentProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitVersioningAttachmentProvider.class);

	private Repository repository;
	private Engine engine;
	private String storageDir;
	private GitVersioningFileProvider gitVersioningFileProvider;
	//	private GitVersionCache cache;
	private GitCommentStrategy gitCommentStrategy;
	private IgnoreNode ignoreNode;
	private JspGitBridge gitBridge;

	@Override
	public void initialize(Engine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);

		this.engine = engine;

		storageDir = TextUtil.getCanonicalFilePathProperty(properties, PROP_STORAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");

		engine.getWikiProperties().getProperty(PROP_DISABLECACHE);
		PageProvider provider = engine.getManager(PageManager.class).getProvider();
		if (provider instanceof CachingProvider) {
			provider = ((CachingProvider) provider).getRealProvider();
		}
		if (provider instanceof GitVersioningFileProvider) {
			gitVersioningFileProvider = (GitVersioningFileProvider) provider;
			repository = ((GitVersioningFileProvider) provider).getRepository();
			File baseDir = new File(((GitVersioningFileProvider) provider).getFilesystemPath());
			ignoreNode = new IgnoreNode();
			File gitignoreFile = new File(baseDir + "/.gitignore");
			if (gitignoreFile.exists()) {
				ignoreNode.parse(new FileInputStream(gitignoreFile));
			}
			gitCommentStrategy = ((GitVersioningFileProvider) provider).getGitCommentStrategy();
			this.gitBridge = gitVersioningFileProvider.gitBridge();
		}
		else {
			throw new NoRequiredPropertyException("GitVersioningFileProvider is not configured", "jspwiki.pageProvider");
		}
	}

	@Override
	public void putAttachmentData(Attachment att, InputStream data) throws ProviderException, IOException {
		Stopwatch stopwatch = new Stopwatch();
		try {
			stopwatch.start();
			gitVersioningFileProvider.canWriteFileLock();
			gitVersioningFileProvider.commitLock();
			putAttachmentDataSafe(att, data);
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
			gitVersioningFileProvider.writeFileUnlock();
			stopwatch.show("Time to put attachment data: " + att.getFileName());
		}
	}

	public File findAttachmentDir(Attachment att) throws ProviderException {
		return JSPUtils.findPageDir(att.getParentName(), storageDir);
	}

	private void putAttachmentDataSafe(Attachment att, InputStream data) throws ProviderException, IOException {
		File attDir = findAttachmentDir(att);
		Git git = new Git(repository);
		if (!attDir.exists()) {
			//create dir
			attDir.mkdirs();
			//add dir
			this.gitVersioningFileProvider.gitBridge().addWithRetry(git, attDir);
		}

		File newFile = JSPUtils.findAttachmentFile(att.getParentName(), att.getFileName(), storageDir);
		Boolean ignored = ignoreNode.checkIgnored(getPath(att), newFile.isDirectory());

		boolean add = !newFile.exists() && (ignored == null || !ignored);

		//check if there are no changes at all on the bytestream
		boolean noChanges = false;
		if (newFile.exists()) {
			byte[] bytesExisting = Files.readAllBytes(newFile.toPath());
			byte[] bytesNew = data.readAllBytes();

			//if these are equal we do not update
			if (Arrays.equals(bytesExisting, bytesNew)) {
				noChanges = true;
			}

			//reset
			data = new ByteArrayInputStream(bytesNew);
		}

		if (noChanges) {
			return;
		}

		copyOnFilesystem(att, data, newFile);

		try {
			if (add) {
				try {
					JspGitBridge.retryGitOperation(() -> {
						git.add().addFilepattern(attDir.getName()).call();
						return null;
					}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");

					Status status = JspGitBridge.retryGitOperation(
							() -> git.status().addPath(attDir.getName()).call()
							, LockFailedException.class, "Retry status of repo, because of lock failed exception");

					if (status.getUntracked().contains(attDir.getName())) {
						JspGitBridge.retryGitOperation(() -> {
							git.add().addFilepattern(attDir.getName()).call();
							return null;
						}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");
					}
				}
				catch (GitAPIException e) {
					LOGGER.error(e.getMessage(), e);
				}
				JspGitBridge.retryGitOperation(() -> {
					git.add().addFilepattern(getPath(att)).call();
					return null;
				}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");
			}
			att.setSize(newFile.length());
			commitAttachment(att, git, GitVersioningWikiEvent.UPDATE, newFile.getAbsolutePath()
					.replace(this.gitVersioningFileProvider.getFilesystemPath(), "")
					.substring(1));
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void copyOnFilesystem(Attachment att, InputStream data, File newFile) throws ProviderException {
		try (OutputStream out = new FileOutputStream(newFile)) {
			LOGGER.info("Uploading attachment " + att.getFileName() + " to page " + att.getParentName());
			LOGGER.info("Saving attachment contents to " + newFile.getAbsolutePath());
			FileUtil.copyContents(data, out);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
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
	}

	private void setMessage(Attachment att, CommitCommand commitCommand) {
		String changeNote = att.getAttribute(Attachment.CHANGENOTE);
		String comment = gitCommentStrategy.getComment(att, gitCommentStrategy.getCommentForUser(att.getAuthor()));
		if (comment.isEmpty()) {
			if (changeNote != null && !"".equals(changeNote)) {
				commitCommand.setMessage(changeNote);
			}
			else {
				commitCommand.setMessage("-");
			}
		}
		else {
			commitCommand.setMessage(comment);
		}
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningAttachmentProvider.class.getSimpleName();
	}

	@Override
	public InputStream getAttachmentData(Attachment att) throws IOException, ProviderException {
		Stopwatch stopwatch = new Stopwatch();

		try {
			stopwatch.start();
			gitVersioningFileProvider.canWriteFileLock();
			return getAttachmentDataSafe(att);
		}
		finally {
			stopwatch.show("Time to get data for attachment: " + att.getFileName() + " and version: " + att.getVersion());
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Nullable
	private InputStream getAttachmentDataSafe(Attachment att) throws ProviderException, IOException {
		File attFile = JSPUtils.findAttachmentFile(att.getParentName(), att.getFileName(), storageDir);
		if (!attFile.exists()) {
			throw new ProviderException("File " + att.getFileName() + " does not exist");
		}
		int version = att.getVersion();
		if (version == -1) {
			return new FileInputStream(attFile);
		}

		Git git = new Git(repository);
		try {
			InputStream ret = null;
			Boolean ignored = ignoreNode.checkIgnored(getPath(att), false);
			if (ignored != null && ignored) {
				ret = new FileInputStream(attFile);
			}
			else {
				ret = getAttachmentDataFromGit(att, git, version, ret);
			}
			return ret;
		}
		catch (GitAPIException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ProviderException("Git call for " + getPath(att) + " throws an error");
		}
	}

	private InputStream getAttachmentDataFromGit(Attachment att, Git git, int version, InputStream ret) throws GitAPIException, IOException, ProviderException {
		Stopwatch watch = new Stopwatch();
		LOGGER.info("Starting retrieve attachment file from git: " + watch.getDisplay());
//		List<RevCommit> revCommitList = this.gitBridge.getRevCommitList(att, git);
		VersionCarryingRevCommit revCommitWithVersion = this.gitBridge.getRevCommitWithVersion(att, git, version);
//		if (revCommitList.isEmpty()) {
//			throw new ProviderException("Can't load Git object for " + getPath(att) + " version " + version);
//		}
		if (revCommitWithVersion == null) {
			throw new ProviderException("Can't load Git object for " + getPath(att) + " version " + version);
		}
		else {
//			if (version > 0 && version <= revCommitList.size()) {
//				RevCommit revCommit = revCommitList.get(version - 1);
//				ObjectId fileId = getObjectOfCommit(revCommit, att);
			ObjectId fileId = getObjectOfCommit(revCommitWithVersion.revCommit(), att);
			if (fileId != null) {
				ObjectLoader loader = repository.open(fileId);
				ret = loader.openStream();
				LOGGER.info("Completed retrieve attachment file from git: " + watch.getDisplay());
			}
			else {
				throw new ProviderException("Can't load Git object for " + getPath(att) + " version " + version);
			}
//			}
		}
		return ret;
	}


	private ObjectId getObjectOfCommit(RevCommit commit, Attachment att) throws IOException {
		try (TreeWalk treeWalkDir = new TreeWalk(repository)) {
			treeWalkDir.reset(commit.getTree());

			String attachmentDir = getAttachmentDir(att);
			if (!Paths.get(this.gitVersioningFileProvider.getFilesystemPath(), attachmentDir).toFile().exists()) {
				attachmentDir = JSPUtils.unmangleName(attachmentDir);
			}
			treeWalkDir.setFilter(PathFilter.create(attachmentDir));
			treeWalkDir.setRecursive(false);
			//only the attachment directory
			while (treeWalkDir.next()) {
				ObjectId objectId = treeWalkDir.getObjectId(0);
				// so now walk a step down
				try (TreeWalk treeWalkFile = new TreeWalk(repository)) {
					treeWalkFile.reset(objectId);
					treeWalkFile.setFilter(PathFilter.create(JSPUtils.mangleName(att.getFileName())));
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
	public List<Attachment> listAttachments(Page page) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();

			return listAttachmentsSafe(page);
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@NotNull
	private List<Attachment> listAttachmentsSafe(Page page) throws ProviderException {
		List<Attachment> ret = new ArrayList<>();
		File attachmentDir = JSPUtils.findPageDir(page.getName(), storageDir);
		if (attachmentDir.exists()) {

			File[] files = attachmentDir.listFiles(file -> !file.isHidden());
			if (files != null) {
				for (File file : files) {
					Attachment attachmentInfo = getAttachmentInfo(page, JSPUtils.unmangleName(file.getName()), LATEST_VERSION);
					if (attachmentInfo != null) {
						ret.add(attachmentInfo);
					}
				}
			}
		}
		return ret;
	}

	@Override
	public Collection<Attachment> findAttachments(QueryItem[] query) {
		return super.findAttachments(query);
	}

	@Override
	public List<Attachment> listAllChanged(Date timestamp) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			return listAllChangedSafe(timestamp);
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	private List<Attachment> listAllChangedSafe(Date timestamp) throws ProviderException {
		boolean initCache = timestamp.getTime() == 0;

		List<Attachment> attachments = new ArrayList<>();
		if (initCache) {

			//loop over pages and get all attachments form filesystem for this initial pass!
			for (Page page : this.gitVersioningFileProvider.getAllPages()) {
				File attachmentDir = JSPUtils.findPageDir(page.getName(), storageDir);
				if (attachmentDir.exists()) {

					File[] files = attachmentDir.listFiles(file -> !file.isHidden());
					if (files != null) {
						for (File file : files) {
							Attachment att = new org.apache.wiki.attachment.Attachment(engine, page.getName(), file.getName());
							att.setVersion(-1);
							att.setSize(file.length());
							att.setLastModified(new Date(file.lastModified()));
							attachments.add(att);
						}
					}
				}
			}
			return attachments;
		}
		else {
			LOGGER.info("Access commits for the repository");
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
				LOGGER.error(e.getMessage(), e);
				throw new ProviderException("Can't get differences for a version");
			}
		}
		return attachments;
	}

	private Attachment getAttachment(String fullMessage, String author, Date modified, String path) {
		String parent = path
				.substring(0, path.indexOf("/"))
				.replace(DIR_EXTENSION, "");
		parent = JSPUtils.unmangleName(parent);
		String attachmentName = JSPUtils.unmangleName(path.substring(path.indexOf("/")));
		Attachment att = new org.apache.wiki.attachment.Attachment(engine, parent, attachmentName);
		att.setAttribute(Attachment.CHANGENOTE, fullMessage);
		att.setAuthor(author);
		att.setLastModified(modified);
		return att;
	}

	@Override
	public Attachment getAttachmentInfo(Page page, String name, int version) throws ProviderException {
		Stopwatch stopwatch = new Stopwatch();
		try {
			stopwatch.start();
			gitVersioningFileProvider.canWriteFileLock();
			return getAttachmentInfoSafe(page, name, version);
		}
		finally {
			stopwatch.show("Time to get attachment info: " + page.getName() + " and version: " + version);
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Nullable
	private Attachment getAttachmentInfoSafe(Page page, String name, int version) throws ProviderException {
		Attachment att = new org.apache.wiki.attachment.Attachment(engine, page.getName(), name);
		att.setVersion(version);
		File attFile = JSPUtils.findAttachmentFile(page.getName(), name, storageDir);
		if (attFile.exists()) {
			Boolean ignored = ignoreNode.checkIgnored(getPath(att), false);
			if (ignored != null && ignored) {
				att.setVersion(1);
				att.setSize(attFile.length());
				att.setLastModified(new Date(attFile.lastModified()));
				return att;
			}

			//otherwise we create it
			try {
				//TODO set correct version!
				LOGGER.info("Obtain revcommits for: " + att.getFileName() + " with version: " + version);
				VersionCarryingRevCommit accordingCommit = this.gitBridge.getRevCommitWithVersion(att, new Git(this.repository), version);
				if (accordingCommit == null) {
					LOGGER.error("Expected to find a commit, as there is a file: " + attFile.getName());
					return null;
				}
				return attachmentFromCommit(att, accordingCommit.revCommit(), accordingCommit.version());
			}
			catch (GitAPIException e) {
				throw new RuntimeException(e);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private Attachment attachmentFromCommit(Attachment att, RevCommit revCommit, int version) {
		PersonIdent authorIdent = revCommit.getAuthorIdent();
//		att.setVersion(LATEST_VERSION);
		att.setVersion(version);
		att.setAuthor(authorIdent.getName());
		try {
			att.setSize(getObjectSize(revCommit, att));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		att.setLastModified(authorIdent.getWhen());
		att.setAttribute(WikiPage.CHANGENOTE, revCommit.getFullMessage());
		return att;
	}

	@Override
	public List<Attachment> getVersionHistory(Attachment att) {
		Stopwatch stopwatch = new Stopwatch();
		try {
			stopwatch.start();
			gitVersioningFileProvider.canWriteFileLock();
			return getVersionHistorySafe(att);
		}
		finally {
			stopwatch.show("Time to get history for attachment: " + att.getFileName());
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@NotNull
	private List<Attachment> getVersionHistorySafe(Attachment att) {
		List<Attachment> ret = null;

		ret = getVersionHistoryFromGit(att);
		if (ret != null) return ret;

		return Collections.emptyList();
	}

	@NotNull
	private Attachment getAttachment(Attachment att, int version, RevCommit revCommit) {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			Attachment attVersion = new org.apache.wiki.attachment.Attachment(engine, att.getParentName(), att.getFileName());
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

	@Override
	public void deleteVersion(Attachment att) {
		// Can't delete version from git
	}

	@Override
	public void deleteAttachment(Attachment att) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			gitVersioningFileProvider.commitLock();
			deleteAttachmentSafe(att);
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	private void deleteAttachmentSafe(Attachment att) throws ProviderException {
		File attFile = JSPUtils.findAttachmentFile(att.getParentName(), att.getFileName(), storageDir);
		if (!attFile.exists()) {
			LOGGER.debug("File " + getPath(att) + " was attempetd to be deleted but does not exist");
			return;
		}

		boolean delete = attFile.delete();
		if (!delete) LOGGER.debug("File " + getPath(att) + " could not be deleted on filesystem");
		try {
			Git git = new Git(repository);
			JspGitBridge.retryGitOperation(() -> {
				Boolean ignored = ignoreNode.checkIgnored(getPath(att), false);
				if (ignored == null || !ignored) {
					git.rm().addFilepattern(getPath(att)).call();
				}
				return null;
			}, LockFailedException.class, "Retry removing to repo, because of lock failed exception");

			commitAttachment(att, git, GitVersioningWikiEvent.DELETE, attFile.getAbsolutePath()
					.replace(this.gitVersioningFileProvider.getFilesystemPath(), "")
					.substring(1));
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ProviderException("File " + getPath(att) + " could not be deleted");
		}
	}

	private void commitAttachment(Attachment att, Git git, int type, String filePath) throws Exception {
		Boolean ignored = ignoreNode.checkIgnored(getPath(att), false);

		if (ignored != null && ignored) {
			return;
		}

		if (gitVersioningFileProvider.openCommits.containsKey(att.getAuthor())) {
			commitAttachmentOpenCommits(att, type);
		}
		else {
			commitAttachmentGit(att, git, type, filePath);
		}
	}

	private void commitAttachmentGit(Attachment att, Git git, int type, String filePath) throws Exception {
		if (att.getAuthor() == null) {
			this.gitBridge.assignAuthor(att);
		}

//		String path = JSPUtils.unmangleName(getPath(att));
//		if(type!= GitVersioningWikiEvent.DELETE &&!Paths.get(this.gitVersioningFileProvider.getFilesystemPath(), path).toFile().exists()){
//			String fileName = JSPUtils.mangleName(att.getParentName())+"-att"+File.separator+att.getFileName();
//			if(Paths.get(this.gitVersioningFileProvider.getFilesystemPath(), fileName).toFile().exists()){
//				path = fileName;
//			}
//			int a=2;
//		}
		CommitCommand commitCommand = git.commit().setAllowEmpty(true).setOnly(filePath);
		setMessage(att, commitCommand);
		this.gitVersioningFileProvider.gitBridge().addUserInfo(engine, att.getAuthor(), commitCommand);
		try {
			gitVersioningFileProvider.commitLock();

			commitAttachmentSafe(att, type, commitCommand);
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
		}
	}

	private void commitAttachmentSafe(Attachment att, int type, CommitCommand commitCommand) throws Exception {
		JspGitBridge.retryGitOperation(() -> {
			RevCommit revCommit = commitCommand.call();
			WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, type,
					att.getAuthor(),
					att.getParentName() + "/" + att.getFileName(),
					revCommit.getId().getName()));
			return null;
		}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");
		this.gitBridge.periodicalGitGC();
	}

	private void commitAttachmentOpenCommits(Attachment att, int type) {
		gitVersioningFileProvider.openCommits.get(att.getAuthor()).add(getPath(att));
	}

	@Override
	public void moveAttachmentsForPage(Page oldParent, String newParent) throws ProviderException {
		try {
			gitVersioningFileProvider.canWriteFileLock();
			gitVersioningFileProvider.commitLock();

			moveAttachmentsForPageSafe(oldParent, newParent);
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	private void moveAttachmentsForPageSafe(Page oldParent, String newParent) throws ProviderException {
		File oldDir = JSPUtils.findPageDir(oldParent.getName(), storageDir);
		File newDir = JSPUtils.findPageDir(newParent, storageDir);
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

		if (files == null) {
			return;
		}
		try {
			List<String> filesForEvent = new ArrayList<>();
			moveFilesInFilesystem(newParent, oldDir, newDir);

			moveFilesGit(oldParent, newParent, files, filesForEvent);
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ProviderException("Can't move attachments from " + oldParent.getName() + " to " + newParent);
		}
	}

	private void moveFilesGit(Page oldParent, String newParent, File[] files, List<String> filesForEvent) throws Exception {
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
			}
			else {
				commit.setOnly(oldPath);
				commit.setOnly(newPath);
				filesForEvent.add(oldParent.getName() + "/" + file.getName());
				filesForEvent.add(newParent + "/" + file.getName());
			}
		}
		add.addFilepattern(getAttachmentDir(oldParent.getName()));
		add.addFilepattern(getAttachmentDir(newParent));

//		WikiEngineGitBridge.retryGitOperation(() -> {
//			rm.call();
//			return null;
//		}, LockFailedException.class, "Retry removing to repo, because of lock failed exception");

		JspGitBridge.retryGitOperation(() -> {
			add.call();
			return null;
		}, LockFailedException.class, "Retry adding to repo, because of lock failed exception");

		if (!gitVersioningFileProvider.openCommits.containsKey(oldParent.getAuthor())) {
			String comment = gitCommentStrategy.getComment(oldParent, "move attachments from " + oldParent.getName() + " to " + newParent);
			commit.setMessage(comment);
			try {
				gitVersioningFileProvider.commitLock();
				JspGitBridge.retryGitOperation(() -> {
					RevCommit revCommit = commit.call();
					WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.MOVED,
							oldParent.getAuthor(),
							filesForEvent,
							revCommit.getId().getName()));
					return null;
				}, LockFailedException.class, "Retry commit to repo, because of lock failed exception");

				this.gitBridge.periodicalGitGC();
			}
			finally {
				gitVersioningFileProvider.commitUnlock();
			}
		}
	}

	private void moveFilesInFilesystem(String newParent, File oldDir, File newDir) throws ProviderException, IOException {
		if (oldDir.getName().equalsIgnoreCase(newDir.getName())) {
			File tmpDir = JSPUtils.findPageDir(newParent + "_tmp", storageDir);
			Files.move(oldDir.toPath(), tmpDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.move(tmpDir.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		else {
			Files.move(oldDir.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	@Nullable
	private List<Attachment> getVersionHistoryFromGit(Attachment att) {
		List<Attachment> ret = new ArrayList<>();
		try {
			File attFile = JSPUtils.findAttachmentFile(att.getParentName(), att.getFileName(), storageDir);
			if (!attFile.exists()) {
				return null;
			}
			Boolean ignored = ignoreNode.checkIgnored(getPath(att), false);
			if (ignored == null || !ignored) {
				Git git = new Git(repository);
				try {
					List<RevCommit> revCommitList = this.gitBridge.getRevCommitList(att, git);
					int version = 1;
					for (RevCommit revCommit : revCommitList) {
						Attachment attVersion = getAttachment(att, version, revCommit);
						ret.add(attVersion);
						version++;
					}
					Collections.reverse(ret);
					return ret;
				}
				catch (GitAPIException | IOException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
			else {
				att.setVersion(1);
				return Collections.singletonList(att);
			}
		}
		catch (ProviderException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}
}
