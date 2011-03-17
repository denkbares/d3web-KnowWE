/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.jspwiki;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import com.ecyrd.jspwiki.attachment.Attachment;
import com.ecyrd.jspwiki.attachment.AttachmentManager;
import com.ecyrd.jspwiki.providers.ProviderException;

import de.d3web.we.wikiConnector.ConnectorAttachment;

public class JSPWikiConnectorAttachment implements ConnectorAttachment {

	private final Attachment attachment;
	private final AttachmentManager attachmentManager;

	public JSPWikiConnectorAttachment(Attachment attachment, AttachmentManager attachmentManager) {
		this.attachment = attachment;
		this.attachmentManager = attachmentManager;
	}

	@Override
	public String getFileName() {
		return attachment.getFileName();
	}

	@Override
	public String getParentName() {
		return attachment.getParentName();
	}

	@Override
	public Date getDate() {
		return attachment.getLastModified();
	}

	@Override
	public long getSize() {
		return attachment.getSize();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof JSPWikiConnectorAttachment)) return false;
		JSPWikiConnectorAttachment other = (JSPWikiConnectorAttachment) o;
		if (other.attachment == attachment) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			return attachmentManager.getAttachmentStream(attachment);
		}
		catch (ProviderException e) {
			throw new IOException(e);
		}
	}

}
