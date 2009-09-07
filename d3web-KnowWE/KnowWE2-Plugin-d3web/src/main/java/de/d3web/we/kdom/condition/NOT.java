package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;

public class NOT extends DefaultAbstractKnowWEObjectType {

	@Override
	public KnowWEDomRenderer getRenderer() {
		return FontColorRenderer.getRenderer(FontColorRenderer.COLOR2);
	}

}
