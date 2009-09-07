package de.d3web.we.kdom.renderer;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class StyleRenderer extends KnowWEDomRenderer {
	
	protected String style;
	
	public StyleRenderer(String s) {
		this.style = s;
	}
	
	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		
		return KnowWEEnvironment.maskHTML("<span style='"+style+"'>"+SpecialDelegateRenderer.getInstance().render(sec, user, web, topic)+"</span>");
	}

}
