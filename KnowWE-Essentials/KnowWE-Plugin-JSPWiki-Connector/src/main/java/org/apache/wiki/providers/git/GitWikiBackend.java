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

/**
 * Everything the git-backed page and attachment providers share: the {@link GitWikiRepository} (git-facing, the
 * gateway to the one wiki repository), the {@link GitCommitBatchRegistry} (the open transactions on it) and the
 * {@link WikiGitContext} (engine-facing). Built once by the page provider at initialization; the attachment provider
 * resolves it from there, so the sibling coupling is this one object instead of three separate accessors.
 */
public record GitWikiBackend(GitWikiRepository repository, GitCommitBatchRegistry batchRegistry, WikiGitContext context) {
}
