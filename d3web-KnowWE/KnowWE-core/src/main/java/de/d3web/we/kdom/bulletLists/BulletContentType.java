package de.d3web.we.kdom.bulletLists;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;

public class BulletContentType extends DefaultAbstractKnowWEObjectType {
	
	public void init() {
		this.sectionFinder = new AllTextFinderTrimmed();
	}

}
