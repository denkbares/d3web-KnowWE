package de.d3web.we.taghandler;

import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Jochen
 * 
 * Interface for a KnowWE-TagHandler. This mechanism is delegated to the 
 * plugin mechanism of JSPWiki (thus (right now still) depending on JSPWiki)
 * 
 * When the KnowWEPlugin is called by JSPWiki the first attribute-name is taken as 
 * Handler-name. All registered TagHandler are checked for this name. In the case of 
 * (case-insensitiv) matching the render method is called. The resulting String is directly
 * put into the wiki page (by JSPWiki) without going through the rendering pipeline.
 *
 */
public interface TagHandler {
	
	/**
	 * Ought to return lowercase!
	 * 
	 * @return name in lowercase
	 */
	public String getTagName();
	

	/**
	 * The resulting String is rendered into the Wiki-page
	 * It can (in most cases should) contain final HTML.
	 * 
	 * @param topic
	 * @param user
	 * @param value
	 * @param web
	 * @return
	 */
	public String render(String topic, KnowWEUserContext user, String value, String web);
	
}
