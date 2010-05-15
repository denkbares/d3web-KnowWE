package de.d3web.we.event;

/**
 * The KnowWE Init event.
 * 
 * The Singleton Pattern can be removed if necessary.
 * 
 * @author Sebastian Furth
 *
 */
public class InitEvent extends Event {
	
	private static InitEvent instance = new InitEvent(); 
	
	private InitEvent() {}
	
	public static InitEvent getInstance() {
		return instance;
	}
	
}
