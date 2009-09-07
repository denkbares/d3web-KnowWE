package de.d3web.we.kdom.dashTree.solutions;

import de.d3web.we.kdom.LineContent;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;

public class SolutionDescription extends LineContent {

	@Override
	public KnowWEDomRenderer getRenderer() {
		return new FontColorRenderer(FontColorRenderer.COLOR3);
	}

}
