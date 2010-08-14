package de.d3web.we.kdom.report;

import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Default renderer for error messages
 * 
 * To have your own customized ErrorRenderer overwrite getErrorRenderer in your
 * KnowWEObjectType and return a (custom) MessageRenderer of your choice
 * 
 * @author Jochen
 * 
 */
public class DefaultErrorRenderer implements MessageRenderer {

	private static DefaultErrorRenderer instance = null;

	public static DefaultErrorRenderer getInstance() {
		if (instance == null) {
			instance = new DefaultErrorRenderer();

		}

		return instance;
	}

	private final String cssClass = "KDDOMError";
	private final String cssStyle = "color:red;text-decoration:underline;";

	@Override
	public String postRenderMessage(KDOMReportMessage m, KnowWEUserContext user) {
		return KnowWEUtils.maskHTML("</span>");
	}

	@Override
	public String preRenderMessage(KDOMReportMessage m, KnowWEUserContext user) {
		StringBuilder string = new StringBuilder();

		string.append(KnowWEUtils.maskHTML("<span"));
		if (m.getVerbalization() != null) {
			string.append(" title='").append(m.getVerbalization()).append("'");
		}
		if (cssClass != null) {
			string.append(" class='").append(cssClass).append("'");
		}
		if (cssStyle != null) {
			string.append(" style='").append(cssStyle).append("'");
		}

		string.append(KnowWEUtils.maskHTML(">"));

		return string.toString();
	}

}
