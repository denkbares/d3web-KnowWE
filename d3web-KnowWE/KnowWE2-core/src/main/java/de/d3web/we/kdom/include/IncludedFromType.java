package de.d3web.we.kdom.include;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;


public class IncludedFromType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.setCustomRenderer(new IncludedFromSectionRenderer());
	}
	

	
}
