package de.d3web.we.kdom.rendering;

import de.d3web.we.kdom.Section;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public abstract class AnchorKDOMRender extends KnowWEDomRenderer{

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		String head = "";
		string.append(head);
		
		renderContent(sec, user, string);
		
		String tail = "";
		string.append(tail);
		
		
	}
	
	public abstract void renderContent(Section sec, KnowWEUserContext user, StringBuilder string);
	
	

}
