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

import java.util.Date;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.Environment;
import org.jetbrains.annotations.NotNull;

public class WikiPageInfo implements WikiObjectInfo {

	private final String name;
	private final String author;
	private final int version;
	private final Date date;
	private final String changeNote;
	private final WikiConnector connector;

	public WikiPageInfo(@NotNull WikiConnector connector, String name, String author, int version, Date date, String changeNote) {
		this.connector = connector;
		this.name = name;
		this.author = author;
		this.version = version;
		this.date = date;
		this.changeNote = changeNote;
	}

	/**
	 * @deprecated pass the connector the page belongs to: {@link #WikiPageInfo(WikiConnector, String, String, int,
	 * Date, String)}
	 */
	@Deprecated
	public WikiPageInfo(String name, String author, int version, Date date, String changeNote) {
		this(Environment.requireInstance().getWikiConnector(), name, author, version, date, changeNote);
	}

	public String getText() {
		return connector.getArticleText(name, version);
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

	@Nullable
	public String getChangeNote() {
		return changeNote;
	}
}
