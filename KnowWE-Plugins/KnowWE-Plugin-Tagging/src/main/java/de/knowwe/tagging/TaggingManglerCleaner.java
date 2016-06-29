package de.knowwe.tagging;

import java.util.ArrayList;
import java.util.Collection;

import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.kdom.Article;
import de.knowwe.event.ArticleRegisteredEvent;

public class TaggingManglerCleaner implements EventListener {

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		ArrayList<Class<? extends Event>> events = new ArrayList<>(1);
		events.add(ArticleRegisteredEvent.class);
		return events;
	}

	@Override
	public void notify(Event event) {
		if (event instanceof ArticleRegisteredEvent) {
			cleanForArticle(((ArticleRegisteredEvent) event).getArticle());
		}
	}

	private void cleanForArticle(Article article) {
		TaggingMangler.getInstance().unregisterTags(article);

	}

}
