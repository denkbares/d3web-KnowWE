package de.knowwe.event;

import de.knowwe.core.event.Event;
import de.knowwe.core.kdom.Article;


public class PreCompileFinishedEvent extends Event {

	private final Article article;

	public PreCompileFinishedEvent(Article article) {
		this.article = article;
	}

	public Article getArticle() {
		return this.article;
	}

}
