package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class UnexpectedSequence extends KDOMError {

	private final String text;

	public UnexpectedSequence(String text) {
		this.text = text;
	}

	@Override
	public String getVerbalization(KnowWEUserContext usercontext) {
		return "Unexpected sequence: " + text;
	}

}
