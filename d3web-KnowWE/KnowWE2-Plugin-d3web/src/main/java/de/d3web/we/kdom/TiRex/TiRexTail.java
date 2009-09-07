package de.d3web.we.kdom.TiRex;

import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.StringSectionFinder;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TiRexTail extends DefaultAbstractKnowWEObjectType {

	@Override
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		return null;
	}

	@Override
	public SectionFinder getSectioner() {
		return new StringSectionFinder("</TiRex>", this);
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return new KnowWEDomRenderer() {
			@Override
			public String render(Section sec, KnowWEUserContext user, String web, String topic) {
				String result = ""; 
				
				return result;
			}
		};
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

}
