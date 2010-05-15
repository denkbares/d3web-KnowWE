package de.d3web.we.event;

/**
 * The Article Created event.
 * 
 * The Singleton Pattern can be removed if necessary.
 * 
 * @author Sebastian Furth
 *
 */
public class ArticleCreatedEvent extends Event {
	
	private static ArticleCreatedEvent instance = new ArticleCreatedEvent(); 
	
	private ArticleCreatedEvent() {}
	
	public static ArticleCreatedEvent getInstance() {
		return instance;
	}
	
}
