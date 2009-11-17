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
package de.d3web.we.jspwiki;

import com.ecyrd.jspwiki.SearchResult;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.WikiPage;

public class SearchResultImpl implements SearchResult {
	private String[] contexts;
	private WikiPage page;
	private int score;
	
	public SearchResultImpl(WikiPage page,String[] contexts, int score){
		this.contexts=contexts;
		this.score=score;
		this.page=page;	
	}
	
	@Override
	public String[] getContexts() {		
		return contexts;
	}

	@Override
	public WikiPage getPage() {		
		return page;
	}

	@Override
	public int getScore() {
		return score;
	}

}
