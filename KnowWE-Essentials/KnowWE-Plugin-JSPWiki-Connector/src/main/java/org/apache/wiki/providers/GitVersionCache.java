package org.apache.wiki.providers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.jetbrains.annotations.NotNull;

import static org.apache.wiki.WikiProvider.LATEST_VERSION;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 12.12.19
 */
public class GitVersionCache {

	private final WikiEngine engine;
	private final Repository repository;
	private static final Logger log = Logger.getLogger(GitVersionCache.class);

	private final Map<String, List<GitCacheItem>> pageRevisionCache;
	private final Map<String, List<GitCacheItem>> attachmentRevisionCache;
	private final Map<String, List<CacheCommand>> cacheCommands;

	public GitVersionCache(WikiEngine engine, Repository repository) {
		this.engine = engine;
		this.repository = repository;
		pageRevisionCache = new TreeMap<>();
		attachmentRevisionCache = new TreeMap<>();
		cacheCommands = new HashMap<>();
	}

	void initializeCache() throws IOException {
		StopWatch sw = new StopWatch();
		sw.start();
		log.debug("Getting all git revisions for cache...");

		final ObjectReader objectReader = this.repository.newObjectReader();
		final CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
		final CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
		final DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
		diffFormatter.setRepository(this.repository);
		final ObjectId ref = this.repository.resolve(Constants.HEAD);
		final RevWalk revWalk = new RevWalk(this.repository);
		if(ref == null) return;
		revWalk.sort(RevSort.REVERSE);
		revWalk.markStart(revWalk.parseCommit(ref));
		revWalk.setRevFilter(RevFilter.NO_MERGES);
		RevCommit commit;
		while ((commit = revWalk.next()) != null) {
			log.debug(""+commit.getParentCount()+" " + commit.getFullMessage());
			final RevCommit[] parents = commit.getParents();
			RevTree tree = commit.getTree();
			if (parents.length > 0) {
				oldTreeParser.reset(objectReader, commit.getParent(0)
						.getTree());
				newTreeParser.reset(objectReader, tree);
				List<DiffEntry> diffs = diffFormatter.scan(oldTreeParser, newTreeParser);
				RenameDetector rd = new RenameDetector(repository);
				rd.addAll(diffs);
				diffs = rd.compute();
				for (final DiffEntry diff : diffs) {
					String path;
					if (diff.getChangeType() == DiffEntry.ChangeType.MODIFY) {
						path = diff.getOldPath();
						if (path != null) {
							mapCommit(commit, path);
						}
					}
					else if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
						path = diff.getNewPath();
						if (path != null) {
							mapCommit(commit, path);
						}
					}
					else if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
						mapDelete(commit, diff.getOldPath());
					}
					else if (diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
						mapMove(commit, diff.getOldPath(), diff.getNewPath());
					}
				}
			}
			else {
				final TreeWalk tw = new TreeWalk(this.repository);
				tw.reset(tree);
				tw.setRecursive(true);
				while (tw.next()) {
					mapCommit(commit, tw.getPathString());
				}
			}
		}
		sw.stop();
		log.info("Git version cache successful initiated in " + sw);
	}

	void mapDelete(RevCommit commit, String path) throws IOException {
		mapCommit(commit, path, true);
	}

	void mapMove(RevCommit commit, String oldPath, String newPath) throws IOException {
		log.debug("move " + oldPath + " -> " + newPath);
		String key;
		String newKey;
		Map<String, List<GitCacheItem>> cache;
		if (oldPath.contains("/")) {
			cache = this.attachmentRevisionCache;
			String[] split = oldPath.split("/");
			String parentName = TextUtil.urlDecodeUTF8(split[0])
					.replace(GitVersioningAttachmentProvider.DIR_EXTENSION, "");
			String attachmentName = TextUtil.urlDecodeUTF8(split[1]);
			key = parentName + "/" + attachmentName;
			String newParentName = TextUtil.urlDecodeUTF8(newPath.substring(0, newPath.indexOf("/")))
					.replace(GitVersioningAttachmentProvider.DIR_EXTENSION, "");
			newKey = newParentName + "/" + attachmentName;
		}
		else {
			cache = this.pageRevisionCache;
			key = TextUtil.urlDecodeUTF8(oldPath.replace(GitVersioningFileProvider.FILE_EXT, ""));
			newKey = TextUtil.urlDecodeUTF8(newPath.replace(GitVersioningFileProvider.FILE_EXT, ""));
		}
		if(cache.containsKey(key)) {
			List<GitCacheItem> gitCacheItems = cache.get(key);
			cache.remove(key);
			cache.put(newKey, gitCacheItems);
		}
		mapCommit(commit, newPath);
	}

	void mapCommit(RevCommit commit, String path) throws IOException {
		mapCommit(commit, path, false);
	}

	void mapCommit(RevCommit commit, String path, boolean delete) throws IOException {
		log.debug("commit " + (delete ? "delete " : "") + path);
		Map<String, List<GitCacheItem>> cache;
		GitCacheItem toCache;
		String key;
		long size = delete ? 0L : getObjectSize(commit, path);
		if (path.contains("/")) {
			cache = this.attachmentRevisionCache;
			String[] split = path.split("/");
			String parentName = TextUtil.urlDecodeUTF8(split[0])
					.replace(GitVersioningAttachmentProvider.DIR_EXTENSION, "");
			String attachmentName = TextUtil.urlDecodeUTF8(split[1]);
			key = parentName + "/" + attachmentName;
			toCache = new AttachmentCacheItem(parentName, attachmentName, commit.getFullMessage(),
					commit.getAuthorIdent().getName(),
					new Date(1000L * commit.getCommitTime()), size, delete, commit.getId());
		}
		else {
			cache = this.pageRevisionCache;
			key = TextUtil.urlDecodeUTF8(path.replace(GitVersioningFileProvider.FILE_EXT, ""));
			toCache = new PageCacheItem(key, commit.getFullMessage(),
					commit.getAuthorIdent().getName(),
					new Date(1000L * commit.getCommitTime()), size, delete, commit.getId());
		}

		putInCache(cache, toCache, key);
	}

	private long getObjectSize(RevCommit version, String path) throws IOException {
		long ret;
		ObjectId objectId = null;
		try (TreeWalk treeWalkDir = new TreeWalk(repository)) {
			treeWalkDir.reset(version.getTree());
			treeWalkDir.setFilter(PathFilter.create(path));
			treeWalkDir.setRecursive(true);
			//only the attachment directory
			if (treeWalkDir.next()) {
				objectId = treeWalkDir.getObjectId(0);
			}
		}
		if (objectId != null) {
			ObjectLoader loader = repository.open(objectId);
			ret = loader.getSize();
		}
		else {
			ret = 0;
		}
		return ret;
	}

	private void putInCache(Map<String, List<GitCacheItem>> cache, GitCacheItem toCache, String key) {
		if (cache.containsKey(key)) {
			List<GitCacheItem> items = cache.get(key);
			toCache.setVersion(items.size() + 1);
			items.add(toCache);
		}
		else {
			toCache.setVersion(1);
			List<GitCacheItem> items = new ArrayList<>();
			items.add(toCache);
			cache.put(key, items);
		}
	}

	public void addPageVersion(WikiPage page, String commitMsg, ObjectId id) {
		PageCacheItem item = new PageCacheItem(page.getName(), commitMsg, page.getAuthor(), new Date(), page.getSize(), id);
		putInCache(pageRevisionCache, item, page.getName());
	}

	public void addAttachmentVersion(Attachment att, String commitMsg, ObjectId id) {
		AttachmentCacheItem item = new AttachmentCacheItem(att.getParentName(), att.getFileName(), commitMsg,
				att.getAuthor(), new Date(), att.getSize(), id);
		String key = getAttachmentKey(att);
		putInCache(attachmentRevisionCache, item, key);
	}

	public AttachmentCacheItem getAttachment(Attachment att) {
		String key = getAttachmentKey(att);
		if (attachmentRevisionCache.containsKey(key)) {
			List<GitCacheItem> gitCacheItems = attachmentRevisionCache.get(key);
			if (att.getVersion() - 1 < gitCacheItems.size()) {
				if (att.getVersion() == LATEST_VERSION) {
					return (AttachmentCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
				}
				else if (att.getVersion() - 1 < gitCacheItems.size()) {
					return (AttachmentCacheItem) gitCacheItems.get(att.getVersion() - 1);
				}
			}
		}
		return null;
	}

	private String getAttachmentKey(Attachment att) {
		return att.getParentName() + "/" + att.getFileName();
	}

	public List<Attachment> getAllLatestAttachments() {
		Set<String> keys = this.attachmentRevisionCache.keySet();
		List<Attachment> ret = new ArrayList<>();
		for (String key : keys) {
			List<GitCacheItem> gitCacheItems = attachmentRevisionCache.get(key);
			AttachmentCacheItem gitCacheItem = (AttachmentCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
			Attachment att = createAttachment(gitCacheItem);
			ret.add(att);
		}
		return ret;
	}

	@NotNull
	private Attachment createAttachment(AttachmentCacheItem gitCacheItem) {
		Attachment att = new Attachment(this.engine, gitCacheItem.getParentName(), gitCacheItem.getAttachmentName());
		att.setVersion(gitCacheItem.getVersion());
		att.setAuthor(gitCacheItem.getAuthor());
		att.setSize(gitCacheItem.getSize());
		att.setLastModified(gitCacheItem.getDate());
		att.setAttribute(WikiPage.CHANGENOTE, gitCacheItem.getFullMessage());
		return att;
	}

	public List<Attachment> getAttachmentHistory(Attachment att) {
		String attachmentKey = getAttachmentKey(att);
		if (attachmentRevisionCache.containsKey(attachmentKey)) {
			List<Attachment> ret = new ArrayList<>();
			List<GitCacheItem> gitCacheItems = attachmentRevisionCache.get(attachmentKey);
			for (GitCacheItem item : gitCacheItems) {
				ret.add(createAttachment((AttachmentCacheItem) item));
			}
			return ret;
		}
		else {
			return null;
		}
	}

	public void moveAttachments(String oldParent, String newParent, File[] files, ObjectId id, String commitMsg, String author) {
		for (File file : files) {
			String oldKey = oldParent + "/" + file.getName();
			List<GitCacheItem> gitCacheItems = attachmentRevisionCache.get(oldKey);
			if (gitCacheItems != null) {
				attachmentRevisionCache.remove(oldKey);
				String newKey = newParent + "/" + file.getName();
				attachmentRevisionCache.put(newKey, gitCacheItems);
				AttachmentCacheItem lastVersion = (AttachmentCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
				AttachmentCacheItem newVersion = new AttachmentCacheItem(newParent, lastVersion.getAttachmentName(),
						commitMsg, author, new Date(), file.length(), id);
				newVersion.setVersion(lastVersion.getVersion() + 1);
				gitCacheItems.add(newVersion);
			}
		}
	}

	public List<WikiPage> getPageHistory(String pageName) {
		if (pageRevisionCache.containsKey(pageName)) {
			List<WikiPage> pages = new ArrayList<>();
			List<GitCacheItem> gitCacheItems = pageRevisionCache.get(pageName);
			for (GitCacheItem item : gitCacheItems) {
				WikiPage page = createWikiPage(pageName, item);
				pages.add(page);
			}
			return pages;
		}
		else {
			return null;
		}
	}

	@NotNull
	WikiPage createWikiPage(String pageName, GitCacheItem item) {
		WikiPage page = new WikiPage(engine, pageName);
		page.setAuthor(item.getAuthor());
		page.setLastModified(item.getDate());
		page.setVersion(item.getVersion());
		page.setSize(item.getSize());
		page.setAttribute(WikiPage.CHANGENOTE, item.getFullMessage());
		return page;
	}

	public PageCacheItem getPageVersion(String pageName, int version) {
		if (pageRevisionCache.containsKey(pageName)) {
			List<GitCacheItem> gitCacheItems = pageRevisionCache.get(pageName);
			if (version == -1) {
				return (PageCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
			}
			else if (version - 1 < gitCacheItems.size()) {
				return (PageCacheItem) gitCacheItems.get(version - 1);
			}
		}
		return null;
	}

	public void movePage(WikiPage from, String to, String message, ObjectId id) {
		List<GitCacheItem> gitCacheItems = pageRevisionCache.get(from.getName());
		if (gitCacheItems != null) {
			pageRevisionCache.remove(from.getName());
			pageRevisionCache.put(to, gitCacheItems);
			PageCacheItem lastVersion = (PageCacheItem) gitCacheItems.get(gitCacheItems.size() - 1);
			PageCacheItem newVersion = new PageCacheItem(to,
					message, from.getAuthor(), new Date(), from.getSize(), id);
			newVersion.setVersion(lastVersion.getVersion() + 1);
			gitCacheItems.add(newVersion);
		}
	}

	public void addCacheCommand(String user, CacheCommand command) {
		if (this.cacheCommands.containsKey(user)) {
			List<CacheCommand> cacheCommands = this.cacheCommands.get(user);
			if(command instanceof CacheCommand.MoveAttachment || command instanceof CacheCommand.MovePage){
				if(cacheCommands.contains(command)){
					int i = cacheCommands.indexOf(command);
					CacheCommand prevCommand = cacheCommands.get(i);
					cacheCommands.remove(command);
					command.page.setVersion(prevCommand.page.getVersion());
				}
				cacheCommands.add(command);
			}
			if(!cacheCommands.contains(command)){
				cacheCommands.add(command);
			}
		}
		else {
			List<CacheCommand> commands = new ArrayList<>();
			commands.add(command);
			this.cacheCommands.put(user, commands);
		}
	}

	public void executeCacheCommands(String user, String commitMsg, ObjectId id) {
		List<CacheCommand> cacheCommands = this.cacheCommands.get(user);
		if (cacheCommands != null) {
			for (CacheCommand cmd : cacheCommands) {
				String name;
				if(cmd.page instanceof Attachment) {
					name = getAttachmentKey((Attachment) cmd.page);
				} else {
					name = cmd.page.getName();
				}
				if(this.pageRevisionCache.containsKey(name)){
					List<GitCacheItem> gitCacheItems = this.pageRevisionCache.get(name);
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
					this.addAttachmentVersion((Attachment) cmd.page, commitMsg, id);
				}
				else if (cmd instanceof CacheCommand.DeleteAttachmentVersion) {
					this.deleteAttachment((Attachment) cmd.page, commitMsg, id);
				}
				else if (cmd instanceof CacheCommand.MoveAttachment) {
					this.moveAttachments(cmd.page.getName(), ((CacheCommand.MoveAttachment) cmd).newParent,
							new File[] { ((CacheCommand.MoveAttachment) cmd).file }, id, commitMsg, user);
				}
			}
			this.cacheCommands.remove(user);
		}
	}

	void deletePage(WikiPage page, String commitMsg, ObjectId id) {
		PageCacheItem item = new PageCacheItem(page.getName(), commitMsg, page.getAuthor(), new Date(), page.getSize(), true, id);
		putInCache(pageRevisionCache, item, page.getName());
	}

	public void deleteAttachment(Attachment att, String commitMsg, ObjectId id) {
		AttachmentCacheItem item = new AttachmentCacheItem(att.getParentName(), att.getFileName(), commitMsg,
				att.getAuthor(), new Date(), att.getSize(), true, id);
		String key = getAttachmentKey(att);
		putInCache(attachmentRevisionCache, item, key);
	}

	public void reset(Attachment att) {
		String key = getAttachmentKey(att);
		attachmentRevisionCache.remove(key);
	}
	public void reset(WikiPage page) {
		pageRevisionCache.remove(page.getName());
	}
}
