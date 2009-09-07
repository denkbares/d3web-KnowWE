package de.d3web.we.taghandler;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.RegexSectioner;

public class TagHandlerTypeStartSymbol extends DefaultAbstractKnowWEObjectType{

	@Override
	protected void init() {
		sectionFinder = new RegexSectioner("\\[\\{KnowWEPlugin ", this);
		
	}

}
