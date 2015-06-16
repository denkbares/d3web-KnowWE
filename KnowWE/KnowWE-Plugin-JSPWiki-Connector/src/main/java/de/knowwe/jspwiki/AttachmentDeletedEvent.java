/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

import de.knowwe.core.event.Event;

/**
 * Gets fired every time an attachment is deleted.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.06.15
 */
public class AttachmentDeletedEvent extends Event {

	private final String parent;
	private final String fileName;

	public AttachmentDeletedEvent(String parent, String fileName) {
		this.parent = parent;
		this.fileName = fileName;
	}

	/**
	 * Returns the file name of the deleted attachment. The file name is not the file
	 * name or path to the attachment in the file system of the wiki, but the
	 * file name with which the attachment was uploaded by a user to the wiki.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Returns the name/title of the article the attachment belonged to.
	 */
	public String getParentName() {
		return parent;
	}

	/**
	 * Returns the path of the attachment in the connected wiki. The path is
	 * comprised of the article name of the attachment, followed by a separator
	 * and the file name of the attachment itself.
	 */
	public String getPath() {
		return getParentName() + "/" + getFileName();
	}
}
