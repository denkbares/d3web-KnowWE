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
package de.d3web.we.search;

/**
 * A SearchTerm contains a term to be searched for and an importance rating,
 * which can be used for ranking of the results.
 * 
 * @author Jochen
 * 
 */

public class SearchTerm implements Comparable<SearchTerm> {

	private double importance = 1;
	private final String term;

	public SearchTerm(String word) {
		this.term = word;
	}

	public SearchTerm(String word, double importance) {
		this.term = word;
		this.importance = importance;
	}

	public double getImportance() {
		return importance;
	}

	public String getTerm() {
		return term;
	}

	public void setImportance(double importance) {
		this.importance = importance;
	}

	@Override
	public String toString() {
		return this.term + " (" + importance + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SearchTerm) {
			return ((SearchTerm) o).getTerm().equals(this.term);
		}
		else {
			return o.equals(this);
		}

	}

	@Override
	public int hashCode() {
		return this.term.hashCode();
	}

	@Override
	public int compareTo(SearchTerm arg0) {
		if (this.importance >= arg0.getImportance()) {
			return +1;
		}
		else {
			return -1;
		}
	}

}
