package de.d3web.we.kdom.decisionTree;

import de.d3web.we.kdom.LineContent;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class QClassID extends LineContent {
	
	@Override
	protected void init() {
		setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR5));
	}
}
