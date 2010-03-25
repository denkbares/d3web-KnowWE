package de.d3web.we.kdom.rulesNew.terminalCondition;

import de.d3web.we.kdom.report.KDOMError;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class InvalidNumberError extends KDOMError {

	private final String text;

	public InvalidNumberError(String t) {
		this.text = t;
	}

	@Override
	public String getVerbalization(KnowWEUserContext usercontext) {
		return "Invalid Number: " + text;
	}

}
