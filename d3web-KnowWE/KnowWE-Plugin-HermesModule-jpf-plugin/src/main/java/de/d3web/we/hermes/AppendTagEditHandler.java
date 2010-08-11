package de.d3web.we.hermes;

import java.util.HashMap;

import de.d3web.we.core.semantic.TagEditPanel;
import de.d3web.we.module.PageAppendHandler;
import de.d3web.we.taghandler.TagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class AppendTagEditHandler implements PageAppendHandler {

	private final TagHandler tagHandler = new TagEditPanel();

	@Override
	public String getDataToAppend(String topic, String web, KnowWEUserContext user) {
		return "\\\\"
				+ tagHandler.render(topic, user, new HashMap<String, String>(), web);
	}

	@Override
	public boolean isPre() {
		return false;
	}

}
