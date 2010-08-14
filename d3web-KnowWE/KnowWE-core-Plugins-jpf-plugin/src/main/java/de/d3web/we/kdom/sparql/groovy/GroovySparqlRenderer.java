package de.d3web.we.kdom.sparql.groovy;

import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class GroovySparqlRenderer extends AbstractXMLObjectType {

	public GroovySparqlRenderer() {
		super("groovysparqlrenderer");

	}

	@Override
	protected void init() {
		childrenTypes.add(new GroovySparqlRendererContent());
	}

}
