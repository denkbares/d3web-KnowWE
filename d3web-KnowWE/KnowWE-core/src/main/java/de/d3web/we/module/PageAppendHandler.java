package de.d3web.we.module;

import de.d3web.we.wikiConnector.KnowWEUserContext;

public interface PageAppendHandler {

	public String getDataToAppend(String topic, String web, KnowWEUserContext user);

	public boolean isPre();

}
