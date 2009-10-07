package de.d3web.we.flow;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class Action extends	AbstractXMLObjectType{
		
		public Action() {
			super("Action");
		}

		
		//		in de.d3web.we.kdom.condition.FindingToConditionBuilder
		
		
		@Override
		protected void init() {
			this.childrenTypes.add(new XMLContent());
		}
}
