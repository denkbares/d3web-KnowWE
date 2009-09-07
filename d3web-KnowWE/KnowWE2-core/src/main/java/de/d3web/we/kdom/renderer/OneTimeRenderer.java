package de.d3web.we.kdom.renderer;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class OneTimeRenderer extends KnowWEDomRenderer{
	
	private Section s;
	private KnowWEDomRenderer renderer;

	public OneTimeRenderer(Section s, KnowWEDomRenderer renderer) {
		this.s = s;
		this.renderer = renderer;
	}

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		s.setRenderer(null);
		return renderer.render(sec, user, web, topic);
	}
	
	
	
	
}
