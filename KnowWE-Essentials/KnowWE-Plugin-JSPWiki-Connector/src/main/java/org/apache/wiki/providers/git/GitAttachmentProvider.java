/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package org.apache.wiki.providers.git;

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

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.BasicAttachmentProvider;
import org.apache.wiki.providers.CachingProvider;
import org.apache.wiki.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitConnector;

/**
 * Git-backed attachment provider for a single-wiki instance, the sibling of {@link GitPageProvider}. Stores
 * attachments flat ({@code <page>-att/<file>}) inside the same git repository the page provider uses and versions
 * them via git; the {@code OLD/} / versioned-directory mechanism of {@link BasicAttachmentProvider} is not used.
 * <p>
 * It routes to the <strong>same</strong> {@link GitPageHistory}, batch registry and {@link WikiGitContext} as the
 * page provider (located lazily via the engine), so attachment changes share the repository, the commit lock, and,
 * within a transaction, the same commit.
 */
public class GitAttachmentProvider extends BasicAttachmentProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitAttachmentProvider.class);

	private GitPageProvider pageProvider;

	@Override
	protected boolean isCreationDateBatchEnabled() {
		// git handles versioning + dates; the file-system OLD/ creation-date batch must not run (it would create an
		// untracked OLD/versioning.properties in the git working tree).
		return false;
	}

	@Override
	public String getProviderInfo() {
		return GitAttachmentProvider.class.getSimpleName();
	}

	/**
	 * Locates the sibling git page provider once, lazily (init order between the two providers is unspecified).
	 */
	private GitPageProvider pageProvider() throws ProviderException {
		if (pageProvider == null) {
			PageProvider provider = m_engine.getManager(PageManager.class).getProvider();
			if (provider instanceof CachingProvider caching) {
				provider = caching.getRealProvider();
			}
			if (provider instanceof GitPageProvider gitProvider) {
				pageProvider = gitProvider;
			}
			else {
				throw new ProviderException("GitAttachmentProvider requires "
						+ "GitPageProvider as the page provider, but found: " + provider);
			}
		}
		return pageProvider;
	}

	private GitPageHistory history() throws ProviderException {
		return pageProvider().history();
	}

	private WikiGitContext context() throws ProviderException {
		return pageProvider().context();
	}

	// --- path helpers (anchored at the page repo) ----------------------------

	/**
	 * Repo-relative path of the attachment, e.g. {@code MyPage-att/image.png}.
	 */
	private String attachmentPath(Attachment attachment) {
		return JSPUtils.getAttachmentDir(attachment.getParentName()) + "/" + JSPUtils.mangleName(attachment.getFileName());
	}

	private File attachmentDir(GitPageHistory history, String parentName) {
		return new File(history.repoKey(), JSPUtils.getAttachmentDir(parentName));
	}

	private File attachmentFile(GitPageHistory history, Attachment attachment) {
		return new File(history.repoKey(), attachmentPath(attachment));
	}

	// --- write path ----------------------------------------------------------

	@Override
	public void putAttachmentData(Attachment attachment, InputStream data) throws ProviderException, IOException {
		GitPageHistory history = history();
		File attFile = attachmentFile(history, attachment);
		String relPath = attachmentPath(attachment);
		byte[] bytes = data.readAllBytes();
		// bracket the file write and its commit, like the page provider's save (no sweep may interleave)
		try {
			history.withCommitLock(() -> {
				putAttachmentDataLocked(attachment, bytes, attFile, relPath, history);
				return null;
			});
		}
		catch (ProviderException | IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ProviderException("Could not save attachment '" + relPath + "': " + e.getMessage());
		}
	}

	private void putAttachmentDataLocked(Attachment attachment, byte[] bytes, File attFile, String relPath,
										 GitPageHistory history) throws ProviderException {
		File dir = attFile.getParentFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// git creates no commit if no byte changed, and we also do not rewrite the file
		if (contentEquals(attFile, bytes)) {
			return;
		}
		writeFile(attachment, bytes, attFile);
		attachment.setSize(attFile.length());

		String user = attachment.getAuthor();
		GitCommitBatchRegistry registry = pageProvider().batchRegistry();
		if (registry.isOpen(user)) {
			history.stageForBatch(relPath);
			registry.stage(user, history.repoKey(), relPath);
		}
		else {
			CommitUserData userData = context().userData(user, message(attachment));
			String commitHash = history.commitFile(attFile, relPath, userData);
			if (commitHash != null) {
				fireEvent(GitVersioningWikiEvent.UPDATE, attachment, commitHash, history);
			}
		}
	}

	private void writeFile(Attachment attachment, byte[] bytes, File target) throws ProviderException {
		try (OutputStream out = new FileOutputStream(target)) {
			LOGGER.info("Saving attachment '{}' of page '{}' to {}", attachment.getFileName(), attachment.getParentName(),
					target.getAbsolutePath());
			FileUtil.copyContents(new ByteArrayInputStream(bytes), out);
		}
		catch (IOException e) {
			throw new ProviderException("Can't write attachment file " + target + ": " + e.getMessage());
		}
	}

	private boolean contentEquals(File file, byte[] data) {
		if (!file.exists()) {
			return false;
		}
		try {
			return Arrays.equals(Files.readAllBytes(file.toPath()), data);
		}
		catch (IOException e) {
			LOGGER.error("Could not read attachment file: {}", file, e);
			return false;
		}
	}

	private String message(Attachment attachment) throws ProviderException {
		String comment = context().commentStrategy()
				.getComment(attachment, context().commentStrategy().getCommentForUser(attachment.getAuthor()));
		if (comment.isEmpty()) {
			String changeNote = attachment.getAttribute(Attachment.CHANGENOTE);
			comment = (changeNote != null && !changeNote.isEmpty()) ? changeNote : "-";
		}
		return comment;
	}

	// --- read path -----------------------------------------------------------

	@Override
	public InputStream getAttachmentData(Attachment attachment) throws IOException, ProviderException {
		GitPageHistory history = history();
		File attFile = attachmentFile(history, attachment);
		if (!attFile.exists()) {
			throw new ProviderException("Attachment file " + attachment.getFileName() + " does not exist");
		}
		String relPath = attachmentPath(attachment);
		int version = attachment.getVersion();
		if (version == WikiProvider.LATEST_VERSION || history.connector().isIgnored(relPath)) {
			return new FileInputStream(attFile);
		}
		return new ByteArrayInputStream(history.connector().log().getBytesForPath(relPath, version));
	}

	@Override
	public List<Attachment> listAttachments(Page page) throws ProviderException {
		File dir = attachmentDir(history(), page.getName());
		if (!dir.exists()) {
			return Collections.emptyList();
		}
		List<Attachment> result = new ArrayList<>();
		File[] files = dir.listFiles(file -> !file.isHidden());
		if (files != null) {
			for (File file : files) {
				Attachment info = getAttachmentInfo(page, JSPUtils.unmangleName(file.getName()),
						WikiProvider.LATEST_VERSION);
				if (info != null) {
					result.add(info);
				}
			}
		}
		return result;
	}

	@Override
	public Attachment getAttachmentInfo(Page page, String name, int version) throws ProviderException {
		GitPageHistory history = history();
		Attachment att = new org.apache.wiki.attachment.Attachment(m_engine, page.getName(), name);
		att.setVersion(version);
		File attFile = attachmentFile(history, att);
		if (!attFile.exists()) {
			return null;
		}
		String relPath = attachmentPath(att);
		GitConnector connector = history.connector();

		// git-ignored attachments: serve metadata from the filesystem (single version)
		if (connector.isIgnored(relPath)) {
			return filesystemAttachment(att, attFile);
		}

		int realVersion = version;
		String commitHash;
		if (version == WikiProvider.LATEST_VERSION) {
			List<String> commitHashes = connector.log().commitHashesForFile(relPath);
			if (commitHashes.isEmpty()) {
				// written but not yet committed (e.g. staged in an open batch)
				return filesystemAttachment(att, attFile);
			}
			realVersion = commitHashes.size();
			commitHash = commitHashes.get(commitHashes.size() - 1);
		}
		else {
			commitHash = connector.log().commitHashForFileAndVersion(relPath, version);
			if (commitHash == null) {
				return null;
			}
		}
		return fromCommit(att, realVersion, commitHash, relPath, connector);
	}

	private Attachment filesystemAttachment(Attachment attachment, File attFile) {
		attachment.setVersion(1);
		attachment.setSize(attFile.length());
		attachment.setLastModified(new Date(attFile.lastModified()));
		return attachment;
	}

	private Attachment fromCommit(Attachment attachment, int version, String commitHash, String relPath, GitConnector connector) {
		CommitUserData userData = connector.log().commitUserDataFor(commitHash);
		Attachment result = new org.apache.wiki.attachment.Attachment(m_engine, attachment.getParentName(), attachment.getFileName());
		result.setCacheable(false);
		result.setAuthor(userData.user);
		result.setVersion(version);
		result.setAttribute(WikiPage.CHANGENOTE, userData.message);
		result.setSize(connector.log().getFilesizeForCommit(commitHash, relPath));
		result.setLastModified(Date.from(Instant.ofEpochSecond(connector.log().commitTimeFor(commitHash))));
		return result;
	}

	@Override
	public List<Attachment> getVersionHistory(Attachment attachment) {
		try {
			GitPageHistory history = history();
			File attFile = attachmentFile(history, attachment);
			if (!attFile.exists()) {
				return Collections.emptyList();
			}
			String relPath = attachmentPath(attachment);
			GitConnector connector = history.connector();
			if (connector.isIgnored(relPath)) {
				attachment.setVersion(1);
				return Collections.singletonList(attachment);
			}
			List<Attachment> result = new ArrayList<>();
			int version = 1;
			for (String commitHash : connector.log().commitHashesForFile(relPath)) {
				result.add(fromCommit(attachment, version, commitHash, relPath, connector));
				version++;
			}
			return result;
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	// --- delete / move -------------------------------------------------------

	@Override
	public void deleteAttachment(Attachment attachment) throws ProviderException {
		GitPageHistory history = history();
		File attFile = attachmentFile(history, attachment);
		if (!attFile.exists()) {
			LOGGER.debug("Attachment {} was to be deleted but does not exist.", attachmentPath(attachment));
			return;
		}
		if (!attFile.delete()) {
			LOGGER.warn("Could not delete attachment file on disk: {}", attFile.getAbsolutePath());
		}
		CommitUserData userData = context().userData(attachment.getAuthor(), message(attachment));
		String commitHash = history.removeFile(attachmentPath(attachment), userData);
		if (commitHash != null) {
			fireEvent(GitVersioningWikiEvent.DELETE, attachment, commitHash, history);
		}
	}

	@Override
	public void deleteVersion(Attachment attachment) {
		// Not supported by git-backed history. The page provider's deleteVersion throws, but the AttachmentProvider
		// contract here is a no-op; revert is the supported undo path.
		LOGGER.debug("deleteVersion is a no-op for git-backed attachments: {}", attachment.getFileName());
	}

	@Override
	public void moveAttachmentsForPage(Page oldParent, String newParent) throws ProviderException {
		GitPageHistory history = history();
		File oldDir = attachmentDir(history, oldParent.getName());
		File newDir = attachmentDir(history, newParent);
		File[] files = oldDir.listFiles();
		if (files == null) {
			return;
		}
		try {
			moveDirOnFilesystem(oldDir, newDir);
			String oldRel = JSPUtils.getAttachmentDir(oldParent.getName());
			String newRel = JSPUtils.getAttachmentDir(newParent);
			List<String> oldPaths = new ArrayList<>();
			List<String> newPaths = new ArrayList<>();
			List<String> eventPages = new ArrayList<>();
			for (File file : files) {
				oldPaths.add(oldRel + "/" + file.getName());
				newPaths.add(newRel + "/" + file.getName());
				eventPages.add(oldParent.getName() + "/" + file.getName());
				eventPages.add(newParent + "/" + file.getName());
			}
			String user = oldParent.getAuthor();
			GitCommitBatchRegistry registry = pageProvider().batchRegistry();
			if (registry.isOpen(user)) {
				// the moved-in files are untracked until staged; a pathspec commit cannot pick up untracked files,
				// so without staging they would miss the batch commit (the moved-away paths are tracked deletions
				// and need no staging)
				for (String newPath : newPaths) {
					history.stageForBatch(newPath);
				}
				registry.stage(user, history.repoKey(), oldPaths);
				registry.stage(user, history.repoKey(), newPaths);
			}
			else {
				String comment = context().commentStrategy().getComment(oldParent,
						"move attachments from " + oldParent.getName() + " to " + newParent);
				CommitUserData userData = context().userData(user, comment);
				String commitHash = history.commitMovedPaths(oldPaths, newPaths, userData);
				if (commitHash != null) {
					context().fireCommitted(this, GitVersioningWikiEvent.MOVED, user, eventPages,
							commitHash, history.repoKey());
				}
			}
		}
		catch (IOException e) {
			throw new ProviderException("Can't move attachments from " + oldParent.getName() + " to " + newParent
					+ ": " + e.getMessage());
		}
	}

	private void moveDirOnFilesystem(File oldDir, File newDir) throws IOException {
		if (oldDir.getName().equalsIgnoreCase(newDir.getName())) {
			// case-only rename: bounce through a temp dir so case-insensitive filesystems don't no-op the move
			File tmpDir = new File(newDir.getParentFile(), newDir.getName() + "_tmp");
			Files.move(oldDir.toPath(), tmpDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.move(tmpDir.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		else {
			Files.move(oldDir.toPath(), newDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Fires both commit notifications (see {@link WikiGitContext#fireCommitted}) with the JSPWiki attachment naming
	 * convention {@code <parent>/<file>} as the page name.
	 */
	private void fireEvent(int type, Attachment attachment, String commitHash, GitPageHistory history) throws ProviderException {
		String page = attachment.getParentName() + "/" + attachment.getFileName();
		context().fireCommitted(this, type, attachment.getAuthor(), List.of(page), commitHash, history.repoKey());
	}
}
