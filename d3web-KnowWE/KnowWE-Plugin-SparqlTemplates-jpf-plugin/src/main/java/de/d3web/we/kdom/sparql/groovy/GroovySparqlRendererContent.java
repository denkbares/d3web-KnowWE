package de.d3web.we.kdom.sparql.groovy;


import de.d3web.we.kdom.xml.XMLContent;

public class GroovySparqlRendererContent extends XMLContent {

	@Override
	protected void init() {
		this.setCustomRenderer(GroovySparqlRendererRenderer.getInstance());
		
	}
	
}
