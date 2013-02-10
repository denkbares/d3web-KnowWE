package de.knowwe.tagging;

import java.util.ArrayList;
import java.util.Collection;

import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.kdom.Article;
import de.knowwe.event.FullParseEvent;

public class TaggingManglerCleaner implements EventListener {

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		ArrayList<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>(1);
		events.add(FullParseEvent.class);
		return events;
	}

	@Override
	public void notify(Event event) {
		if (event instanceof FullParseEvent) {
			cleanForArticle(((FullParseEvent) event).getArticle());
		}
	}

	private void cleanForArticle(Article article) {
		TaggingMangler.getInstance().unregisterTags(article);

	}

}
