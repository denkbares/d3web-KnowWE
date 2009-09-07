package de.d3web.we.kdom.kopic.renderer;

import de.d3web.we.kdom.ColorRenderer;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DefaultDelegateRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ErrorRendererTable extends ColorRenderer{

	private static ErrorRendererTable instance;
	
	public static ErrorRendererTable getInstance() {
		if (instance == null) {
			instance = new ErrorRendererTable();
			
		}

		return instance;
		
	}

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		String title = "Error";
		return spanColorTitle(SpecialDelegateRenderer.getInstance()
				.render(sec, user, web, topic), "red", title);
	}
}
