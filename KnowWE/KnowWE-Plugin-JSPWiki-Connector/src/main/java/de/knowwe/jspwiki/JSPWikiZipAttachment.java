/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.jspwiki;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.attachment.AttachmentManager;

import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * An attachment for an entry in an zip file (which is itself an attachment).
 * 
 * Right now we do not support version in this sort of attachments. It should be
 * possible to implement, but it will probably never be needed. I guess it would
 * also be slow.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 09.10.2013
 */
public class JSPWikiZipAttachment implements WikiAttachment {

	private final String entryName;
	private final Attachment attachment;
	private final AttachmentManager attachmentManager;

	public JSPWikiZipAttachment(String entryName, Attachment attachment, AttachmentManager attachmentManager) {
		this.entryName = entryName;
		this.attachment = attachment;
		this.attachmentManager = attachmentManager;
	}

	@Override
	public String getFileName() {
		return attachment.getFileName() + "/" + this.entryName;
	}

	@Override
	public String getParentName() {
		return attachment.getParentName();
	}

	@Override
	public String getPath() {
		return attachment.getParentName() + "/" + getFileName();
	}

	@Override
	public Date getDate() {
		return attachment.getLastModified();
	}

	@Override
	public Date getDate(int version) throws IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getSize() {
		// TODO do we need an implementation here?
		return -1;
	}

	@Override
	public long getSize(int version) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			InputStream attachmentStream = attachmentManager.getAttachmentStream(attachment);
			ZipInputStream zipStream = new ZipInputStream(attachmentStream);
			for (ZipEntry e; (e = zipStream.getNextEntry()) != null;) {
				if (e.getName().equals(entryName)) {
					break;
				}
			}
			return zipStream;
		}
		catch (ProviderException e) {
			throw new IOException(e);
		}
	}

	@Override
	public InputStream getInputStream(int version) throws IOException, IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getVersion() {
		return attachment.getVersion();
	}

	@Override
	public void delete(int version) throws IOException, IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int[] getAvailableVersions() throws IOException {
		throw new UnsupportedOperationException();
	}

}
