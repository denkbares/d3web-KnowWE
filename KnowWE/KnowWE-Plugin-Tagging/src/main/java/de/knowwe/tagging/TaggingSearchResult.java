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

package de.knowwe.tagging;

import java.util.Arrays;

/**
 * A class for search-results of the KnowWE-MultiSearch
 * 
 * @author Jochen
 * 
 */
public class TaggingSearchResult {

	private String pagename;
	private String[] contexts;
	private int score;

	public TaggingSearchResult(String page, String[] contexts, int score) {
		this.pagename = page;
		this.contexts = Arrays.copyOf(contexts, contexts.length);
		this.score = score;
	}

	public String getPagename() {
		return pagename;
	}

	public void setPagename(String pagename) {
		this.pagename = pagename;
	}

	public String[] getContexts() {
		return contexts;
	}

	public void setContexts(String[] contexts) {
		this.contexts = Arrays.copyOf(contexts, contexts.length);
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public int hashCode() {
		int hash = pagename.hashCode();
		for (String string : contexts) {
			hash += string.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TaggingSearchResult) {
			TaggingSearchResult other = ((TaggingSearchResult) o);
			if (pagename.equals(other.pagename)) {
				for (int i = 0; i < contexts.length; i++) {
					if (!contexts[i].equals(other.contexts[i])) {
						return false;
					}
				}
				return true;
			}
		}

		return false;
	}

}
