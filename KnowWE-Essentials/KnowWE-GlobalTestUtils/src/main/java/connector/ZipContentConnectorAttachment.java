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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.04.2012
 */
public class ZipContentConnectorAttachment extends SingleVersionAttachment {

	private final String parentName;

	private final String fileName;

	private final ZipFile zipFile;

	private final ZipEntry zipEntry;

	public ZipContentConnectorAttachment(DummyPageProvider pageProvider, String fileName, String parentName, ZipEntry zipEntry, ZipFile zipFile) {
		super(pageProvider);
		this.zipFile = zipFile;
		this.zipEntry = zipEntry;
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
	public String getPath() {
		return getParentName() + "/" + getFileName();
	}

	@Override
	public Date getDate() {
		return new Date(zipEntry.getTime());
	}

	@Override
	public long getSize() {
		return zipEntry.getSize();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return zipFile.getInputStream(zipEntry);
	}

	@Override
	public String toString() {
		return getPath();
	}

}
