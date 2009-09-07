package de.d3web.we.kdom;

import de.d3web.we.kdom.sectionFinder.LineBreakFinder;

public class LineBreak extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new LineBreakFinder(this);
	}

}
