package de.knowwe.jspwiki.changeannotations;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.wiki.event.WikiPageEvent;
import org.apache.wiki.event.WikiPageRenameEvent;
import org.junit.Test;

import com.denkbares.knowwe.changeannotations.InMemoryPageAnnotationCache;
import com.denkbares.knowwe.changeannotations.LineBlame;
import com.denkbares.knowwe.changeannotations.PageAnnotation;

import static org.junit.Assert.assertEquals;

/**
 * Verifies the WikiEvent → invalidation wiring. Pure storage behavior is covered by
 * {@code InMemoryPageAnnotationCacheTest} in the change-annotations plugin.
 */
public class JspWikiPageAnnotationCacheTest {

	@Test
	public void pageReindexEventInvalidates() {
		JspWikiPageAnnotationCache cache = newCache();
		AtomicInteger calls = new AtomicInteger();
		Function<String, PageAnnotation> loader = countingLoader(calls);

		cache.getOrCompute("Main", loader);
		cache.actionPerformed(new WikiPageEvent(this, WikiPageEvent.PAGE_REINDEX, "Main"));
		cache.getOrCompute("Main", loader);

		assertEquals(2, calls.get());
	}

	@Test
	public void pageDeletedEventInvalidates() {
		JspWikiPageAnnotationCache cache = newCache();
		AtomicInteger calls = new AtomicInteger();
		Function<String, PageAnnotation> loader = countingLoader(calls);

		cache.getOrCompute("Main", loader);
		cache.actionPerformed(new WikiPageEvent(this, WikiPageEvent.PAGE_DELETED, "Main"));
		cache.getOrCompute("Main", loader);

		assertEquals(2, calls.get());
	}

	@Test
	public void renameEventInvalidatesOldAndNew() {
		JspWikiPageAnnotationCache cache = newCache();
		AtomicInteger calls = new AtomicInteger();
		Function<String, PageAnnotation> loader = countingLoader(calls);

		cache.getOrCompute("OldName", loader);
		cache.getOrCompute("NewName", loader);
		cache.actionPerformed(new WikiPageRenameEvent(this, "OldName", "NewName"));
		cache.getOrCompute("OldName", loader);
		cache.getOrCompute("NewName", loader);

		assertEquals(4, calls.get());
	}

	@Test
	public void unrelatedEventsDoNotInvalidate() {
		JspWikiPageAnnotationCache cache = newCache();
		AtomicInteger calls = new AtomicInteger();
		Function<String, PageAnnotation> loader = countingLoader(calls);

		cache.getOrCompute("Main", loader);
		cache.actionPerformed(new WikiPageEvent(this, WikiPageEvent.PAGE_REQUESTED, "Main"));
		cache.actionPerformed(new WikiPageEvent(this, WikiPageEvent.PAGE_LOCK, "Main"));
		cache.getOrCompute("Main", loader);

		assertEquals(1, calls.get());
	}

	private static JspWikiPageAnnotationCache newCache() {
		return new JspWikiPageAnnotationCache(new InMemoryPageAnnotationCache());
	}

	private static Function<String, PageAnnotation> countingLoader(AtomicInteger calls) {
		return name -> {
			calls.incrementAndGet();
			return new PageAnnotation("Main", 1,
					List.of(new LineBlame(1, 1, "alice", Instant.EPOCH, null)));
		};
	}
}
