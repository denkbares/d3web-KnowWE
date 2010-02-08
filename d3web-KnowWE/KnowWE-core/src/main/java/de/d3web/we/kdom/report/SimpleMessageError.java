package de.d3web.we.kdom.report;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SimpleMessageError extends KDOMError{

	private String message = null;
	
	public SimpleMessageError(String m ) {
		this.message = m;
	}
	
	@Override
	public String getVerbalization(KnowWEUserContext usercontext) {
		return message;
	}

}
