/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.ci4ke.build;

/**
 * This class servers as a wrapper for a changed Article during build execution
 * 
 * @author Marc-Oliver Ochlast
 * @created 18.07.2010
 */
public class ModifiedArticleWrapper {

	private final String articleTitle;
	private final Integer versionRangeFrom;
	private final Integer versionRangeTo;

	public ModifiedArticleWrapper(String articleTitle, int versionRangeFrom, int versionRangeTo) {
		super();
		this.articleTitle = articleTitle;
		this.versionRangeFrom = versionRangeFrom;
		this.versionRangeTo = versionRangeTo;
	}

	public String getArticleTitle() {
		return articleTitle;
	}

	public Integer getVersionRangeFrom() {
		return versionRangeFrom;
	}

	public Integer getVersionRangeTo() {
		return versionRangeTo;
	}
}
