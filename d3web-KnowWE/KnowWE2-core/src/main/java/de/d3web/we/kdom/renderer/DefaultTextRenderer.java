package de.d3web.we.kdom.renderer;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DefaultTextRenderer extends KnowWEDomRenderer{

	
private static DefaultTextRenderer instance;
	
	public static synchronized DefaultTextRenderer getInstance() {
		if(instance == null) instance = new DefaultTextRenderer();
		return instance;
	}
	
	/**
	 * prevent cloning
	 */
	 @Override
	public Object clone()
		throws CloneNotSupportedException
	  {
	    throw new CloneNotSupportedException(); 	   
	  }
	
	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		return sec.getOriginalText();
	}

}
