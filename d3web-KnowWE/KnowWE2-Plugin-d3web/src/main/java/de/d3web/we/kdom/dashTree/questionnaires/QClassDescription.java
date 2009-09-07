package de.d3web.we.kdom.dashTree.questionnaires;

import de.d3web.we.kdom.LineContent;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;

public class QClassDescription extends LineContent {

	@Override
	public KnowWEDomRenderer getRenderer() {
		return new FontColorRenderer(FontColorRenderer.COLOR3);
	}

}
