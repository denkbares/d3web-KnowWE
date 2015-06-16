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

package org.apache.wiki.event;

/**
 * JSPWiki Event for Attachments.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.06.15
 */
public class WikiAttachmentEvent extends WikiEvent {

	public static final int DELETED = 0;
	public static final int STORED = 1;
	private final String parentName;
	private final String fileName;

	/**
	 * Constructs an instance of this event.
	 *
	 * @param src        the Object that is the source of the event
	 * @param parentName the name of the parent of this Attachment, i.e. the page
	 *                   which contains this attachment.
	 * @param fileName   the  file name of the attachment.
	 * @param type       the event type.
	 */
	public WikiAttachmentEvent(Object src, String parentName, String fileName, int type) {
		super(src, type);
		this.parentName = parentName;
		this.fileName = fileName;
	}

	public String getParentName() {
		return parentName;
	}

	public String getFileName() {
		return fileName;
	}
}
