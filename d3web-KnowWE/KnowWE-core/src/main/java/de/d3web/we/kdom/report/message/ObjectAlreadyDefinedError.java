package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMError;

public class ObjectAlreadyDefinedError extends KDOMError {

	private String text;

	public ObjectAlreadyDefinedError(String text) {
		this.text = text;
	}

	@Override
	public String getVerbalization() {
		return "Object already defined: " + text;
	}

}
