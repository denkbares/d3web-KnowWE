package de.d3web.we.kdom.report;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SyntaxError extends KDOMError {

	private final String text;

	public SyntaxError(String text) {
		this.text = text;
	}

	@Override
	public String getVerbalization(KnowWEUserContext usercontext) {
		// TODO Auto-generated method stub
		return "Syntax Error: " + text;
	}

}
