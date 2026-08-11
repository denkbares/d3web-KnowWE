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

import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * What the user asked for.
 *
 * @param query   the raw input, exactly as typed
 * @param partial whether the user is still typing, in which case the last word is treated as a prefix
 * @param offset  first hit to return
 * @param limit   how many hits to return
 * @param scopes  where to look, see {@link Scope}; any combination, never empty
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public record SearchRequest(@NotNull String query, boolean partial, int offset, int limit,
							@NotNull Set<Scope> scopes) {

	/**
	 * The places a search can look, freely combinable.
	 * <p>
	 * {@link #TITLES} and {@link #CONTENT} say which <i>part</i> of a document is searched, {@link #ATTACHMENTS} says
	 * which <i>documents</i> take part. That is why attachments alone still searches their name and their text: with
	 * neither part chosen, all of them are meant.
	 */
	public enum Scope {
		/** Page names, and the file names of attachments. */
		TITLES,
		/** What is written on the page: its text, its headings and its markup. */
		CONTENT,
		/** The attachments, next to the pages or instead of them. */
		ATTACHMENTS
	}

	public static final int DEFAULT_LIMIT = 10;

	/** Pages, by name and by text -- what a search means unless it says otherwise. */
	public static final Set<Scope> DEFAULT_SCOPES = Set.of(Scope.TITLES, Scope.CONTENT);

	public SearchRequest {
		// an empty choice would find nothing anywhere, which no caller can mean
		scopes = scopes.isEmpty() ? DEFAULT_SCOPES : Set.copyOf(scopes);
	}

	public SearchRequest(@NotNull String query) {
		this(query, false, 0, DEFAULT_LIMIT, DEFAULT_SCOPES);
	}

	/** Searches the pages, which is what everything but the filtered search page wants. */
	public SearchRequest(@NotNull String query, boolean partial, int offset, int limit) {
		this(query, partial, offset, limit, DEFAULT_SCOPES);
	}

	public boolean titles() {
		return scopes.contains(Scope.TITLES);
	}

	public boolean content() {
		return scopes.contains(Scope.CONTENT);
	}

	public boolean attachments() {
		return scopes.contains(Scope.ATTACHMENTS);
	}

	/** Whether the pages of the wiki take part at all, as opposed to the attachments alone. */
	public boolean pages() {
		return titles() || content();
	}

	/**
	 * Which fields to look at. With neither part of a document chosen -- attachments alone -- everything about it is
	 * meant, otherwise a search for attachments would have nowhere to match.
	 */
	public boolean inTitles() {
		return titles() || !pages();
	}

	public boolean inContent() {
		return content() || !pages();
	}

	public boolean isBlank() {
		return query.isBlank();
	}
}
