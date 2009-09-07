package de.d3web.we.kdom.dashTree.solutions;

import de.d3web.we.kdom.LineContent;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class RootSolution extends LineContent {
	
	@Override
	public void init() {
		setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR1));
	}
	
}
