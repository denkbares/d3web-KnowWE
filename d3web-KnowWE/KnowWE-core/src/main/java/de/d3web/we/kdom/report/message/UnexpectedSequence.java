package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMError;

public class UnexpectedSequence extends KDOMError {

	private final String text;

	public UnexpectedSequence(String text) {
		this.text = text;
	}

	@Override
	public String getVerbalization() {
		return "Unexpected sequence: " + text;
	}

}
