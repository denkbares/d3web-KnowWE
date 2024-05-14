package org.apache.wiki.providers.gitCache;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.providers.gitCache.commands.CacheCommand;
import org.apache.wiki.providers.gitCache.items.AttachmentCacheItem;
import org.apache.wiki.providers.gitCache.items.PageCacheItem;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.NotNull;

public interface GitVersionCache {

    String PROPERTIES_KEY_GIT_VERSION_CACHE = "GitVersionCache.init";
    String SYNC = "sync";
    String ASYNC = "async";

    /**
     * Clears the cached data. Prepares new cache data (for instance on another git branch).
     *
     * @throws IOException exception
     */
    void clearAndReinitialize() throws IOException;

    /**
     * Returns wethere initialization has been completed.
     *
     * @return true if initialization has been completed, false otherwise
     */
    boolean isInitialized();

    /**
     * Returns the cache item for the wiki page in the specified version.
     *
     * @param pageName wiki page name
     * @param version version of the page
     * @return cache item about the specified version of the page
     */
    PageCacheItem getPageVersion(@NotNull String pageName, int version);

    /**
     * Updating cache during wiki change operation: adds a new version of a page
     *
     * @param page wiki page that has been changed
     * @param commitMsg commit message
     * @param id jgit id
     */
    void addPageVersion(Page page, String commitMsg, ObjectId id);

    /**
     * Updating cache during wiki change operation: adds a new version of an attachment
     *
     * @param att attachment that is changed
     * @param commitMsg commit message
     * @param id jgit id
     */
    void addAttachmentVersion(Attachment att, String commitMsg, ObjectId id);

    /**
     * Obtains the CacheItem for a given Attachment (which includes the jgit id).
     *
     * @param att attachment
     * @return cached item
     */
    AttachmentCacheItem getAttachmentCacheItem(Attachment att);

    /**
     * Returns all attachments in their most recent (current) version.
     * @return all current attachments
     */
    List<Attachment> getAllAttachmentsLatest();

    /**
     * Creates an Attachment object for the given file - latest version.
     *
     * @param filename file
     * @param parentPageName parent page
     * @return attachment
     */
    Attachment getAttachmentLatest(String filename, String parentPageName);

    void setPageHistory(String pageName, List<Page> page);

    /**
     * Creates a WikiPage object for the given page name - latest version.
     *
     * @param pageName page name
     * @return WikiPage
     */
    WikiPage createWikiPageLatest(String pageName);




    /**
     * Returns the history of the given attachments as a list of versions
     *
     * @param att attachment
     * @return list of versions of the attachment
     */
    List<Attachment> getAttachmentHistory(Attachment att);

    /**
     * Returns the history of the given attachments as a list of WikiPage versions.
     *
     * @param pageName wiki page name
     * @return list of versions
     */
    List<Page> getPageHistory(String pageName);


    /**
     * Applies the delete-page-operation on the cache data.
     *
     * @param page page to be deleted
     * @param commitMsg commit message
     * @param id jgit id
     */
    void deletePage(Page page, String commitMsg, ObjectId id);


    /**
     * Applies the delete-attachment-operation on the cache data
     *
     * @param att attachment that has been deleted
     * @param commitMsg commit message
     * @param id jgit id
     */
    void deleteAttachment(Attachment att, String commitMsg, ObjectId id);

    /**
     * Applies the move-attachment-operation on the cache data.
     * The move-attachment-operation moves an attachment from one wiki page to another.
     *
     * @param oldParent old parent of the attachment
     * @param newParent new parent of the attachment
     * @param files files that are moved
     * @param id jgit id
     * @param commitMsg commit message
     * @param author user name
     */
    void moveAttachments(String oldParent, String newParent, File[] files, ObjectId id, String commitMsg, String author);


    /**
     * Applies the move page operation (page renaming) on the cache data.
     *
     * @param from the original page
     * @param to the target name of the page
     * @param message the commit message
     * @param id jgit id
     */
    void movePage(Page from, String to, String message, ObjectId id);


    /**
     * Adds a cache command to the cache.
     *
     * @param user user that executed the command
     * @param command the command
     */
    void addCacheCommand(String user, CacheCommand command);


    /**
     * Executes all the cached git commands.
     *
     * TODO: why do we need this?
     *
     * @param user user
     * @param commitMsg commit message
     * @param id jgit it
     */
    void executeCacheCommands(String user, String commitMsg, ObjectId id);

    /**
     * Called when the file provider gets shut down, can be used to persist any leftover artefacts
     */
    void shutdown();
}
