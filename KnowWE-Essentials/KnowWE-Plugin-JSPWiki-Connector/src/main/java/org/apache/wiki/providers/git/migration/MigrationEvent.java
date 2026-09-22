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

package org.apache.wiki.providers.git.migration;

import java.io.File;

import org.jetbrains.annotations.Nullable;

/**
 * One version of one page or attachment as it will be replayed into git, one event becomes exactly one commit.
 *
 * @param path       repo relative target path, for example {@code Main.txt} or {@code Main-att/logo.png}
 * @param version    the version number this event will have in git, 1 is the oldest
 * @param timeMillis the commit time, taken from the stored version date or the file timestamp
 * @param author     the author string exactly as the file provider stored it, resolved to a git identity later
 * @param message    the stored change note, empty where the wiki kept none
 * @param content    the file holding this version's content, null for a deletion
 * @param kind       how the content of this event was obtained
 */
public record MigrationEvent(
		String path,
		int version,
		long timeMillis,
		String author,
		String message,
		@Nullable File content,
		Kind kind
) {

	public enum Kind {
		/** A version file that exists on disk. */
		VERSION,
		/** A version the properties file knows about but whose content file is missing, filled with neighbouring content. */
		PLACEHOLDER,
		/** The removal of a page or attachment whose content survived only in the version directory. */
		DELETE
	}

	MigrationEvent withTime(long newTimeMillis) {
		return new MigrationEvent(path, version, newTimeMillis, author, message, content, kind);
	}

	@Override
	public String toString() {
		return path + " v" + version + " (" + kind + ")";
	}
}
