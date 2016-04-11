/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.kdom.search;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Search result
 * 
 * @author Alex Legler
 */
public class Result implements Comparable<Result> {

	private final String query;

	private final Article article;

	private final Section<?> section;

	private final int start;

	private final int end;

	/**
	 * Creates a new Result object.
	 * 
	 * @param query The query used to find this node
	 * @param article The article containing the match
	 * @param section The section containing the match
	 * @param start The index of the start position of the query in the section
	 */
	public Result(String query, Article article, Section<?> section, int start, int end) {
		this.query = query;
		this.article = article;
		this.section = section;
		this.start = start;
		this.end = end;
	}

	public String getQuery() {
		return query;
	}

	public Article getArticle() {
		return article;
	}

	public Section<?> getSection() {
		return section;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	/**
	 * Returns additional text around the Result
	 * 
	 * @param length If length is >0 this will return characters after the
	 *        Result, if it is <0 characters before the Result.
	 * @return The context text
	 */
	public String getAdditionalContext(int length) {
		final String originalText = article.getRootSection().getText();
		final int absPosition = section.getOffsetInArticle() + start;

		if (length < 0) {
			// length is negative, so "+ length" is ok ;)
			int startAdditional = absPosition + length;

			if (startAdditional < 0) startAdditional = 0;

			return originalText.substring(startAdditional, absPosition);
		}
		else {
			int startAdditional = absPosition + (end - start);
			int endAdditional = startAdditional + length;

			if (endAdditional >= originalText.length()) {
				endAdditional = originalText.length();
			}

			return originalText.substring(startAdditional, endAdditional);
		}
	}

	@Override
	public int compareTo(Result o) {
		int a = article.getTitle().compareTo(o.getArticle().getTitle());

		if (a != 0) return a;

		a = start > o.getStart() ? 1 : start < o.getStart() ? -1 : 0;

		if (a != 0) return a;

		a = query.compareTo(o.getQuery());

		if (a != 0) return a;

		return section.get().getName().compareTo(o.getSection().get().getName());
	}
}
