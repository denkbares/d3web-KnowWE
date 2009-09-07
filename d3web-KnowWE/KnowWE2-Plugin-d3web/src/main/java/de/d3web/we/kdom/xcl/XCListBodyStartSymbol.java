package de.d3web.we.kdom.xcl;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.StringSectionFinder;

public class XCListBodyStartSymbol extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new StringSectionFinder("{", this);
		
	}
	
	

}
