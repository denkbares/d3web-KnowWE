package de.d3web.we.kdom.decisionTree;

import de.d3web.we.kdom.LineContent;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class SolutionID extends LineContent {
	
	@Override
	public void init() {
		setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR1));
	}
	
}
