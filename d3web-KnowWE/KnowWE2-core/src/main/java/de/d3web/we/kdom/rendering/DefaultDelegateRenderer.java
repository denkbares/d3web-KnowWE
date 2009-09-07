package de.d3web.we.kdom.rendering;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DefaultDelegateRenderer extends KnowWEDomRenderer {
	
	private static DefaultDelegateRenderer instance;
	
	public static synchronized DefaultDelegateRenderer getInstance() {
		if(instance == null) instance = new DefaultDelegateRenderer();
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
				
		StringBuilder result = new StringBuilder();
		List<Section> subsecs = sec.getChildren();
		//Collections.sort(subsecs, new TextOrderComparator());
		if(subsecs.size() == 0) {
			return sec.getOriginalText();
		}
		for (Section section : subsecs) {
			KnowWEObjectType objectType = section.getObjectType();
			KnowWEDomRenderer renderer = objectType.getRenderer();
			result.append(renderer.render(section, user, web, topic));
		}
		
		return result.toString();
	}
	
}
