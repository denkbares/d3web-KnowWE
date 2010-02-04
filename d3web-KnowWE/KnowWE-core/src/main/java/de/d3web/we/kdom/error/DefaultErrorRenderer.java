package de.d3web.we.kdom.error;

import de.d3web.we.kdom.renderer.StyleRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;

public class DefaultErrorRenderer extends StyleRenderer{

	private static DefaultErrorRenderer instance = null;
	
	public static DefaultErrorRenderer getInstance() {
		if (instance == null) {
			instance = new DefaultErrorRenderer();
			
		}

		return instance;
	}
	
	public DefaultErrorRenderer() {
		super("color:red;text-decoration:underline;");
	}
	

}
