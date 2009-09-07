package de.d3web.we.kdom.rules;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;

public class RuleAction extends DefaultAbstractKnowWEObjectType{

	@Override
	protected void init() {
		sectionFinder = new AllTextFinder(this);
		
	}
	
	

}
