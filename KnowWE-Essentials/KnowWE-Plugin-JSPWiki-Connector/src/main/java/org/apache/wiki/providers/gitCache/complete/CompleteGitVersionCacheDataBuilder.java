package org.apache.wiki.providers.gitCache.complete;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.providers.GitVersioningAttachmentProvider;
import org.apache.wiki.providers.GitVersioningFileProvider;
import org.apache.wiki.providers.gitCache.GitVersionCacheData;
import org.apache.wiki.providers.gitCache.commands.CacheCommand;
import org.apache.wiki.providers.gitCache.items.AttachmentCacheItem;
import org.apache.wiki.providers.gitCache.items.GitCacheItem;
import org.apache.wiki.providers.gitCache.items.PageCacheItem;
import org.apache.wiki.util.TextUtil;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.ignore.IgnoreNode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.wiki.providers.gitCache.GitVersionCacheData.createAttachmentStoreKey;

/**
 * Builds up a GitVersionCacheData set by reading the entire git history from the git log.
 */
public class CompleteGitVersionCacheDataBuilder {

    private static final Logger log = LoggerFactory.getLogger(CompleteGitVersionCacheDataBuilder.class);

    private final IgnoreNode ignoreNode;
    private final Engine engine;
    private final Repository repository;

    private Map<String, List<GitCacheItem>> pageRevisionCache;
    private Map<String, List<GitCacheItem>> attachmentRevisionCache;
    private Map<String, List<CacheCommand>> cacheCommands;

    public CompleteGitVersionCacheDataBuilder(Engine engine, Repository repository, IgnoreNode ignoreNode) {
        this.ignoreNode = ignoreNode;
        this.engine = engine;
        this.repository = repository;
    }

    GitVersionCacheData build() throws IOException {
        pageRevisionCache = new TreeMap<>();
        attachmentRevisionCache = new TreeMap<>();
        cacheCommands = new HashMap<>();
        StopWatch sw = new StopWatch();
        sw.start();
        log.info("Getting all git revisions for cache...");

        final ObjectReader objectReader = this.repository.newObjectReader();
        final CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
        final CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
        final DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(this.repository);
        final ObjectId ref = this.repository.resolve(Constants.HEAD);
        final RevWalk revWalk = new RevWalk(this.repository);
        if(ref == null) return new GitVersionCacheData();
        revWalk.sort(RevSort.REVERSE);
        RevCommit refCommit = revWalk.parseCommit(ref);
        revWalk.markStart(refCommit);
        revWalk.setRevFilter(RevFilter.NO_MERGES);
        RevCommit commit;
        while ((commit = revWalk.next()) != null) {
            log.debug(""+commit.getParentCount()+" " + commit.getFullMessage());
            final RevCommit[] parents = commit.getParents();
            RevTree tree = commit.getTree();
            if (parents.length > 0) {
                oldTreeParser.reset(objectReader, commit.getParent(0).getTree());
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
        return new GitVersionCacheData(pageRevisionCache, attachmentRevisionCache, cacheCommands);
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
            key = createAttachmentStoreKey(parentName, attachmentName);
            String newParentName = TextUtil.urlDecodeUTF8(newPath.substring(0, newPath.indexOf("/")))
                    .replace(GitVersioningAttachmentProvider.DIR_EXTENSION, "");
            newKey = createAttachmentStoreKey(newParentName, attachmentName);
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
        Boolean ignored = ignoreNode.checkIgnored(path, false);
        if(ignored != null && ignored)
            return;
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
            key = createAttachmentStoreKey(parentName, attachmentName);
            toCache = new AttachmentCacheItem(parentName, attachmentName, commit.getFullMessage(),
                    commit.getAuthorIdent().getName(),
                    commit.getAuthorIdent().getWhen(), size, delete, commit.getId());
        }
        else {
            cache = this.pageRevisionCache;
            key = TextUtil.urlDecodeUTF8(path.replace(GitVersioningFileProvider.FILE_EXT, ""));
            toCache = new PageCacheItem(key, commit.getFullMessage(),
                    commit.getAuthorIdent().getName(),
                    commit.getAuthorIdent().getWhen(), size, delete, commit.getId());
        }

        GitVersionCacheData.putInCache(cache, toCache, key);
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

        // TODO: fix me -> works only for the latest version
        //String baseDirPathString = this.engine.getWikiProperties().getProperty("var.basedir");
        //return new File(baseDirPathString, path).length();
    }
}
