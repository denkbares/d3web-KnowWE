package de.d3web.we.kdom.kopic.renderer;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.semanticAnnotation.AnnotationProperty;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class AnnotationInlineAnswerRenderer extends KnowWEDomRenderer{

	@Override
	public String render(Section sec, KnowWEUserContext user, String web, String topic) {
		Section prop = sec.findSuccessor(AnnotationProperty.class);
		if(prop != null && prop.getOriginalText().contains("asks")) {
			//TODO merge classes 
			return new D3webAnnotationRenderer().render(sec, user, web, topic);
		}
		return null;
	}

}
