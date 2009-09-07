package de.d3web.we.kdom;

import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class NothingRenderer extends KnowWEDomRenderer{
	
	private static NothingRenderer instance ;
	
	public static synchronized  NothingRenderer getInstance() {
		if (instance == null) {
			instance = new NothingRenderer();
			
		}

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
		return "";
	}

}
