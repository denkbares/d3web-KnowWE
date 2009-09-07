package de.d3web.we.kdom.xml;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.NothingRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;


public class XMLHead extends DefaultAbstractKnowWEObjectType {
	
	@Override
	protected void init() {
		sectionFinder = new XMLHeadFinder(this);
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return NothingRenderer.getInstance();
	}

}
