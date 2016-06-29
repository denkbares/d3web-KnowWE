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

package de.knowwe.jspwiki;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.attachment.AttachmentManager;

import de.knowwe.core.wikiConnector.WikiAttachment;

public class JSPWikiAttachment implements WikiAttachment {

	private final Attachment attachment;
	private final AttachmentManager attachmentManager;

	public JSPWikiAttachment(Attachment attachment, AttachmentManager attachmentManager) {
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
	public Date getDate(int version) throws IllegalArgumentException {
		return fetchAttachment(version).getLastModified();
	}

	@Override
	public long getSize() {
		return attachment.getSize();
	}

	@Override
	public long getSize(int version) throws IllegalArgumentException {
		return fetchAttachment(version).getSize();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JSPWikiAttachment)) return false;
		JSPWikiAttachment other = (JSPWikiAttachment) o;
		if (other.attachment == attachment) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.attachment.hashCode();
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

	@Override
	public InputStream getInputStream(int version) throws IOException, IllegalArgumentException {
		try {
			return attachmentManager.getAttachmentStream(fetchAttachment(version));
		}
		catch (ProviderException e) {
			throw new IOException("cannot open attachment", e);
		}
	}

	private Attachment fetchAttachment(int version) {
		try {
			Attachment info = attachmentManager.getAttachmentInfo(getPath(), version);
			if (info != null) return info;
		}
		catch (ProviderException e) {
		}
		throw new IllegalArgumentException("cannot access requested attachment version " + version);
	}

	@Override
	public String getPath() {
		return getParentName() + "/" + getFileName();
	}

	@Override
	public int[] getAvailableVersions() throws IOException {
		try {
			@SuppressWarnings("unchecked")
			List<WikiPage> history = attachmentManager.getVersionHistory(getPath());
			int[] result = new int[history.size()];
			int index = result.length;
			for (WikiPage info : history) {
				result[--index] = info.getVersion();
			}
			return result;
		}
		catch (ProviderException e) {
			throw new IOException("cannot create version list", e);
		}
	}

	@Override
	public int getVersion() {
		return attachment.getVersion();
	}

	@Override
	public void delete(int version) throws IOException, IllegalArgumentException {
		try {
			attachmentManager.deleteVersion(fetchAttachment(version));
		}
		catch (ProviderException e) {
			throw new IOException("cannot delete attachment version", e);
		}
	}
}
