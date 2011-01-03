package de.d3web.we.kdom.report.message;

import de.d3web.we.kdom.report.KDOMNotice;

/**
 * This class is for all the notices that are only used in one place inside the
 * code, so there is no need to create an own notice class.
 * 
 * @author Albrecht Striffler
 * @created 28.12.2010
 */
public class GenericNotice extends KDOMNotice {

	private final String text;

	public GenericNotice(String text) {
		this.text = text;
	}

	@Override
	public String getVerbalization() {
		return text;
	}

}
