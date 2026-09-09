/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.core.sse;

import java.time.Duration;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * Limits for {@link ServerSentEventWriter#streamBatched}. A batch is closed when its duration budget is used up, when
 * it holds maxBatchItems items or when maxItems items have been emitted in total.
 */
public record StreamOptions(@NotNull Duration batchDuration, int maxBatchItems, int maxItems) {

	public static final StreamOptions DEFAULT =
			new StreamOptions(Duration.ofMillis(100), Integer.MAX_VALUE, Integer.MAX_VALUE);

	public StreamOptions {
		Objects.requireNonNull(batchDuration, "batchDuration");
		if (batchDuration.isZero() || batchDuration.isNegative()) {
			throw new IllegalArgumentException("batchDuration must be > 0");
		}
		if (maxBatchItems <= 0) throw new IllegalArgumentException("maxBatchItems must be > 0");
		if (maxItems < 0) throw new IllegalArgumentException("maxItems must be >= 0");
	}

	@NotNull
	public StreamOptions withBatchDuration(@NotNull Duration batchDuration) {
		return new StreamOptions(batchDuration, maxBatchItems, maxItems);
	}

	@NotNull
	public StreamOptions withMaxBatchItems(int maxBatchItems) {
		return new StreamOptions(batchDuration, maxBatchItems, maxItems);
	}

	@NotNull
	public StreamOptions withMaxItems(int maxItems) {
		return new StreamOptions(batchDuration, maxBatchItems, maxItems);
	}
}
