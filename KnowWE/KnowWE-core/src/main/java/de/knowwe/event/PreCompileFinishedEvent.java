package de.knowwe.event;

import de.knowwe.core.event.Event;
import de.knowwe.core.kdom.KnowWEArticle;


public class PreCompileFinishedEvent extends Event {

	private final KnowWEArticle article;

	public PreCompileFinishedEvent(KnowWEArticle article) {
		this.article = article;
	}

	public KnowWEArticle getArticle() {
		return this.article;
	}

}
