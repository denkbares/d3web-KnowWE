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
