package de.d3web.we.flow;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class StartSection extends AbstractXMLObjectType {

	public StartSection() {
		super("start");
	}

	@Override
	protected void init() {
		this.childrenTypes.add(new XMLContent());
	}

}
