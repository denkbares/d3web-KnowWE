package de.d3web.we.kdom.basic;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;

public class AnonymousType extends DefaultAbstractKnowWEObjectType{
	
	private String name;
	
	public AnonymousType(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

}
