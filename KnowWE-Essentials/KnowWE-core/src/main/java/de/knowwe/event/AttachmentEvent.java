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

package de.knowwe.event;

import com.denkbares.events.Event;

/**
 * Event concerning attachments attachments
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.06.15
 */
public abstract class AttachmentEvent implements Event {

	private final String parentName;
	private final String fileName;
	private final String web;

	public AttachmentEvent(String web, String parentName, String fileName) {
		this.web = web;
		this.parentName = parentName;
		this.fileName = fileName;
	}

	public String getWeb() {
		return web;
	}

	/**
	 * Returns the file name of the attachment. The file name is not the file
	 * name or path to the attachment in the file system of the wiki, but the
	 * file name with which the attachment was uploaded by a user to the wiki.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Returns the name/title of the article the attachment belongs to.
	 */
	public String getParentName() {
		return parentName;
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
