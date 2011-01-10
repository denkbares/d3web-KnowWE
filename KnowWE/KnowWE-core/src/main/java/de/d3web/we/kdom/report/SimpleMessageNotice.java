package de.d3web.we.kdom.report;


/**
 * This class is for all the notices that are only used in one place inside the
 * code, so there is no need to create an own notice class.
 * 
 * @author Albrecht Striffler
 * @created 28.12.2010
 */
public class SimpleMessageNotice extends KDOMNotice {

	private final String text;

	public SimpleMessageNotice(String text) {
		this.text = text;
	}

	@Override
	public String getVerbalization() {
		return text;
	}

}
