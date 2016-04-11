/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.jspwiki;

import java.util.List;

import org.apache.wiki.search.SearchResult;

/**
 * 
 * @author Jochen Reutelshöfer
 * @created 17.04.2013
 */
public interface SearchProvider {

	/**
	 * Searches the entire wiki for the given query.
	 * 
	 * @created 17.04.2013
	 * @param query
	 * @param flags
	 * @return
	 */
	public List<? extends SearchResult> findResults(String query, int flags);

}
