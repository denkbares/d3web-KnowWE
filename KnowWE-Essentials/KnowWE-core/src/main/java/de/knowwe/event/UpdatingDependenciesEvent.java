package de.knowwe.event;

import com.denkbares.events.Event;
import de.knowwe.core.kdom.Article;


public class UpdatingDependenciesEvent implements Event {

	private final Article article;

	public UpdatingDependenciesEvent(Article article) {
		this.article = article;
	}

	public Article getArticle() {
		return this.article;
	}
}
