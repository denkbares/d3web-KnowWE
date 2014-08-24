/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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
import java.util.Date;

import de.knowwe.core.Environment;

public class WikiAttachmentInfo implements WikiObjectInfo {

	private final String name;
	private final String author;
	private final int version;
	private final Date date;

	public WikiAttachmentInfo(String name, String author, int version, Date date) {
		this.name = name;
		this.author = author;
		this.version = version;
		this.date = date;
	}

	public WikiAttachment getAttachment() throws IOException {
		return Environment.getInstance().getWikiConnector().getAttachment(name, version);
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public Date getSaveDate() {
		return date;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public String getName() {
		return name;
	}
}
