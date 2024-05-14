package org.apache.wiki.providers.gitCache.adhoc;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.gitBridge.JspGitBridge;
import org.apache.wiki.providers.gitCache.GitVersionCache;
import org.apache.wiki.providers.gitCache.commands.CacheCommand;
import org.apache.wiki.providers.gitCache.items.AttachmentCacheItem;
import org.apache.wiki.providers.gitCache.items.PageCacheItem;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.NotNull;

public class AdhocGitCache implements GitVersionCache {
	private final Engine engine;
	private final Repository repository;
	private final IgnoreNode ignoreNode;
	private final JspGitBridge gitBridge;
	private final String baseDirPathString;

	public AdhocGitCache(Engine engine, Repository repository, IgnoreNode ignoreNode) {
		this.engine = engine;
		this.repository = repository;
		this.ignoreNode = ignoreNode;
		this.gitBridge = new JspGitBridge(engine);
		Properties wikiProperties = this.engine.getWikiProperties();
		 this.baseDirPathString = wikiProperties.getProperty("var.basedir");
	}


	@Override
	public void clearAndReinitialize() throws IOException {
		/**
		 * Nothing to Do as we dont cache anything
		 */
	}

	@Override
	public boolean isInitialized() {
		//always ready
		return true;
	}

	@Override
	public PageCacheItem getPageVersion(@NotNull String pageName, int version) {
		return null;
	}

	@Override
	public void addPageVersion(Page page, String commitMsg, ObjectId id) {
		/**
		 * Nothing to Do as we dont cache anything
		 */
	}

	@Override
	public void addAttachmentVersion(Attachment att, String commitMsg, ObjectId id) {
		/**
		 * Nothing to Do as we dont cache anything
		 */
	}

	@Override
	public AttachmentCacheItem getAttachmentCacheItem(Attachment att) {
		return null;
	}

	@Override
	public List<Attachment> getAllAttachmentsLatest() {
		return null;
	}

	@Override
	public Attachment getAttachmentLatest(String filename, String parentPageName) {
		return null;
	}

	@Override
	public void setPageHistory(String pageName, List<Page> page) {

	}

	@Override
	public WikiPage createWikiPageLatest(String pageName) {
//		WikiPage page = new WikiPage(engine, pageName);
//		page.setVersion(LATEST_VERSION);
//		page.setSize(getObjectSize(pageName));
//		Date date = new Date(getLastModified(pageName+".txt"));
//		page.setLastModified(date);
//		return page;
		return null;
	}

	protected long getLastModified(String filename)  {
		return new File(baseDirPathString, filename).lastModified();
	}

	public long getObjectSize(String filename)  {
		return new File(baseDirPathString, filename).length();
	}


	@Override
	public List<Attachment> getAttachmentHistory(Attachment att) {
		return null;
	}

	@Override
	public List<Page> getPageHistory(String pageName) {
		return null;
	}

	@Override
	public void deletePage(Page page, String commitMsg, ObjectId id) {

	}

	@Override
	public void deleteAttachment(Attachment att, String commitMsg, ObjectId id) {

	}

	@Override
	public void moveAttachments(String oldParent, String newParent, File[] files, ObjectId id, String commitMsg, String author) {

	}

	@Override
	public void movePage(Page from, String to, String message, ObjectId id) {

	}

	@Override
	public void addCacheCommand(String user, CacheCommand command) {
		/**
		 * Nothing to Do as we dont cache anything
		 */
	}

	@Override
	public void executeCacheCommands(String user, String commitMsg, ObjectId id) {
		/**
		 * Nothing to Do as we dont cache anything
		 */
	}

	@Override
	public void shutdown() {
		/**
		 * Nothing to do
		 */
	}
}
