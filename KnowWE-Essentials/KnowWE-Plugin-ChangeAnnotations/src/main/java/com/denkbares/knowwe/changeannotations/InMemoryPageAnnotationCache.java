package com.denkbares.knowwe.changeannotations;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Simple {@link ConcurrentHashMap}-backed {@link PageAnnotationCache}. Unbounded — fine for
 * the typical wiki size, can be swapped for a bounded variant if memory pressure shows up.
 */
public final class InMemoryPageAnnotationCache implements PageAnnotationCache {

	private final ConcurrentMap<String, PageAnnotation> store = new ConcurrentHashMap<>();

	@Override
	public PageAnnotation getOrCompute(String pageName, Function<String, PageAnnotation> loader) {
		if (pageName == null) throw new NullPointerException("pageName");
		if (loader == null) throw new NullPointerException("loader");
		return store.computeIfAbsent(pageName, loader);
	}

	@Override
	public void invalidate(String pageName) {
		if (pageName == null) return;
		store.remove(pageName);
	}

	@Override
	public void invalidateAll() {
		store.clear();
	}
}
