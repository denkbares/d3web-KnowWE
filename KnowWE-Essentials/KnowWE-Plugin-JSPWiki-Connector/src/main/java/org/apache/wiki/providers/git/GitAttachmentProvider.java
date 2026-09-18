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
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.event.GitVersioningWikiEvent;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.AbstractFileProvider;
import org.apache.wiki.providers.BasicAttachmentProvider;
import org.apache.wiki.providers.CachingProvider;
import org.apache.wiki.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.CommitUserData;
import de.uniwue.d3web.gitConnector.GitFileAtCommit;
import de.uniwue.d3web.gitConnector.GitFileRevision;

/**
 * Git-backed attachment provider for a single-wiki instance, the sibling of {@link GitPageProvider}. Stores
 * attachments flat ({@code <page>-att/<file>}) inside the same git repository the page provider uses and versions
 * them via git; the {@code OLD/} / versioned-directory mechanism of {@link BasicAttachmentProvider} is not used.
 * <p>
 * It routes to the <strong>same</strong> {@link GitWikiRepository}, batch registry and {@link WikiGitContext} as the
 * page provider (located lazily via the engine), so attachment changes share the repository, its lock, and,
 * within a transaction, the same commit.
 */
public class GitAttachmentProvider extends BasicAttachmentProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitAttachmentProvider.class);

	private GitPageProvider pageProvider;

	@Override
	public void initialize(Engine engine, Properties properties) throws NoRequiredPropertyException, IOException {
		super.initialize(engine, properties);
		// this provider anchors every path at the page repository and ignores m_storageDir, but inherited methods
		// that are not overridden (notably listAllChanged, which Recent Changes calls) scan m_storageDir - the two
		// must be the same directory, and a mismatch would fail in ways that look like git problems
		String pageDir = new File(AbstractFileProvider.get_m_pageDirectory(properties)).getCanonicalPath();
		String storageDir = new File(m_storageDir).getCanonicalPath();
		if (!storageDir.equals(pageDir)) {
			throw new IOException(getClass().getSimpleName() + " stores attachments inside the page repository: '"
					+ PROP_STORAGEDIR + "' (" + storageDir + ") must equal '"
					+ AbstractFileProvider.PROP_PAGEDIR + "' (" + pageDir + ").");
		}
	}

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

	private GitWikiBackend backend() throws ProviderException {
		return pageProvider().backend();
	}

	private GitWikiRepository repository() throws ProviderException {
		return backend().repository();
	}

	private WikiGitContext context() throws ProviderException {
		return backend().context();
	}

	// --- path helpers (anchored at the page repo) ----------------------------

	/**
	 * Repo-relative path of the attachment, e.g. {@code MyPage-att/image.png}.
	 */
	private String attachmentPath(Attachment attachment) {
		return JSPUtils.getAttachmentDir(attachment.getParentName()) + "/" + JSPUtils.mangleName(attachment.getFileName());
	}

	private File attachmentDir(GitWikiRepository repository, String parentName) {
		return new File(repository.path(), JSPUtils.getAttachmentDir(parentName));
	}

	private File attachmentFile(GitWikiRepository repository, Attachment attachment) {
		return new File(repository.path(), attachmentPath(attachment));
	}

	/**
	 * The legacy {@link BasicAttachmentProvider} version directory of the attachment
	 * ({@code <page>-att/<file>-dir}), constructed exactly like the inherited lookup does.
	 */
	private File legacyVersionDir(String parentName, String fileName) throws ProviderException {
		return new File(findPageDir(parentName), mangleName(fileName + ATTDIR_EXTENSION));
	}

	// --- write path ----------------------------------------------------------

	@Override
	public void putAttachmentData(Attachment attachment, InputStream data) throws ProviderException, IOException {
		GitWikiRepository repository = repository();
		File attFile = attachmentFile(repository, attachment);
		String relPath = attachmentPath(attachment);
		byte[] bytes = data.readAllBytes();
		// bracket the file write and its commit, like the page provider's save (no sweep may interleave)
		try {
			repository.withRepositoryLock(() -> {
				putAttachmentDataLocked(attachment, bytes, attFile, relPath, repository);
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
										 GitWikiRepository repository) throws ProviderException {
		File dir = attFile.getParentFile();
		if (!dir.exists() && !dir.mkdirs()) {
			throw new ProviderException("Could not create attachment directory " + dir.getAbsolutePath());
		}
		// git creates no commit if no byte changed, and we also do not rewrite the file
		if (contentEquals(attFile, bytes)) {
			return;
		}
		writeFile(attachment, bytes, attFile);
		attachment.setSize(attFile.length());

		String user = attachment.getAuthor();
		// attachments may always be new files; the registry stages the git index alongside its bookkeeping
		if (!backend().batchRegistry().stage(user, relPath, true)) {
			CommitUserData userData = context().userData(user, message(attachment));
			String commitHash = repository.commitFile(attFile, relPath, userData);
			if (commitHash != null) {
				fireEvent(GitVersioningWikiEvent.UPDATE, attachment, commitHash, repository);
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
		GitWikiRepository repository = repository();
		File attFile = attachmentFile(repository, attachment);
		if (!attFile.exists()) {
			if (legacyVersionDir(attachment.getParentName(), attachment.getFileName()).exists()) {
				return super.getAttachmentData(attachment);
			}
			throw new ProviderException("Attachment file " + attachment.getFileName() + " does not exist");
		}
		String relPath = attachmentPath(attachment);
		int version = attachment.getVersion();
		if (version == WikiProvider.LATEST_VERSION || repository.isIgnored(relPath)) {
			return new FileInputStream(attFile);
		}
		InputStream data = repository.bytesAtVersion(relPath, version);
		if (data == null) {
			throw new ProviderException("Attachment " + attachment.getFileName() + " has no version " + version);
		}
		return data;
	}

	@Override
	public List<Attachment> listAttachments(Page page) throws ProviderException {
		GitWikiRepository repository = repository();
		File dir = attachmentDir(repository, page.getName());
		if (!dir.exists()) {
			return Collections.emptyList();
		}
		List<Attachment> result = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		File[] files = dir.listFiles(file -> !file.isHidden() && !file.isDirectory());
		if (files != null && files.length > 0) {
			for (Attachment attachment : latestOfEach(page, files, repository)) {
				result.add(attachment);
				seen.add(attachment.getFileName());
			}
		}
		// legacy version directories (<file>-dir); a flat file of the same name takes precedence
		for (Attachment legacy : super.listAttachments(page)) {
			if (seen.add(legacy.getFileName())) {
				result.add(legacy);
			}
		}
		return result;
	}

	/**
	 * The latest version of each of the given attachment files. Ignore state, history and sizes are looked up for the
	 * whole directory at once, so listing a page's attachments costs a fixed handful of git calls instead of one per
	 * attachment.
	 */
	private List<Attachment> latestOfEach(Page page, File[] files, GitWikiRepository repository) throws ProviderException {
		List<Attachment> attachments = new ArrayList<>(files.length);
		List<String> relPaths = new ArrayList<>(files.length);
		for (File file : files) {
			Attachment attachment = new org.apache.wiki.attachment.Attachment(m_engine, page.getName(),
					JSPUtils.unmangleName(file.getName()));
			attachment.setVersion(WikiProvider.LATEST_VERSION);
			attachments.add(attachment);
			relPaths.add(attachmentPath(attachment));
		}
		// one index snapshot for the whole directory, so every attachment is read at the same HEAD
		Map<String, List<GitFileRevision>> revisionsByFile = repository.revisionsByFile();
		Set<String> ignored = repository.ignoredPaths(relPaths);

		List<GitFileAtCommit> sizeRequest = new ArrayList<>(files.length);
		for (int i = 0; i < files.length; i++) {
			List<GitFileRevision> revisions = revisionsByFile.get(relPaths.get(i));
			if (!ignored.contains(relPaths.get(i)) && revisions != null && !revisions.isEmpty()) {
				sizeRequest.add(new GitFileAtCommit(revisions.get(0).commitHash(), relPaths.get(i)));
			}
		}
		Map<GitFileAtCommit, Long> sizes = repository.fileSizesAt(sizeRequest);

		List<Attachment> result = new ArrayList<>(files.length);
		for (int i = 0; i < files.length; i++) {
			String relPath = relPaths.get(i);
			if (!attachmentFile(repository, attachments.get(i)).exists()) {
				// the name does not map back to the file it was read from, so the inherited lookup decides, which is
				// what serves the legacy layout and answers null for a file it does not recognize either
				Attachment inherited = getAttachmentInfo(page, attachments.get(i).getFileName(),
						WikiProvider.LATEST_VERSION);
				if (inherited != null) {
					result.add(inherited);
				}
				continue;
			}
			List<GitFileRevision> revisions = revisionsByFile.get(relPath);
			if (ignored.contains(relPath) || revisions == null || revisions.isEmpty()) {
				// git-ignored, or written but not yet committed, both served from the filesystem
				result.add(filesystemAttachment(attachments.get(i), files[i]));
				continue;
			}
			GitFileRevision latest = revisions.get(0);
			Long size = sizes.get(new GitFileAtCommit(latest.commitHash(), relPath));
			result.add(fromRevision(attachments.get(i), revisions.size(), latest, size == null ? -1 : size));
		}
		return result;
	}

	@Override
	public Attachment getAttachmentInfo(Page page, String name, int version) throws ProviderException {
		GitWikiRepository repository = repository();
		Attachment att = new org.apache.wiki.attachment.Attachment(m_engine, page.getName(), name);
		att.setVersion(version);
		File attFile = attachmentFile(repository, att);
		if (!attFile.exists()) {
			// legacy layout: the inherited lookup serves <page>-att/<file>-dir content, and returns null if absent
			return super.getAttachmentInfo(page, name, version);
		}
		String relPath = attachmentPath(att);

		// git-ignored attachments: serve metadata from the filesystem (single version)
		if (repository.isIgnored(relPath)) {
			return filesystemAttachment(att, attFile);
		}

		// the eager index covers attachment paths too, so this costs no git call per attachment
		List<GitFileRevision> revisions = repository.index().revisionsNewestFirst(relPath);
		if (revisions.isEmpty()) {
			// written but not yet committed (e.g. staged in an open batch)
			return filesystemAttachment(att, attFile);
		}
		int count = revisions.size();
		if (version == WikiProvider.LATEST_VERSION) {
			return fromRevision(att, count, revisions.get(0), relPath, repository);
		}
		if (version < 1 || version > count) {
			return null;
		}
		// newest-first list: oldest-first version v is at index count - v
		return fromRevision(att, version, revisions.get(count - version), relPath, repository);
	}

	/**
	 * Single-attachment form of {@link #fromRevision(Attachment, int, GitFileRevision, long)}, which fetches the one
	 * size it needs.
	 */
	private Attachment fromRevision(Attachment attachment, int version, GitFileRevision revision, String relPath,
									GitWikiRepository repository) {
		return fromRevision(attachment, version, revision, repository.fileSizeAt(revision.commitHash(), relPath));
	}

	private Attachment filesystemAttachment(Attachment attachment, File attFile) {
		attachment.setVersion(1);
		attachment.setSize(attFile.length());
		attachment.setLastModified(new Date(attFile.lastModified()));
		return attachment;
	}

	/**
	 * Builds the attachment metadata from an index revision. Author/time/message come from the index (free after the
	 * one walk); the size is the one the caller looked up, since the index walk does not report sizes.
	 */
	private Attachment fromRevision(Attachment attachment, int version, GitFileRevision revision, long size) {
		Attachment result = new org.apache.wiki.attachment.Attachment(m_engine, attachment.getParentName(), attachment.getFileName());
		result.setCacheable(false);
		result.setAuthor(revision.userData().user);
		result.setVersion(version);
		result.setAttribute(WikiPage.CHANGENOTE, revision.message());
		result.setSize(size);
		result.setLastModified(Date.from(Instant.ofEpochSecond(revision.timeSeconds())));
		return result;
	}

	/**
	 * Newest version first, matching the {@link BasicAttachmentProvider} contract (the archived multi-wiki provider
	 * returned oldest-first, a deliberate divergence-fix here).
	 */
	@Override
	public List<Attachment> getVersionHistory(Attachment attachment) {
		try {
			GitWikiRepository repository = repository();
			File attFile = attachmentFile(repository, attachment);
			if (!attFile.exists()) {
				if (legacyVersionDir(attachment.getParentName(), attachment.getFileName()).exists()) {
					return super.getVersionHistory(attachment);
				}
				return Collections.emptyList();
			}
			String relPath = attachmentPath(attachment);
			if (repository.isIgnored(relPath)) {
				attachment.setVersion(1);
				return Collections.singletonList(attachment);
			}
			List<GitFileRevision> revisions = repository.index().revisionsNewestFirst(relPath);
			int count = revisions.size();
			// one bulk size lookup for the whole history, a size per version would be a git call per version
			List<GitFileAtCommit> sizeRequest = new ArrayList<>(count);
			for (GitFileRevision revision : revisions) {
				sizeRequest.add(new GitFileAtCommit(revision.commitHash(), relPath));
			}
			Map<GitFileAtCommit, Long> sizes = repository.fileSizesAt(sizeRequest);
			List<Attachment> result = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				GitFileRevision revision = revisions.get(i);
				Long size = sizes.get(new GitFileAtCommit(revision.commitHash(), relPath));
				result.add(fromRevision(attachment, count - i, revision, size == null ? -1 : size));
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
		GitWikiRepository repository = repository();
		File attFile = attachmentFile(repository, attachment);
		if (!attFile.exists()) {
			deleteLegacyAttachment(attachment, repository);
			return;
		}
		// disk deletion and commit happen inside commitDelete under the repository lock, so no sweep can interleave
		CommitUserData userData = context().userData(attachment.getAuthor(), message(attachment));
		File legacyDir = legacyVersionDir(attachment.getParentName(), attachment.getFileName());
		String commitHash = legacyDir.exists()
				? deleteBothLayouts(attachment, repository, attFile, legacyDir, userData)
				: repository.commitDelete(attFile, attachmentPath(attachment), userData);
		if (commitHash != null) {
			fireEvent(GitVersioningWikiEvent.DELETE, attachment, commitHash, repository);
		}
	}

	/**
	 * Deletes an attachment that exists in both layouts. Saving over a legacy attachment leaves its version directory
	 * in place next to the new flat file, and every read path falls back to that directory once the flat file is gone,
	 * so removing only the flat file would serve the pre-git content again.
	 */
	private String deleteBothLayouts(Attachment attachment, GitWikiRepository repository, File attFile, File legacyDir,
									 CommitUserData userData) throws ProviderException {
		try {
			return repository.withRepositoryLock(() -> {
				List<String> relPaths = new ArrayList<>();
				if (!repository.isIgnored(attachmentPath(attachment))) {
					relPaths.add(attachmentPath(attachment));
				}
				if (!attFile.delete()) {
					LOGGER.warn("Failed to delete attachment file on disk: {}", attFile.getAbsolutePath());
				}
				relPaths.addAll(deleteLegacyVersionDir(repository, legacyDir));
				return repository.commitRemovedPaths(relPaths, userData);
			});
		}
		catch (ProviderException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ProviderException("Could not delete attachment '" + attachmentPath(attachment)
					+ "': " + e.getMessage());
		}
	}

	/**
	 * Deletes the files of a legacy version directory from disk and returns their repo-relative paths.
	 */
	private static List<String> deleteLegacyVersionDir(GitWikiRepository repository, File legacyDir) throws IOException {
		Path repoRoot = new File(repository.path()).toPath();
		List<String> relPaths = new ArrayList<>();
		try (Stream<Path> walk = Files.walk(legacyDir.toPath())) {
			for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
				if (Files.isRegularFile(path)) {
					relPaths.add(repoRoot.relativize(path).toString().replace(File.separatorChar, '/'));
				}
				Files.delete(path);
			}
		}
		return relPaths;
	}

	/**
	 * Deletes a legacy-layout attachment (its {@code <page>-att/<file>-dir} version directory) and commits the
	 * removal. A silent no-op if the attachment does not exist in either layout.
	 */
	private void deleteLegacyAttachment(Attachment attachment, GitWikiRepository repository) throws ProviderException {
		File legacyDir = legacyVersionDir(attachment.getParentName(), attachment.getFileName());
		if (!legacyDir.exists()) {
			LOGGER.debug("Attachment {} was to be deleted but does not exist.", attachmentPath(attachment));
			return;
		}
		CommitUserData userData = context().userData(attachment.getAuthor(), message(attachment));
		String commitHash;
		try {
			// disk deletion and commit share one lock bracket, like the flat delete path
			commitHash = repository.withRepositoryLock(
					() -> repository.commitRemovedPaths(deleteLegacyVersionDir(repository, legacyDir), userData));
		}
		catch (ProviderException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ProviderException("Could not delete legacy attachment '" + attachmentPath(attachment)
					+ "': " + e.getMessage());
		}
		if (commitHash != null) {
			fireEvent(GitVersioningWikiEvent.DELETE, attachment, commitHash, repository);
		}
	}

	@Override
	public void deleteVersion(Attachment attachment) {
		// Not supported by git-backed repository. The page provider's deleteVersion throws, but the AttachmentProvider
		// contract here is a no-op; revert is the supported undo path.
		LOGGER.debug("deleteVersion is a no-op for git-backed attachments: {}", attachment.getFileName());
	}

	@Override
	public void moveAttachmentsForPage(Page oldParent, String newParent) throws ProviderException {
		GitWikiRepository repository = repository();
		String user = oldParent.getAuthor();
		List<String> eventPages = new ArrayList<>();
		String commitHash;
		// bracket the directory move and its commit (or staging), so no concurrent sweep can commit the half-done
		// move as a reconciliation commit with the wrong author
		try {
			commitHash = repository.withRepositoryLock(() -> {
				File oldDir = attachmentDir(repository, oldParent.getName());
				File newDir = attachmentDir(repository, newParent);
				if (!oldDir.isDirectory()) {
					return null;
				}
				List<String> movedFiles = attachmentFilesRelative(oldDir);
				GitWikiRepository.moveCaseSafe(oldDir, newDir);
				String oldRel = JSPUtils.getAttachmentDir(oldParent.getName());
				String newRel = JSPUtils.getAttachmentDir(newParent);
				List<String> oldPaths = new ArrayList<>();
				List<String> newPaths = new ArrayList<>();
				Set<String> movedAttachments = new LinkedHashSet<>();
				for (String movedFile : movedFiles) {
					oldPaths.add(oldRel + "/" + movedFile);
					newPaths.add(newRel + "/" + movedFile);
					movedAttachments.add(attachmentNameOf(movedFile));
				}
				for (String attachmentName : movedAttachments) {
					eventPages.add(oldParent.getName() + "/" + attachmentName);
					eventPages.add(newParent + "/" + attachmentName);
				}
				// the moved-in files are untracked (newFiles = true stages them in the git index, a pathspec commit
				// cannot pick up untracked files); the moved-away paths are tracked deletions and need no staging
				GitCommitBatchRegistry registry = backend().batchRegistry();
				if (registry.stage(user, oldPaths, false)) {
					registry.stage(user, newPaths, true);
					return null;
				}
				String comment = context().commentStrategy().getComment(oldParent,
						"move attachments from " + oldParent.getName() + " to " + newParent);
				return repository.commitMovedPaths(oldPaths, newPaths, context().userData(user, comment));
			});
		}
		catch (ProviderException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ProviderException("Can't move attachments from " + oldParent.getName() + " to " + newParent
					+ ": " + e.getMessage());
		}
		if (commitHash != null) {
			context().fireCommitted(this, GitVersioningWikiEvent.MOVED, user, eventPages, commitHash, repository.path());
		}
	}

	/**
	 * The files of an attachment directory as paths relative to it. The legacy layout keeps the versions of an
	 * attachment inside a {@code <file>-dir} directory, and staging a directory path removes nothing from the index,
	 * so a move has to name the files themselves.
	 */
	private static List<String> attachmentFilesRelative(File attachmentDir) throws IOException {
		Path root = attachmentDir.toPath();
		try (Stream<Path> walk = Files.walk(root)) {
			return walk.filter(Files::isRegularFile)
					.map(path -> root.relativize(path).toString().replace(File.separatorChar, '/'))
					.sorted()
					.toList();
		}
	}

	/**
	 * The attachment a moved file belongs to, which is the file itself in the flat layout and the owner of the
	 * version directory in the legacy one.
	 */
	private static String attachmentNameOf(String relativePath) {
		String name = JSPUtils.unmangleName(relativePath.split("/", 2)[0]);
		return name.endsWith(ATTDIR_EXTENSION) ? name.substring(0, name.length() - ATTDIR_EXTENSION.length()) : name;
	}

	/**
	 * Fires both commit notifications (see {@link WikiGitContext#fireCommitted}) with the JSPWiki attachment naming
	 * convention {@code <parent>/<file>} as the page name.
	 */
	private void fireEvent(int type, Attachment attachment, String commitHash, GitWikiRepository repository) throws ProviderException {
		String page = attachment.getParentName() + "/" + attachment.getFileName();
		context().fireCommitted(this, type, attachment.getAuthor(), List.of(page), commitHash, repository.path());
	}
}
