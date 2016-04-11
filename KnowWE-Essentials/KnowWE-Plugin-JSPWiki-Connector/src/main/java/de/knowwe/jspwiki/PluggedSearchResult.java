package de.knowwe.jspwiki;

import java.util.Arrays;

import org.apache.wiki.WikiPage;
import org.apache.wiki.search.SearchResult;

public class PluggedSearchResult implements SearchResult, Comparable<PluggedSearchResult> {

	private final String[] contexts;
	private final int score;
	private final WikiPage page;

	public PluggedSearchResult(WikiPage page, int score, String[] contexts) {
		this.page = page;
		this.score = score;
		this.contexts = Arrays.copyOf(contexts, contexts.length);
	}

	@Override
	public WikiPage getPage() {
		return page;
	}

	@Override
	public int getScore() {
		return score;
	}

	@Override
	public String[] getContexts() {
		return contexts;
	}

	@Override
	public int compareTo(PluggedSearchResult o) {
		if (this.score > o.score) return -1;
		else if (this.score < o.score) return 1;
		else return this.page.getName().compareTo(o.page.getName());
	}

}
