package org.apache.wiki.providers.gitCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wiki.providers.gitCache.commands.CacheCommand;
import org.apache.wiki.providers.gitCache.items.GitCacheItem;
import org.jetbrains.annotations.NotNull;

public class GitVersionCacheData {

    public Map<String, List<GitCacheItem>> pageRevisionCache;
    public Map<String, List<GitCacheItem>> attachmentRevisionCache;
    public Map<String, List<CacheCommand>> cacheCommands;


    public GitVersionCacheData(Map<String, List<GitCacheItem>> pageRevisionCache, Map<String, List<GitCacheItem>> attachmentRevisionCache, Map<String, List<CacheCommand>> cacheCommands) {
        this.pageRevisionCache = pageRevisionCache;
        this.attachmentRevisionCache = attachmentRevisionCache;
        this.cacheCommands = cacheCommands;
    }

    public GitVersionCacheData() {
        this.pageRevisionCache = new HashMap<>();
        this.attachmentRevisionCache =  new HashMap<>();
        this.cacheCommands =  new HashMap<>();
    }

    @NotNull
    public static String createAttachmentStoreKey(String parentName, String attachmentName) {
        return parentName + "/" + attachmentName;
    }

    public static void putInCache(Map<String, List<GitCacheItem>> cache, GitCacheItem toCache, String key) {
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
}
