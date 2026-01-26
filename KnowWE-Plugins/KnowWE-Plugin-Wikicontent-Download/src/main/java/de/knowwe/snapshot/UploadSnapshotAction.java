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
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

import static de.knowwe.snapshot.SnapshotAction.getSnapshotsPath;
import static de.knowwe.snapshot.SnapshotAction.storageLimitWasReached;

public class UploadSnapshotAction extends AbstractAction {

	private final Logger LOGGER = Logger.getLogger(UploadSnapshotAction.class.getName());

	@Override
	public void execute(UserActionContext context) throws IOException {

		if (!context.userIsAdmin()) {
			context.sendError(500, "You do not have permission to perform this action.");
		}

		if (!ServletFileUpload.isMultipartContent(context.getRequest())) {
			context.sendError(500, "Request has no multipart content.");
			return;
		}

		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		try {
			// find file among all fields of the multipart form request
			List<FileItem> potentialFiles = upload.parseRequest(context.getRequest());
			var fileOptional = potentialFiles.stream().filter(field -> !field.isFormField()).findFirst();
			fileOptional.ifPresent(file -> {
				// check file
				boolean hasZipExtension = file.getName() != null && file.getName().toLowerCase().endsWith(".zip");
				boolean hasZipMimeType = List.of("application/zip", "application/x-zip-compressed")
						.contains(file.getContentType().toLowerCase());
				if (!hasZipExtension || !hasZipMimeType) throw new RuntimeException("File has an invalid format");

				if (file.getName().toLowerCase().contains("autosavesnapshot")) {
					throw new RuntimeException("'AutosaveSnapshot' can not be part of your filename. Try to use a more descriptive filename");
				}

				if (storageLimitWasReached()) {
					throw new RuntimeException("Snapshot limit was reached. Please delete a snapshot before continuing.");
				}

				// store file
				try {
					File newSnapshotFile = new File(getSnapshotsPath(), file.getName());
					file.write(newSnapshotFile);
				}
				catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			});
		}
		catch (Exception e) {
			context.sendError(500, "Error while uploading a snapshot: " + e.getMessage());
		}
	}
}