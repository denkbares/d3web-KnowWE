package de.d3web.we.kdom.bulletLists;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;

public class BulletContentType extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.sectionFinder = new AllTextFinderTrimmed();
	}

}
