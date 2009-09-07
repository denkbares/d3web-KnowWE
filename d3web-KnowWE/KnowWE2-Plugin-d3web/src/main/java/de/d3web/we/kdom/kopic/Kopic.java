package de.d3web.we.kdom.kopic;

import de.d3web.we.kdom.kopic.renderer.KopicRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

public class Kopic extends AbstractXMLObjectType {
	
	public Kopic() {
		super("Kopic");
		
	}
	
	@Override
	protected void init() {
		childrenTypes.add(new KopicContent());
	}


	@Override
	public KnowWEDomRenderer getRenderer() {
		return new KopicRenderer();
	}

}
