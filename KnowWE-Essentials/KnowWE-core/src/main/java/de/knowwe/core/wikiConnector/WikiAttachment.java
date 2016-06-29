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

/**
 * Wrapper implementation to make the attachments of the connected wiki
 * available to KnowWE (see {@link WikiConnector} for more details).
 * 
 * @author volker_belli
 * @created 18.05.2012
 */
public interface WikiAttachment {

	/**
	 * Returns the file name of the attachment. The file name is not the file
	 * name or path to the attachment in the file system of the wiki, but the
	 * file name with which the attachment was uploaded by a user to the wiki.
	 * 
	 * @created 11.04.2012
	 */
	String getFileName();

	/**
	 * Returns the name/title of the article the attachment belongs to.
	 * 
	 * @created 11.04.2012
	 */
	String getParentName();

	/**
	 * Returns the path of the attachment in the connected wiki. The path is
	 * comprised of the article name of the attachment, followed by a separator
	 * and the file name of the attachment itself.
	 * 
	 * @created 11.04.2012
	 */
	String getPath();

	/**
	 * Returns the date of the last change of attachment.
	 * 
	 * @return the date of the attachment
	 * @created 11.04.2012
	 */
	Date getDate();

	/**
	 * Returns the date when the specified version of attachment has been
	 * created. Throws an IllegalArgumentException if the specified version does
	 * not exists.
	 * 
	 * @param version the version of the attachment to be accessed
	 * @return the date of the attachment
	 * @throws IllegalArgumentException if the version does not exists
	 * @created 11.04.2012
	 */
	Date getDate(int version) throws IllegalArgumentException;

	/**
	 * Returns the size of the attachment in bytes. If the size is unknown, -1
	 * will be returned.
	 * 
	 * @return the size of the attachment
	 * @created 11.04.2012
	 */
	long getSize();

	/**
	 * Returns the size of specified version of the attachment in bytes. Throws
	 * an IllegalArgumentException if the specified version does not exists.
	 * 
	 * @param version the version of the attachment to be accessed
	 * @return the size of the attachment
	 * @throws IllegalArgumentException if the version does not exists
	 * @created 11.04.2012
	 */
	long getSize(int version);

	/**
	 * Returns an InputStream with the content of the attachment.
	 * <p>
	 * <b>Note:</b> Don't forget to close the stream after usage.
	 * 
	 * @return the input stream to access the attachment's content
	 * @throws IOException if the attachment cannot be opened
	 * @created 11.04.2012
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Returns an InputStream with the content of the specified version of the
	 * attachment. Throws an IllegalArgumentException if the specified version
	 * does not exists.
	 * <p>
	 * <b>Note:</b> Don't forget to close the stream after usage.
	 * 
	 * @param version the version of the attachment to be accessed
	 * @return the input stream to access the attachment's content
	 * @throws IllegalArgumentException if the version does not exists
	 * @throws IOException if the attachment cannot be opened
	 * @created 11.04.2012
	 */
	InputStream getInputStream(int version) throws IOException, IllegalArgumentException;

	/**
	 * Returns the version of this attachment.
	 * 
	 * @created 18.05.2012
	 * @return the version of this attachment
	 */
	int getVersion();

	/**
	 * Deletes a specific version of the attachment from the underlying wiki.
	 * Please be careful, the attachment cannot be restored later. You should
	 * also avoid to further access the specific version, this will produce an
	 * exception.
	 * 
	 * @created 18.05.2012
	 * @param version to be deleted
	 * @exception IllegalArgumentException if the specified version does not
	 *            exists
	 * @exception IOException if the version cannot be deleted
	 */
	void delete(int version) throws IOException, IllegalArgumentException;

	/**
	 * Returns an array of all available versions of this attachment. The
	 * version numbers are sorted naturally in ascending order.
	 * <p/>
	 * <b>Attention:</b> This method can be very slow if there are a lot of
	 * versions. Use with care!
	 * 
	 * @created 18.05.2012
	 * @return the array of versions still available
	 * @throws IOException if the list of versions could not been created
	 */
	int[] getAvailableVersions() throws IOException;
}
