package de.d3web.we.kdom.questionTreeNew;

import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class InvalidNumberError extends KDOMError {

	private String v;
	
	public InvalidNumberError(String value) {
		this.v = value;
	}
	
	@Override
	public String getVerbalization(KnowWEUserContext usercontext) {
		return "Not a valid number: "+v;
	}

}
