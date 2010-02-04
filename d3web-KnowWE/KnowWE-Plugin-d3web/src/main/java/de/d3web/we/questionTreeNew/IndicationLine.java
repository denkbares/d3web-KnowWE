package de.d3web.we.questionTreeNew;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;

public class IndicationLine extends DefaultAbstractKnowWEObjectType{
	
	@Override
	protected void init() {
		this.sectionFinder = AllTextFinderTrimmed.getInstance();
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR5));
	}

}
