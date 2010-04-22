package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ObjectAlreadyDefinedError extends KDOMError {
	
	private String text;
	
	public ObjectAlreadyDefinedError(String text) {
			this.text = text;
	}

	@Override
	public String getVerbalization(KnowWEUserContext usercontext) {
		return "Object already defined: "+text;
	}

}
