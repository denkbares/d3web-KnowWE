package de.d3web.we.event;

import de.d3web.we.kdom.KnowWEArticle;


public class UpdatingDependenciesEvent extends Event {

	private final KnowWEArticle article;

	public UpdatingDependenciesEvent(KnowWEArticle article) {
		this.article = article;
	}

	public KnowWEArticle getArticle() {
		return this.article;
	}
}
