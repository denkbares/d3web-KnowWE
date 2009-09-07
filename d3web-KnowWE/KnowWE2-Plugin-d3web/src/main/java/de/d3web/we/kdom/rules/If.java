package de.d3web.we.kdom.rules;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectioner;

public class If extends DefaultAbstractKnowWEObjectType{

	@Override
	protected void init() {
		sectionFinder = new RegexSectioner("( *WENN\\s+| *IF\\s+)",this);
		
	}
	
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return FontColorRenderer.getRenderer(FontColorRenderer.COLOR1);
	}


}
