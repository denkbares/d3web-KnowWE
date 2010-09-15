package de.d3web.we.event;

import de.d3web.we.kdom.KnowWEArticle;


public class PreCompileFinishedEvent extends Event {

	private final KnowWEArticle article;

	public PreCompileFinishedEvent(KnowWEArticle article) {
		this.article = article;
	}

	public KnowWEArticle getArticle() {
		return this.article;
	}

}
