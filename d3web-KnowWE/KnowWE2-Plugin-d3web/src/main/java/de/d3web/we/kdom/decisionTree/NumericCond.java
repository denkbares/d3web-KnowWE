package de.d3web.we.kdom.decisionTree;

import de.d3web.we.kdom.LineContent;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;

public class NumericCond extends LineContent {
	
	@Override
	protected void init() {
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR5));
	}
}
