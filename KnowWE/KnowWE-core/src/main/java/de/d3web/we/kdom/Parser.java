package de.d3web.we.kdom;


public interface Parser {

	public Section<? extends Type> parse(String text, Section<? extends Type> father, KnowWEArticle article);

}
