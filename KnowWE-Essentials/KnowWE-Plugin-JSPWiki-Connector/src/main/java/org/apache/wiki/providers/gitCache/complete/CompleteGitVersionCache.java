package org.apache.wiki.providers.gitCache.complete;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.providers.gitCache.GitVersionCache;
import org.apache.wiki.providers.gitCache.GitVersionCacheData;
import org.apache.wiki.providers.gitCache.commands.CacheCommand;
import org.apache.wiki.providers.gitCache.items.AttachmentCacheItem;
import org.apache.wiki.providers.gitCache.items.GitCacheItem;
import org.apache.wiki.providers.gitCache.items.PageCacheItem;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.wiki.api.providers.WikiProvider.LATEST_VERSION;
import static org.apache.wiki.providers.gitCache.GitVersionCacheData.createAttachmentStoreKey;

/**
 * Contains the entire history of all wiki pages and attachments files as well as git commands.
 * This cache is used in the background both, the GitVersioningFileProvider and the GitVersioningAttachmentProvider
 *
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 12.12.19
 */
public class CompleteGitVersionCache implements GitVersionCache {

	public final Engine engine;
	public final Repository repository;
	private static final Logger LOGGER = LoggerFactory.getLogger(CompleteGitVersionCache.class);
	private final IgnoreNode ignoreNode;

	public GitVersionCacheData data;
	public boolean isInitialized = false;
	public String baseDirPathString;

	public CompleteGitVersionCache(Engine engine, Repository repository, IgnoreNode ignoreNode) {
		this.engine = engine;
		this.repository = repository;
		this.ignoreNode = ignoreNode;
		Properties wikiProperties = this.engine.getWikiProperties();
		baseDirPathString = wikiProperties.getProperty("var.basedir");
	}

	@Override
	public boolean isInitialized() {
		return isInitialized;
	}

	@Override
	public void clearAndReinitialize() throws IOException {
		this.data = new CompleteGitVersionCacheDataBuilder(engine, repository, ignoreNode).build();
		isInitialized = true;
	}

