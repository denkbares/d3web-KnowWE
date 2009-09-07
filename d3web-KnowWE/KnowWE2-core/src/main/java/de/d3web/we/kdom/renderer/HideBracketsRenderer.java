package de.d3web.we.kdom.renderer;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class HideBracketsRenderer extends KnowWEDomRenderer{

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		String text = SpecialDelegateRenderer.getInstance().render(sec, user, web, topic);
		text = text.replaceAll("\\[", KnowWEEnvironment.HTML_BRACKET_OPEN );
		text = text.replaceAll("\\]", KnowWEEnvironment.HTML_BRACKET_CLOSE );
		return text;
	}

}
