/*
 * Copyright (C) 2012 denkbares GmbH
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
package connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import de.knowwe.core.wikiConnector.ConnectorAttachment;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.04.2012
 */
public class FileSystemConnectorAttachment implements ConnectorAttachment {

	private final File attachmentFile;

	private final String parentName;

	private final String fileName;

	public FileSystemConnectorAttachment(String fileName, String parentName, File attachmentFile) {
		this.attachmentFile = attachmentFile;
		this.fileName = fileName;
		this.parentName = parentName;
	}

	@Override
	public String getFileName() {
		return this.fileName;
	}

	@Override
	public String getParentName() {
		return this.parentName;
	}

	@Override
	public String getFullName() {
		return getParentName() + "/" + getFileName();
	}

	@Override
	public Date getDate() {
		return new Date(attachmentFile.lastModified());
	}

	@Override
	public long getSize() {
		return attachmentFile.length();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(attachmentFile);
	}

	@Override
	public String toString() {
		return getFullName();
	}

}
