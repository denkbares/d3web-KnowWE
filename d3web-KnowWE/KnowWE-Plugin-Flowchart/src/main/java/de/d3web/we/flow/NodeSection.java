package de.d3web.we.flow;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class NodeSection extends AbstractXMLObjectType {

	public NodeSection() {
		super("node");
	}

	@Override
	protected void init() {
		this.childrenTypes.add(new StartSection());
		this.childrenTypes.add(new ExitSection());
		this.childrenTypes.add(new Position());
		this.childrenTypes.add(new Action());
		this.childrenTypes.add(new XMLContent());
	}

}
