package de.d3web.we.kdom.rendering;

import de.d3web.we.kdom.Section;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public abstract class KnowWEDomRenderer {
	
	public abstract String render(Section sec, KnowWEUserContext user, String web, String topic);

}
