package de.d3web.we.kdom.renderer;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.ColorRenderer;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class HighlightRenderer extends ColorRenderer{

	private static HighlightRenderer instance;
	
	public static HighlightRenderer getInstance() {
		if (instance == null) {
			instance = new HighlightRenderer();
			
		}

		return instance;
		
	}

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		String title = "Marker";
		return KnowWEEnvironment.maskHTML("<span id=\"uniqueMarker\">"+spanColorTitle(SpecialDelegateRenderer.getInstance()
				.render(sec, user, web, topic), "yellow", title)+"</span>");
	}

}
