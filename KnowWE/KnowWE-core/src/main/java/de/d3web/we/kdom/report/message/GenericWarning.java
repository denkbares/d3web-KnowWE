package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMWarning;

/**
 * This class is for all the warnings that are only used in one place inside the
 * code, so there is no need to create an own warning class.
 * 
 * @author Albrecht Striffler
 * @created 28.12.2010
 */
public class GenericWarning extends KDOMWarning {

	private final String text;

	public GenericWarning(String text) {
		this.text = text;
	}

	@Override
	public String getVerbalization() {
		return text;
	}

}