	@Override
	public void addPageVersion(Page page, String commitMsg, ObjectId id) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache addPageVersion() is called, while cache is not yet initialized");
			return;
		}
		PageCacheItem item = new PageCacheItem(page.getName(), commitMsg, page.getAuthor(), new Date(), page.getSize(), id);
		GitVersionCacheData.putInCache(data.pageRevisionCache, item, page.getName());
	}

	@Override
	public void addAttachmentVersion(Attachment att, String commitMsg, ObjectId id) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache addAttachmentVersion() is called, while cache is not yet initialized");
			return;
		}
		AttachmentCacheItem item = new AttachmentCacheItem(att.getParentName(), att.getFileName(), commitMsg,
				att.getAuthor(), new Date(), att.getSize(), id);
		String key = getAttachmentKey(att);
		GitVersionCacheData.putInCache(data.attachmentRevisionCache, item, key);
	}

	@Override
	public AttachmentCacheItem getAttachmentCacheItem(Attachment att) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache getAttachmentItem() is called, while cache is not yet initialized");
			return null;
		}
		String key = getAttachmentKey(att);
		if (data.attachmentRevisionCache.containsKey(key)) {
			List<GitCacheItem> gitCacheItems = data.attachmentRevisionCache.get(key);
			if (att.getVersion() - 1 < gitCacheItems.size()) {
				if (att.getVersion() == LATEST_VERSION) {
					return (AttachmentCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
				}
				else if (att.getVersion() - 1 < gitCacheItems.size()) {
					return (AttachmentCacheItem) gitCacheItems.get(att.getVersion() - 1);
				}
			}
		}
		LOGGER.error("Git Cache is completely initialized but misses!");
		return null;
	}

	private static String getAttachmentKey(Attachment att) {
		return att.getParentName().replace("+", " ") + "/" + att.getFileName();
	}

	@Override
	public List<Attachment> getAllAttachmentsLatest() {
		Set<String> keys = data.attachmentRevisionCache.keySet();
		List<Attachment> ret = new ArrayList<>();
		for (String key : keys) {
			List<GitCacheItem> gitCacheItems = data.attachmentRevisionCache.get(key);
			AttachmentCacheItem gitCacheItem = (AttachmentCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
			Attachment att = createAttachment(gitCacheItem);
			ret.add(att);
		}
		return ret;
	}

	@NotNull
	private Attachment createAttachment(AttachmentCacheItem gitCacheItem) {
		Attachment att = new org.apache.wiki.attachment.Attachment(this.engine, gitCacheItem.getParentName(), gitCacheItem.getAttachmentName());
		att.setVersion(gitCacheItem.getVersion());
		att.setAuthor(gitCacheItem.getAuthor());
		att.setSize(gitCacheItem.getSize());
		att.setLastModified(gitCacheItem.getDate());
		att.setAttribute(WikiPage.CHANGENOTE, gitCacheItem.getFullMessage());
		return att;
	}

	@Override
	public List<Attachment> getAttachmentHistory(Attachment att) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache getAttachmentHistory() is called, while cache is not yet initialized");
			return null;
		}
		String attachmentKey = getAttachmentKey(att);
		if (data.attachmentRevisionCache.containsKey(attachmentKey)) {
			List<Attachment> ret = new ArrayList<>();
			List<GitCacheItem> gitCacheItems = data.attachmentRevisionCache.get(attachmentKey);
			for (GitCacheItem item : gitCacheItems) {
				ret.add(createAttachment((AttachmentCacheItem) item));
			}
			return ret;
		}
		else {
			LOGGER.error("Git Cache is completely initialized but getAttachmentHistory misses!");
			return null;
		}
	}

	@Override
	public void moveAttachments(String oldParent, String newParent, File[] files, ObjectId id, String commitMsg, String author) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache moveAttachements() is called, while cache is not yet initialized");
			return;
		}
		for (File file : files) {
			String oldKey = createAttachmentStoreKey(oldParent, file.getName());
			List<GitCacheItem> gitCacheItems = data.attachmentRevisionCache.get(oldKey);
			if (gitCacheItems != null) {
				data.attachmentRevisionCache.remove(oldKey);
				String newKey = createAttachmentStoreKey(newParent, file.getName());
				data.attachmentRevisionCache.put(newKey, gitCacheItems);
				AttachmentCacheItem lastVersion = (AttachmentCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
				AttachmentCacheItem newVersion = new AttachmentCacheItem(newParent, lastVersion.getAttachmentName(),
						commitMsg, author, new Date(), file.length(), id);
				newVersion.setVersion(lastVersion.getVersion() + 1);
				gitCacheItems.add(newVersion);
			}
		}
	}

	@Override
	public List<Page> getPageHistory(String pageName) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache getPageHistory() is called, while cache is not yet initialized");
			return null;
		}
		if (data.pageRevisionCache.containsKey(pageName)) {
			List<Page> pages = new ArrayList<>();
			List<GitCacheItem> gitCacheItems = data.pageRevisionCache.get(pageName);
			for (GitCacheItem item : gitCacheItems) {
				WikiPage page = createWikiPage(pageName, item);
				pages.add(page);
			}
			return pages;
		}
		else {
			LOGGER.error("Git Cache is completely initialized but getPageHistory misses!");
			return null;
		}
	}

	@Override
	public @NotNull WikiPage createWikiPageLatest(String pageName) {

		List<GitCacheItem> gitCacheItems = this.data.pageRevisionCache.get(pageName);
		if (gitCacheItems == null) {
			gitCacheItems =this.data.pageRevisionCache.get(JSPUtils.unmangleName(pageName));
		}
		GitCacheItem gitCacheItem = gitCacheItems.get(gitCacheItems.size() - 1);

		WikiPage page = new WikiPage(engine, pageName);
		page.setVersion(gitCacheItem.getVersion());
		page.setSize(gitCacheItem.getSize());
//		Date date = new Date(getLastModified(pageName+".txt"));
		page.setLastModified(gitCacheItem.getDate());
		return page;
	}

	protected long getLastModified(String filename) {
		return new File(baseDirPathString, filename).lastModified();
	}

	public long getObjectSize(String filename) {
		return new File(baseDirPathString, filename).length();
	}

	@Override
	public Attachment getAttachmentLatest(String filename, String parentPageName) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache getAttachmentLatest() is called, while cache is not yet initialized");
			return null;
		}
		List<GitCacheItem> gitCacheItems = data.attachmentRevisionCache.get(createAttachmentStoreKey(parentPageName, filename));
		AttachmentCacheItem gitCacheItem = (AttachmentCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
		return createAttachment(gitCacheItem);
	}

	@Override
	public void setPageHistory(String pageName, List<Page> page) {
		this.data.pageRevisionCache.remove(pageName);
		for (Page pageVersion : page) {
			this.addPageVersion(pageVersion, null, null);
		}
		// TODO: implement
	}

	@NotNull
	private WikiPage createWikiPage(String pageName, @NotNull GitCacheItem item) {
		WikiPage page = new WikiPage(engine, pageName);
		page.setAuthor(item.getAuthor());
		page.setLastModified(item.getDate());
		page.setVersion(item.getVersion());
		page.setSize(item.getSize());
		page.setAttribute(WikiPage.CHANGENOTE, item.getFullMessage());
		return page;
	}

	@Override
	public PageCacheItem getPageVersion(@NotNull String pageName, int version) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache getPageVersion() is called, while cache is not yet initialized");
			return null;
		}
		if (data.pageRevisionCache.containsKey(pageName)) {
			List<GitCacheItem> gitCacheItems = data.pageRevisionCache.get(pageName);
			if (version == -1) {
				return (PageCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
			}
			else if (version - 1 < gitCacheItems.size()) {
				return (PageCacheItem) gitCacheItems.get(version - 1);
			}
		}
		return null;
	}

	@Override
	public void movePage(Page from, String to, String message, ObjectId id) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache movePage() is called, while cache is not yet initialized");
			return;
		}
		String author = from.getAuthor();
		List<GitCacheItem> gitCacheItems = data.pageRevisionCache.get(from.getName());
		if (gitCacheItems != null) {
			data.pageRevisionCache.remove(from.getName());
			data.pageRevisionCache.put(to, gitCacheItems);
			PageCacheItem lastVersion = (PageCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
			PageCacheItem newVersion = new PageCacheItem(to, message, author, new Date(), from.getSize(), id);
			newVersion.setVersion(lastVersion.getVersion() + 1);
			gitCacheItems.add(newVersion);
		}
	}

	@Override
	public void addCacheCommand(String user, CacheCommand command) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache addCacheCommand() is called, while cache is not yet initialized");
			return;
		}
		if (data.cacheCommands.containsKey(user)) {
			List<CacheCommand> cacheCommands = data.cacheCommands.get(user);
			if (command instanceof CacheCommand.MoveAttachment || command instanceof CacheCommand.MovePage) {
				if (cacheCommands.contains(command)) {
					int i = cacheCommands.indexOf(command);
					CacheCommand prevCommand = cacheCommands.get(i);
					cacheCommands.remove(command);
					command.page.setVersion(prevCommand.page.getVersion());
				}
				cacheCommands.add(command);
			}
			if (!cacheCommands.contains(command)) {
				cacheCommands.add(command);
			}
		}
		else {
			List<CacheCommand> commands = new ArrayList<>();
			commands.add(command);
			data.cacheCommands.put(user, commands);
		}
	}

	@Override
	public void executeCacheCommands(String user, String commitMsg, ObjectId id) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache executeCacheCommands() is called, while cache is not yet initialized");
			return;
		}
		List<CacheCommand> cacheCommands = data.cacheCommands.get(user);
		if (cacheCommands != null) {
			for (CacheCommand cmd : cacheCommands) {
				String name;
				if (cmd.page instanceof Attachment) {
					name = getAttachmentKey((Attachment) cmd.page);
				}
				else {
					name = cmd.page.getName();
				}
				if (data.pageRevisionCache.containsKey(name)) {
					List<GitCacheItem> gitCacheItems = data.pageRevisionCache.get(name);
					gitCacheItems.removeIf(item -> item.getId() == null);
				}
				if (cmd instanceof CacheCommand.AddPageVersion) {
					this.addPageVersion(cmd.page, commitMsg, id);
				}
				else if (cmd instanceof CacheCommand.DeletePageVersion) {
					this.deletePage(cmd.page, commitMsg, id);
				}
				else if (cmd instanceof CacheCommand.MovePage) {
					this.movePage(cmd.page, ((CacheCommand.MovePage) cmd).to, commitMsg, id);
				}
				else if (cmd instanceof CacheCommand.AddAttachmentVersion) {
					if (cmd.page instanceof Attachment) {
						this.addAttachmentVersion((Attachment) cmd.page, commitMsg, id);
					}
				}
				else if (cmd instanceof CacheCommand.DeleteAttachmentVersion) {
					if (cmd.page instanceof Attachment) {
						this.deleteAttachment((Attachment) cmd.page, commitMsg, id);
					}
				}
				else if (cmd instanceof CacheCommand.MoveAttachment) {
					this.moveAttachments(cmd.page.getName(), ((CacheCommand.MoveAttachment) cmd).newParent,
							new File[] { ((CacheCommand.MoveAttachment) cmd).file }, id, commitMsg, user);
				}
			}
			data.cacheCommands.remove(user);
		}
	}

	@Override
	public void shutdown() {
		/**
		 * DO NOTHING
		 */
	}

	@Override
	public void deletePage(Page page, String commitMsg, ObjectId id) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache deletePage() is called, while cache is not yet initialized");
			return;
		}
		PageCacheItem item = new PageCacheItem(page.getName(), commitMsg, page.getAuthor(), new Date(), page.getSize(), true, id);
		GitVersionCacheData.putInCache(data.pageRevisionCache, item, page.getName());
	}

	@Override
	public void deleteAttachment(Attachment att, String commitMsg, ObjectId id) {
		if (!isInitialized) {
			LOGGER.debug("Git Cache deleteAttachment() is called, while cache is not yet initialized");
			return;
		}
		AttachmentCacheItem item = new AttachmentCacheItem(att.getParentName(), att.getFileName(), commitMsg,
				att.getAuthor(), new Date(), att.getSize(), true, id);
		String key = getAttachmentKey(att);
		GitVersionCacheData.putInCache(data.attachmentRevisionCache, item, key);
	}

	/*
    public void reset(Attachment att) {
        String key = getAttachmentKey(att);
        data.attachmentRevisionCache.remove(key);
    }

     */
	public void reset(WikiPage page) {
		data.pageRevisionCache.remove(page.getName());
	}
}
