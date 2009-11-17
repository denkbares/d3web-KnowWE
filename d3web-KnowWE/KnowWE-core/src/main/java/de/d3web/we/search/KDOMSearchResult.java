/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.search;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

/**
 * Search result
 * @author Alex Legler
 */
public class KDOMSearchResult {

	private String query;
	
	private KnowWEArticle article;
	
	private Section section;
	
	public KDOMSearchResult(String query, KnowWEArticle article, Section section) {
		this.query = query;
		this.article = article;
		this.section = section;
	}
	
	public String getQuery() {
		return query;
	}
	
	public KnowWEArticle getArticle() {
		return article;
	}
	
	public Section getSection() {
		return section;
	}
}
