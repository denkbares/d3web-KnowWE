package org.apache.wiki.providers.gitCache.items;

import java.util.ArrayList;
import java.util.List;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.providers.gitCache.async.AsyncInitGitVersionCache;
import org.apache.wiki.providers.gitCache.complete.CompleteGitVersionCache;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Stopwatch;

public class GitCachedWikiPage extends WikiPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompleteGitVersionCache.class);

    private final AsyncInitGitVersionCache lazyGitVersionCache;

    public GitCachedWikiPage(Engine engine, String pageName, AsyncInitGitVersionCache lazyGitVersionCache) {
        super(engine, pageName);
        this.lazyGitVersionCache = lazyGitVersionCache;
    }

    @Override
    public String getAuthor() {
        if(lazyGitVersionCache.isInitialized) {
            List<GitCacheItem> gitCacheItems = lazyGitVersionCache.data.pageRevisionCache.get(this.getName());
            int version = this.getVersion();
            GitCacheItem gitCacheItemLatest;
            if(version == -1) {
                gitCacheItemLatest = gitCacheItems.get(gitCacheItems.size() - 1);
            }
            else if (version - 1 < gitCacheItems.size()) {
                gitCacheItemLatest = gitCacheItems.get(version - 1);
            }
            else {
                LOGGER.warn("No Author found for invalid version number "+version+" of page "+getName());
                 return null;
            }
            if(gitCacheItemLatest != null) {
                return gitCacheItemLatest.getAuthor();
            }
        } else {
            // here we could either retrieve the author for this page and version from git directly
            // or wait for the full initialization to be finished.
            /*
            while(!lazyGitVersionCache.isInitialized) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return getAuthor();

             */
           //String author =  lookupAuthor();
           String author =  "NotAvailable";
           return author;

        }

        return super.getAuthor();
    }

    private String lookupAuthor() {
        Git git = new Git(this.lazyGitVersionCache.repository);
        Stopwatch sw = new Stopwatch();
        List<RevCommit> gitLogHistoryOfPage = new ArrayList<>();
        try {
            Iterable<RevCommit> gitLogResult = git.log().addPath(getName()+".txt").call();
            gitLogResult.forEach(gitLogHistoryOfPage::add);
            LOGGER.info("Looking up Author in Git for page "+getName()+" took: "+sw.getTime());

        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        if(getVersion() == -1) {
            return gitLogHistoryOfPage.get(gitLogHistoryOfPage.size()-1).getAuthorIdent().getName();
        } else
        if(getVersion() < gitLogHistoryOfPage.size()) {
            return gitLogHistoryOfPage.get(getVersion()-1).getAuthorIdent().getName();
        } else {
            LOGGER.warn("No Author found for invalid version number "+getVersion()+" of page "+getName());
            return null;
        }
    }

    @Override
    public <T> T getAttribute(String key) {
        if(key.equals(WikiPage.CHANGENOTE)) {
            // TODO: ask lazy
            int foo = 1;
        }
        return super.getAttribute(key);
    }

    @Override
    public int getVersion() {
        return -1; // TODO: ask lazy
    }
}
