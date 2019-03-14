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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.search.QueryItem;
import org.apache.wiki.util.FileUtil;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jetbrains.annotations.NotNull;

import static org.apache.wiki.providers.GitVersioningUtils.addUserInfo;
import static org.apache.wiki.providers.GitVersioningUtils.reverseToList;

/**
 * @author Josua Nürnberger
 * @created 2019-03-13
 */
public class GitVersioningAttachmentProvider extends BasicAttachmentProvider {

	private Repository repository;
	private WikiEngine engine;
	private String storageDir;

	@Override
	public void initialize(WikiEngine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);

		this.engine = engine;
		storageDir = TextUtil.getCanonicalFilePathProperty(properties, PROP_STORAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");

		String patternString = engine.getWikiProperties().getProperty(PROP_DISABLECACHE);
		WikiPageProvider provider = engine.getPageManager().getProvider();
		if (provider instanceof CachingProvider) {
			provider = ((CachingProvider) provider).getRealProvider();
		}
		if (provider instanceof GitVersioningFileProvider) {
			repository = ((GitVersioningFileProvider) provider).getRepository();
		}
		else {
			throw new NoRequiredPropertyException("GitVersioningFileProvider is not configured", "jspwiki.pageProvider");
		}
	}

	File findPageDir(String page) throws ProviderException {
		File dir = new File(storageDir, getAttachmentDir(page));
		if (dir.exists() && !dir.isDirectory()) {
			throw new ProviderException("Attachment directory for " + page + " is not a directory");
		}
		return dir;
	}

	@NotNull
	private String getAttachmentDir(String page) {
		return TextUtil.urlEncodeUTF8(page) + DIR_EXTENSION;
	}

	File findAttachmentDir(Attachment att) throws ProviderException {
		return findPageDir(att.getParentName());
	}

