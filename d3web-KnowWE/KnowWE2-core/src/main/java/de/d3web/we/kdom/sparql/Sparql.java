package de.d3web.we.kdom.sparql;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class Sparql extends AbstractXMLObjectType{
		
	public Sparql(){
	    super("sparql");
		
	}
	
	@Override
	protected void init() {
		childrenTypes.add(new SparqlContent());
	}


}
