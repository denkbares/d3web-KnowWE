package de.d3web.we.search;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

/**
 * Search result
 * @author Alex Legler
 */
public class Result {

	private String query;
	
	private KnowWEArticle article;
	
	private Section section;
	
	public Result(String query, KnowWEArticle article, Section section) {
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
