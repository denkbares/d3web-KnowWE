package de.d3web.we.hermes.kdom;

import org.openrdf.model.URI;

import de.d3web.we.kdom.contexts.URIContext;


public class TimeEventContext extends URIContext{
	
	private static String STORE_KEY = "timeEventURI";
	
	public void setTimeEventURI(URI tURI) {
		this.attributes.put(STORE_KEY, tURI);
	}
	
	public URI getTimeEventURI() {
		return this.attributes.get(STORE_KEY);
	}
	
	public final static String CID="TimeEventURI";
	@Override
	public String getCID() {
		return CID;
	}



}
