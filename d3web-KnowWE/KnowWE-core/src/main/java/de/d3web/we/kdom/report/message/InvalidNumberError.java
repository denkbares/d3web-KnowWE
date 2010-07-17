package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMError;

public class InvalidNumberError extends KDOMError {

	private final String text;

	public InvalidNumberError(String t) {
		this.text = t;
	}

	@Override
	public String getVerbalization() {
		return "Invalid Number: " + text;
	}

}