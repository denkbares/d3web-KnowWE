package de.d3web.we.event;

import de.d3web.we.kdom.KnowWEArticle;


public class KDOMCreatedEvent extends Event {

	private final KnowWEArticle article;

	public KDOMCreatedEvent(KnowWEArticle article) {
		this.article = article;
	}

	public KnowWEArticle getArticle() {
		return this.article;
	}

}
