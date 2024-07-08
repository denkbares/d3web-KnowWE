/*
 * Copyright (C) 2024 denkbares GmbH, Germany
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

package de.knowwe.core.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Streams;
import de.knowwe.core.Environment;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Simple action allowing to download an attachment, but with a different name as the attachment itself. This is
 * necessary, because unfortunately the download-attribut at href links doesn't work for chrome, if filename is also
 * set in the header.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.07.2024
 */
public class AttachmentDownloadAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String attachmentPath = context.getParameter("attachment");
		WikiAttachment attachment = Environment.getInstance().getWikiConnector().getAttachment(attachmentPath);
		if (attachment == null) fail(context, 404, "Attachment " + attachmentPath + " not found");

		String filename = context.getParameter("filename");
		if (Strings.isBlank(filename)) {
			filename = attachment.getFileName();
		}

		try (InputStream in = attachment.getInputStream(); OutputStream out = context.getOutputStream()) {
			context.setContentType(BINARY);
			context.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
			Streams.stream(in, out);
		}
	}
}
