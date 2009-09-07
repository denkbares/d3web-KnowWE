package de.d3web.we.kdom.include;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.NothingRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectioner;

public class IncludedFromTypeTail extends DefaultAbstractKnowWEObjectType {

	private static IncludedFromTypeTail instance;
	
	public static synchronized IncludedFromTypeTail getInstance() {
		if(instance == null) {
			instance = new IncludedFromTypeTail();
		}
		return instance;
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return new NothingRenderer();
	}
	
	@Override
	protected void init() {
		sectionFinder = new RegexSectioner("</includedFrom>\\s*", this);
	}

}
