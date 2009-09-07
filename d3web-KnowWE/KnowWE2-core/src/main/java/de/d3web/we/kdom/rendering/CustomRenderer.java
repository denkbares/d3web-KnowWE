package de.d3web.we.kdom.rendering;

/**
 * @author Jochen
 * 
 * A renderer that is applicable in certain cases - depending on the user, the topic 
 * and the rendering-mode
 *
 */
public abstract class CustomRenderer extends KnowWEDomRenderer{
	
	/**
	 * Decides whether this renderer should be used is this situation
	 * 
	 * @param user
	 * @param topic
	 * @param type
	 * @return
	 */
	public abstract boolean doesApply(String user, String topic, RenderingMode type) ;

}
