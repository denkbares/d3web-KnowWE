/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

import java.util.Collection;


/**
 * @author Jochen
 * 
 *         Interface for a SearchTermExpander to be used in graph-based
 *         query-expansion
 * 
 */
public interface SearchTermExpander {

	/**
	 * 
	 * expands a SearchTerm (in some way)
	 * 
	 * @created 16.09.2010
	 * @param t
	 * @return
	 */
	public Collection<SearchTerm> expandSearchTerm(SearchTerm t);

	/**
	 * expands a SearchTerm for a number of level, i.e., when Terms are
	 * organized in hierarchical structures (taxonomy)
	 * 
	 * @created 16.09.2010
	 * @param t
	 * @param level
	 * @return
	 */
	public Collection<SearchTerm> expandSearchTerm(SearchTerm t, int level);

}