	@Override
	public void putAttachmentData(Attachment att, InputStream data) throws ProviderException, IOException {
		File attDir = findAttachmentDir(att);
		Git git = new Git(repository);
		if (!attDir.exists()) {
			attDir.mkdirs();
			try {
				git.add().addFilepattern(attDir.getName()).call();
			}
			catch (GitAPIException e) {
				e.printStackTrace();
			}
		}

		File newfile = new File(attDir, att.getFileName());
		boolean add = !newfile.exists();
		try (OutputStream out = new FileOutputStream(newfile)) {
			log.info("Uploading attachment " + att.getFileName() + " to page " + att.getParentName());
			log.info("Saving attachment contents to " + newfile.getAbsolutePath());
			FileUtil.copyContents(data, out);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (data != null) {
				data.close();
			}
		}
		try {

			if (add) {
				git.add().addFilepattern(getPath(att)).call();
			}
			CommitCommand commitCommand = git.commit()
					.setOnly(getPath(att));
			setMessage(att, commitCommand);
			addUserInfo(engine, att.getAuthor(), commitCommand);
			commitCommand.call();
		}
		catch (NoFilepatternException e) {
			e.printStackTrace();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	private void setMessage(Attachment att, CommitCommand commitCommand) {
		String changeNote = (String) att.getAttribute(Attachment.CHANGENOTE);
		if (changeNote != null && !"".equals(changeNote)) {
			commitCommand.setMessage(changeNote);
		}
		else {
			commitCommand.setMessage("-");
		}
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningAttachmentProvider.class.getSimpleName();
	}

	@Override
	public InputStream getAttachmentData(Attachment att) throws IOException, ProviderException {
		File attFile = new File(findAttachmentDir(att), att.getFileName());
		if (!attFile.exists()) {
			throw new ProviderException("File " + att.getFileName() + " does not exist");
		}
		int version = att.getVersion();
		Git git = new Git(repository);
		try {
			List<RevCommit> revCommitList = getRevCommitList(att, git);
			InputStream ret = null;
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
			return ret;
		}
		catch (GitAPIException e) {
			e.printStackTrace();
			throw new ProviderException("");
		}
	}

	private ObjectId getObjectOfCommit(RevCommit commit, Attachment att) throws IOException {
		TreeWalk treeWalkDir = new TreeWalk(repository);
		treeWalkDir.reset(commit.getTree());
		treeWalkDir.setFilter(PathFilter.create(getAttachmentDir(att)));
		treeWalkDir.setRecursive(false);
		//only the attachment directory
		while (treeWalkDir.next()) {
			ObjectId objectId = treeWalkDir.getObjectId(0);
			// so now walk a step down
			TreeWalk treeWalkFile = new TreeWalk(repository);
			treeWalkFile.reset(objectId);
			treeWalkFile.setFilter(PathFilter.create(att.getFileName()));
			treeWalkFile.setRecursive(false);
			while (treeWalkFile.next()) {
				// now we should have our file
				ObjectId fileId = treeWalkFile.getObjectId(0);
				return fileId;
			}
		}
		return null;
	}

	private String getAttachmentDir(Attachment att) {
		return TextUtil.urlEncodeUTF8(att.getParentName()) + DIR_EXTENSION;
	}

	private String getPath(Attachment att) {
		return getAttachmentDir(att.getParentName()) + "/" + att.getFileName();
	}

	@Override
	public Collection<Attachment> listAttachments(WikiPage page) throws ProviderException {
		File attachmentDir = findPageDir(page.getName());
		if (attachmentDir.exists()) {
			List<Attachment> ret = new ArrayList<>();
			File[] files = attachmentDir.listFiles();
			for (File file : files) {
				ret.add(getAttachmentInfo(page, file.getName(), LATEST_VERSION));
			}
			return ret;
		}
		else {
			return null;
		}
	}

	@Override
	public Collection<Attachment> findAttachments(QueryItem[] query) {
		return super.findAttachments(query);
	}

	@Override
	public List<Attachment> listAllChanged(Date timestamp) throws ProviderException {
		//TODO schwer
		return super.listAllChanged(timestamp);
	}

	@Override
	public Attachment getAttachmentInfo(WikiPage page, String name, int version) throws ProviderException {
		File attFile = new File(findPageDir(page.getName()), name);
		if (attFile.exists()) {
			Attachment att = new Attachment(engine, page.getName(), name);
			List<Attachment> versionHistory = getVersionHistory(att);
			if (version == LATEST_VERSION) {
				return versionHistory.get(versionHistory.size() - 1);
			}
			else {
				if (version > 0 && version <= versionHistory.size()) {
					return versionHistory.get(version - 1);
				}
			}
		}
		return null;
	}

	@Override
	public List<Attachment> getVersionHistory(Attachment att) {
		List<Attachment> ret = new ArrayList<>();
		try {
			File attFile = new File(findPageDir(att.getParentName()), att.getFileName());
			if (attFile.exists()) {

				Git git = new Git(repository);
				try {
					List<RevCommit> revCommitList = getRevCommitList(att, git);
					int version = 1;
					for (RevCommit revCommit : revCommitList) {
						Attachment attVersion = getAttachment(att, version, revCommit);
						ret.add(attVersion);
						version++;
					}
					return ret;
				}
				catch (GitAPIException e) {
					e.printStackTrace();
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
			}
		}
		catch (ProviderException e) {

		}
		return ret;
	}

	@NotNull Attachment getAttachment(Attachment att, int version, RevCommit revCommit) throws IOException, ProviderException {
		Attachment attVersion = new Attachment(engine, att.getParentName(), att.getFileName());
		//TODO check pattern
		attVersion.setCacheable(false);
		attVersion.setAuthor(revCommit.getCommitterIdent().getName());
		attVersion.setVersion(version);
		attVersion.setAttribute(WikiPage.CHANGENOTE, revCommit.getFullMessage());
		attVersion.setSize(getObjectSize(revCommit, att));
		attVersion.setLastModified(new Date(1000l * revCommit.getCommitTime()));
		return attVersion;
	}

	List<RevCommit> getRevCommitList(Attachment att, Git git) throws GitAPIException, IOException {
		Iterable<RevCommit> revCommits = git
				.log()
				.add(repository.resolve(Constants.HEAD))
				.addPath(getPath(att))
				.call();
		return reverseToList(revCommits);
	}

	private long getObjectSize(RevCommit version, Attachment att) throws IOException, ProviderException {
		long ret;
		ObjectId objectId = getObjectOfCommit(version, att);
		if (objectId != null) {
			ObjectLoader loader = repository.open(objectId);
			ret = loader.getSize();
		}
		else {
			throw new ProviderException("Can't load Git object for " + att.getFileName());
		}
		return ret;
	}

	@Override
	public void deleteVersion(Attachment att) throws ProviderException {

	}

	@Override
	public void deleteAttachment(Attachment att) throws ProviderException {
		File attFile = new File(findPageDir(att.getParentName()), att.getFileName());
		if (attFile.exists()) {
			attFile.delete();
			try {
				Git git = new Git(repository);
				git.rm().addFilepattern(getPath(att)).call();
				CommitCommand commitCommand = git.commit().setOnly(getPath(att));
				setMessage(att, commitCommand);
				addUserInfo(engine, att.getAuthor(), commitCommand);
				commitCommand.call();
			}
			catch (AbortedByHookException e) {
				e.printStackTrace();
			}
			catch (ConcurrentRefUpdateException e) {
				e.printStackTrace();
			}
			catch (NoHeadException e) {
				e.printStackTrace();
			}
			catch (UnmergedPathsException e) {
				e.printStackTrace();
			}
			catch (NoFilepatternException e) {
				e.printStackTrace();
			}
			catch (NoMessageException e) {
				e.printStackTrace();
			}
			catch (WrongRepositoryStateException e) {
				e.printStackTrace();
			}
			catch (GitAPIException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void moveAttachmentsForPage(String oldParent, String newParent) throws ProviderException {
		File oldDir = findPageDir(oldParent);
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
		try {
			Files.move(oldDir.toPath(), newDir.toPath(), StandardCopyOption.ATOMIC_MOVE);
			Git git = new Git(repository);
			RmCommand rm = git.rm();
			AddCommand add = git.add();
			CommitCommand commit = git.commit();
			for (File file : files) {
				rm.addFilepattern(getAttachmentDir(oldParent) + "/" + file.getName());
				commit.setOnly(getAttachmentDir(oldParent) + "/" + file.getName());
				add.addFilepattern(getAttachmentDir(newParent) + "/" + file.getName());
				commit.setOnly(getAttachmentDir(newParent) + "/" + file.getName());
			}
			rm.addFilepattern(getAttachmentDir(oldParent));
			add.addFilepattern(getAttachmentDir(newParent));
			rm.call();
			add.call();
			commit.setMessage("move attachments form " + oldParent + " to " + newParent);
			commit.call();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (NoFilepatternException e) {
			e.printStackTrace();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
}
