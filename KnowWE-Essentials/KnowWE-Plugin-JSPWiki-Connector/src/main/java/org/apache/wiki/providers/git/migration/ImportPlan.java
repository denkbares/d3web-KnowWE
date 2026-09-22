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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * The commits that will actually be written, derived from the scanned versions by dropping what git cannot represent.
 * <p>
 * A wiki version whose content equals the version before it is not a change in git, so no history read would ever
 * report it and it cannot carry a version number of its own. The running git provider refuses such a save for the same
 * reason. Those versions are therefore left out here, which shifts the numbers of the versions after them down. This is
 * the one place where the migration cannot be faithful, so every dropped version is recorded for the report.
 */
public final class ImportPlan {

	/**
	 * One commit to write, with the author it is attributed to and the name the written content will have in git.
	 *
	 * @param blob the object name of the content, null for a deletion
	 */
	public record PlannedCommit(MigrationEvent event, AuthorMapping.Identity identity, @Nullable String blob) {
	}

	private final List<PlannedCommit> commits = new ArrayList<>();
	private final Map<String, List<Integer>> droppedVersions = new LinkedHashMap<>();
	private final Map<String, MigrationEvent> finalState = new LinkedHashMap<>();

	private ImportPlan() {
	}

	/**
	 * Reads every version once to find out which of them change anything, and resolves every author.
	 */
	public static ImportPlan build(List<MigrationEvent> events, AuthorMapping mapping) throws IOException {
		ImportPlan plan = new ImportPlan();
		Map<String, String> currentBlob = new LinkedHashMap<>();
		for (MigrationEvent event : events) {
			plan.finalState.put(event.path(), event);
			if (event.kind() == MigrationEvent.Kind.DELETE) {
				if (currentBlob.remove(event.path()) != null) {
					plan.commits.add(new PlannedCommit(event, mapping.resolve(event.author()), null));
				}
				continue;
			}
			String blob = FastImportWriter.objectName(FastImportWriter.contentOf(event));
			if (blob.equals(currentBlob.get(event.path()))) {
				plan.droppedVersions.computeIfAbsent(event.path(), path -> new ArrayList<>()).add(event.version());
				continue;
			}
			currentBlob.put(event.path(), blob);
			plan.commits.add(new PlannedCommit(event, mapping.resolve(event.author()), blob));
		}
		return plan;
	}

	public List<PlannedCommit> commits() {
		return commits;
	}

	/**
	 * The last event of every path, which says what the working tree has to look like when the import is done. It is
	 * taken from all events, not only from the written ones, because a deletion of something that was never written is
	 * dropped as a commit but still has to remove the file the wiki left behind.
	 */
	public Map<String, MigrationEvent> finalState() {
		return finalState;
	}

	/**
	 * How many versions were dropped because they changed nothing.
	 */
	public int droppedCount() {
		return droppedVersions.values().stream().mapToInt(List::size).sum();
	}

	/**
	 * One report line per file that lost versions, naming them and the resulting shift.
	 */
	public List<String> droppedReport() {
		List<String> lines = new ArrayList<>(droppedVersions.size());
		for (Map.Entry<String, List<Integer>> entry : droppedVersions.entrySet()) {
			List<Integer> versions = entry.getValue();
			lines.add(entry.getKey() + " version(s) " + versions
					+ " changed nothing and are not represented in git, later versions shift down by "
					+ versions.size());
		}
		return lines;
	}
}
