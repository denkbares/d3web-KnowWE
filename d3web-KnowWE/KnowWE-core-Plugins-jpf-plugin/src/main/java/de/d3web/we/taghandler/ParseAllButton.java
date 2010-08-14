package de.d3web.we.taghandler;

import java.util.Map;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ParseAllButton extends AbstractTagHandler {

	public ParseAllButton() {
		super("parseallbutton");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		return "<input type=\"button\" value=\"ParseAll\" class=\"parseAllButton\"\"><div id=\"parseAllResult\"></div>";
	}

}
