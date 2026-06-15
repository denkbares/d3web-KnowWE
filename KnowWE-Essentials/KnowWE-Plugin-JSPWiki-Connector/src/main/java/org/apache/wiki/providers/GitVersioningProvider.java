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

package org.apache.wiki.providers;

/**
 * Capability of a page provider to batch multiple page (and attachment) changes of a user into git commits, instead of
 * committing each change individually.
 * <p>
 * The methods form a transaction protocol: {@link #openCommit(String)} starts collecting changes for the given user,
 * {@link #commit(String, String)} closes the batch by committing all collected changes (one commit per affected git
 * repository), {@link #rollback(String)} discards the batch and restores the working tree state of the affected files.
 * Changes made outside an open batch are committed immediately by the provider.
 * <p>
 * {@code JSPWikiConnector} resolves this capability of the configured page provider once and routes the
 * {@code WikiConnector} page transaction methods to it.
 */
public interface GitVersioningProvider {

	/**
	 * Starts collecting the changes of the given user into a single batch, instead of committing
	 * each change individually.
	 *
	 * @param user the wiki user name the batch belongs to
	 */
	void openCommit(String user);

	/**
	 * Commits all changes collected for the given user since {@link #openCommit(String)}, one git
	 * commit per affected repository, and closes the batch.
	 *
	 * @param user      the wiki user name the batch belongs to
	 * @param commitMessage the commit message to use
	 */
	void commit(String user, String commitMessage);

	/**
	 * Discards all changes collected for the given user since {@link #openCommit(String)} and
	 * restores the affected files, closing the batch.
	 *
	 * @param user the wiki user name the batch belongs to
	 */
	void rollback(String user);
}
