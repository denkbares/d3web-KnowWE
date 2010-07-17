package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMWarning;

public class InvalidNumberWarning extends KDOMWarning {

	private final String text;

	public InvalidNumberWarning(String t) {
		this.text = t;
	}

	@Override
	public String getVerbalization() {
		return "Invalid Number: " + text;
	}

}
