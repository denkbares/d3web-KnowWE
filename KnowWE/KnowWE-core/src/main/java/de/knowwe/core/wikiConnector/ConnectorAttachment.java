/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.core.wikiConnector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public interface ConnectorAttachment {

	/**
	 * Returns the file name of the attachment. The file name is not the file
	 * name or path to the attachment in the file system of the wiki, but the
	 * file name with which the attachment was uploaded by a user to the wiki.
	 * 
	 * @created 11.04.2012
	 */
	public String getFileName();

	/**
	 * Returns the name/title of the article the attachment belongs to.
	 * 
	 * @created 11.04.2012
	 */
	public String getParentName();

	/**
	 * Returns the path of the attachment in the connected wiki. The path is
	 * comprised of the article name of the attachment, followed by a separator
	 * and the file name of the attachment itself.
	 * 
	 * @created 11.04.2012
	 */
	public String getPath();

	/**
	 * Returns the date of the last change of attachment.
	 * 
	 * @created 11.04.2012
	 */
	public Date getDate();

	/**
	 * Returns the size of the attachment in bytes.
	 * 
	 * @created 11.04.2012
	 */
	public long getSize();

	/**
	 * Returns an InputStream with the content of the attachment. Dont forget to
	 * close the stream after usage.
	 * 
	 * @created 11.04.2012
	 */
	public InputStream getInputStream() throws IOException;
}
