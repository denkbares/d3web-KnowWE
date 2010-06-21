package de.d3web.we.taghandler;

import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ParseFullLocalPageButton extends AbstractTagHandler {

	public ParseFullLocalPageButton() {
		super("fullParse");
	}

	// TODO: factor me out!
	static final String text = "full-parse";

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {

		// might be placed in 'secondary' pages (i.e., leftmenu, moremenu...)
		String theMainTopic = user.getPage();

		String baseURL = KnowWEEnvironment.getInstance().getWikiConnector().getBaseUrl();

		String link = KnowWEUtils.maskHTML("<a href=\"" + baseURL + "Wiki.jsp?page="
				+ theMainTopic
				+ "&parse=full\" title=\"full parse action for current article\">");
		link += text;
		link += KnowWEUtils.maskHTML("</a>");

		return link;
	}

}
