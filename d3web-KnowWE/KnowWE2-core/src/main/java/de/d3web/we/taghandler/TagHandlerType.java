package de.d3web.we.taghandler;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectioner;


public class TagHandlerType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		//searches for Strings like [{KnowWEPlugin ...}]
		sectionFinder = new RegexSectioner("\\[\\{KnowWEPlugin [^}]*}]", this);
		childrenTypes.add(new TagHandlerTypeStartSymbol());
		childrenTypes.add(new TagHandlerTypeEndSymbol());
		childrenTypes.add(new TagHandlerTypeContent());
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return new TagRenderer();
	}

}
