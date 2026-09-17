/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.renderer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests the open-ended page selection used by grouped section lists.
 */
public class OpenPaginationPredicateTest {

	@Test
	public void stopsFilteringAfterLookaheadMatch() {
		AtomicInteger evaluations = new AtomicInteger();
		OpenPaginationPredicate<Integer> predicate = new OpenPaginationPredicate<>(value -> {
			evaluations.incrementAndGet();
			return value % 2 == 0;
		}, 2, 3, -1);

		List<Integer> page = IntStream.rangeClosed(1, 100).boxed().filter(predicate).toList();

		assertThat(page, is(List.of(6, 8, 10)));
		assertThat(predicate.getDisplayedCount(), is(3));
		assertThat(predicate.hasMore(), is(true));
		assertThat(evaluations.get(), is(12));
	}

	@Test
	public void detectsLastIncompletePage() {
		OpenPaginationPredicate<Integer> predicate = new OpenPaginationPredicate<>(value -> true, 3, 3, -1);

		List<Integer> page = IntStream.rangeClosed(1, 5).boxed().filter(predicate).toList();

		assertThat(page, is(List.of(4, 5)));
		assertThat(predicate.getDisplayedCount(), is(2));
		assertThat(predicate.getMatchCount(), is(5));
		assertThat(predicate.hasMore(), is(false));
	}

	@Test
	public void respectsLogicalResultLimit() {
		AtomicInteger evaluations = new AtomicInteger();
		OpenPaginationPredicate<Integer> predicate = new OpenPaginationPredicate<>(value -> {
			evaluations.incrementAndGet();
			return true;
		}, 2, 2, 4);

		List<Integer> page = IntStream.rangeClosed(1, 10).boxed().filter(predicate).toList();

		assertThat(page, is(List.of(3, 4)));
		assertThat(predicate.getMatchCount(), is(4));
		assertThat(predicate.hasMore(), is(false));
		assertThat(evaluations.get(), is(4));
	}
}
