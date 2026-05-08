package com.denkbares.knowwe.changeannotations;

import java.util.function.Function;

/**
 * Storage abstraction for {@link PageAnnotation}s, keyed by page name. Lets the wiki-side
 * adapter (e.g. {@code JspWikiPageAnnotationCache} in the connector) plug in invalidation on
 * top of a generic backing store, without coupling the domain plugin to a specific wiki
 * backend.
 *
 * <p>The default implementation is {@link InMemoryPageAnnotationCache}; richer
 * implementations (bounded memory, time-based expiry, Caffeine-backed, ...) can drop in
 * without touching call sites.
 */
public interface PageAnnotationCache {

	/**
	 * Returns the cached annotation for {@code pageName}, computing and caching it via
	 * {@code loader} if no entry exists. The contract follows {@link
	 * java.util.concurrent.ConcurrentMap#computeIfAbsent(Object, Function)} — concurrent
	 * callers see exactly one loader invocation per missing key.
	 */
	PageAnnotation getOrCompute(String pageName, Function<String, PageAnnotation> loader);

	/** Removes the cached entry for {@code pageName}, if any. */
	void invalidate(String pageName);

	/** Removes all cached entries. */
	void invalidateAll();
}
