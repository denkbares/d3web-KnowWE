package de.d3web.we.flow;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class Position extends AbstractXMLObjectType{
	
	public Position() {
		super("Position");
	}

	@Override
	protected void init() {
		this.childrenTypes.add(new XMLContent());
	}

}
