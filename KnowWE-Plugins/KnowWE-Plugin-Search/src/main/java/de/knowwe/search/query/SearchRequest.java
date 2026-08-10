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

package de.knowwe.search.query;

import org.jetbrains.annotations.NotNull;

/**
 * What the user asked for.
 *
 * @param query   the raw input, exactly as typed
 * @param partial whether the user is still typing, in which case the last word is treated as a prefix
 * @param offset  first hit to return
 * @param limit   how many hits to return
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public record SearchRequest(@NotNull String query, boolean partial, int offset, int limit, boolean titleOnly,
							boolean attachmentsOnly) {

	public static final int DEFAULT_LIMIT = 10;

	public SearchRequest(@NotNull String query) {
		this(query, false, 0, DEFAULT_LIMIT, false, false);
	}

	/** Searches everywhere, which is what everything but the filtered search page wants. */
	public SearchRequest(@NotNull String query, boolean partial, int offset, int limit) {
		this(query, partial, offset, limit, false, false);
	}

	public boolean isBlank() {
		return query.isBlank();
	}
}
