package de.knowwe.event;

import de.knowwe.core.event.Event;
import de.knowwe.core.kdom.KnowWEArticle;


public class KDOMCreatedEvent extends Event {

	private final KnowWEArticle article;

	public KDOMCreatedEvent(KnowWEArticle article) {
		this.article = article;
	}

	public KnowWEArticle getArticle() {
		return this.article;
	}

}
