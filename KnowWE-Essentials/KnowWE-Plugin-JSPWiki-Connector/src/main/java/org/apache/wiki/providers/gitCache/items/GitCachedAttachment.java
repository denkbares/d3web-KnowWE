package org.apache.wiki.providers.gitCache.items;

import java.util.List;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.providers.gitCache.complete.CompleteGitVersionCache;

import static org.apache.wiki.providers.gitCache.GitVersionCacheData.createAttachmentStoreKey;

public class GitCachedAttachment extends Attachment {
    private final CompleteGitVersionCache gitVersionCache;

    public GitCachedAttachment(Engine engine, String parentPage, String fileName, CompleteGitVersionCache gitVersionCache) {
        super(engine, parentPage, fileName);
        this.gitVersionCache = gitVersionCache;
    }

    @Override
    public int getVersion() {
        if(gitVersionCache.isInitialized) {
            List<GitCacheItem> gitCacheItems = gitVersionCache.data.attachmentRevisionCache.get(createAttachmentStoreKey(getParentName(), getFileName()));
            return gitCacheItems.size();
        } else {
            return -1;
        }
    }

    @Override
    public long getSize() {
        if(gitVersionCache.isInitialized) {
            List<GitCacheItem> gitCacheItems = gitVersionCache.data.attachmentRevisionCache.get(createAttachmentStoreKey(getParentName(), getFileName()));
            return gitCacheItems.get(gitCacheItems.size()-1).getSize();
        } else {
            return -2;
        }
    }

    @Override
    public String getAuthor() {
        if(gitVersionCache.isInitialized) {
            List<GitCacheItem> gitCacheItems = gitVersionCache.data.attachmentRevisionCache.get(createAttachmentStoreKey(getParentName(), getFileName()));
            GitCacheItem latestVersionItem = gitCacheItems.get(gitCacheItems.size() - 1);
            return latestVersionItem.getAuthor();
        }
        return "";  // TODO: ask lazy
    }

    @Override
    public <T> T getAttribute(String key) {
        if(key.equals(WikiPage.CHANGENOTE)) {
            if (gitVersionCache.isInitialized) {
                List<GitCacheItem> gitCacheItems = gitVersionCache.data.attachmentRevisionCache.get(createAttachmentStoreKey(getParentName(), getFileName()));
                GitCacheItem latestVersionItem = gitCacheItems.get(gitCacheItems.size() - 1);
                return (T)latestVersionItem.getFullMessage();
            }
        }
        return super.getAttribute(key);
    }
}
