package de.d3web.we.kdom.dashTree;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLContent;

public class XMLDashTree extends AbstractXMLObjectType{
	
	public XMLDashTree() {
		super("DashTree");
		this.childrenTypes.add(new XMLDashTreeContent());
	}
	
	
	
	
	class XMLDashTreeContent extends XMLContent {

		@Override
		protected void init() {
			this.childrenTypes.add(new DashTree());
			
		}
		
	}
}
