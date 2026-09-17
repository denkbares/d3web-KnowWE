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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything one scan of a versioning file provider directory found, the commits to replay plus the findings a human
 * has to look at before the migration is taken productive.
 */
public final class ScanResult {

	/** The commits to replay, in the order they will be applied. */
	public final List<MigrationEvent> events = new ArrayList<>();

	/** Author string to how often and when it occurs, the input of the author mapping template. */
	public final Map<String, AuthorMapping.Stats> authors = new LinkedHashMap<>();

	/** Versions the metadata knows about but whose content file is gone, replayed as placeholders to keep numbering. */
	public final List<String> gaps = new ArrayList<>();

	/** Versions that only hold a deletion marker and are replayed as a deletion of the page. */
	public final List<String> softDeletes = new ArrayList<>();

	/** Version directories whose page file is gone, replayed and then deleted again. */
	public final List<String> orphans = new ArrayList<>();

	/** Stored dates that contradicted a neighbouring version and were moved to keep the history in order. */
	public final List<String> correctedDates = new ArrayList<>();

	/** Versions that exist in no layout at all and are therefore lost for good. */
	public final List<String> lostVersions = new ArrayList<>();

	/** Anything unexpected that was skipped or guessed, for example unparseable file names. */
	public final List<String> anomalies = new ArrayList<>();

	/** Paths relative to the page directory that the migration moves out of the working tree. */
	public final List<String> legacyArtifacts = new ArrayList<>();

	/** Paths relative to the page directory that git would see as untracked once the migration is done. */
	public final List<String> leftovers = new ArrayList<>();

	public int pages;
	public int attachments;
	public int pageVersions;
	public int attachmentVersions;

	void countAuthor(String author, long timeMillis) {
		authors.computeIfAbsent(author, key -> new AuthorMapping.Stats()).add(timeMillis);
	}
}
