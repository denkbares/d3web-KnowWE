package org.apache.wiki.providers.gitCache.async;

/**
 * A delayed request is a request that could not have been handled in an async setting as the cache is not built yet.
 */
public interface DelayedRequest {

	void executeRequest(AsyncInitGitVersionCache cache);
}
