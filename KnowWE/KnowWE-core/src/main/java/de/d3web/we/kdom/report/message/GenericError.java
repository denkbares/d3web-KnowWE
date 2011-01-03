package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMError;

/**
 * This class is for all the errors that are only used in one place inside the
 * code, so there is no need to create an own error class.
 * 
 * @author Albrecht Striffler
 * @created 28.12.2010
 */
public class GenericError extends KDOMError {

	private final String text;

	public GenericError(String text) {
		this.text = text;
	}

	@Override
	public String getVerbalization() {
		return text;
	}

}
