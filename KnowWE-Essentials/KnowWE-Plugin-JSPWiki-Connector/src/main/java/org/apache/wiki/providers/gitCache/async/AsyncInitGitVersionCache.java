package org.apache.wiki.providers.gitCache.async;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.providers.gitCache.GitVersionCache;
import org.apache.wiki.providers.gitCache.complete.CompleteGitVersionCache;
import org.apache.wiki.providers.gitCache.items.GitCachedAttachment;
import org.apache.wiki.providers.gitCache.items.GitCachedWikiPage;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This version of the CompleteGitVersionCache does the reading of the git log and the initialization of the cache
 * asyncronously in the background (distinct thread) while the wiki can already start up using the local files already
 * in the git folder.
 */
public class AsyncInitGitVersionCache extends CompleteGitVersionCache implements GitVersionCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompleteGitVersionCache.class);

	private static final String ATTACHMENT_FOLDER_SUFFIX = "-att";
	private final Map<String, WikiPage> wikiPageCache = new HashMap<>();

	private final List<DelayedRequest> delayedRequests = new ArrayList<>();

	public AsyncInitGitVersionCache(Engine engine, Repository repository, IgnoreNode ignoreNode) {
		super(engine, repository, ignoreNode);
	}

	@Override
	public void clearAndReinitialize() {
		// here initialization is started asyncronous
		new Thread(() -> {
			try {
				AsyncInitGitVersionCache.super.clearAndReinitialize();
				AsyncInitGitVersionCache.this.executeDelayedRequests();
			}
			catch (IOException e) {
				LOGGER.error("Exception during ansync reading of git history to GitCache", e);
				throw new RuntimeException(e);
			}
		}).start();
	}

	private void executeDelayedRequests() {
		this.delayedRequests.forEach(request -> request.executeRequest(this));
		this.delayedRequests.clear();
	}

	private File getFileForAttachment(String filename, String parentPageName) {
		return new File(baseDirPathString + File.separator + parentPageName.replace(" ", "+") + ATTACHMENT_FOLDER_SUFFIX, filename);
	}

	@Override
	public Attachment getAttachmentLatest(String filename, String parentPageName) {
		return createAttachment(parentPageName, getFileForAttachment(filename, parentPageName), filename);
	}

	@Override
	public List<Page> getPageHistory(String pageName) {
		if (isInitialized) {
			return super.getPageHistory(pageName);
		}
		else {
			// we do not have any while initialization is still running...
			return null;
		}
	}

	@Override
	public void setPageHistory(String pageName, List<Page> page) {
		if (isInitialized) {
			super.setPageHistory(pageName, page);
			// TODO: implement ?? Karsten: why not simply delegate to super
		}
		else {
			// we do not have any while initialization is still running...
			this.delayedRequests.add(new SetPageHistoryDelayedRequest(pageName, page));
//           throw new IllegalStateException("Cache not yet available..");
		}
	}

	@Override
	public @NotNull WikiPage createWikiPageLatest(String pageName) {
		if (wikiPageCache.containsKey(pageName)) return wikiPageCache.get(pageName);
		GitCachedWikiPage page = new GitCachedWikiPage(engine, pageName, this);
		page.setSize(getObjectSize(pageName));
		Date date = new Date(getLastModified(pageName + ".txt"));
		page.setLastModified(date);
		wikiPageCache.put(pageName, page);
		return page;
	}

	@Override
	public List<Attachment> getAllAttachmentsLatest() {
		// here we just refer to the attachment files lying in the wiki base folder
		List<Attachment> result = new ArrayList<>();
		String wikiContentDirectory = this.engine.getWikiProperties().getProperty("var.basedir");
		File wikiContentDirectoryFile = new File(wikiContentDirectory);
		Collection<File> folders = FileUtils.listFilesAndDirs(wikiContentDirectoryFile, FileFilterUtils.falseFileFilter(), new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.getName().equals(".git")) return false;
				return true;
			}

			@Override
			public boolean accept(File file, String s) {
				if (file.getName().equals(".git")) return false;
				return true;
			}
		});
		// we need to exclude  the root folder, otherwise we include all the actual wiki pages...
		folders.remove(wikiContentDirectoryFile);

		folders.forEach(folder -> {
			String folderName = folder.getName();
			String parentWikiPage = folderName.substring(0, folderName.length() - ATTACHMENT_FOLDER_SUFFIX.length());

			File[] attachmentFilesToThisPage = folder.listFiles();
			Arrays.stream(attachmentFilesToThisPage).forEach(file -> {
				String attachmentFilename = file.getName();
				Attachment att = createAttachment(parentWikiPage, file, attachmentFilename);
				result.add(att);
			});
		});
		return result;
	}

	@NotNull
	private Attachment createAttachment(String parentWikiPage, File file, String attachmentFilename) {
		Attachment att = new GitCachedAttachment(this.engine, parentWikiPage, attachmentFilename, this);
		// we do not set the version, as we do not know it (yet)
		att.setSize(file.length());
		long lastModifiedL = file.lastModified();
		att.setLastModified(new Date(lastModifiedL));
		return att;
	}

	@Override
	public void shutdown() {
		if (!isInitialized && !this.delayedRequests.isEmpty()) {
			throw new IllegalStateException("Cache not yet available, current behaviour may have resulted in an inconsistent state as requests could not have been handled correctly!");
		}
		this.executeDelayedRequests();
	}
}
