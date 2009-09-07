package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectioner;

public class OrOperator extends DefaultAbstractKnowWEObjectType {

	@Override
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		List<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();
		types.add(new AndOperator());		
		return types;
	}

	@Override
	public void init() {
		this.childrenTypes.add(new AndOperator());
		this.sectionFinder = new RegexSectioner("OR", this);
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return FontColorRenderer.getRenderer(FontColorRenderer.COLOR2);
	}

}
