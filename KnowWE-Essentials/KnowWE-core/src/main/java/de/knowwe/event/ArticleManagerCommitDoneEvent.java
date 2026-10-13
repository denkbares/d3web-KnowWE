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

package de.knowwe.event;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.ArticleManager;

/**
 * Fired after the outermost commit of an {@link ArticleManager}, once the registered and removed articles
 * have been handed to the compilation.
 */
public class ArticleManagerCommitDoneEvent extends ArticleManagerEvent {

	private final Set<String> committedTitles;

	/**
	 * @param committedTitles the titles of the articles registered or removed with this commit, empty if
	 *                        nothing has been committed
	 */
	public ArticleManagerCommitDoneEvent(ArticleManager articleManager, @NotNull Collection<String> committedTitles) {
		super(articleManager);
		this.committedTitles = Collections.unmodifiableSet(new LinkedHashSet<>(committedTitles));
	}

	/**
	 * Returns the titles of the articles registered or removed with this commit, as given by the articles
	 * (not normalized to lower case). The set is empty if nothing has been committed.
	 */
	public @NotNull Set<String> getCommittedTitles() {
		return committedTitles;
	}

	/**
	 * Returns whether any article has been registered or removed with this commit.
	 */
	public boolean changesCommitted() {
		return !committedTitles.isEmpty();
	}
}
