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

package org.apache.wiki.providers.git;

import java.util.Date;

import de.uniwue.d3web.gitConnector.CommitUserData;

/**
 * One version of a page as seen by git: a branch-relative version number, the backing commit, the commit's user data
 * (author, email, change note) and the file's size and timestamp at that commit.
 * <p>
 * This is the engine-free value object {@link GitPageHistory} produces. The provider maps it to a JSPWiki {@code Page},
 * which is where the JSPWiki engine dependency lives, keeping {@code GitPageHistory} testable against a bare git
 * repository without a wiki engine.
 *
 * @param version    branch-relative position of this version in the file's git log, 1 = oldest
 * @param commitHash the commit this version corresponds to
 * @param userData   author, email and change note of the commit
 * @param size       size in bytes of the file at this commit
 * @param date       commit time
 */
public record GitPageVersion(int version, String commitHash, CommitUserData userData, long size, Date date) {
}
