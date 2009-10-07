package de.d3web.we.flow;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class ExitSection extends AbstractXMLObjectType {

	public ExitSection() {
		super("exit");
	}

	@Override
	protected void init() {
		this.childrenTypes.add(new XMLContent());
	}
}
