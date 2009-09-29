package de.d3web.we.kdom.tagging;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sparql.SparqlContent;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class Tags extends AbstractXMLObjectType{

	public Tags(){
		super("tags");
	}
	
	@Override
	protected void init() {
		childrenTypes.add(new TagsContent());
	}
}
