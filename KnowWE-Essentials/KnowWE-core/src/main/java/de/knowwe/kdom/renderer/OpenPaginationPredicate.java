/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.renderer;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Selects one page of matching values without determining the total number of matches. Evaluation
 * of the delegate predicate stops after the first match following the requested page. This extra
 * match is used to determine whether a subsequent page exists.
 *
 * @param <T> the type of values to filter
 */
final class OpenPaginationPredicate<T> implements Predicate<T> {

	private final Predicate<T> filter;
	private final int offset;
	private final int pageSize;
	private final int maximumMatches;

	private int matches;
	private int displayedCount;
	private boolean hasMore;

	/**
	 * Creates a predicate selecting an open-ended page.
	 *
	 * @param filter         predicate defining matching values
	 * @param offset         number of matching values to skip
	 * @param pageSize       maximum number of values on the page
	 * @param maximumMatches maximum number of logical matches, or {@code -1} for no limit
	 */
	OpenPaginationPredicate(Predicate<T> filter, int offset, int pageSize, int maximumMatches) {
		this.filter = Objects.requireNonNull(filter);
		if (offset < 0) throw new IllegalArgumentException("Offset must not be negative");
		if (pageSize <= 0) throw new IllegalArgumentException("Page size must be positive");
		if (maximumMatches < -1) throw new IllegalArgumentException("Maximum matches must be -1 or greater");
		this.offset = offset;
		this.pageSize = pageSize;
		this.maximumMatches = maximumMatches;
	}

	@Override
	public boolean test(T value) {
		if (hasMore || maximumMatches >= 0 && matches >= maximumMatches) return false;
		if (!filter.test(value)) return false;

		matches++;
		if (matches <= offset) return false;
		if (displayedCount < pageSize) {
			displayedCount++;
			return true;
		}

		hasMore = true;
		return false;
	}

	/**
	 * Returns the number of values selected for the current page.
	 *
	 * @return displayed value count
	 */
	int getDisplayedCount() {
		return displayedCount;
	}

	/**
	 * Returns the number of logical matches encountered so far. If {@link #hasMore()} is
	 * {@code false} after processing the complete input, this is the exact result size.
	 *
	 * @return number of logical matches encountered
	 */
	int getMatchCount() {
		return matches;
	}

	/**
	 * Returns whether another matching value exists after the current page.
	 *
	 * @return whether navigation to a subsequent page is possible
	 */
	boolean hasMore() {
		return hasMore;
	}
}
