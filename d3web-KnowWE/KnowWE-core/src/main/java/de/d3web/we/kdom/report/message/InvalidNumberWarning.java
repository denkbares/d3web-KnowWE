package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class InvalidNumberWarning extends KDOMWarning {

	private final String text;

	public InvalidNumberWarning(String t) {
		this.text = t;
	}

	@Override
	public String getVerbalization(KnowWEUserContext usercontext) {
		return "Invalid Number: " + text;
	}

}
