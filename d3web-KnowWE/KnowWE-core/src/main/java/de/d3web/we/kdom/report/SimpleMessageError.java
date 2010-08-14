package de.d3web.we.kdom.report;

public class SimpleMessageError extends KDOMError {

	private String message = null;

	public SimpleMessageError(String m) {
		this.message = m;
	}

	@Override
	public String getVerbalization() {
		return message;
	}

}
