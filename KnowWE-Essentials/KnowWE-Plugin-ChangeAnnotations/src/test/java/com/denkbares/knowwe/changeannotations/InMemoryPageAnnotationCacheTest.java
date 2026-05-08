package com.denkbares.knowwe.changeannotations;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class InMemoryPageAnnotationCacheTest {

	@Test
	void getOrComputeRunsLoaderOnceAndReturnsCached() {
		InMemoryPageAnnotationCache cache = new InMemoryPageAnnotationCache();
		PageAnnotation snap = sampleSnapshot();
		AtomicInteger calls = new AtomicInteger();
		Function<String, PageAnnotation> loader = name -> {
			calls.incrementAndGet();
			return snap;
		};

		assertSame(snap, cache.getOrCompute("Main", loader));
		assertSame(snap, cache.getOrCompute("Main", loader));

		assertEquals(1, calls.get());
	}

	@Test
	void invalidateForcesRecompute() {
		InMemoryPageAnnotationCache cache = new InMemoryPageAnnotationCache();
		AtomicInteger calls = new AtomicInteger();
		Function<String, PageAnnotation> loader = countingLoader(calls);

		cache.getOrCompute("Main", loader);
		cache.invalidate("Main");
		cache.getOrCompute("Main", loader);

		assertEquals(2, calls.get());
	}

	@Test
	void invalidateAllClearsEverything() {
		InMemoryPageAnnotationCache cache = new InMemoryPageAnnotationCache();
		AtomicInteger calls = new AtomicInteger();
		Function<String, PageAnnotation> loader = countingLoader(calls);

		cache.getOrCompute("A", loader);
		cache.getOrCompute("B", loader);
		cache.invalidateAll();
		cache.getOrCompute("A", loader);
		cache.getOrCompute("B", loader);

		assertEquals(4, calls.get());
	}

	@Test
	void invalidateUnknownPageIsNoop() {
		InMemoryPageAnnotationCache cache = new InMemoryPageAnnotationCache();
		// must not throw, must not affect future loads
		cache.invalidate("never-cached");
		cache.invalidate(null);
	}

	private static Function<String, PageAnnotation> countingLoader(AtomicInteger calls) {
		return name -> {
			calls.incrementAndGet();
			return sampleSnapshot();
		};
	}

	private static PageAnnotation sampleSnapshot() {
		return new PageAnnotation("Main", 1,
				List.of(new LineBlame(1, 1, "alice", Instant.EPOCH, null)));
	}
}
