package de.d3web.we.kdom;

import de.d3web.we.kdom.sectionFinder.AllTextFinder;

public class LineContent extends DefaultAbstractKnowWEObjectType{

	@Override
	protected void init() {
		this.sectionFinder = new AllTextFinder(this);
		this.setCustomRenderer(new DefaultTextLineRenderer());
		
	}
}
