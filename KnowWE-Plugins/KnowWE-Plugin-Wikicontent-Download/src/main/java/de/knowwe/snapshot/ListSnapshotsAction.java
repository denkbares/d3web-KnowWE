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
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Retrieves a list of all created snapshots.
 */
public class ListSnapshotsAction extends SnapshotAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		var tmpSnapshots = transformTmpSnapshots(
				DeployRepoSnapshotProvider.getSnapshotFilesFromTmpRepo()
		);
		var attachmentsSnapshots = transformAttachmentSnapshots(
				DeployAttachmentSnapshotProvider.getWikiAttachmentSnapshots()
		);
		var attachments = Stream.concat(tmpSnapshots.stream(), attachmentsSnapshots.stream())
				.sorted(Comparator.comparing(snapshot -> snapshot.name))
				.toList();
		writeJson(context, attachments);
	}

	private List<SnapshotDTO> transformTmpSnapshots(List<File> files) {
		return files.stream().map(file ->
			new SnapshotDTO(
				file.getPath(), // absolute path
				file.getParent(),
				file.getName().replace(".zip", ""),
				SnapshotType.TMP_FILE
			)
		).toList();
	}

	private List<SnapshotDTO> transformAttachmentSnapshots(List<WikiAttachment> attachments) {
		return attachments.stream().map(attachment ->
			new SnapshotDTO(
				attachment.getPath(), // relative path
				attachment.getParentName(),
				attachment.getFileName().replace(".zip", ""),
				SnapshotType.ATTACHMENT
			)
		).toList();
	}

	private record SnapshotDTO(
		String path,
		String parent,
		String name,
		SnapshotType type
	){}

	private enum SnapshotType {
		ATTACHMENT,
		TMP_FILE
	}

}
