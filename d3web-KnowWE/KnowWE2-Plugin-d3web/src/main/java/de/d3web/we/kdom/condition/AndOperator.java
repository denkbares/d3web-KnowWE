package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectioner;

public class AndOperator extends DefaultAbstractKnowWEObjectType{
	
	@Override
	public void init() {
		this.sectionFinder = new RegexSectioner("AND", this);
		this.childrenTypes.add(new Finding());
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return FontColorRenderer.getRenderer(FontColorRenderer.COLOR2);
	}
}
