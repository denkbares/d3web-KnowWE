/*
 * Copyright (C) 2025 denkbares GmbH, Germany
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

package de.knowwe.snapshot;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import de.knowwe.core.action.UserActionContext;

/**
 * Retrieves a list of all created snapshots.
 */
public class ListSnapshotsAction extends SnapshotAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		var snapshots = transformTmpSnapshots(DeploySnapshotProvider.getSnapshotFilesFromTmpRepo())
				.stream()
				.sorted(Comparator.comparing(snapshot -> snapshot.date))
				.toList();
		writeJson(context, snapshots);
	}

	private List<SnapshotDTO> transformTmpSnapshots(List<File> files) {
		return files.stream().map(file ->
				new SnapshotDTO(
						file.getPath(), // absolute path
						file.getParent(),
						file.getName().replace(".zip", ""),
						file.lastModified(),
						file.length()
				)
		).toList();
	}

	private record SnapshotDTO(
			String path,
			String parent,
			String name,
			long date,
			long size
	) {
	}
}
