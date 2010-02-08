package de.d3web.we.kdom.report;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public class NoSuchObjectError extends KDOMError{
	
	private String name;
	
	public NoSuchObjectError(String name) {
		this.name = name;
	}

	@Override
	public String getVerbalization(KnowWEUserContext usercontext) {
		// TODO Auto-generated method stub
		return "Object not found: "+name;
	}

}
