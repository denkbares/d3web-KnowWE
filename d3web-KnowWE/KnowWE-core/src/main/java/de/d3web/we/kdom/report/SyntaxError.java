package de.d3web.we.kdom.report;


public class SyntaxError extends KDOMError {

	private final String text;

	public SyntaxError(String text) {
		this.text = text;
	}

	@Override
	public String getVerbalization() {
		// TODO Auto-generated method stub
		return "Syntax Error: " + text;
	}

}
