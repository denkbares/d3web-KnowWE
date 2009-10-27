package de.d3web.we.hermes.kdom.conceptMining;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ConceptOccurrenceRenderer extends KnowWEDomRenderer {

	@Override
	public void render(Section arg0, KnowWEUserContext arg1, StringBuilder arg2) {
		arg2.append(KnowWEEnvironment.maskHTML("<b>"+arg0.getOriginalText()+"</b>"));

	}

}
