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
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.providers.commentStrategy.GitCommentStrategy;
import org.apache.wiki.util.FileUtil;
import org.apache.wiki.util.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.GitConnector;
import de.uniwue.d3web.gitConnector.UserData;

import static org.apache.wiki.gitBridge.JSPUtils.getAttachmentDir;
import static org.apache.wiki.gitBridge.JSPUtils.getPath;

/**
 * @author Josua Nürnberger
 * @created 2019-03-13
 */
@SuppressWarnings("rawtypes")
public class GitVersioningAttachmentProviderDelegate extends BasicAttachmentProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitVersioningAttachmentProviderDelegate.class);

	private Engine engine;
	//TODO idk if really required
	private String storageDir;

	//TODO this is bad that its here but i guess it will stay...
	private final GitVersioningFileProvider gitVersioningFileProvider;

	private final GitCommentStrategy gitCommentStrategy;
	private final GitConnector gitConnector;

	//prevent initilization from outside this package!
	GitVersioningAttachmentProviderDelegate(GitVersioningFileProvider fileProvider) {
		this.gitVersioningFileProvider = fileProvider;
		gitCommentStrategy = this.gitVersioningFileProvider.getGitCommentStrategy();
		this.gitConnector = gitVersioningFileProvider.getGitConnector();
	}

	@Override
	public void initialize(Engine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);

		this.engine = engine;

		storageDir = TextUtil.getCanonicalFilePathProperty(properties, PROP_STORAGEDIR,
				System.getProperty("user.home") + File.separator + "jspwiki-files");
	}

	@Override
	public String getProviderInfo() {
		return GitVersioningAttachmentProviderDelegate.class.getSimpleName();
	}

	private File findAttachmentDir(Attachment att) throws ProviderException {
		return JSPUtils.findPageDir(att.getParentName(), storageDir);
	}

	private boolean contentEquals(File file, byte[] data) {
		if (!file.exists()) {
			return false;
		}
		byte[] bytesExisting = null;
		try {
			bytesExisting = Files.readAllBytes(file.toPath());
		}
		catch (IOException e) {
			LOGGER.error("Could not read file: " + file);
			return false;
		}

		//if these are equal we do not update
		return Arrays.equals(bytesExisting, data);
	}

	private File findAttachmentFile(Attachment attachment) {
		try {
			return JSPUtils.findAttachmentFile(attachment.getParentName(), attachment.getFileName(), storageDir);
		}
		catch (ProviderException e) {
			LOGGER.error("Could not find the according file for attachment: " + attachment.getName());
			throw new RuntimeException(e);
		}
	}

	@Override
	public void putAttachmentData(Attachment att, InputStream data) throws ProviderException, IOException {
		File attDir = findAttachmentDir(att);

		if (!attDir.exists()) {
			//create dir
			attDir.mkdirs();
			//add dir (TODO idk this is an empty directory - i think this doesnt even have an effect)
			this.gitConnector.addPath(attDir.getName());
		}
		File newFile = findAttachmentFile(att);

		byte[] bytesNew = data.readAllBytes();
		data = new ByteArrayInputStream(bytesNew);
		//git doesnt create commits if no byte is changed! (and we also do not change on filesystem)
		if (contentEquals(newFile, bytesNew)) {
			return;
		}

		boolean isIgnored = this.gitConnector.isIgnored(getPath(att));
		//TODO adding by default should never hurt
//		boolean add = !newFile.exists() && !isIgnored;

		copyOnFilesystem(att, data, newFile);

		gitConnector.addPath(getPath(att));

		att.setSize(newFile.length());

		commitAttachment(att, GitVersioningWikiEvent.UPDATE, getPath(att));
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

	private String getMessage(Attachment att) {
		String changeNote = att.getAttribute(Attachment.CHANGENOTE);
		String comment = gitCommentStrategy.getComment(att, gitCommentStrategy.getCommentForUser(att.getAuthor()));
		if (comment.isEmpty()) {
			if (changeNote != null && !"".equals(changeNote)) {
				comment = changeNote;
			}
			else {
				comment = "-";
			}
		}
		return comment;
	}


	@Override
	public InputStream getAttachmentData(Attachment att) throws IOException, ProviderException {
		File attFile = findAttachmentFile(att);
		if (!attFile.exists()) {
			throw new ProviderException("File " + att.getFileName() + " does not exist");
		}
		int version = att.getVersion();
		if (version == -1) {
			return new FileInputStream(attFile);
		}

		InputStream ret = null;
		boolean ignored = this.gitConnector.isIgnored(getPath(att));
		if (ignored) {
			ret = new FileInputStream(attFile);
		}
		else {
			ret = getAttachmentDataFromGit(att, version);
		}
		return ret;
	}

	private InputStream getAttachmentDataFromGit(Attachment att, int version) {
		String path = getPath(att);
		byte[] bytesForPath = this.gitConnector.getBytesForPath(path, version);
		return new ByteArrayInputStream(bytesForPath);
	}

	@Override
	public List<Attachment> listAttachments(Page page) throws ProviderException {
		File attachmentDir = JSPUtils.findPageDir(page.getName(), storageDir);

		if (!attachmentDir.exists()) {
			return Collections.emptyList();
		}

		List<Attachment> ret = new ArrayList<>();
		File[] files = attachmentDir.listFiles(file -> !file.isHidden());
		if (files != null) {
			for (File file : files) {
				Attachment attachmentInfo = getAttachmentInfo(page, JSPUtils.unmangleName(file.getName()), LATEST_VERSION);
				if (attachmentInfo != null) {
					ret.add(attachmentInfo);
				}
			}
		}
		return ret;
	}

	@Override
	public List<Attachment> listAllChanged(Date timestamp) {
		boolean initCache = timestamp.getTime() == 0;

		List<Attachment> attachmentsFromFilesystem = getAllAttachmentsFromFilesystem();
		if (initCache) {
			return attachmentsFromFilesystem;
		}

		//TODO previously there was a very complicated chunk of code here - that in my best impression was wrong either way!
		//if for some reason the current implementation appears to cause trouble compare with the previous version

		List<Attachment> attachments = new ArrayList<>();

		//for every attachments thats currently still available, check the timestamp of its latest commit
		for (Attachment att : attachmentsFromFilesystem) {
			List<String> commitHashes = this.gitConnector.commitHashesForFileSince(getPath(att), timestamp);
			if (!commitHashes.isEmpty()) {
				String lastCommit = commitHashes.get(commitHashes.size() - 1);
				setAttachmentDataFromCommit(att, lastCommit);
				attachments.add(att);
			}
		}

		return attachments;
	}

	private void setAttachmentDataFromCommit(Attachment att, String commit) {
		long epochTimeInSeconds = this.gitConnector.commitTimeFor(commit);
		UserData userData = this.gitConnector.userDataFor(commit);

		att.setAttribute(Attachment.CHANGENOTE, userData.message);
		att.setAuthor(userData.user);
		att.setLastModified(Date.from(Instant.ofEpochSecond(epochTimeInSeconds)));
	}

	private List<Attachment> getAllAttachmentsFromFilesystem() {
		return this.gitVersioningFileProvider.getAllPages().stream().flatMap(page -> {
			try {
				return listAttachments(page).stream();
			}
			catch (ProviderException e) {
				throw new RuntimeException(e);
			}
		}).toList();
	}

	@Override
	public Attachment getAttachmentInfo(Page page, String name, int version) {
		Attachment att = new org.apache.wiki.attachment.Attachment(engine, page.getName(), name);
		att.setVersion(version);
		File attFile = findAttachmentFile(att);
		if (attFile.exists()) {
			String attPath = getPath(att);
			//use filesystem for ignore files
			if (this.gitConnector.isIgnored(attPath)) {
				att.setVersion(1);
				att.setSize(attFile.length());
				att.setLastModified(new Date(attFile.lastModified()));
				return att;
			}

			//otherwise we create it
			int realversion = version;
			String hash = null;
			if (realversion == LATEST_VERSION) {
				List<String> commitHashes = this.gitConnector.commitHashesForFile(attPath);
				//if there are no commits, we can still use the information from the filesystem
				if(commitHashes.isEmpty()){
					att.setVersion(1);
					att.setSize(attFile.length());
					att.setLastModified(new Date(attFile.lastModified()));
					return att;
				}

				realversion = commitHashes.size();
				hash = commitHashes.get(commitHashes.size() - 1);
			}
			else {
				hash = this.gitConnector.commitHashForFileAndVersion(attPath, realversion);
			}

			return fillAttachmentMetadata(att, realversion, hash);
		}
		return null;
	}

	@NotNull
	private Attachment fillAttachmentMetadata(Attachment att, int version, String commitHash) {
		UserData userData = this.gitConnector.userDataFor(commitHash);
		try {
			gitVersioningFileProvider.canWriteFileLock();
			Attachment attVersion = new org.apache.wiki.attachment.Attachment(engine, att.getParentName(), att.getFileName());
			attVersion.setCacheable(false);
			attVersion.setAuthor(userData.user);
			attVersion.setVersion(version);
			attVersion.setAttribute(WikiPage.CHANGENOTE, userData.message);
			long filesizeForCommit = this.gitConnector.getFilesizeForCommit(commitHash, getPath(att));
			attVersion.setSize(filesizeForCommit);

			long timeInSeconds = this.gitConnector.commitTimeFor(commitHash);
			Date date = Date.from(Instant.ofEpochSecond(timeInSeconds));
			attVersion.setLastModified(date);
			return attVersion;
		}
		finally {
			gitVersioningFileProvider.writeFileUnlock();
		}
	}

	@Override
	@NotNull
	public List<Attachment> getVersionHistory(Attachment att) {

		File attFile = findAttachmentFile(att);

		//no history if it does not exist
		if (!attFile.exists()) {
			return Collections.emptyList();
		}
		//only 1 version if it exists but is ignored
		if (this.gitConnector.isIgnored(getPath(att))) {
			att.setVersion(1);
			return Collections.singletonList(att);
		}

		List<Attachment> ret = new ArrayList<>();
		int version = 1;
		for (String commitHash : this.gitConnector.commitHashesForFile(getPath(att))) {
			Attachment attVersion = fillAttachmentMetadata(att, version, commitHash);
			ret.add(attVersion);
			version++;
		}

		return ret;
	}

	@Override
	public void deleteVersion(Attachment att) {
		// Can't delete version from git
	}

	private void commitAttachment(Attachment att, int type, String filePath) {
		if (this.gitConnector.isIgnored(getPath(att))) {
			return;
		}

		if (gitVersioningFileProvider.openCommits.containsKey(att.getAuthor())) {
			gitVersioningFileProvider.openCommits.get(att.getAuthor()).add(getPath(att));
		}
		else {
			commitAttachmentGit(att, type, filePath);
		}
	}

	private void commitAttachmentGit(Attachment att, int type, String filePath) {
		//TODO no idea why we might need it, im leaving this here since i dont know!
//		if (att.getAuthor() == null) {
//			this.gitBridge.assignAuthor(att);
//		}

		try {
			//TODO this filelock is pretty bad to be used in here!
			gitVersioningFileProvider.commitLock();

			UserData userData = this.gitVersioningFileProvider.getDelegate()
					.getUserData(att.getAuthor(), getMessage(att));
			//TODO commit only filepath?
			String commitHash = this.gitConnector.commitForUser(userData);
			WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, type,
					att.getAuthor(),
					att.getParentName() + "/" + att.getFileName(),
					commitHash));
		}
		finally {
			gitVersioningFileProvider.commitUnlock();
		}
	}

	@Override
	public void moveAttachmentsForPage(Page oldParent, String newParent) throws ProviderException {
		File oldDir = JSPUtils.findPageDir(oldParent.getName(), storageDir);
		File newDir = JSPUtils.findPageDir(newParent, storageDir);

		verifyNewDirectory(newParent, newDir);

		File[] files = oldDir.listFiles();

		if (files == null) {
			return;
		}
		try {
			moveFilesInFilesystem(newParent, oldDir, newDir);

			moveFilesGit(oldParent, newParent, files);
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ProviderException("Can't move attachments from " + oldParent.getName() + " to " + newParent);
		}
	}

	private void verifyNewDirectory(String newParent, File newDir) throws ProviderException {
		if (newDir.exists() && !newDir.isDirectory()) {
			throw new ProviderException(newParent + DIR_EXTENSION + " is not a directory");
		}
		if (newDir.exists() && !newDir.canWrite()) {
			throw new ProviderException("Can't write to directory " + newParent + DIR_EXTENSION);
		}
		if (!newDir.exists()) {
			newDir.mkdirs();
		}
	}

	private void moveFilesGit(Page oldParent, String newParent, File[] files) throws Exception {
		//we assume that the files are moved already
		List<String> filesForEvent = new ArrayList<>();
		String comment = gitCommentStrategy.getComment(oldParent, "move attachments from " + oldParent.getName() + " to " + newParent);

		//if tracked by openCommits we only have to update this list
		if (gitVersioningFileProvider.openCommits.containsKey(oldParent.getAuthor())) {
			moveFilesOpenCommits(oldParent, newParent, files);
			//done already
			return;
		}

		//here we do the move via the git connector

		//get all old and new paths
		List<String> oldPaths = new ArrayList<>();
		List<String> newPaths = new ArrayList<>();
		for (File file : files) {
			String oldPath = getAttachmentDir(oldParent.getName()) + "/" + file.getName();
			String newPath = getAttachmentDir(newParent) + "/" + file.getName();
			oldPaths.add(oldPath);
			newPaths.add(newPath);
			filesForEvent.add(oldParent.getName() + "/" + file.getName());
			filesForEvent.add(newParent + "/" + file.getName());
		}

		//remove old files
		UserData userData = this.gitVersioningFileProvider.getDelegate().getUserData(oldParent.getAuthor(), comment);
		this.gitConnector.deletePaths(oldPaths, userData, true);
		//add new files
		this.gitConnector.addPaths(newPaths);
		//and perform the commit TODO only the pages specified?
		String commitHash = this.gitConnector.commitForUser(userData);

		//notify wiki
		WikiEventManager.fireEvent(this, new GitVersioningWikiEvent(this, GitVersioningWikiEvent.MOVED,
				oldParent.getAuthor(),
				filesForEvent,
				commitHash));
	}

	private void moveFilesOpenCommits(Page oldParent, String newParent, File[] files) {
		for (File file : files) {
			String oldPath = getAttachmentDir(oldParent.getName()) + "/" + file.getName();
			String newPath = getAttachmentDir(newParent) + "/" + file.getName();
			gitVersioningFileProvider.openCommits.get(oldParent.getAuthor()).add(oldPath);
			gitVersioningFileProvider.openCommits.get(oldParent.getAuthor()).add(newPath);
		}
	}

	private void moveFilesInFilesystem(String newParent, File oldDir, File newDir) throws
			ProviderException, IOException {
		if (oldDir.getName().equalsIgnoreCase(newDir.getName())) {
			File tmpDir = JSPUtils.findPageDir(newParent + "_tmp", storageDir);
			Files.move(oldDir.toPath(), tmpDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.move(tmpDir.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		else {
			Files.move(oldDir.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	@Override
	public void deleteAttachment(Attachment att) {
		File attFile = findAttachmentFile(att);
		if (!attFile.exists()) {
			LOGGER.debug("File " + getPath(att) + " was attempted to be deleted but does not exist");
			return;
		}

		boolean delete = attFile.delete();
		if (!delete) LOGGER.debug("File " + getPath(att) + " could not be deleted on filesystem");

		if (this.gitConnector.isIgnored(getPath(att))) {
			return;
		}
		this.gitConnector.deletePath(getPath(att), this.gitVersioningFileProvider.getDelegate()
				.getUserData(att.getAuthor(), getMessage(att)), false);
	}
}
